package com.wenky.log.trace.spring;

import com.wenky.log.trace.Tracing;
import com.wenky.log.trace.propagation.B3Propagation;
import com.wenky.log.trace.propagation.CurrentTraceContext;
import com.wenky.log.trace.propagation.Propagation;
import com.wenky.log.trace.propagation.Slf4jScopeDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author zhongwenjian
 * @date 2021/6/26
 */
@Configuration
@ConditionalOnProperty(value = "log.trace.enabled", havingValue = "true", matchIfMissing = true)
public class LogTraceAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public Tracing tracing(Propagation.Factory propagationFactory, CurrentTraceContext currentTraceContext){

        Tracing.Builder builder = Tracing.newBuilder();
        builder.propagationFactory(propagationFactory);
        builder.currentTraceContext(currentTraceContext);
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CurrentTraceContext currentTraceContext(@Autowired(required = false) List<CurrentTraceContext.ScopeDecorator> scopeDecorators){
        CurrentTraceContext.Builder builder = CurrentTraceContext.Default.newBuilder();
        if (scopeDecorators != null ){
            scopeDecorators.forEach(builder::addScopeDecorator);
        }
        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public Propagation.Factory propagationFactory(){
        return B3Propagation.FACTORY;
    }

    @Configuration
    static class DecoratorConfiguration{

        @Bean
        public Slf4jScopeDecorator logScopeDecorator(){
            return new Slf4jScopeDecorator();
        }
    }
}
