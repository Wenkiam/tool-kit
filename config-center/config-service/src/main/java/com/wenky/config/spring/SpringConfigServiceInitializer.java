package com.wenky.config.spring;

import com.wenky.config.ConfigManager;
import com.wenky.config.service.ConfigService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhongwenjian
 * @date 2021/6/26
 */

public class SpringConfigServiceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        SpringConfigService configService = new SpringConfigService();
        configService.setEnvironment(applicationContext.getEnvironment());
        ConfigManager.addLast(configService);
    }
}
