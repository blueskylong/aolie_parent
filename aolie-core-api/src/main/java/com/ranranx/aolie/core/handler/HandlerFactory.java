package com.ranranx.aolie.core.handler;

import com.ranranx.aolie.common.types.CommonUtils;
import com.ranranx.aolie.common.types.Constants;
import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.common.exceptions.InvalidParamException;
import com.ranranx.aolie.common.exceptions.NotExistException;
import com.ranranx.aolie.core.handler.param.*;
import com.ranranx.aolie.core.interceptor.IOperInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2020/8/9 15:34
 **/
@Component
public class HandlerFactory {

    @Autowired
    private List<IDbHandler> lstHandler;

    @Autowired(required = false)
    private List<IOperInterceptor> lstInterceptor;

    public IDbHandler getHandler(String type, Object objExtinfo) {
        if (CommonUtils.isEmpty(type)) {
            throw new InvalidParamException("处理器类型不可以为空");

        }
        for (IDbHandler handler : lstHandler) {
            //这里只取优先级高的处理器
            if (handler.isCanHandle(type, objExtinfo)) {
                return handler;
            }
        }
        throw new NotExistException("处理器类型(" + type + ")");
    }

    /**
     * 直接处理请示
     *
     * @param type
     * @param params
     * @return
     */
    public HandleResult handleRequest(String type, OperParam params) {
        IDbHandler handler = this.getHandler(type, params);
        return handler.doHandle(params);
    }

    public <T> HandleResult handleQuery(QueryParam params) {
        IDbHandler handler = this.getHandler(Constants.HandleType.TYPE_QUERY, params);
        return handler.doHandle(params);
    }

    public HandleResult handleDelete(DeleteParam params) {
        IDbHandler handler = this.getHandler(Constants.HandleType.TYPE_DELETE, params);
        return handler.doHandle(params);
    }

    public HandleResult handleInsert(InsertParam params) {
        IDbHandler handler = this.getHandler(Constants.HandleType.TYPE_INSERT, params);
        return handler.doHandle(params);
    }

    public HandleResult handleUpdate(UpdateParam params) {
        IDbHandler handler = this.getHandler(Constants.HandleType.TYPE_UPDATE, params);
        return handler.doHandle(params);
    }


    @PostConstruct
    public void afterInit() {
        sort();
        setInterceptor();
    }

    private void sort() {
        CommonUtils.sortOrder(lstHandler);
        CommonUtils.sortOrder(lstInterceptor);
    }

    private void setInterceptor() {
        if (lstInterceptor == null || lstInterceptor.isEmpty()) {
            return;
        }
        for (IDbHandler handler : lstHandler) {
            if (handler instanceof BaseHandler) {
                ((BaseHandler) handler).setLstInterceptor(lstInterceptor);
            }
        }
    }
}
