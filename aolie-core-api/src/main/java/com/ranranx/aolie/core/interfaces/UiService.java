package com.ranranx.aolie.core.interfaces;

import com.ranranx.aolie.core.datameta.datamodel.BlockViewer;
import com.ranranx.aolie.core.datameta.dto.BlockViewDto;

import java.util.List;
import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/23 0023 18:38
 **/
public interface UiService {
    /**
     * 取得视图信息
     *
     * @param blockViewId
     * @param version
     * @return
     */
    BlockViewer getViewerInfo(Long blockViewId, String version);

    /**
     * 查询所有的视图信息
     *
     * @param schemaId
     * @return
     */
    List<BlockViewDto> getBlockViews(Long schemaId);

    /**
     * 清除视图信息的缓存
     *
     * @param blockViewId
     * @param version
     */
    void clearViewCache(Long blockViewId, String version);

    /**
     * 取得表与服务的对应关系
     *
     * @return
     */
    Map<String, List<Long>> getViewServiceNameRelation();
}
