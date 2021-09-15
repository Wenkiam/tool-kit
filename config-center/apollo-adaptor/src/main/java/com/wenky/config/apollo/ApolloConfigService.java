package com.wenky.config.apollo;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.wenky.config.ChangeListener;
import com.wenky.config.model.ChangeEvent;
import com.wenky.config.service.ConfigService;

import java.util.Collections;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public class ApolloConfigService implements ConfigService {

    private final com.ctrip.framework.apollo.Config config = com.ctrip.framework.apollo.ConfigService.getAppConfig();

    @Override
    public String getValue(String configKey, String defaultValue) {
        return config.getProperty(configKey, defaultValue);
    }

    @Override
    public void addChangeListener(String configKey, ChangeListener listener) {
        config.addChangeListener(changeEvent -> {
            ConfigChange change = changeEvent.getChange(configKey);
            ChangeEvent.Type type = ChangeEvent.Type.valueOf(change.getChangeType().name());
            String oldValue = change.getOldValue();
            String newValue = change.getNewValue();
            listener.onChange(new ChangeEvent(configKey, oldValue, newValue, type));
        }, Collections.singleton(configKey));
    }
}
