package com.wenky.config.nacos.parser;

import com.alibaba.nacos.api.utils.StringUtils;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author zhongwenjian
 * @date 2021/9/15
 */
public class PropertiesConfigParser extends AbstractConfigParser {

    public PropertiesConfigParser() {
        super("properties");
    }
    @Override
    public Map<String, Object> parse(String content) throws Exception {
        if (StringUtils.isBlank(content)){
            return null;
        }
        Properties properties = new Properties();
        properties.load(new StringReader(content));
        Map<String, Object> result = new LinkedHashMap<>();
        properties.forEach((k,v)->{
            result.put(k.toString(), v);
        });
        return result;
    }
}
