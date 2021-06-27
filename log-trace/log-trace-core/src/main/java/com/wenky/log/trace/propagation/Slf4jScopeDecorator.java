package com.wenky.log.trace.propagation;

import org.slf4j.MDC;

import java.util.*;

/**
 * @author zhongwenjian
 * @date 2021/6/26
 */
public class Slf4jScopeDecorator implements CurrentTraceContext.ScopeDecorator {
    @Override
    public CurrentTraceContext.Scope decorateScope(TraceContext context, CurrentTraceContext.Scope scope) {

        Map<String, String> extraFields = getExtraKeys(context);
        Set<String> extraKeys = extraFields.keySet();
        final List<AbstractMap.SimpleEntry<String, String>> previousMdc = previousMdc(extraKeys);
        final String previousTraceId = MDC.get("traceId");

        if (context != null ){
            extraFields.forEach(MDC::put);
            String traceId = context.traceIdString();
            MDC.put("traceId", traceId);
        }else {
            extraKeys.forEach(MDC::remove);
            MDC.remove("traceId");
        }

        return ()->{
            scope.close();
            replace("traceId", previousTraceId);
            for (AbstractMap.SimpleEntry<String,String> entry : previousMdc){
                replace(entry.getKey(), entry.getValue());
            }
        };
    }

    void replace(String key, String value) {
        if (value != null) {
            MDC.put(key, value);
        }
        else {
            MDC.remove(key);
        }
    }

    private List<AbstractMap.SimpleEntry<String, String>> previousMdc(Set<String> keys) {
        List<AbstractMap.SimpleEntry<String, String>> previousMdc = new ArrayList<>();
        for (String key : keys) {
            previousMdc.add(new AbstractMap.SimpleEntry<>(key, MDC.get(key)));
        }
        return previousMdc;
    }

    private Map<String, String> getExtraKeys(TraceContext context){
        return  ExtraFieldPropagation.getExtraFields(context);
    }
}
