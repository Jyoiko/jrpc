package fun.jyoiko.registry;

import java.net.InetSocketAddress;

/**
 * 应该是提供给服务端用于注册服务的
 */
public interface ServiceRegistry {

    void registry(String rpcServiceName, InetSocketAddress address);
}
