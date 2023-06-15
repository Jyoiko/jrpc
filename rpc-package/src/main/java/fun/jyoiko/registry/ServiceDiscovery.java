package fun.jyoiko.registry;

import fun.jyoiko.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * 应该是提供给客户端用于发现服务的
 */
public interface ServiceDiscovery {
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
