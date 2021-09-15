package com.wenky.config.service.impl;

import com.wenky.config.ChangeListener;
import com.wenky.config.service.ConfigService;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public class CompositeConfigService implements ConfigService {

    public final List<ConfigService> configServices = new CopyOnWriteArrayList<>();

    @Override
    public String getValue(String configKey, String defaultValue) {
        String value;
        for (ConfigService configService : configServices){
            value = configService.getValue(configKey);
            if (value != null && value.trim().length()>0){
                return value;
            }
        }
        return defaultValue;
    }

    @Override
    public void addChangeListener(String configKey, ChangeListener listener) {
        configServices.forEach(configService -> configService.addChangeListener(configKey, listener));
    }

    public void add(ConfigService configService){
        if (!configServices.contains(configService)) {
            configServices.add(configService);
        }
    }
}
