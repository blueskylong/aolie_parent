package com.ranranx.aolie.common.interfaces;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/17 0017 11:08
 **/
public interface SessionStoreService {

    String getValue(String key);

    void setValue(String key, String value);

    void setValue(String key, String value, long timeout);


    /**
     * 写入缓存设置时效时间
     *
     * @param key
     * @param expireTime
     * @return
     */
    void setExpire(final String key, Long expireTime);
}
