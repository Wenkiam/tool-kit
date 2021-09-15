package com.wenky.config.nacos;

import com.alibaba.nacos.api.config.ConfigChangeEvent;
import com.alibaba.nacos.api.config.ConfigChangeItem;

import com.alibaba.nacos.api.config.PropertyChangeType;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.client.config.filter.impl.ConfigFilterChainManager;
import com.alibaba.nacos.client.config.filter.impl.ConfigResponse;
import com.alibaba.nacos.client.config.http.HttpAgent;
import com.alibaba.nacos.client.config.http.MetricsHttpAgent;
import com.alibaba.nacos.client.config.http.ServerHttpAgent;
import com.alibaba.nacos.client.config.impl.ClientWorker;

import com.alibaba.nacos.client.utils.ParamUtil;
import com.wenky.config.ChangeListener;
import com.wenky.config.model.ChangeEvent;
import com.wenky.config.nacos.exception.UnKnownExtensionException;
import com.wenky.config.nacos.parser.ConfigParser;
import com.wenky.config.nacos.parser.JsonConfigParser;
import com.wenky.config.nacos.parser.PropertiesConfigParser;
import com.wenky.config.nacos.parser.YmlConfigParser;
import com.wenky.config.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author zhongwenjian
 * @date 2021/9/13
 */
public class NacosConfigService implements ConfigService, Listener {

    private static final Logger log = LoggerFactory.getLogger(NacosConfigService.class);

    private final String dataId;

    private final String groupId;

    private final ConfigParser configParser;

    private final Map<String, Object> localCache;

    private final Map<String, Set<ChangeListener>> listenerMap = new HashMap<>();

    private static final Executor EXECUTOR = Executors.newFixedThreadPool(1);

    public NacosConfigService(String dataId, String groupId, Properties properties) throws Exception {
        this.dataId = dataId;
        this.groupId = groupId;
        HttpAgent agent = new MetricsHttpAgent(new ServerHttpAgent(properties));
        agent.start();
        ClientWorker worker = new ClientWorker(agent, new ConfigFilterChainManager(properties), properties);
        String namespace = ParamUtil.parseNamespace(properties);
        ConfigResponse response = worker.getServerConfig(dataId, groupId, namespace, 5000);
        worker.addTenantListeners(dataId, groupId, Collections.singletonList(this));
        String extension = response.getConfigType();
        ConfigParser configParser = new YmlConfigParser();
        if (configParser.isResponsibleFor(extension)) {
            this.configParser = configParser;
        } else if ((configParser = new PropertiesConfigParser()).isResponsibleFor(extension)) {
            this.configParser = configParser;
        } else if ((configParser = new JsonConfigParser()).isResponsibleFor(extension)) {
            this.configParser = configParser;
        } else {
            throw new UnKnownExtensionException(extension);
        }
        localCache = configParser.parse(response.getContent());
    }

    @Override
    public synchronized void addChangeListener(String configKey, ChangeListener listener) {
        listenerMap.computeIfAbsent(configKey, k->new HashSet<>()).add(listener);
    }

    @Override
    public String getValue(String configKey, String defaultValue) {
        Object result = localCache.get(configKey);
        return result == null ? defaultValue : String.valueOf(result);
    }

    @Override
    public Executor getExecutor() {
        return EXECUTOR;
    }

    @Override
    public void receiveConfigInfo(String configInfo) {
        try {
            Map<String, Object> newCache = configParser.parse(configInfo);

            Set<String> removed = new HashSet<>(localCache.keySet());

            Map<String, ConfigChangeItem> changeItemMap = new LinkedHashMap<>();

            newCache.forEach((key, value) -> {
                Object oldValue = localCache.get(key);
                if (!localCache.containsKey(key)) {
                    ConfigChangeItem configChangeItem = new ConfigChangeItem(key, String.valueOf(oldValue), String.valueOf(value));
                    localCache.put(key, value);
                    configChangeItem.setType(PropertyChangeType.ADDED);
                    changeItemMap.put(key, configChangeItem);
                } else if (!Objects.equals(oldValue, value)) {
                    ConfigChangeItem configChangeItem = new ConfigChangeItem(key, String.valueOf(oldValue), String.valueOf(value));
                    localCache.replace(key, oldValue, value);
                    configChangeItem.setType(PropertyChangeType.MODIFIED);
                    changeItemMap.put(key, configChangeItem);
                }
                removed.remove(key);
            });
            removed.forEach(key -> {
                ConfigChangeItem configChangeItem = new ConfigChangeItem(key, String.valueOf(localCache.remove(key)), null);
                configChangeItem.setType(PropertyChangeType.DELETED);
                changeItemMap.put(key, configChangeItem);
            });
            applyChangeEvent(new ConfigChangeEvent(changeItemMap));
        } catch (Exception e){
            log.error("[notify-error] dataId={}, group={}, tx={}", dataId,
                    groupId,   e.getMessage(), e);
        }
    }

    private ChangeEvent convert(ConfigChangeItem item) {
        return new ChangeEvent(item.getKey(), item.getOldValue(),
                item.getNewValue(), ChangeEvent.Type.valueOf(item.getType().name()));
    }

    private void applyChangeEvent(ConfigChangeEvent event){
        event.getChangeItems().forEach(item->{
            String key = item.getKey();
            Set<ChangeListener> listeners = listenerMap.get(key);
            if (listeners != null){
                listeners.forEach(eventHandler -> eventHandler.onChange(convert(item)));
            }
        });
    }
}
