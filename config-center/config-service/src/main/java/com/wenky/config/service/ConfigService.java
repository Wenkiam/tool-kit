package com.wenky.config.service;

import com.wenky.config.ChangeListener;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public interface ConfigService {

    String getValue(String configKey);

    String getValue(String configKey, String defaultValue);

    void addChangeListener(String configKey, ChangeListener listener);
}
