package com.wenky.example;

import com.wenky.log.trace.propagation.B3Propagation;
import com.wenky.log.trace.propagation.ExtraFieldPropagation;
import com.wenky.log.trace.propagation.Propagation;
import com.wenky.log.trace.spring.LogTraceAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author zhongwenjian
 * @date 2021/6/26
 */
@Configuration
@AutoConfigureAfter(LogTraceAutoConfiguration.class)
public class ExampleConfig {

    @Bean
    public Propagation.Factory factory(){
        return new ExtraFieldPropagation.Factory(B3Propagation.FACTORY);
    }
}
