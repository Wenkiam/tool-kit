package com.wenky.log.trace.rocketmq;

import com.wenky.log.trace.ContextScope;
import com.wenky.log.trace.Tracer;
import com.wenky.log.trace.Tracing;
import com.wenky.log.trace.propagation.Propagation;
import com.wenky.log.trace.propagation.TraceContext;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author zhongwenjian
 * @date 2021/3/20 16:10
 */
@Configuration
@ConditionalOnBean(Tracing.class)
@ConditionalOnProperty(name = "log.trace.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
public class RocketmqAutoConfiguration{

    @Bean
    @ConditionalOnClass(name = "org.apache.rocketmq.client.producer.MQProducer")
    public LogTraceRocketmqAspect rocketmqAspect(Tracing tracing){
        return new LogTraceRocketmqAspect(tracing);
    }

    @Bean("rocketmqConsumerPostProcessor")
    @ConditionalOnClass(name = "org.apache.rocketmq.client.consumer.DefaultMQPushConsumer")
    public static BeanPostProcessor consumerPostProcessor(BeanFactory beanFactory){
        return new RocketMqConsumerPostProcessor(beanFactory) {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DefaultMQPushConsumer){
                    wrapConsumer((DefaultMQPushConsumer) bean);
                }
                return bean;
            }
        };
    }
    @Bean("rocketmqContainerPostProcessor")
    @ConditionalOnClass(name = "org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer")
    public static BeanPostProcessor containerPostProcessor(BeanFactory beanFactory){
        return new RocketMqConsumerPostProcessor(beanFactory) {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DefaultRocketMQListenerContainer){
                    wrapContainer((DefaultRocketMQListenerContainer) bean);
                }
                return bean;
            }
        };
    }

    static abstract class RocketMqConsumerPostProcessor implements BeanPostProcessor{

        final BeanFactory beanFactory;

        @Override
        public abstract Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException ;

        RocketMqConsumerPostProcessor(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        void wrapConsumer(DefaultMQPushConsumer consumer){
            MessageListener listener = consumer.getMessageListener();
            if (listener instanceof AbstractTraceableMessageListener){
                return;
            }
            Tracing tracing = beanFactory.getBean(Tracing.class);
            if (listener instanceof MessageListenerOrderly){
                consumer.setMessageListener(new TraceableMessageListenerOrderly(tracing, (MessageListenerOrderly) listener));
            }
            if (listener instanceof MessageListenerConcurrently){
                consumer.setMessageListener(new TraceableMessageListenerConcurrently(tracing, (MessageListenerConcurrently) listener));
            }
        }

        void wrapContainer(DefaultRocketMQListenerContainer container) {
            Tracing tracing = beanFactory.getBean(Tracing.class);
            DefaultMQPushConsumer consumer = container.getConsumer();
            switch (container.getConsumeMode()) {
                case ORDERLY:
                    consumer.setMessageListener((new TraceableContainerMessageListenerOrderly(container,tracing)));
                    break;
                case CONCURRENTLY:
                    consumer.setMessageListener((new TraceableContainerMessageListenerConcurrently(container,tracing)));
                    break;
                default:
                    throw new IllegalArgumentException("Property 'consumeMode' was wrong.");
            }
        }

    }
}

@Aspect
class LogTraceRocketmqAspect {

    static final Propagation.Setter<Message,String> SETTER =(message, k, v)->{
        if (k == null || k.trim().isEmpty() || v == null || v.trim().isEmpty()){
            return;
        }
        message.putUserProperty(k, v);
    };

    private final Tracer tracer;

    private final TraceContext.Injector<Message> injector;

    LogTraceRocketmqAspect(Tracing tracing){
        tracer = tracing.tracer();
        injector = tracing.propagation().injector(SETTER);
    }

    /**
     * rocketmq producer的发送方法都在MQProducer这个接口里，直接拦截这个接口里面带Message类型参数的方法即可
     */
    @Pointcut("execution(public * org.apache.rocketmq.client.producer.MQProducer.*(..))")
    private void anyProducerSendMethod() {

    }

    /**
     * 查找带Message类型参数的方法，拿到所有的Message对象（包括集合里面的），
     * 在Message对象里面的userProperties属性里面注入trace信息
     * @param pjp 切面
     * @return 执行结果
     * @throws Throwable 可能的异常
     */
    @Around("anyProducerSendMethod()")
    public Object wrapProducerMessage(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        List<Message> messages = findMessages(Arrays.asList(args));
        if (messages.isEmpty()){
            return pjp.proceed();
        }
        ContextScope scope = tracer.newScope();
        messages.forEach(message -> injector.inject(scope.context(),message));
        try{
            return pjp.proceed();
        }finally {
            scope.finish();
        }

    }

    private List<Message> findMessages(Collection<?> args){
        List<Message> result = new ArrayList<>();
        for (Object arg : args){
            if (arg instanceof Message){
                result.add((Message) arg);
            }
            if (arg instanceof Collection){
                result.addAll(findMessages((Collection<?>) arg));
            }
        }
        return result;
    }

}