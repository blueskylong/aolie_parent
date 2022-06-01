package com.ranranx.aolie.gateway.session;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/16 0016 15:08
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SimpleSessionProviderRegister.class)
public @interface EnableSimpleSession {

}
