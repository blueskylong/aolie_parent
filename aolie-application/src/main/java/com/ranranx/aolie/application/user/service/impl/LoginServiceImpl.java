package com.ranranx.aolie.application.user.service.impl;

import com.ranranx.aolie.application.menu.dto.MenuButtonDto;
import com.ranranx.aolie.application.menu.service.MenuService;
import com.ranranx.aolie.application.right.RightNode;
import com.ranranx.aolie.application.right.dto.Role;
import com.ranranx.aolie.application.right.service.RightService;
import com.ranranx.aolie.application.user.dto.UserDto;
import com.ranranx.aolie.application.user.service.ILoginService;
import com.ranranx.aolie.application.user.service.UserService;
import com.ranranx.aolie.core.fixrow.service.FixRowService;
import com.ranranx.aolie.core.handler.HandlerFactory;
import com.ranranx.aolie.core.handler.param.QueryParam;
import com.ranranx.aolie.core.interfaces.ICacheRefTableChanged;
import com.ranranx.aolie.common.runtime.LoginUser;
import com.ranranx.aolie.core.interfaces.IParamService;
import com.ranranx.aolie.core.interfaces.ISchemaService;
import com.ranranx.aolie.core.interfaces.UiService;
import com.ranranx.aolie.core.runtime.SessionUtils;
import com.ranranx.aolie.common.types.CommonUtils;
import com.ranranx.aolie.common.types.Constants;
import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.common.types.UserRightNode;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2021/2/6 0006 21:16
 **/
//
@Service
//@ConditionalOnMissingBean(LoginService.class)
@DubboService
public class LoginServiceImpl implements ILoginService, ICacheRefTableChanged, CommandLineRunner {
    @Autowired
    private HandlerFactory factory;

    @Autowired
    private UserService userService;
    @Autowired
    private RightService rightService;

    @Autowired
    private MenuService menuService;

    @DubboReference
    private ISchemaService schemaService;

    @DubboReference
    private UiService uiService;
    @DubboReference
    private FixRowService fixRowService;

//    @DubboReference
//    private IParamService paramService;

    private Map<String, String> mapBtnRights;

    @Override
    public LoginUser loadUserByUserNameAndVersion(String username, String version) throws UsernameNotFoundException {
        if (CommonUtils.isEmpty(version) || CommonUtils.isEmpty(username)) {
            return null;
        }
        QueryParam param = new QueryParam();
        param.setTableDtos(Constants.DEFAULT_SYS_SCHEMA, version, UserDto.class);
        param.appendCriteria().andEqualTo(null,
                Constants.FixColumnName.ACCOUNT_CODE, username);
        param.setResultClass(LoginUser.class);

        HandleResult result = factory.handleQuery(param);
        if (!result.isSuccess()) {
            throw new UsernameNotFoundException("????????????");
        }
        List<LoginUser> data = (List<LoginUser>) result.getData();
        if (data == null || data.isEmpty()) {
            throw new UsernameNotFoundException("????????????????????????");
        }
        if (data.size() > 1) {
            throw new UsernameNotFoundException("??????????????????");
        }
        LoginUser loginUser = data.get(0);
//        loginUser.setParams(paramService.getUserParam(loginUser));
        return loginUser;
    }

    /**
     * ?????????????????????
     *
     * @param roleId
     */
    @Override
    public void setSelectRole(Long roleId) {
        //?????????????????????????????????????????????
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return;
        }
        SessionUtils.getLoginUser().setRoleId(roleId);

    }

    /**
     * ??????????????????????????????,?????????????????????????????????,??????????????????,????????????????????????,??????????????????????????????
     *
     * @param user
     */
    @Override
    public Role initUserRight(LoginUser user, Long roleId) {
        try {
            //??????????????????
            RightNode rightNodeRoot = (RightNode) SessionUtils.getMapRightStruct().get(user.getVersionCode());
            //??????????????????????????????,??????Map
            Map<Long, Set<Long>> mapRights = populateNodeStruct(rightNodeRoot, user, roleId);
            //?????????????????????
            user.setCustomRights(findUserCustomRight(mapBtnRights,
                    mapRights.get(Constants.DefaultRsIds.menuButton), user.getVersionCode()));

            user.setMapRights(mapRights);
            setSelectRole(roleId);
            if (roleId == null) {
                return null;
            }
            return rightService.findRoleById(roleId, user.getVersionCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * ???????????????????????????
     */

    private Set<String> findUserCustomRight(Map<String, String> mapBtnRights, Set<Long> lstBtns, String version) {
        Set<String> result = new HashSet<>();
        if (lstBtns == null || lstBtns.isEmpty() || mapBtnRights == null || mapBtnRights.isEmpty()) {
            return result;
        }
        lstBtns.forEach(btnId -> {
            result.add(mapBtnRights.get(version + "_" + btnId));
        });
        return result;
    }

    /**
     * ?????????????????????ID,????????????????????????????????????,????????????????????????,????????????????????????????????????????????????,???????????????????????????
     *
     * @param root
     * @return key: rsId,value:array of ids
     */
    private Map<Long, Set<Long>> populateNodeStruct(RightNode root, LoginUser user, Long roleId) {
        Map<Long, Set<Long>> mapResult = userService.findUserDirectAllRights(user.getUserId(),
                user.getVersionCode(), roleId);
        List<UserRightNode> lstSub = root.getLstSub();
        if (lstSub == null || lstSub.isEmpty()) {
            return mapResult;
        }
        if (mapResult == null || mapResult.isEmpty()) {
            return mapResult;
        }

        lstSub.forEach(el -> {
            //???????????????,???????????????
            //????????????????????????????????????,?????????????????????????????????,??????????????????????????????
            List<UserRightNode> subNodes = el.getLstSub();
            if (subNodes != null && !subNodes.isEmpty()) {
                subNodes.forEach(node -> {
                    findSubRights(el, node, mapResult, user);
                });
            }
        });
        return mapResult;
    }


    /**
     * ???????????????????????????;
     * ???????????????????????????????????????????????????
     *
     * @param fromNode
     * @param toNode
     * @param mapRights
     */
    private void findSubRights(UserRightNode fromNode, UserRightNode toNode, Map<Long, Set<Long>> mapRights, LoginUser user) {
        Set<Long> rights = findRight(fromNode, toNode, mapRights.get(fromNode.getRightId()), user);
        Set<Long> lstExists = mapRights.computeIfAbsent(toNode.getRightId(), key -> {
            return new HashSet<Long>();
        });
        //?????????????????????
        if (rights != null) {
            lstExists.addAll(rights);
        }

        //????????????
        List<UserRightNode> lstSub = toNode.getLstSub();
        if (lstSub != null && !lstSub.isEmpty()) {
            //????????????
            lstSub.forEach(el -> {
                findSubRights(toNode, el, mapRights, user);
            });
        }
    }


    /**
     * ??????TO?????????????????????ID
     *
     * @param fromNode
     * @param toNode
     * @param lstFormIds
     * @param user
     * @return
     */
    private Set<Long> findRight(UserRightNode fromNode, UserRightNode toNode, Set<Long> lstFormIds, LoginUser user) {
        //??????????????????,???????????????,?????????????????????????????????????????????
        if (fromNode.getLstParent().isEmpty()) {
            return null;
        } else {
            //???????????????????????????
            return userService.findNextRights(fromNode.getRightId(),
                    toNode.getRightId(), lstFormIds, user.getVersionCode());
        }
    }

    @Override
    public List<String> getCareTables() {
        return Arrays.asList(CommonUtils.getTableName(MenuButtonDto.class));
    }

    @Override
    public void refresh(String tableName) {
        this.mapBtnRights = menuService.findCustomPermission();
    }

    @Override
    public void run(String... args) throws Exception {
        this.refresh(null);
    }

    /**
     * ?????????????????????????????????
     * <p>
     * ??????????????????????????????????????????gateway?????????????????????
     *
     * @return
     */
    @Override
    public Map<String, List<Long>> getDsServiceNameRelation() {
        return schemaService.getDsServiceNameRelation();
    }


    /**
     * ?????????????????????????????????
     *
     * @return
     */
    @Override
    public Map<String, List<Long>> getViewServiceNameRelation() {
        return uiService.getViewServiceNameRelation();
    }

    /**
     * ????????????????????????????????????
     *
     * @return
     */
    @Override
    public Map<String, List<Long>> getFixToServiceName() {
        return fixRowService.findFixToServiceName();
    }
}
