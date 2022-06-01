package com.ranranx.aolie.common.types;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限树,生成时,可检查权限树是不是出现循环
 *
 * @author xxl
 * @version V0.0.1
 * @date 2021/2/20 0020 10:43
 **/
public class UserRightNode implements Serializable {
    /**
     * 下级权限节点,根据关系 配置查询
     */
    private List<UserRightNode> lstSub = new ArrayList<>();

    private List<UserRightNode> lstParent = new ArrayList<>();

    /**
     * 权限ID 即RS_ID
     */
    private Long rightId;
    /**
     * 权限名称
     */
    private String rightName;

    public UserRightNode() {

    }

    public UserRightNode(Long id, String rightName) {
        this.rightId = id;
        this.rightName = rightName;
    }

    public List<UserRightNode> getLstSub() {
        return lstSub;
    }

    public void setLstSub(List<UserRightNode> lstSub) {
        this.lstSub = lstSub;
    }

    public void addSubNode(UserRightNode node) {
        lstSub.add(node);
    }

    public void addParentNode(UserRightNode node) {
        lstParent.add(node);
    }

    public Long getRightId() {
        return rightId;
    }

    public void setRightId(Long rightId) {
        this.rightId = rightId;
    }

    public String getRightName() {
        return rightName;
    }

    public void setRightName(String rightName) {
        this.rightName = rightName;
    }


    public List<UserRightNode> getLstParent() {
        return lstParent;
    }

    public void setLstParent(List<UserRightNode> lstParent) {
        this.lstParent = lstParent;
    }

}
