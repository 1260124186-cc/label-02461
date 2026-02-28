package com.cqjtjc.system.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 菜单树形VO
 */
@Data
public class MenuTreeVO {

    private Long id;
    private Long parentId;
    private String menuName;
    private String path;
    private String component;
    private String permission;
    private String menuType;
    private String icon;
    private Integer sort;
    private Integer visible;
    private List<MenuTreeVO> children;
}
