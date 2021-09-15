package com.wenky.config.nacos.autoconfigure;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.cloud.nacos.client.NacosPropertySourceLocator;

import com.wenky.config.ChangeListener;
import com.wenky.config.nacos.NacosConfigService;
import com.wenky.config.service.ConfigService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.*;

/**
 * @author zhongwenjian
 * @date 2021/3/6 13:51
 */
@Configuration
public class NacosCloudConfigServiceAutoConfig {
    @Bean
    @ConditionalOnBean({NacosConfigProperties.class,NacosPropertySourceLocator.class})
    public ConfigService configService(NacosConfigProperties configProperties, NacosPropertySourceLocator propertySourceLocator, Environment environment) throws Exception {
        Collection<PropertySource<?>> propertySources =  locateCollection(propertySourceLocator,environment);
        NacosCloudConfigService nacosCloudConfigService = new NacosCloudConfigService();
        Properties properties = configProperties.assembleConfigServiceProperties();
        for (PropertySource<?> propertySource : propertySources){
            if (propertySource instanceof NacosPropertySource){
                String dataId = ((NacosPropertySource) propertySource).getDataId();
                String groupId = ((NacosPropertySource) propertySource).getGroup();
                NacosConfigService configService = new NacosConfigService(dataId, groupId, properties);
                nacosCloudConfigService.add(configService);
            }
        }
        return nacosCloudConfigService;
    }

    private Collection<PropertySource<?>> locateCollection(PropertySourceLocator locator, Environment environment) {
        PropertySource<?> propertySource = locator.locate(environment);
        if (propertySource == null) {
            return Collections.emptyList();
        }
        if (propertySource instanceof CompositePropertySource) {
            Collection<PropertySource<?>> sources = ((CompositePropertySource) propertySource)
                    .getPropertySources();
            List<PropertySource<?>> filteredSources = new ArrayList<>();
            for (PropertySource<?> p : sources) {
                if (p != null) {
                    filteredSources.add(p);
                }
            }
            return filteredSources;
        }
        else {
            return Collections.singletonList(propertySource);
        }
    }

    static class NacosCloudConfigService extends ArrayList<ConfigService> implements ConfigService {

        @Override
        public String getValue(String configKey, String defaultValue) {
            String result;
            for (ConfigService configService : this) {
                if ( (result = configService.getValue(configKey)) != null) {
                    return result;
                }
            }
            return defaultValue;
        }

        @Override
        public void addChangeListener(String configKey, ChangeListener listener) {
            this.forEach(configService -> configService.addChangeListener(configKey, listener));
        }
    }
}
