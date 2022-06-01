package com.ranranx.aolie.gateway.session;

import com.ranranx.aolie.gateway.interfaces.TokenProvider;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;


/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/17 0017 17:23
 **/
public class SimpleSessionProviderRegister implements ImportBeanDefinitionRegistrar {
    /**
     * Register bean definitions as necessary based on the given annotation metadata of
     * the importing {@code @Configuration} class.
     * <p>Note that {@link SimpleSessionProvider} types may <em>not</em> be
     * registered here, due to lifecycle constraints related to {@code @Configuration}
     * class processing.
     * <p>The default implementation is empty.
     *
     * @param importingClassMetadata annotation metadata of the importing class
     * @param registry               current bean definition registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(TokenProvider.BEAN_NAME_TOKEN_PROVIDER)) {
            AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(SimpleSessionProvider.class).getBeanDefinition();
            registry.registerBeanDefinition(TokenProvider.BEAN_NAME_TOKEN_PROVIDER, beanDefinition);
        }
    }
}
