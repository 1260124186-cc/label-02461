-- =============================================
-- RBAC 权限管理系统 - 数据库初始化脚本
-- =============================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS `label_rbac` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `label_rbac`;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(64)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(128) NOT NULL COMMENT '密码',
    `nickname`    VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
    `email`       VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `avatar`      VARCHAR(256) DEFAULT NULL COMMENT '头像',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 角色表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`   VARCHAR(64)  NOT NULL COMMENT '角色名称',
    `role_key`    VARCHAR(64)  NOT NULL COMMENT '角色标识',
    `sort`        INT          DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    `remark`      VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- 菜单/权限表
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    `menu_name`   VARCHAR(64)  NOT NULL COMMENT '菜单名称',
    `path`        VARCHAR(256) DEFAULT NULL COMMENT '路由路径',
    `component`   VARCHAR(256) DEFAULT NULL COMMENT '组件路径',
    `permission`  VARCHAR(128) DEFAULT NULL COMMENT '权限标识',
    `menu_type`   CHAR(1)      NOT NULL COMMENT '菜单类型 M-目录 C-菜单 F-按钮',
    `icon`        VARCHAR(64)  DEFAULT NULL COMMENT '菜单图标',
    `sort`        INT          DEFAULT 0 COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    `visible`     TINYINT      DEFAULT 1 COMMENT '是否可见 0-隐藏 1-显示',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- ----------------------------
-- 用户-角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------
-- 角色-菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- =============================================
-- 初始化数据
-- =============================================

-- 管理员用户 (密码: admin123, BCrypt加密)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `status`) VALUES
(1, 'admin', '$2a$10$1.Ag0D/kUg.YR8PxggBQ7ePM.mCmsFp0SaSJrLY.jhmHW3Pq8pZjK', '超级管理员', 1);

-- 角色（共 2 条）
INSERT INTO `sys_role` (`id`, `role_name`, `role_key`, `sort`, `status`, `remark`) VALUES
(1, '超级管理员', 'admin', 1, 1, '拥有所有权限'),
(2, '普通用户', 'user', 2, 1, '普通用户角色');

-- 用户-角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 菜单数据
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `path`, `component`, `permission`, `menu_type`, `icon`, `sort`, `status`, `visible`) VALUES
-- 系统管理目录
(1, 0, '系统管理', '/system', NULL, NULL, 'M', 'setting', 1, 1, 1),
-- 用户管理
(100, 1, '用户管理', '/system/user', 'system/user/index', 'system:user:list', 'C', 'user', 1, 1, 1),
(1001, 100, '用户查询', NULL, NULL, 'system:user:query', 'F', NULL, 1, 1, 1),
(1002, 100, '用户新增', NULL, NULL, 'system:user:add', 'F', NULL, 2, 1, 1),
(1003, 100, '用户修改', NULL, NULL, 'system:user:edit', 'F', NULL, 3, 1, 1),
(1004, 100, '用户删除', NULL, NULL, 'system:user:delete', 'F', NULL, 4, 1, 1),
(1005, 100, '重置密码', NULL, NULL, 'system:user:resetPwd', 'F', NULL, 5, 1, 1),
-- 角色管理
(101, 1, '角色管理', '/system/role', 'system/role/index', 'system:role:list', 'C', 'peoples', 2, 1, 1),
(1011, 101, '角色查询', NULL, NULL, 'system:role:query', 'F', NULL, 1, 1, 1),
(1012, 101, '角色新增', NULL, NULL, 'system:role:add', 'F', NULL, 2, 1, 1),
(1013, 101, '角色修改', NULL, NULL, 'system:role:edit', 'F', NULL, 3, 1, 1),
(1014, 101, '角色删除', NULL, NULL, 'system:role:delete', 'F', NULL, 4, 1, 1),
-- 菜单管理
(102, 1, '菜单管理', '/system/menu', 'system/menu/index', 'system:menu:list', 'C', 'tree-table', 3, 1, 1),
(1021, 102, '菜单查询', NULL, NULL, 'system:menu:query', 'F', NULL, 1, 1, 1),
(1022, 102, '菜单新增', NULL, NULL, 'system:menu:add', 'F', NULL, 2, 1, 1),
(1023, 102, '菜单修改', NULL, NULL, 'system:menu:edit', 'F', NULL, 3, 1, 1),
(1024, 102, '菜单删除', NULL, NULL, 'system:menu:delete', 'F', NULL, 4, 1, 1);

-- 管理员角色拥有所有菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 1), (1, 100), (1, 1001), (1, 1002), (1, 1003), (1, 1004), (1, 1005),
(1, 101), (1, 1011), (1, 1012), (1, 1013), (1, 1014),
(1, 102), (1, 1021), (1, 1022), (1, 1023), (1, 1024);

-- 普通用户角色只有查看权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(2, 1), (2, 100), (2, 1001), (2, 101), (2, 1011), (2, 102), (2, 1021);

-- =============================================
-- 初始化脚本结束（以上为完整初始化数据）
-- =============================================
