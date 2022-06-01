package com.ranranx.aolie.core.datameta.datamodel;

import com.ranranx.aolie.common.types.Constants;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2020/11/30 14:39
 **/
public class SchemaTools {
    /**
     * 方案版本缓存前缀
     */
    static final String SCHEMA_VERSION_PREFIX = "SCHEMA_VERSION";
    /**
     * 方案缓存前缀
     */
    static final String SCHEMA_PREFIX = "SCHEMA";

    /**
     * 是不是全局引用方案
     *
     * @param schemaId
     * @return
     */
    public static boolean isReferenceSchema(Long schemaId) {
        if (schemaId == null) {
            return false;
        }
        return schemaId.equals(Constants.DEFAULT_REFERENCE_SCHEMA);
    }

    public static String getSchemaCacheKey(Long schemaId, String version) {
        return SCHEMA_PREFIX + "_" + version + "_" + schemaId;
    }

    public static String getSchemaVersionCacheKey(Long schemaId, String version) {
        return SCHEMA_VERSION_PREFIX + "_" + version + "_" + schemaId;

    }
}
