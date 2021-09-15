package com.wenky.config.nacos.parser;

import java.util.Map;

/**
 * @author zhongwenjian
 * @date 2021/9/15
 */
public interface ConfigParser {

    boolean isResponsibleFor(String extension);

    Map<String, Object> parse(String content) throws Exception;
}
