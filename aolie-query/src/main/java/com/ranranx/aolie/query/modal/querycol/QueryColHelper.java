package com.ranranx.aolie.query.modal.querycol;

import com.ranranx.aolie.common.exceptions.InvalidParamException;
import com.ranranx.aolie.core.datameta.datamodel.ReferenceData;
import com.ranranx.aolie.core.interfaces.IDmService;
import com.ranranx.aolie.querydesign.QueryConstants;
import com.ranranx.aolie.querydesign.dto.QrTempletDetailDto;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 查询分析列工具类
 *
 * @author xxl
 * @version V0.0.1
 * @date 2021/6/21 0021 21:32
 **/
@Component
public class QueryColHelper {


    private static IDmService service;


    /**
     * 创建查询列
     *
     * @param dto
     * @return
     */
    public static BaseQueryCol createQueryCol(QrTempletDetailDto dto) {
        if (dto.getExtendType() == null || dto.getExtendType().equals(QueryConstants.ExtendType.NO_SEPARATE)) {
            return new NormalCol(dto);
        } else if (dto.getExtendType().equals(QueryConstants.ExtendType.HORIZON_SEPARATE)) {
            return new HorizonSeparateCol(dto);
        } else if (dto.getExtendType().equals(QueryConstants.ExtendType.VERTICAL_SEPARATE)) {
            return new VerticalRollupCol(dto);
        } else {
            throw new InvalidParamException("查询列类型错误:" + dto.getExtendType());
        }
    }

    @DubboReference
    public void setService(IDmService dmService) {
        QueryColHelper.service = dmService;
    }

    public static List<ReferenceData> findReferenceData(long referenceId, String version) {
        return service.findReferenceData(referenceId, version);
    }


}
