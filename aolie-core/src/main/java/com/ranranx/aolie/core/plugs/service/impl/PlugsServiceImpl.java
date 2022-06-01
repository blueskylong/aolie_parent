package com.ranranx.aolie.core.plugs.service.impl;

import com.ranranx.aolie.common.dto.PlugInfo;
import com.ranranx.aolie.common.types.Constants;
import com.ranranx.aolie.core.interfaces.IPlus;
import com.ranranx.aolie.core.interfaces.PlugsService;
import com.ranranx.aolie.core.plugs.dto.PlugDto;
import com.ranranx.aolie.core.service.BaseDbService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TODO 改微服务后，不可以这样直接调用
 * //这个启动后会很慢，需要修改
 *
 * @author xxl
 * @version V0.0.1
 * @date 2021/7/6 0006 7:42
 **/
//@Service
//@Transactional(readOnly = true)
//@Order(2000)
public class PlugsServiceImpl extends BaseDbService implements PlugsService {

    @Autowired(required = false)
    private List<IPlus> lstPlus;

    public boolean updatePlug(Long plugId) {
        IPlus plug = findPlug(plugId);
        if (plug == null) {
            return false;
        }
        return plug.update();
    }

    /**
     * 查询插件
     *
     * @param plugId
     * @return
     */
    private IPlus findPlug(Long plugId) {
        if (lstPlus == null || lstPlus.isEmpty()) {
            return null;
        }
        for (IPlus plus : lstPlus) {
            if (plus.getPlugInfo().getPlugId().equals(plugId)) {
                return plus;
            }
        }
        return null;
    }

    @Override
    public IPlus findPlugByCode(String plugCode) {
        if (lstPlus == null || lstPlus.isEmpty()) {
            return null;
        }
        for (IPlus plus : lstPlus) {
            if (plus.getPlugInfo().getPlugUnitCode().equals(plugCode)) {
                return plus;
            }
        }
        return null;
    }

    /**
     * 取得插件信息
     *
     * @return
     */
    @Override
    public List<PlugInfo> getPlugInfos() {
        if (lstPlus == null || lstPlus.isEmpty()) {
            return null;
        }
        List<PlugInfo> lstDto = new ArrayList<>();
        for (IPlus plus : lstPlus) {
            lstDto.add(plus.getPlugInfo());
        }
        return lstDto;

    }

    /**
     * 安装
     *
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public boolean install(Long plugId) {
        IPlus plug = findPlug(plugId);
        if (plug == null) {
            return false;
        }
        return plug.install();
    }

    /**
     * 卸载
     *
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public boolean uninstall(Long plugId) {
        IPlus plug = findPlug(plugId);
        if (plug == null) {
            return false;
        }
        return plug.uninstall();
    }

    /**
     * 升级
     *
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public boolean update(Long plugId) {
        IPlus plug = findPlug(plugId);
        if (plug == null) {
            return false;
        }
        return plug.update();
    }

    /**
     * 最新版本
     *
     * @return
     */
    public String getNewVersion(Long plugId) {
        IPlus plug = findPlug(plugId);
        if (plug == null) {
            return null;
        }
        return plug.getNewVersion();
    }

    /**
     * 检查当前系统配置问题
     *
     * @return
     */
    @Override
    public String check(Long plugId) {
        IPlus plug = findPlug(plugId);
        if (plug == null) {
            return null;
        }
        return plug.check();
    }

    /**
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public String repair(Long plugId) {
        IPlus plug = findPlug(plugId);
        if (plug == null) {
            return null;
        }
        return plug.repair();
    }

//    @Override
//    public void run(String... args) throws Exception {
//        //更新插件状态
//        if (lstPlus == null || lstPlus.isEmpty()) {
//            return;
//        }
//        for (IPlus plus : lstPlus) {
//            plus.updatePlug();
//        }
//    }

    @Override
    public void updatePlugInfo(PlugInfo plugInfo) {
        PlugDto dto = new PlugDto();
        BeanUtils.copyProperties(plugInfo, dto);
        updateByIds(Arrays.asList(dto), true, Constants.DEFAULT_SYS_SCHEMA);
    }

    @Override
    public void addPlug(PlugInfo plugInfo) {
        PlugDto dto = new PlugDto();
        BeanUtils.copyProperties(plugInfo, dto);
        insert(dto, Constants.DEFAULT_SYS_SCHEMA);
    }
}
