package com.wenky.config.spring;

import com.wenky.config.ChangeListener;
import com.wenky.config.service.ConfigService;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * @author zhongwenjian
 * @date 2021/6/26
 */
public class SpringConfigService implements ConfigService, EnvironmentAware {

    private Environment environment;
    @Override
    public String getValue(String configKey) {
        return environment.getProperty(configKey);
    }

    @Override
    public String getValue(String configKey, String defaultValue) {
        return environment.getProperty(configKey, defaultValue);
    }

    @Override
    public void addChangeListener(String configKey, ChangeListener listener) {

    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
