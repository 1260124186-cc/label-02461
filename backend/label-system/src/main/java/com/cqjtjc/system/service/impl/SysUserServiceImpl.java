package com.cqjtjc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqjtjc.common.exception.BusinessException;
import com.cqjtjc.common.exception.ErrorCode;
import com.cqjtjc.system.dto.SysUserDTO;
import com.cqjtjc.system.entity.SysUser;
import com.cqjtjc.system.entity.SysUserRole;
import com.cqjtjc.system.mapper.SysUserMapper;
import com.cqjtjc.system.mapper.SysUserRoleMapper;
import com.cqjtjc.system.service.SysUserService;
import com.cqjtjc.system.util.PasswordStrengthValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    /**
     * 用户分页查询（支持按用户名模糊查询、按状态过滤）。
     *
     * <p>该方法返回持久化实体，接口层会对敏感字段（如 password）做脱敏处理。</p>
     */
    public Page<SysUser> pageList(Page<SysUser> page, String username, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysUser::getUsername, username)
                .eq(status != null, SysUser::getStatus, status)
                .orderByDesc(SysUser::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    /**
     * 根据用户名查询用户（用于登录与鉴权）。
     */
    public SysUser getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 新增用户并绑定角色。
     *
     * <p>安全约束：</p>
     * <ul>
     *   <li>用户名必须唯一；</li>
     *   <li>密码必须填写且通过强度校验（避免默认弱密码）；</li>
     *   <li>密码只保存加密结果（由 {@link PasswordEncoder} 处理）。</li>
     * </ul>
     *
     * <p>角色绑定策略：若 {@code roleIds} 为空则不插入关联记录。</p>
     *
     * @return 新增用户 ID
     */
    public Long addUser(SysUserDTO dto) {
        // 检查用户名是否存在
        if (getByUsername(dto.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        // 密码必填且须满足强度要求（无默认弱密码）
        String rawPassword = dto.getPassword();
        if (!StringUtils.hasText(rawPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_REQUIRED);
        }
        PasswordStrengthValidator.validate(rawPassword);

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(rawPassword));
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
    /**
     * 修改用户基础信息并更新用户-角色关联。
     *
     * <p>关联更新采用“先删后插”策略，确保最终关联集合与传入 {@code roleIds} 一致。</p>
     */
    public void updateUser(SysUserDTO dto) {
        SysUser user = this.getById(dto.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
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
    /**
     * 删除用户（逻辑删除）并清理用户-角色关联。
     */
    public void deleteUser(Long id) {
        this.removeById(id);
        userRoleMapper.deleteByUserId(id);
        log.info("删除用户成功, userId={}", id);
    }

    @Override
    /**
     * 重置用户密码。
     *
     * <p>新密码需要通过强度校验；保存时仅存加密结果。</p>
     */
    public void resetPassword(Long id, String newPassword) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        PasswordStrengthValidator.validate(newPassword);
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
        log.info("重置密码成功, userId={}, username={}", id, user.getUsername());
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (userId == null || CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        List<SysUserRole> userRoles = roleIds.stream()
                .map(roleId -> {
                    SysUserRole ur = new SysUserRole();
                    ur.setUserId(userId);
                    ur.setRoleId(roleId);
                    return ur;
                })
                .collect(Collectors.toList());
        userRoleMapper.insertBatch(userRoles);
    }
}
