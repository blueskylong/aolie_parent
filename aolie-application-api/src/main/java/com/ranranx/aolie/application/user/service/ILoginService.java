package com.ranranx.aolie.application.user.service;

import com.ranranx.aolie.common.dto.RoleInfo;
import com.ranranx.aolie.common.runtime.LoginUser;

import java.util.List;
import java.util.Map;


/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/2/16 0016 18:46
 **/

public interface ILoginService {
    /**
     * 查询用户
     *
     * @param username
     * @param version
     * @return
     * @throws
     */
    LoginUser loadUserByUserNameAndVersion(String username, String version);

    /**
     * 初始化用户的权限信息,如果此用户只有一个角色,可以直接查询,但如果是多个角色,则需要选择角色后查询
     *
     * @param user
     */
    RoleInfo initUserRight(LoginUser user, Long roleId);

    /**
     * 设置选择的角色
     *
     * @param roleId
     */
    void setSelectRole(Long roleId);

    /**
     * 取得表与服务的对应关系
     *
     * @return
     */
    Map<String, List<Long>> getDsServiceNameRelation();

    /**
     * 取得表与服务的对应关系
     *
     * @return
     */
    Map<String, List<Long>> getViewServiceNameRelation();

    /**
     * 取得固定行表对应的服务名
     *
     * @return
     */
    Map<String, List<Long>> getFixToServiceName();


}
