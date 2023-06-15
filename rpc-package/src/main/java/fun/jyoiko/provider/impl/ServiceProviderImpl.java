package fun.jyoiko.provider.impl;

import fun.jyoiko.dto.ServiceConfig;
import fun.jyoiko.enums.ServiceCenterEnum;
import fun.jyoiko.extension.ExtensionLoader;
import fun.jyoiko.provider.ServiceProvider;
import fun.jyoiko.registry.ServiceRegistry;
import fun.jyoiko.transfer.server.NettyServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ServiceProviderImpl implements ServiceProvider {

    private final ServiceRegistry registry;
    private final Map<String,Object> registeredServiceMap;
    public ServiceProviderImpl() {
        registry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceCenterEnum.SERVICE_REGISTRY_ENUM.getName());

        registeredServiceMap = new ConcurrentHashMap<>();
    }

    @Override
    public void addService(ServiceConfig serviceConfig) {
        String serviceName = serviceConfig.getServiceName();
        if (registeredServiceMap.containsKey(serviceName)) {
            return;
        }
        registeredServiceMap.put(serviceName,serviceConfig.getService());
        log.info("Add service: {} and interfaces:{}", serviceName, serviceConfig.getService().getClass().getInterfaces());

    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = registeredServiceMap.get(rpcServiceName);
        if (service == null) {
            throw new RuntimeException("没有找到指定的服务");
        }
        return service;
    }

    @Override
    public void publishService(ServiceConfig serviceConfig) {
        try {
            String hostName = InetAddress.getLocalHost().getHostAddress();//这边hostAddress和hostname会导
            // 致zookeeper上发布的服务名字不同，需要注意getHostName会使得在服务名字上增加主机名字段

            addService(serviceConfig);
            registry.registry(serviceConfig.getServiceName(),new InetSocketAddress(hostName, NettyServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}
