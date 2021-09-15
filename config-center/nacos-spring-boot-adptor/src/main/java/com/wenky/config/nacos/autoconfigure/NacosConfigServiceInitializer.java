package com.wenky.config.nacos.autoconfigure;

import com.alibaba.boot.nacos.config.properties.NacosConfigProperties;
import com.alibaba.boot.nacos.config.util.NacosConfigPropertiesUtils;
import com.alibaba.boot.nacos.config.util.NacosPropertiesBuilder;
import com.alibaba.nacos.api.common.Constants;
import com.wenky.config.ConfigManager;
import com.wenky.config.nacos.NacosConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author zhongwenjian
 * @date 2021/3/5 10:18
 */

public class NacosConfigServiceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        NacosConfigProperties nacosConfigProperties = NacosConfigPropertiesUtils
                .buildNacosConfigProperties(environment);
        registerConfigService(nacosConfigProperties,environment);

    }

    private void registerConfigService(NacosConfigProperties nacosConfigProperties, ConfigurableEnvironment environment){
        Properties properties = NacosPropertiesBuilder.buildNacosProperties(environment,
                nacosConfigProperties.getServerAddr(),
                nacosConfigProperties.getNamespace(), nacosConfigProperties.getEndpoint(),
                nacosConfigProperties.getSecretKey(),
                nacosConfigProperties.getAccessKey(),
                nacosConfigProperties.getRamRoleName(),
                nacosConfigProperties.getConfigLongPollTimeout(),
                nacosConfigProperties.getConfigRetryTime(),
                nacosConfigProperties.getMaxRetry(),
                nacosConfigProperties.isEnableRemoteSyncConfig(),
                nacosConfigProperties.getUsername(), nacosConfigProperties.getPassword());
        String group = nacosConfigProperties.getGroup();
        group = StringUtils.isBlank(group) ? Constants.DEFAULT_GROUP : group;
        properties.put("group", nacosConfigProperties.getGroup());
        Set<String> dataIdSet = new HashSet<>();
        if (StringUtils.isNotBlank(nacosConfigProperties.getDataId())){
            properties.put("dataId", nacosConfigProperties.getDataId());
            dataIdSet.add(nacosConfigProperties.getDataId());
        }
        if (StringUtils.isNotBlank(nacosConfigProperties.getDataIds())){
            properties.put("dataIds", nacosConfigProperties.getDataIds());
            String dataIds = nacosConfigProperties.getDataIds();
            dataIdSet.addAll(Arrays.asList(dataIds.split(",")));
        }
        for (String dataId : dataIdSet) {
            try {
                ConfigManager.registerConfigService(new NacosConfigService(dataId, group, properties));
            } catch (Exception ignore) {

            }
        }
    }

}
