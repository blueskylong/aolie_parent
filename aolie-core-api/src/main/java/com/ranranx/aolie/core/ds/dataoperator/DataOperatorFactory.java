package com.ranranx.aolie.core.ds.dataoperator;

import com.ranranx.aolie.common.types.CommonUtils;
import com.ranranx.aolie.core.datameta.datamodel.SchemaHolder;
import com.ranranx.aolie.core.datameta.datamodel.TableInfo;
import com.ranranx.aolie.common.exceptions.InvalidException;
import com.ranranx.aolie.common.exceptions.NotExistException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2020/8/10 17:38
 **/
@Component("DataOperatorFactory")
public class DataOperatorFactory {


    @Autowired
    private Map<String, IDataOperator> mapDataOperator;


    public IDataOperator getDataOperatorByName(String name, String version) {
        IDataOperator iDataOperator = mapDataOperator.get(CommonUtils.makeKey(name, version));
        if (iDataOperator != null) {
            return iDataOperator;
        }
        throw new NotExistException("数据源名:" + name + ",版本:" + version + " 的数据源不存在");
    }

    public IDataOperator getDefaultDataOperator() {
        return mapDataOperator.get(DataSourceUtils.getDefaultDataSourceKey());
    }

    public void regDataOperator(String name, IDataOperator dop) {
        mapDataOperator.put(name, dop);
    }

    public IDataOperator getDataOperatorByKey(String key) {
        if (key == null) {
            key = DataSourceUtils.getDefaultDataSourceKey();
        }
        IDataOperator iDataOperator = mapDataOperator.get(key);
        if (iDataOperator != null) {
            return iDataOperator;
        }
        throw new NotExistException("数据源:" + key + " 数据源不存在");
    }

    public IDataOperator getDataOperatorBySchema(long schemaId, String versionCode) {
        String key = SchemaHolder.getSchema(schemaId, versionCode).getDsKey();
        return getDataOperatorByKey(key);
    }

    public IDataOperator getDataOperatorByTable(long tableId, String versionCode) {
        TableInfo table = SchemaHolder.getTable(tableId, versionCode);
        if (table == null) {
            throw new InvalidException("指定的表不存在:" + tableId + "_" + versionCode);

        }
        String key = table.getDsKey();
        return getDataOperatorByKey(key);

    }


}
