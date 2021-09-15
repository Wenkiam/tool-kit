package com.wenky.config.nacos.parser;


import com.alibaba.nacos.api.utils.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zhongwenjian
 * @date 2021/9/15
 */
public class JsonConfigParser extends AbstractConfigParser {
    public JsonConfigParser() {
        super("json");
    }


    @Override
    public Map<String, Object> parse(String data) throws Exception {
        if (StringUtils.isEmpty(data)) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>(32);

        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> nacosDataMap = null;

        nacosDataMap = mapper.readValue(data, LinkedHashMap.class);

        if (nacosDataMap == null || nacosDataMap.isEmpty()) {
            return result;
        }
        flattenedMap(result, nacosDataMap, EMPTY_STRING);

        result.putAll(nacosDataMap);

        return result;
    }
}
