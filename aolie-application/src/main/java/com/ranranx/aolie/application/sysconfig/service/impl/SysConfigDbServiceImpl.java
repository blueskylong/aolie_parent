package com.ranranx.aolie.application.sysconfig.service.impl;

import com.ranranx.aolie.application.sysconfig.dto.SysConfigDto;
import com.ranranx.aolie.application.sysconfig.service.SysConfigDbService;
import com.ranranx.aolie.common.types.CommonUtils;
import com.ranranx.aolie.common.types.Constants;
import com.ranranx.aolie.common.types.HandleResult;
import com.ranranx.aolie.core.handler.HandlerFactory;
import com.ranranx.aolie.core.handler.param.QueryParam;
import com.ranranx.aolie.core.handler.param.UpdateParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/1/18 0018 14:27
 **/
@Service
public class SysConfigDbServiceImpl implements SysConfigDbService {
    private final Map<Long, SysConfigDto> map = new HashMap<>();
    @Autowired
    private HandlerFactory factory;

    /**
     * 更新配置的值
     *
     * @param id
     * @param value
     * @return
     */
    @Override
    public HandleResult updateConfigValue(long id, String value) {
        if (CommonUtils.isEmpty(value)) {
            return HandleResult.failure("没有指定值");
        }

        SysConfigDto sysConfigDto = getDto(id);
        if (sysConfigDto != null) {
            sysConfigDto.setConfigValue(value);
        }
        UpdateParam updateParam = UpdateParam.genUpdateByObject(Constants.DEFAULT_SYS_SCHEMA,
                Constants.DEFAULT_VERSION, sysConfigDto, true);
        return factory.handleUpdate(updateParam);
    }

    private SysConfigDto getDto(long id) {
        if (map.isEmpty()) {
            synchronized (map) {
                if (map.isEmpty()) {
                    initData();
                }
            }
        }
        return map.get(id);
    }

    /**
     * 取得小数参数值
     *
     * @param id
     * @return
     */
    @Override
    public Double getDoubleParamValue(long id) {
        SysConfigDto sysConfigDto = getDto(id);
        if (sysConfigDto == null) {
            return null;
        }
        return Double.parseDouble(sysConfigDto.getConfigValue());
    }

    /**
     * 取得字符串参数值
     *
     * @param id
     * @return
     */
    @Override
    public String getStringParamValue(long id) {
        SysConfigDto sysConfigDto = getDto(id);
        if (sysConfigDto == null) {
            return null;
        }
        return sysConfigDto.getConfigValue();
    }

    /**
     * 取得长整型参数值
     *
     * @param id
     * @return
     */
    @Override
    public Long getLongParamValue(long id) {
        SysConfigDto sysConfigDto = getDto(id);
        if (sysConfigDto == null) {
            return null;
        }
        return Long.parseLong(sysConfigDto.getConfigValue());
    }


    private void initData() {
        QueryParam param = new QueryParam();
        SysConfigDto systemParam = new SysConfigDto();
        systemParam.setVersionCode(Constants.DEFAULT_VERSION);
        param.setFilterObjectAndTableAndResultType(Constants.DEFAULT_SYS_SCHEMA, Constants.DEFAULT_VERSION, systemParam);
        HandleResult result = factory.handleQuery(param);
        List<SysConfigDto> lstDto = result.getData();
        if (lstDto == null || lstDto.isEmpty()) {
            return;
        }
        for (SysConfigDto sysConfigDto : lstDto) {
            map.put(sysConfigDto.getConfigId(), sysConfigDto);
        }
    }
}
