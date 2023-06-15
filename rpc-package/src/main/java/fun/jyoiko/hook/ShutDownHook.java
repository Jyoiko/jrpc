package fun.jyoiko.hook;

import fun.jyoiko.factory.ThreadPoolFactory;
import fun.jyoiko.registry.zookeeper.CuratorUtils;
import fun.jyoiko.transfer.server.NettyServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Slf4j
public class ShutDownHook {

    private static final ShutDownHook shutDownHook=new ShutDownHook();

    public static ShutDownHook getShutDownHook(){
        return shutDownHook;
    }

    public void clearAll(){
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), new InetSocketAddress(InetAddress.getLocalHost().getHostName(), NettyServer.PORT));
            } catch (UnknownHostException ignored) {
            }
            ThreadPoolFactory.shutdownAll();//useless now
        }));
    }
}
