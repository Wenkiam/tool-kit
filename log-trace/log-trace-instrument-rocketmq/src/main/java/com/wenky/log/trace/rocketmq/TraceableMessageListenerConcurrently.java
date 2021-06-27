package com.wenky.log.trace.rocketmq;

import com.wenky.log.trace.Tracing;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @author zhongwenjian
 * @date 2021/3/20 19:08
 */
public class TraceableMessageListenerConcurrently extends AbstractTraceableMessageListener implements MessageListenerConcurrently {
    private final MessageListenerConcurrently delegate;
    TraceableMessageListenerConcurrently(Tracing tracing, MessageListenerConcurrently delegate) {
        super(tracing);
        if (delegate instanceof TraceableMessageListenerConcurrently){
            this.delegate = ((TraceableMessageListenerConcurrently) delegate).delegate;
        }else {
            this.delegate = delegate;
        }

    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        return consume(msgs, delegate, d->delegate.consumeMessage(msgs, context));
    }
}
