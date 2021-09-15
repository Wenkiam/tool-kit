package com.wenky.config.service;

import com.wenky.config.ChangeListener;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public interface ConfigService {

    default String getValue(String configKey){
        return getValue(configKey, null);
    }

    String getValue(String configKey, String defaultValue);

    default void addChangeListener(String configKey, ChangeListener listener){

    }
}
