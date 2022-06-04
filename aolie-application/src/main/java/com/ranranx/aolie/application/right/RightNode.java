package com.ranranx.aolie.application.right;

import com.ranranx.aolie.application.user.dto.RightResourceDto;
import com.ranranx.aolie.common.types.UserRightNode;

import java.io.Serializable;

/**
 * 权限树,生成时,可检查权限树是不是出现循环
 *
 * @author xxl
 * @version V0.0.1
 * @date 2021/2/20 0020 10:43
 **/
public class RightNode extends UserRightNode implements Serializable {

    private RightResourceDto dto;


    public RightNode(RightResourceDto dto) {
        super();
        this.setRightId(dto.getRsId());
        this.setRightName(dto.getRsName());

    }

    public RightNode(Long id, String rightName) {
        super(id, rightName);
    }

    public RightResourceDto getDto() {
        return dto;
    }

    public void setDto(RightResourceDto dto) {
        this.dto = dto;
    }

}
