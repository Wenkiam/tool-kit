package com.wenky.config;

import com.wenky.config.model.ChangeEvent;
import com.wenky.config.service.ConfigService;
import com.wenky.config.service.impl.CompositeConfigService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.wenky.config.Formatters.*;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public class ConfigManager {
    private static final CompositeConfigService configService = new CompositeConfigService();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    private static final Map<String,Object> configMap = new ConcurrentHashMap<>();
    static {
        ServiceLoader<ConfigService> loader = ServiceLoader.load(ConfigService.class, ConfigManager.class.getClassLoader());
        for (ConfigService service : loader) {
            configService.add(service);
        }
    }
    private ConfigManager(){

    }

    public static String getString(String configKey){
        return getString(configKey,null);
    }
    public static String getString(String configKey, String def){
        return configService.getValue(configKey,def);
    }


    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> clazz, Function<String, T> formatter) {
        Object value = configMap.get(key);
        if (clazz.isInstance(value)){
            return (T) value;
        }
        if (value instanceof String) {
            T result = formatter.apply((String) value);
            configMap.put(key, result);
            return result;
        }
        if (value != null){
            throw new ClassCastException(value.getClass().getName()+" cannot be cast to "+clazz.getName());
        }
        value = getString(key);
        if (value == null){
            return null;
        }
        T result = formatter.apply((String) value);
        configMap.put(key, result);
        addListener(key, event -> {
            if (event.getType() == ChangeEvent.Type.DELETED){
                Object previous = configMap.remove(key);
                LOGGER.info("remove config {} success, previous value:{}", key, previous);
                return;
            }
            String configValue = event.getNewValue();
            try {
                T newValue = formatter.apply(configValue);
                Object previous = configMap.put(key, newValue);
                LOGGER.info("update config for {} success, previous:{}, new:{}", key, previous, newValue);
            } catch (Exception e){
                configMap.put(key, configValue);
                LOGGER.error("config value cast exception, {} can't be cast to {} , config key is {}",
                        configValue,clazz, key, e);
            }
        });
        return result;
    }

    public static Integer getInt(String key) {
        return get(key, Integer.class, INTEGER_FORMATTER);
    }
    public static int getInt(String key, int def){
        try {
            Integer result = getInt(key);
            return result == null ? def : result;
        } catch (Exception e){
            LOGGER.warn("error to get int value with key:{},use default value:{},error={}",key,def,e);
            return def;
        }
    }

    public static Long getLong(String key) {
        return get(key, Long.class, LONG_FORMATTER);
    }
    public static long getLong(String key,long def){
        try {
            Long result = getLong(key);
            return result == null ? def : result;
        } catch (Exception e){
            LOGGER.warn("error to get long value with key:{},use default value:{},error is {}",key,def,e);
            return def;
        }
    }

    public static Float getFloat(String key) {
        return get(key, Float.class, FLOAT_FORMATTER);
    }

    public static float getFloat(String key,float def){
        try {
            Float result = getFloat(key);
            return result == null ? def : result;
        } catch (Exception e){
            LOGGER.warn("error to get float value with key:{},use default value:{},error is {}",key,def,e);
            return def;
        }
    }

    public static Double getDouble(String key) {
        return get(key, Double.class, DOUBLE_FORMATTER);
    }
    public static double getDouble(String key,double def){
        try {
            Double result = getDouble(key);
            return result == null ? def : result;
        } catch (Exception e){
            LOGGER.warn("error to get double value with key:{},use default value:{},error is {}",key,def,e);
            return def;
        }
    }


    public static Boolean getBoolean(String key) {
        return get(key, Boolean.class, BOOLEAN_FORMATTER);
    }
    public static boolean getBoolean(String key,boolean def){
        try {
            Boolean result = getBoolean(key);
            return result == null ? def : result;
        } catch (Exception e){
            LOGGER.warn("error to get boolean value with key:{},use default value:{},error is {}",key,def,e);
            return def;
        }
    }

    public static void registerConfigService(ConfigService configService){
        ConfigManager.configService.add(configService);
        LOGGER.info("init config service:{} success",configService.getClass().getName());
    }
    public static void addListener(String configKey, ChangeListener listener){
        configService.addChangeListener(configKey, listener);
    }
}
