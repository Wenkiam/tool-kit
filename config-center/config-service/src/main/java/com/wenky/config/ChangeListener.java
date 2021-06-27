package com.wenky.config;

import com.wenky.config.model.ChangeEvent;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public interface ChangeListener {

    void onChange(ChangeEvent event);
}
