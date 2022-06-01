package com.ranranx.aolie.core.service.referencefilter;

import com.ranranx.aolie.common.exceptions.InvalidException;
import com.ranranx.aolie.common.types.CommonUtils;
import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.core.datameta.datamodel.SchemaHolder;
import com.ranranx.aolie.core.ds.definition.SqlExp;
import com.ranranx.aolie.core.handler.HandlerFactory;
import com.ranranx.aolie.core.handler.param.QueryParam;
import com.ranranx.aolie.core.handler.param.condition.Criteria;
import com.ranranx.aolie.core.interfaces.IReferenceDataFilter;
import com.ranranx.aolie.core.runtime.SessionUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2020/12/23 0023 14:22
 **/
@Component("menuButtonTableRefFilter")
public class MenuButtonTableRefFilter implements IReferenceDataFilter {
    /**
     * 菜单ID的字段
     */
    public static final String menuIDField = "menu_id";

    @Autowired
    private HandlerFactory factory;

    public MenuButtonTableRefFilter() {
        System.out.println("err");
    }


    @Override
    public Criteria getExtFilter(Long refId, Long colId, Map<String, Object> values) throws InvalidException {
        //values里必须要有MenuId字段.
        try {
            Long menuColId = Long.parseLong(values.keySet().iterator().next());
            if (!SchemaHolder.getColumn(menuColId, SessionUtils.getLoginVersion()).getColumnDto().getFieldName().equals(menuIDField)) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        Object menuId = values.values().iterator().next();
        //查询菜单对应的页面信息
        if (CommonUtils.isEmpty(menuId)) {
            return null;
        }
        String sSql = "select distinct a.table_id\n" +
                "  from aolie_dm_column     a,\n" +
                "        aolie_dm_component b,\n" +
                "       aolie_s_page_detail d,\n" +
                "       aolie_s_menu f\n" +
                "  where a.column_id =b.column_id\n" +
                "       and  a.version_code=b.version_code\n" +
                "        and   b.block_view_id=d.view_id\n" +
                "       and    b.version_code=d.version_code\n" +
                "       and    d.page_id =f.page_id\n" +
                "       and    d.version_code=f.version_code\n" +
                "       and    f.menu_id=#{menuId}\n" +
                "       and f.version_code=#{versionCode}";
        Map<String, Object> map = new LinkedHashMap<>(2);
        map.put("menuId", menuId);
        map.put("versionCode", SessionUtils.getLoginUser());
        SqlExp sqlExp = new SqlExp(sSql, map);
        QueryParam param = new QueryParam().setSqlExp(sqlExp);
        HandleResult handleResult = factory.handleQuery(param);
        if (handleResult.getLstData() == null || handleResult.getLstData().isEmpty()) {
            return null;
        }
        List<Long> pageRefTables = new ArrayList<>();
        handleResult.getLstData().forEach(mapData -> {
            pageRefTables.add(Long.parseLong(mapData.get("table_id").toString()));
        });
        Criteria criteria = new Criteria();
        criteria.andIn(null, "table_id", pageRefTables);
        return criteria;
    }

}
