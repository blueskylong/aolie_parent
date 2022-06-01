package com.ranranx.aolie.core.datameta.dto;

import com.ranranx.aolie.common.types.BaseDto;

import javax.persistence.Table;

/**
 * @author xxl
 *  引用数据
 * @date 2020/8/4 17:46
 * @version V0.0.1
 **/
@Table(name = "aolie_dm_reference")
public class ReferenceDto extends BaseDto {
    private Long refId;
    private String refName;
    private String tableName;
    private String idField;
    private String nameField;
    private Integer onlyLeaf;
    private String parentField;
    private String codeField;
    private Integer xh;

    private Long tableId;
    private int treeLike;
    /**
     * 如果是统一表中的引用,则要提供类型
     */
    private String commonType;

    public Long getRefId() {
        return refId;
    }

    public void setRefId(Long refId) {
        this.refId = refId;
    }

    public String getCommonType() {
        return commonType;
    }

    public void setCommonType(String commonType) {
        this.commonType = commonType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIdField() {
        return idField;
    }

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public String getNameField() {
        return nameField;
    }

    public void setNameField(String nameField) {
        this.nameField = nameField;
    }

    public Integer getOnlyLeaf() {
        return onlyLeaf;
    }

    public void setOnlyLeaf(Integer onlyLeaf) {
        this.onlyLeaf = onlyLeaf;
    }

    public String getParentField() {
        return parentField;
    }

    public void setParentField(String parentField) {
        this.parentField = parentField;
    }

    public String getCodeField() {
        return codeField;
    }

    public void setCodeField(String codeField) {
        this.codeField = codeField;
    }

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    public Long getTableId() {
        return tableId;
    }

    public void setTableId(Long tableId) {
        this.tableId = tableId;
    }

    public int getTreeLike() {
        return treeLike;
    }

    public void setTreeLike(int treeLike) {
        this.treeLike = treeLike;
    }

    public Integer getXh() {
        return xh;
    }

    public void setXh(Integer xh) {
        this.xh = xh;
    }
}
