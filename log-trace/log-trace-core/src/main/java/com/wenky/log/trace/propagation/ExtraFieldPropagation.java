package com.wenky.log.trace.propagation;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wenky.config.ConfigManager;
import com.wenky.config.model.ChangeEvent;
import com.wenky.log.trace.Tracing;

import java.util.*;

/**
 * @author zhongwenjian
 * @date 2021/6/26
 */
public class ExtraFieldPropagation<K> implements Propagation<K>{

    final Propagation<K> delegate;

    final KeyFactory<K> keyFactory;

    final Factory factory;

    public ExtraFieldPropagation(Propagation<K> delegate, KeyFactory<K> keyFactory,Factory factory) {
        this.delegate = delegate;
        this.keyFactory = keyFactory;
        this.factory = factory;
    }

    @Override
    public List<K> keys() {
        return delegate.keys();
    }

    Map<String, String> extraFields(){
        return factory.extraFields;
    }
    @Override
    public <C> TraceContext.Injector<C> injector(Setter<C, K> setter) {
        return new Injector<>(delegate.injector(setter),this,setter);
    }

    @Override
    public <C> TraceContext.Extractor<C> extractor(Getter<C, K> getter) {
        return new Extractor<>(delegate.extractor(getter),this, getter);
    }

    public static boolean addExtra(String key, String value){
        return addExtra(Tracing.currentContext(), key, value);
    }

    public static boolean addExtra(TraceContext context, String key, String value){
        if (context == null){
            return false;
        }
        context.addExtra(key, value);
        return true;
    }

    public static String get(TraceContext context, String key){
        Map<String,String> extra = context.extra();
        return extra.get(key);
    }

    public static Map<String,String> getExtraFields(TraceContext context){
        if (context == null ){
            return new HashMap<>();
        }
        return context.extra();
    }

    static class Extractor<C,K> implements TraceContext.Extractor<C>{
        final TraceContext.Extractor<C> delegate;
        final ExtraFieldPropagation<K> propagation;
        final Getter<C,K> getter;
        Extractor(TraceContext.Extractor<C> delegate, ExtraFieldPropagation<K> propagation, Getter<C, K> getter) {
            this.delegate = delegate;
            this.propagation = propagation;
            this.getter = getter;
        }

        @Override
        public TraceContext extract(C carrier) {
            TraceContext result = delegate.extract(carrier);
            Map<String,String> extra = propagation.extraFields();
            extra.forEach((key,v)->{
                String value = getter.get(carrier,propagation.keyFactory.create(key));
                value = value == null ? v : value;
                if (value != null){
                    result.addExtra(key, value);
                }
            });
            return result;
        }
    }

    static class  Injector<C1,K> implements TraceContext.Injector<C1>{

        private final TraceContext.Injector<C1> delegate;

        private final ExtraFieldPropagation<K> propagation;

        final Propagation.Setter<C1, K> setter;

        Injector(TraceContext.Injector<C1> delegate, ExtraFieldPropagation<K> propagation, Setter<C1, K> setter) {
            this.delegate = delegate;
            this.propagation = propagation;
            this.setter = setter;
        }

        @Override
        public void inject(TraceContext context, C1 carrier) {
            delegate.inject(context, carrier);
            propagation.factory.extraFields.forEach(context::addExtraNX);
            context.extra().forEach((k,v)->{
                setter.put(carrier,propagation.keyFactory.create(k), v);
            });
        }
    }

    public static class Factory extends Propagation.Factory{

        final Propagation.Factory delegate;

        private final Map<String,String> extraFields = new HashMap<>();

        public static final String EXTRA_CONFIG_KEY = "trace.extra.fields";

        public Factory(Propagation.Factory delegate) {
            this.delegate = delegate;
            loadExtraKeys();
            ConfigManager.addListener(EXTRA_CONFIG_KEY,this::loadExtraKeys);
        }

        @Override
        public <K1> Propagation<K1> create(KeyFactory<K1> keyFactory) {
            return new ExtraFieldPropagation<>(delegate.create(keyFactory),keyFactory,this);
        }

        private void loadExtraKeys(ChangeEvent event){
            if (event.getType() == ChangeEvent.Type.DELETED){
                extraFields.clear();
                return;
            }
            setExtraFields(event.getNewValue());
        }
        private void setExtraFields(String configValue){
            if (configValue == null || configValue.isEmpty()){
                return;
            }
            JSONObject extraFields = JSON.parseObject(configValue);
            extraFields.forEach((k,v)->this.extraFields.put(k,String.valueOf(v)));
        }
        private void loadExtraKeys(){
            String value = ConfigManager.getString(EXTRA_CONFIG_KEY);
            setExtraFields(value);
        }
    }

}
