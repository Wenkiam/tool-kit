package com.wenky.config.nacos.parser;

import com.alibaba.nacos.api.utils.StringUtils;

import java.util.*;

/**
 * @author zhongwenjian
 * @date 2021/9/15
 */
public abstract class AbstractConfigParser implements ConfigParser {

    private final Set<String> configTypes;

    private static final String DOT = ".";

    static final String EMPTY_STRING = "";

    protected AbstractConfigParser(String... configTypes) {
        if (configTypes == null || configTypes.length == 0) {
            throw new IllegalArgumentException("config type must not be empty");
        }
        this.configTypes = new HashSet<>();
        for (String configType : configTypes) {
            if (configType == null || configType.isEmpty()) {
                throw new IllegalArgumentException("config type must not be empty");
            }
            this.configTypes.add(configType);
        }

    }

    @Override
    public boolean isResponsibleFor(String extension) {
        for (String configType : configTypes) {
            if (configType.equalsIgnoreCase(extension)){
                return true;
            }
        }
        return false;
    }

    @Override
    public abstract Map<String, Object> parse(String content) throws Exception;

    protected void flattenedMap(Map<String, Object> result, Map<String, Object> dataMap,
                                String parentKey) {
        Set<Map.Entry<String, Object>> entries = dataMap.entrySet();
        for (Iterator<Map.Entry<String, Object>> iterator = entries.iterator(); iterator
                .hasNext();) {
            Map.Entry<String, Object> entry = iterator.next();
            String key = entry.getKey();
            Object value = entry.getValue();

            String fullKey = StringUtils.isEmpty(parentKey) ? key : key.startsWith("[")
                    ? parentKey.concat(key) : parentKey.concat(DOT).concat(key);

            if (value instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) value;
                flattenedMap(result, map, fullKey);
                continue;
            }
            else if (value instanceof Collection) {
                int count = 0;
                Collection<Object> collection = (Collection<Object>) value;
                for (Object object : collection) {
                    flattenedMap(result,
                            Collections.singletonMap("[" + (count++) + "]", object),
                            fullKey);
                }
                continue;
            }

            result.put(fullKey, value);
        }
    }
}
