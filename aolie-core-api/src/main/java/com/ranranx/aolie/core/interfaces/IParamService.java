package com.ranranx.aolie.core.interfaces;

import com.ranranx.aolie.common.types.SystemParam;
import com.ranranx.aolie.common.runtime.LoginUser;

import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/25 0025 8:13
 **/
public interface IParamService {

    /**
     * TODO 这里有问题,参数变化时,没办法同步到内存
     *
     * @param user
     * @return
     */
    Map<Long, SystemParam> getUserParam(LoginUser user);
}
