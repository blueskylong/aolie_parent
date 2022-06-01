package com.ranranx.aolie.core.handler;

import com.ranranx.aolie.core.interfaces.IHandleFilter;
import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.common.types.Ordered;
import com.ranranx.aolie.core.handler.param.OperParam;

/**
 * @author xxl
 * 数据处理接口, 就对内的使用接口,调用 IDataOperator接口进行数据操作
 * 使用这一层的接口,可以附加上各类拦截器,以及以后对多种数据源操作的能力
 * @version V0.0.1
 * @date 2020/8/8 19:52
 **/
public interface IDbHandler extends Ordered, IHandleFilter {

    /**
     * 处理操作
     *
     * @param mapParam
     * @return
     */
    HandleResult doHandle(OperParam mapParam);

    /**
     * 开始事务
     *
     * @return
     */
    default long beginTransaction() {
        return 0;
    }

    /**
     * 提交事务
     */
    default void commit() {

    }

    /**
     * 是否需要事务
     *
     * @return
     */
    default boolean needTransaction() {
        return true;
    }


    /**
     * 回滚事务
     */
    default void rollback() {

    }

}
