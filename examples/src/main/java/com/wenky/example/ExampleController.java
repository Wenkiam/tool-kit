package com.wenky.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author zhongwenjian
 * @date 2021/6/26
 */
@RestController
@RequestMapping
public class ExampleController {

    private static final Logger log = LoggerFactory.getLogger(ExampleController.class);

    @Resource
    private ExampleService service;

    @GetMapping("/hello/{name}")
    public String hello(@PathVariable String name){
        log.info(" request:/hello/"+name);
        return service.hello(name);
    }
}
