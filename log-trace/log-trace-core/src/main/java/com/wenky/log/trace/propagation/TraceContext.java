package com.wenky.log.trace.propagation;

import com.wenky.log.trace.internal.Platform;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wenky.log.trace.HexCodec.lenientLowerHexToUnsignedLong;
import static com.wenky.log.trace.HexCodec.toLowerHex;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public class TraceContext implements Cloneable{

    private final long traceId;

    volatile String traceIdString;

    private final Map<String, String> extra = new HashMap<>();

    public TraceContext(long traceId, Map<String, String> extra){
        this.traceId = traceId;
        if (extra != null){
            this.extra.putAll(extra);
        }
    }

    public TraceContext(long traceId){
        this(traceId, null);
    }

    long traceId(){
        return traceId;
    }

    TraceContext withExtra(Map<String, String> extra) {
        return new TraceContext(traceId, extra);
    }

    public String traceIdString() {
        String r = traceIdString;
        if (r == null) {
            r = toLowerHex(traceId);
            traceIdString = r;
        }
        return r;
    }
    public String addExtra(String key, String value){
        return extra.put(key, value);
    }
    public String remove(String key){
        return extra.remove(key);
    }
    public String addExtraNX(String key, String value){
        return extra.putIfAbsent(key, value);
    }
    public boolean remove(String key, String expected){
        return extra.remove(key, expected);
    }
    public Map<String, String> extra(){
        return Collections.unmodifiableMap(extra);
    }

    public String getExtra(String key) {
        return extra.get(key);
    }
    public Builder toBuilder(){
        return new Builder(this);
    }

    @Override
    public TraceContext clone() {
        return new TraceContext(traceId, extra);
    }
    public static class Builder {

        long traceId;

        Map<String, String> extra;

        TraceContext build(){
            if (traceId == 0L){
                throw new IllegalStateException("missing traceId");
            }
            return new TraceContext(traceId, extra);
        }

        Builder (TraceContext context){
            this.traceId = context.traceId;
            this.extra = context.extra;
        }

        Builder traceId(long traceId){
            this.traceId = traceId;
            return this;
        }

        Builder extra(Map<String, String> extra){
            this.extra = extra;
            return this;
        }

        static long parseTraceId(String traceIdString) {
            if (traceIdString == null) {
                throw new NullPointerException("traceId is null");
            }
            int length = traceIdString.length();
            if (length<1 || length > 32) {
                throw new IllegalArgumentException("traceId is wrong:"+traceIdString);
            }

            // left-most characters, if any, are the high bits
            int traceIdIndex = Math.max(0, length - 16);

            // right-most up to 16 characters are the low bits
            long traceId = lenientLowerHexToUnsignedLong(traceIdString, traceIdIndex, length);
            if (traceId == 0) {
                throw new IllegalArgumentException("traceId is wrong:"+traceIdString);
            }
            return traceId;
        }
        Builder(){

        }
    }

    public interface Injector<C>{
        void inject(TraceContext traceContext, C carrier);
    }

    public interface Extractor<C> {
        TraceContext extract(C carrier);
    }

    public static long newTraceId(){
        long traceId = Platform.get().randomLong();
        while (traceId == 0L) {
            traceId = Platform.get().randomLong();
        }
        return traceId;
    }
    static <T> T findExtra(Class<T> type, List<Object> extra) {
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        for (int i = 0, length = extra.size(); i < length; i++) {
            Object nextExtra = extra.get(i);
            if (nextExtra.getClass() == type) {
                return (T) nextExtra;
            }
        }
        return null;
    }
}
