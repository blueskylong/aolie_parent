package com.ranranx.aolie.core.interfaces;

import com.ranranx.aolie.core.datameta.datamodel.ReferenceData;
import com.ranranx.aolie.core.datameta.dto.ReferenceDto;
import com.ranranx.aolie.core.datameta.dto.VersionDto;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

/**
 * 数据模式服务接口
 *
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/23 0023 22:21
 **/
public interface IDmService {

    void clearReferenceData(long refId, String versionCode);

    List<VersionDto> getVersions();

    /**
     * 查询所有引用主信息
     *
     * @param version
     * @return
     */
    List<ReferenceDto> findAllReferences(String version);

    /**
     * 查询引用数据
     *
     * @param referenceId
     * @param version
     * @return
     */
    List<ReferenceData> findReferenceData(long referenceId, String version);

}
