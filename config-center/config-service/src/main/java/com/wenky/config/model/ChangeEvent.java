package com.wenky.config.model;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public class ChangeEvent {

    private String configKey;

    private String oldValue;

    private String newValue;

    private Type type;

    public ChangeEvent(String configKey, String oldValue, String newValue, Type type) {
        this.configKey = configKey;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.type = type;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        ADDED, MODIFIED, DELETED
    }
}
