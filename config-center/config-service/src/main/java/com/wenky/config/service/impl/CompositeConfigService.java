package com.wenky.config.service.impl;

import com.wenky.config.ChangeListener;
import com.wenky.config.service.ConfigService;

import java.util.Deque;
import java.util.LinkedList;


/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public class CompositeConfigService implements ConfigService {

    public final Deque<ConfigService> configServices = new LinkedList<>();

    @Override
    public String getValue(String configKey) {
        String value;
        for (ConfigService configService : configServices){
            value = configService.getValue(configKey);
            if (value != null && value.trim().length()>0){
                return value;
            }
        }
        return null;
    }

    @Override
    public String getValue(String configKey, String defaultValue) {
        String value;
        return (value = getValue(configKey)) == null ? defaultValue : value;
    }

    @Override
    public void addChangeListener(String configKey, ChangeListener listener) {
        configServices.forEach(configService -> configService.addChangeListener(configKey, listener));
    }

    public void addFirst(ConfigService configService){
        configServices.addFirst(configService);
    }

    public void addLast(ConfigService configService){
        configServices.addLast(configService);
    }
}
