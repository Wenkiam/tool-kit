package com.wenky.log.trace.rocketmq;


import com.wenky.log.trace.TraceScope;
import com.wenky.log.trace.Tracer;
import com.wenky.log.trace.Tracing;
import com.wenky.log.trace.propagation.TraceContext;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

/**
 * @author zhongwenjian
 * @date 2021/3/20 17:00
 */
public abstract class AbstractTraceableMessageListener {

    final Logger log = LoggerFactory.getLogger(getClass());

    final Tracer tracer;

    private final TraceContext.Extractor<MessageExt> extractor;

    AbstractTraceableMessageListener(Tracing tracing){
        tracer = tracing.tracer();
        extractor = tracing.propagation().extractor(MessageExt::getUserProperty);
    }

    TraceScope createScope(MessageExt messageExt){
        TraceContext extracted = extractor.extract(messageExt);
        return tracer.newScope(extracted);
    }

    <T> T consume(List<MessageExt> msgs, MessageListener delegate, Function<MessageListener, T> resultMapper){
        if (msgs == null || msgs.isEmpty()){
            return resultMapper.apply(delegate);
        }
        MessageExt messageExt = msgs.get(0);
        try (TraceScope ignored = createScope(messageExt)) {
            return resultMapper.apply(delegate);
        }
    }

}
