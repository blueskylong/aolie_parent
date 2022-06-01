package com.ranranx.aolie.core.runtime;

import com.ranranx.aolie.core.interfaces.UiService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.stereotype.Service;

/**
 * 远程微服务使用
 *
 * @author xxl
 * @version V0.0.1
 * @date 2020/9/11 14:43
 **/

@Service
@ConditionalOnMissingClass("com.ranranx.aolie.core.AolieCoreApplication")
public class JQParameterRemote extends JQParameter {

    @Override
    @DubboReference
    public void setUiService(UiService uiService) {
        this.uiService = uiService;
    }

}