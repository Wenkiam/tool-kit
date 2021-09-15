package com.wenky.config.spring;

import com.wenky.config.ConfigManager;
import com.wenky.config.service.ConfigService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author zhongwenjian
 * @date 2021/9/10
 */
@Configuration
public class ConfigServiceAutoConfiguration {

    @Bean
    public static ConfigServiceBeanPostProcessor configServiceBeanPostProcessor(){
        return new ConfigServiceBeanPostProcessor();
    }
    static class ConfigServiceBeanPostProcessor implements BeanPostProcessor {

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof ConfigService) {
                ConfigManager.registerConfigService((ConfigService) bean);
            }
            return bean;
        }
    }
}
