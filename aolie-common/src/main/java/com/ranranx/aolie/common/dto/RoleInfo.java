package com.ranranx.aolie.common.dto;

import com.ranranx.aolie.common.types.BaseDto;

/**
 * @author xxl
 * @version 1.0
 * @date 2021-06-07 09:25:50
 */
public class RoleInfo extends BaseDto implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private String roleName;
    private Integer xh;
    private Integer roleType;
    private String lvlCode;
    private Short enabled;

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getRoleId() {
        return this.roleId;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return this.roleName;
    }

    public void setXh(Integer xh) {
        this.xh = xh;
    }

    public Long roleId;

    private Integer getXh() {
        return this.xh;
    }

    public void setRoleType(Integer roleType) {
        this.roleType = roleType;
    }

    public Integer getRoleType() {
        return this.roleType;
    }

    public void setLvlCode(String lvlCode) {
        this.lvlCode = lvlCode;
    }

    public String getLvlCode() {
        return this.lvlCode;
    }

    public void setEnabled(Short enabled) {
        this.enabled = enabled;
    }

    public Short getEnabled() {
        return this.enabled;
    }

}