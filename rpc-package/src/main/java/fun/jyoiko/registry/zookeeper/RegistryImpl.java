package fun.jyoiko.registry.zookeeper;

import fun.jyoiko.registry.ServiceRegistry;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

public class RegistryImpl implements ServiceRegistry {
    @Override
    public void registry(String rpcServiceName, InetSocketAddress address) {
        String path=CuratorUtils.ZK_ROOT+"/"+rpcServiceName+address.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient,path);
    }
}
