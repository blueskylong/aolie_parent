package com.ranranx.aolie.core.runtime;

import com.alibaba.druid.support.json.JSONUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ranranx.aolie.common.exceptions.InvalidParamException;
import com.ranranx.aolie.common.types.CommonUtils;
import com.ranranx.aolie.common.types.Constants;
import com.ranranx.aolie.core.datameta.datamodel.BlockViewer;
import com.ranranx.aolie.core.datameta.datamodel.Column;
import com.ranranx.aolie.core.datameta.datamodel.SchemaHolder;
import com.ranranx.aolie.core.datameta.datamodel.TableInfo;
import com.ranranx.aolie.core.ds.definition.FieldOrder;
import com.ranranx.aolie.core.handler.param.Page;
import com.ranranx.aolie.core.handler.param.QueryParam;
import com.ranranx.aolie.core.handler.param.condition.Criteria;
import com.ranranx.aolie.core.interfaces.UiService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 本地微服务使用
 *
 * @author xxl
 * @version V0.0.1
 * @date 2020/9/11 14:43
 **/

@Service
@ConditionalOnClass(name = "com.ranranx.aolie.core.AolieCoreApplication")
public class JQParameterLocal extends JQParameter {
    @Override
    @Autowired
    public void setUiService(UiService uiService) {
        this.uiService = uiService;
    }

}