package com.wenky.config.nacos.parser;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zhongwenjian
 * @date 2021/9/15
 */
public class YmlConfigParser extends AbstractConfigParser {

    public YmlConfigParser() {
        super("yml", "yaml");
    }
    @Override
    public Map<String, Object> parse(String content) {
        Yaml yaml = new Yaml(new SafeConstructor());
        Map<String, Object> sourceMap = yaml.load(content);
        Map<String, Object> result = new LinkedHashMap<>();
        flattenedMap(result, sourceMap, EMPTY_STRING);
        return result;
    }
}
