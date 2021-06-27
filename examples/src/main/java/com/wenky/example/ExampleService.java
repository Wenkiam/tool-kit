package com.wenky.example;

import com.wenky.log.trace.async.TraceExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zhongwenjian
 * @date 2021/6/26
 */
@Service
public class ExampleService {

    private static final Logger log = LoggerFactory.getLogger(ExampleService.class);
    private final ExecutorService executorService = new TraceExecutorService(Executors.newSingleThreadExecutor());
    String hello(String name){
        log.info(" hello {}",name);
        executorService.submit(()->{
            log.info("async log:{},env:{}", name, MDC.get("env"));
        });
        return "hello "+name;
    }
}
