package com.wenky.config.nacos.exception;

/**
 * @author zhongwenjian
 * @date 2021/9/15
 */
public class UnKnownExtensionException extends RuntimeException {

    public UnKnownExtensionException(String extension) {
        super("unknown extension:"+extension);
    }
}
