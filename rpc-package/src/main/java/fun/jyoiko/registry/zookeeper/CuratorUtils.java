package fun.jyoiko.registry.zookeeper;

import fun.jyoiko.enums.RpcConfigEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class CuratorUtils {

    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    private static CuratorFramework zkClient;
    public static String ZK_ROOT="/JRpc";
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    private static final Set<String> registeredSet= ConcurrentHashMap.newKeySet();
    private static final Map<String,List<String>> serviceAddresses=new ConcurrentHashMap<>();
    private CuratorUtils(){}

    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName){
        if(serviceAddresses.containsKey(rpcServiceName)){
            return serviceAddresses.get(rpcServiceName);
        }
        List<String> result = null;
        String path=ZK_ROOT+"/"+rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(path);
            serviceAddresses.put(rpcServiceName,result);
            registerWatcher(rpcServiceName,zkClient);
        } catch (Exception e) {
            log.error("get children nodes for path [{}] fail", path);
        }
        return result;
    }

    /**
     * Registers to listen for changes to the specified node
     *
     * @param rpcServiceName rpc service name eg:github.javaguide.HelloServicetest2version
     */
    public static void registerWatcher(String rpcServiceName,CuratorFramework zkClient) throws Exception {
        String path=ZK_ROOT+"/"+rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient,path,true);
        pathChildrenCache.getListenable().addListener((curatorFramework, pathChildrenCacheEvent) -> {
            List<String> list = curatorFramework.getChildren().forPath(path);
            serviceAddresses.put(rpcServiceName,list);
        });
        pathChildrenCache.start();
    }

    public static Properties readPropertiesFile(String fileName){
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath="";
        if (url != null) {
            rpcConfigPath=url.getPath()+fileName;
        }
        Properties properties;
        try(InputStreamReader reader=new InputStreamReader(
                new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8
        )){
            properties = new Properties();
            properties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
    public static CuratorFramework getZkClient(){
        Properties properties = readPropertiesFile(RpcConfigEnum.RPC_CONFIG.getPath());
        String zkAddress=(properties!=null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPath())!=null)?
                properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPath()):DEFAULT_ZOOKEEPER_ADDRESS;
        //调用start方法之前为 LATENT，调用start方法之后为 STARTED ,调用close()方法之后为STOPPED
        if(zkClient!=null && zkClient.getState()== CuratorFrameworkState.STARTED){
            return zkClient;
        }
        RetryPolicy policy=new ExponentialBackoffRetry(BASE_SLEEP_TIME,MAX_RETRIES);
        zkClient= CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .retryPolicy(policy)
                .build();
        zkClient.start();
        try {
            if(!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)){
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }

    public static void createPersistentNode(CuratorFramework zkClient, String path)  {
        try {
            if(registeredSet.contains(path)||zkClient.checkExists().forPath(path)!=null){
                log.info("The node already exists. The node is:[{}]", path);
            }else{
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("The node was created successfully. The node is:[{}]", path);
            }
        } catch (Exception e) {
            log.error("create persistent node for path [{}] fail", path);
        }
    }

    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        registeredSet.stream().parallel().forEach(p->{
            if(p.endsWith(inetSocketAddress.toString())){
                try {
                    zkClient.delete().forPath(p);
                } catch (Exception e) {
                    log.error("clear registry for path [{}] fail", p);
                }
            }
        });
        log.info("All registered services on the server are cleared:[{}]", registeredSet.toString());
    }
}
