package com.wenky.log.trace.propagation;


import java.util.List;

/**
 * @author zhongwenjian
 * @date 2021/6/25
 */
public interface Propagation<K> {

    abstract class Factory {

        public abstract <K> Propagation<K> create(KeyFactory<K> keyFactory);

        public TraceContext decorate(TraceContext context) {
            return context;
        }
    }

    interface KeyFactory<K> {
        KeyFactory<String> STRING = new KeyFactory<String>() { // retrolambda no likey
            @Override public String create(String name) {
                return name;
            }

            @Override public String toString() {
                return "StringKeyFactory{}";
            }
        };

        K create(String name);
    }

    /** Replaces a propagated key with the given value */
    interface Setter<C, K> {
        void put(C carrier, K key, String value);
    }

    List<K> keys();

    <C> TraceContext.Injector<C> injector(Setter<C, K> setter);

    interface Getter<C, K> {
        String get(C carrier, K key);
    }

    /**
     * @param getter invoked for each propagation key to get.
     */
    <C> TraceContext.Extractor<C> extractor(Getter<C, K> getter);
}
