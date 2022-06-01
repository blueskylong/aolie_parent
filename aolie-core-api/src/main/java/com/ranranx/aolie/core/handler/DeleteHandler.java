package com.ranranx.aolie.core.handler;

import com.ranranx.aolie.common.types.Constants;
import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.core.ds.dataoperator.DataOperatorFactory;
import com.ranranx.aolie.core.ds.definition.DeleteParamDefinition;
import com.ranranx.aolie.common.exceptions.InvalidParamException;
import com.ranranx.aolie.core.handler.param.DeleteParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author xxl
 * 数据删除服务
 * @version V0.0.1
 * @date 2020/8/4 14:35
 **/
@Component
public class DeleteHandler<T extends DeleteParam> extends BaseHandler<T> {
    @Autowired
    private DataOperatorFactory factory;

    /**
     * 默认可以处理的类型
     *
     * @return
     */
    @Override
    String getCanHandleType() {
        return Constants.HandleType.TYPE_DELETE;
    }


    @Override
    protected HandleResult handle(DeleteParam deleteParam) {
        HandleResult result = new HandleResult();

        if (deleteParam.getSqlExp() != null) {
            return HandleResult.success(factory.getDefaultDataOperator().executeDirect(deleteParam.getSqlExp().getExecuteMap()));
        }
        DeleteParamDefinition deleteParamDefinition = new DeleteParamDefinition();
        BeanUtils.copyProperties(deleteParam, deleteParamDefinition);
        deleteParamDefinition.setTableName(deleteParam.getTable().getTableDto().getTableName());
        deleteParamDefinition.setIdField(deleteParam.getTable().getKeyColumn()
                .get(0).getColumnDto().getFieldName());
        int num = factory.getDataOperatorByKey(deleteParam.getTable().getDsKey()).delete(
                deleteParamDefinition
        );
        result.setSuccess(true);
        result.setChangeNum(num);

        return result;
    }

    @Override
    protected T checkAndMakeParam(Object mapParam) {
        DeleteParam param = super.checkAndMakeParam(mapParam);
        if (param.getTable() == null) {
            throw new InvalidParamException("没有指定删除的表信息");

        }
        if (param.getCriteria().isEmpty()
                && (param.getIds() == null || param.getIds().isEmpty())) {
            //这里需要做出决定 ,是不是可以全表删除
            throw new InvalidParamException("没有指定删除条件");
        }
        return (T) param;
    }

    public static void main(String[] args) {
        DeleteHandler handler = new DeleteHandler<DeleteParam>();
        handler.checkAndMakeParam(null);
    }
}
