package fun.jyoiko.registry.zookeeper;

import fun.jyoiko.dto.RpcRequest;
import fun.jyoiko.enums.LoadBalanceEnum;
import fun.jyoiko.extension.ExtensionLoader;
import fun.jyoiko.loadBalance.LoadBalance;
import fun.jyoiko.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class DiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;
    public DiscoveryImpl() {
        loadBalance= ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.HASH_LoadBalance.getName());
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> childrenNodes = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if(CollectionUtils.isEmpty(childrenNodes)){
            throw new RuntimeException("没有找到指定的服务: "+rpcServiceName);
        }
        //load balancing
        String targetUrl = loadBalance.selectServiceAddress(childrenNodes, rpcRequest);
        log.info("Successfully found the service address:[{}]", targetUrl);
        String[] socketAddress = targetUrl.split(":");
        String host=socketAddress[0];
        int port=Integer.parseInt(socketAddress[1]);
        return new InetSocketAddress(host,port);
    }
}
