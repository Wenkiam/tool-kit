package com.wenky.log.trace.rocketmq;

import com.wenky.log.trace.TraceScope;
import com.wenky.log.trace.Tracing;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.utils.MessageUtil;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQReplyListener;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.MethodParameter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.SmartMessageConverter;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 *
 * 处理 @RocketMQMessageListener 注解的消费类，拿到container对象后仿照
 * {@link DefaultRocketMQListenerContainer}重写consumeMessage方法
 * 在MessageExt对象的userProperties里面拿到trace信息放到trace context里面
 *
 * @author zhongwenjian
 * @date 2021/3/23 11:50
 */
class TraceableContainerMessageListener extends AbstractTraceableMessageListener{

    private final RocketMQListener rocketMQListener;
    private final RocketMQReplyListener rocketMQReplyListener;
    private final DefaultMQPushConsumer consumer;
    private final RocketMQMessageListener rocketMQMessageListener;
    DefaultRocketMQListenerContainer container;
    private MessageConverter messageConverter;
    private String charset;
    TraceableContainerMessageListener(DefaultRocketMQListenerContainer container, Tracing tracing){
        super(tracing);
        this.container = container;
        rocketMQListener = container.getRocketMQListener();
        rocketMQReplyListener = container.getRocketMQReplyListener();
        rocketMQMessageListener = container.getRocketMQMessageListener();
        consumer = container.getConsumer();
        messageConverter = container.getMessageConverter();
        charset = container.getCharset();
    }
    void handleMessage(MessageExt messageExt) throws Exception {
        log.debug("received msg: {}", messageExt);
        TraceScope span = createScope(messageExt);
        try {
            if (rocketMQListener != null) {
                rocketMQListener.onMessage(doConvertMessage(messageExt));
            } else if (rocketMQReplyListener != null) {
                Object replyContent = rocketMQReplyListener.onMessage(doConvertMessage(messageExt));
                Message<?> message = MessageBuilder.withPayload(replyContent).build();

                org.apache.rocketmq.common.message.Message replyMessage = MessageUtil.createReplyMessage(messageExt, convertToBytes(message));
                consumer.getDefaultMQPushConsumerImpl().getmQClientFactory().getDefaultMQProducer().send(replyMessage, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
                            log.error("Consumer replies message failed. SendStatus: {}", sendResult.getSendStatus());
                        } else {
                            log.info("Consumer replies message success.");
                        }
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("Consumer replies message failed. error: {}", e.getLocalizedMessage());
                    }
                });
            }
        }finally {
            span.close();
        }
    }
    private byte[] convertToBytes(Message<?> message) {
        Message<?> messageWithSerializedPayload = doConvert(message.getPayload(), message.getHeaders());
        Object payloadObj = messageWithSerializedPayload.getPayload();
        byte[] payloads;
        try {
            if (payloadObj instanceof String) {
                payloads = ((String) payloadObj).getBytes(Charset.forName(charset));
            } else if (payloadObj instanceof byte[]) {
                payloads = (byte[]) messageWithSerializedPayload.getPayload();
            } else {
                String jsonObj = (String) this.messageConverter.fromMessage(messageWithSerializedPayload, payloadObj.getClass());
                if (null == jsonObj) {
                    throw new RuntimeException(String.format(
                            "empty after conversion [messageConverter:%s,payloadClass:%s,payloadObj:%s]",
                            this.messageConverter.getClass(), payloadObj.getClass(), payloadObj));
                }
                payloads = jsonObj.getBytes(Charset.forName(charset));
            }
        } catch (Exception e) {
            throw new RuntimeException("convert to bytes failed.", e);
        }
        return payloads;
    }
    private Message<?> doConvert(Object payload, MessageHeaders headers) {
        Message<?> message = this.messageConverter instanceof SmartMessageConverter ?
                ((SmartMessageConverter) this.messageConverter).toMessage(payload, headers, null) :
                this.messageConverter.toMessage(payload, headers);
        if (message == null) {
            String payloadType = payload.getClass().getName();
            Object contentType = headers != null ? headers.get(MessageHeaders.CONTENT_TYPE) : null;
            throw new MessageConversionException("Unable to convert payload with type='" + payloadType +
                    "', contentType='" + contentType + "', converter=[" + this.messageConverter + "]");
        }
        MessageBuilder<?> builder = MessageBuilder.fromMessage(message);
        builder.setHeaderIfAbsent(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.TEXT_PLAIN);
        return builder.build();
    }
    private Object doConvertMessage(MessageExt messageExt) {
        Type messageType = getMessageType();
        if (Objects.equals(messageType, MessageExt.class)) {
            return messageExt;
        } else {
            String str = new String(messageExt.getBody(), StandardCharsets.UTF_8);
            if (Objects.equals(messageType, String.class)) {
                return str;
            } else {
                // If msgType not string, use objectMapper change it.
                try {
                    if (messageType instanceof Class) {
                        //if the messageType has not Generic Parameter
                        return container.getMessageConverter().fromMessage(MessageBuilder.withPayload(str).build(), (Class<?>) messageType);
                    } else {
                        //if the messageType has Generic Parameter, then use SmartMessageConverter#fromMessage with third parameter "conversionHint".
                        //we have validate the MessageConverter is SmartMessageConverter in this#getMethodParameter.
                        return ((SmartMessageConverter) container.getMessageConverter()).fromMessage(MessageBuilder.withPayload(str).build(), (Class<?>) ((ParameterizedType) messageType).getRawType(), getMethodParameter());
                    }
                } catch (Exception e) {
                    log.info("convert failed. str:{}, msgType:{}", str, messageType);
                    throw new RuntimeException("cannot convert message to " + messageType, e);
                }
            }
        }
    }
    private Type getMessageType() {
        Class<?> targetClass;
        if (rocketMQListener != null) {
            targetClass = AopProxyUtils.ultimateTargetClass(rocketMQListener);
        } else {
            targetClass = AopProxyUtils.ultimateTargetClass(rocketMQReplyListener);
        }
        Type matchedGenericInterface = null;
        while (Objects.nonNull(targetClass)) {
            Type[] interfaces = targetClass.getGenericInterfaces();
            if (Objects.nonNull(interfaces)) {
                for (Type type : interfaces) {
                    if (type instanceof ParameterizedType &&
                            (Objects.equals(((ParameterizedType) type).getRawType(), RocketMQListener.class) || Objects.equals(((ParameterizedType) type).getRawType(), RocketMQReplyListener.class))) {
                        matchedGenericInterface = type;
                        break;
                    }
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        if (Objects.isNull(matchedGenericInterface)) {
            return Object.class;
        }

        Type[] actualTypeArguments = ((ParameterizedType) matchedGenericInterface).getActualTypeArguments();
        if (Objects.nonNull(actualTypeArguments) && actualTypeArguments.length > 0) {
            return actualTypeArguments[0];
        }
        return Object.class;
    }
    private MethodParameter getMethodParameter() {
        Class<?> targetClass;
        if (rocketMQListener != null) {
            targetClass = AopProxyUtils.ultimateTargetClass(rocketMQListener);
        } else {
            targetClass = AopProxyUtils.ultimateTargetClass(rocketMQReplyListener);
        }
        Type messageType = this.getMessageType();
        Class clazz = null;
        if (messageType instanceof ParameterizedType && container.getMessageConverter() instanceof SmartMessageConverter) {
            clazz = (Class) ((ParameterizedType) messageType).getRawType();
        } else if (messageType instanceof Class) {
            clazz = (Class) messageType;
        } else {
            throw new RuntimeException("parameterType:" + messageType + " of onMessage method is not supported");
        }
        try {
            final Method method = targetClass.getMethod("onMessage", clazz);
            return new MethodParameter(method, 0);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException("parameterType:" + messageType + " of onMessage method is not supported");
        }
    }
}

class TraceableContainerMessageListenerConcurrently extends TraceableContainerMessageListener implements MessageListenerConcurrently {

    TraceableContainerMessageListenerConcurrently(DefaultRocketMQListenerContainer container,Tracing tracing) {
        super(container,tracing);
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt messageExt : msgs) {
            try {
                handleMessage(messageExt);
            } catch (Exception e) {
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }


}

class TraceableContainerMessageListenerOrderly extends TraceableContainerMessageListener implements MessageListenerOrderly {

    TraceableContainerMessageListenerOrderly(DefaultRocketMQListenerContainer container,Tracing tracing) {
        super(container,tracing);
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        for (MessageExt messageExt : msgs) {
            try {
                handleMessage(messageExt);
            } catch (Exception e) {
                context.setSuspendCurrentQueueTimeMillis(container.getSuspendCurrentQueueTimeMillis());
                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
            }
        }

        return ConsumeOrderlyStatus.SUCCESS;
    }

}
