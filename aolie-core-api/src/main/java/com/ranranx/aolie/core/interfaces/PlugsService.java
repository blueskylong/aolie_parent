package com.ranranx.aolie.core.interfaces;

import com.ranranx.aolie.common.dto.PlugInfo;

import java.util.List;

/**
 * 插件管理服务
 */
public interface PlugsService extends IBaseDbService {

    /**
     * 取得插件信息
     *
     * @return
     */
    List<PlugInfo> getPlugInfos();

    /**
     * 安装
     *
     * @return
     */
    boolean install(Long plugId);


    IPlus findPlugByCode(String plugCode);

    /**
     * 卸载
     *
     * @return
     */
    boolean uninstall(Long plugId);

    /**
     * 升级
     *
     * @return
     */
    boolean update(Long plugId);


    /**
     * 检查当前系统配置问题
     *
     * @return
     */
    String check(Long plugId);

    /**
     * 修复
     *
     * @return
     */

    String repair(Long plugId);

    /**
     * 更新信息
     *
     * @param plugInfo
     */
    void updatePlugInfo(PlugInfo plugInfo);

    /**
     * 增加插件
     *
     * @param plugInfo
     */
    void addPlug(PlugInfo plugInfo);


}
