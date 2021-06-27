package com.wenky.log.trace.rocketmq;

import com.wenky.log.trace.Tracing;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @author zhongwenjian
 * @date 2021/3/20 19:11
 */
public class TraceableMessageListenerOrderly extends AbstractTraceableMessageListener implements MessageListenerOrderly {

    private final MessageListenerOrderly delegate;

    TraceableMessageListenerOrderly(Tracing tracing, MessageListenerOrderly delegate) {
        super(tracing);
        this.delegate = delegate;
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        return consume(msgs, delegate, d-> delegate.consumeMessage(msgs, context));
    }
}
