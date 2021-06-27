package com.wenky.log.trace.web;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

/**
 * @author zhongwenjian
 * @date 2021/6/27
 */
@Configuration
@ConditionalOnProperty(value = "log.web.trace.enable", havingValue = "true", matchIfMissing = true)
public class TraceWebAutoConfiguration {

    @Bean
    public FilterRegistrationBean<Filter> traceFilter(BeanFactory beanFactory){
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>(
                new LazyTracingFilter(beanFactory));
        filterRegistrationBean.setDispatcherTypes(DispatcherType.ASYNC,
                DispatcherType.ERROR, DispatcherType.FORWARD, DispatcherType.INCLUDE,
                DispatcherType.REQUEST);
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }
}
