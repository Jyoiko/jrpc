package fun.jyoiko.provider;

import fun.jyoiko.dto.ServiceConfig;

public interface ServiceProvider {
    void addService(ServiceConfig serviceConfig);
    Object getService(String rpcServiceName);

    /**
     * publish的操作是在服务器启动时进行的，因此这个方法是在ServerMain中才会调用
     * @param serviceConfig
     */
    void publishService(ServiceConfig serviceConfig);
}
