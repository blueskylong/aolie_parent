package com.ranranx.aolie.common.types;

import java.io.Serializable;

/**
 * @author xxl
 * 基础数据库表对象
 * @version V0.0.1
 * @date 2020/8/5 9:03
 **/
public class BaseDto implements Serializable {
    protected String createDate;
    protected String lastUpdateDate;
    protected Long createUser;
    protected Long lastUpdateUser;
    /**
     * 这里的version是为了做一个区分用的,可能是年度,可能是区划,也可以是租户
     */
    protected String versionCode;


    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Long getCreateUser() {
        return createUser;
    }

    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
    }

    public Long getLastUpdateUser() {
        return lastUpdateUser;
    }

    public void setLastUpdateUser(Long lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }
}
