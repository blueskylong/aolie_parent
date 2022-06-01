package com.ranranx.aolie.common.runtime;

import com.ranranx.aolie.common.types.Constants;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/27 0027 9:31
 **/
public class SimpleSessionUtils {
    protected static String defaultVersion = Constants.DEFAULT_VERSION;

    public static final String KEY_TOKEN = "X-TOKEN";


    public static String getToken(ServerHttpRequest request) {
        return request.getHeaders().getFirst(KEY_TOKEN);
    }

    public static String getDefaultVersion() {
        return defaultVersion;
    }
}
