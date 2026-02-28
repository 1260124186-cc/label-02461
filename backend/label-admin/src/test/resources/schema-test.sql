-- H2 兼容的测试用表结构（与 MySQL init.sql 对应）
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64) NOT NULL,
    password    VARCHAR(128) NOT NULL,
    nickname    VARCHAR(64),
    email       VARCHAR(128),
    phone       VARCHAR(20),
    avatar      VARCHAR(256),
    status      INT NOT NULL DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_username UNIQUE (username)
);

CREATE TABLE sys_role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name   VARCHAR(64) NOT NULL,
    role_key    VARCHAR(64) NOT NULL,
    sort        INT DEFAULT 0,
    status      INT NOT NULL DEFAULT 1,
    remark      VARCHAR(256),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_role_key UNIQUE (role_key)
);

CREATE TABLE sys_menu (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id   BIGINT NOT NULL DEFAULT 0,
    menu_name   VARCHAR(64) NOT NULL,
    path        VARCHAR(256),
    component   VARCHAR(256),
    permission  VARCHAR(128),
    menu_type   CHAR(1) NOT NULL,
    icon        VARCHAR(64),
    sort        INT DEFAULT 0,
    status      INT NOT NULL DEFAULT 1,
    visible     INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted     INT NOT NULL DEFAULT 0
);

CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);
