package com.wenky.config;

import java.util.function.Function;

/**
 * @author zhongwenjian
 * @date 2021/9/10
 */
public class Formatters {

    private static final String TRUE = "true";
    private static final String FALSE = "false";
    static final Function<String, Boolean> BOOLEAN_FORMATTER = value->{
        if (value == null){
            return null;
        }
        if (TRUE.equalsIgnoreCase(value)){
            return true;
        }
        if (FALSE.equalsIgnoreCase(value)){
            return false;
        }
        throw new IllegalArgumentException("Invalid boolean value [" + value + "]");
    };

    static final Function<String, Integer> INTEGER_FORMATTER = Integer::parseInt;

    static final Function<String, Long> LONG_FORMATTER = Long::parseLong;

    static final Function<String, Double> DOUBLE_FORMATTER = Double::parseDouble;

    static final Function<String, Float> FLOAT_FORMATTER = Float::parseFloat;
}
