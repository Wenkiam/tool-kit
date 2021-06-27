package com.wenky.config;

import com.wenky.config.service.ConfigService;
import com.wenky.config.service.impl.CompositeConfigService;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public class ConfigManager {
    private static final ConfigManager instance = new ConfigManager();

    private ConfigManager(){
        for (ConfigService service : ServiceLoader.load(ConfigService.class)) {
            configService.addFirst(service);
            break;
        }
    }

    private final CompositeConfigService configService = new CompositeConfigService();

    public static void addFirst(ConfigService configService){
        instance.configService.addFirst(configService);
    }

    public static void addLast(ConfigService configService){
        instance.configService.addLast(configService);
    }

    public static void addListener(String configKey, ChangeListener changeListener){
        instance.configService.addChangeListener(configKey, changeListener);
    }

    public static String getString(String configKey,String def){
        return instance.configService.getValue(configKey, def);
    }

    public static String getString(String configKey){
        return instance.configService.getValue(configKey);
    }

    public static int getInt(String configKey){
        return Integer.parseInt(getString(configKey));
    }
    public static int getInt(String configKey, int def){
        return getDefaultIfNull(configKey, Integer::parseInt, def);
    }

    public static long getLong(String configKey){
        return Long.parseLong(getString(configKey));
    }

    public static long getLong(String configKey, long def){
        return getDefaultIfNull(configKey,Long::parseLong,def);
    }

    public static float getFloat(String configKey){
        return Float.parseFloat(getString(configKey));
    }
    public static float getFloat(String configKey, float def){
        return getDefaultIfNull(configKey,Float::parseFloat,def);
    }

    public static double getDouble(String configKey){
        return Double.parseDouble(configKey);
    }
    public static double getDouble(String configKey, double def){
        return getDefaultIfNull(configKey,Double::parseDouble,def);
    }

    public static <T> T getDefaultIfNull(String configKey, Function<String, T> function, T def){
        return Optional.ofNullable(getString(configKey)).map(function).orElse(def);
    }
    public static boolean getBoolean(String configKey){
        return Boolean.parseBoolean(getString(configKey));
    }

    public static boolean getBoolean(String configKey, boolean def){
        return getDefaultIfNull(configKey, Boolean::parseBoolean, def);
    }
}
