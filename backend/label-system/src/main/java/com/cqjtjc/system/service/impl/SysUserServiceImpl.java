package com.cqjtjc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqjtjc.common.exception.BusinessException;
import com.cqjtjc.system.dto.SysUserDTO;
import com.cqjtjc.system.entity.SysUser;
import com.cqjtjc.system.entity.SysUserRole;
import com.cqjtjc.system.mapper.SysUserMapper;
import com.cqjtjc.system.mapper.SysUserRoleMapper;
import com.cqjtjc.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<SysUser> pageList(Page<SysUser> page, String username, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysUser::getUsername, username)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public SysUser getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addUser(SysUserDTO dto) {
        // 检查用户名是否存在
        if (getByUsername(dto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(
                StringUtils.hasText(dto.getPassword()) ? dto.getPassword() : "123456"));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        this.save(user);

        Long userId = user.getId();
        saveUserRoles(userId, dto.getRoleIds());
        log.info("新增用户成功, username={}, userId={}", dto.getUsername(), userId);
        return userId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUserDTO dto) {
        SysUser user = this.getById(dto.getId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus());
        this.updateById(user);

        // 更新用户角色
        userRoleMapper.deleteByUserId(user.getId());
        saveUserRoles(user.getId(), dto.getRoleIds());
        log.info("修改用户成功, userId={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        this.removeById(id);
        userRoleMapper.deleteByUserId(id);
        log.info("删除用户成功, userId={}", id);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
        log.info("重置密码成功, userId={}, username={}", id, user.getUsername());
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (userId == null || CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }
}
