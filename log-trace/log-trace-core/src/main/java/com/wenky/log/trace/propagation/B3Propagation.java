package com.wenky.log.trace.propagation;


import java.util.Collections;
import java.util.List;

import static com.wenky.log.trace.propagation.TraceContext.newTraceId;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public final class B3Propagation<K> implements Propagation<K> {


    public static final Propagation.Factory FACTORY = newFactoryBuilder().build();

    public static FactoryBuilder newFactoryBuilder() {
        return new FactoryBuilder();
    }

    public static final class FactoryBuilder {

        FactoryBuilder() {

        }
        public Propagation.Factory build() {
            return new Factory();
        }
    }
    
    static final String TRACE_ID_NAME = "X-B3-TraceId";
    

    final K traceIdKey;


    B3Propagation(KeyFactory<K> keyFactory) {
        this.traceIdKey = keyFactory.create(TRACE_ID_NAME);
    }

    @Override public List<K> keys() {
        return Collections.singletonList(traceIdKey);
    }

    @Override public <C> TraceContext.Injector<C> injector(Setter<C, K> setter) {
        if (setter == null) {
            throw new NullPointerException("setter == null");
        }
        return new B3Injector<>(this, setter);
    }

    static final class B3Injector<C, K> implements TraceContext.Injector<C> {
        final B3Propagation<K> propagation;
        final Setter<C, K> setter;

        B3Injector(B3Propagation<K> propagation, Setter<C, K> setter) {
            this.propagation = propagation;
            this.setter = setter;
        }

        @Override
        public void inject(TraceContext context, C carrier) {
            setter.put(carrier, propagation.traceIdKey, context.traceIdString());
        }
    }

    @Override
    public <C> TraceContext.Extractor<C> extractor(Getter<C, K> getter) {
        if (getter == null) {
            throw new NullPointerException("getter == null");
        }
        return new B3Extractor<>(this, getter);
    }

    static final class B3Extractor<C, K> implements TraceContext.Extractor<C> {
        final B3Propagation<K> propagation;
        final Getter<C, K> getter;

        B3Extractor(B3Propagation<K> propagation, Getter<C, K> getter) {
            this.propagation = propagation;
            this.getter = getter;
        }

        @Override public TraceContext extract(C carrier) {
            if (carrier == null) {
                throw new NullPointerException("carrier == null");
            }
            String traceIdString = getter.get(carrier, propagation.traceIdKey);
            if (traceIdString == null) {
                return new TraceContext(newTraceId());
            }
            long traceId = TraceContext.Builder.parseTraceId(traceIdString);

            return new TraceContext(traceId);
        }

    }

    static final class Factory extends Propagation.Factory {

        Factory() {

        }

        @Override public <K1> Propagation<K1> create(KeyFactory<K1> keyFactory) {
            return new B3Propagation<>(keyFactory);
        }

        @Override
        public String toString() {
            return "B3PropagationFactory";
        }
    }
}
