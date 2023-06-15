package fun.jyoiko.transfer.server;

import fun.jyoiko.dto.ServiceConfig;
import fun.jyoiko.enums.ServiceCenterEnum;
import fun.jyoiko.extension.ExtensionLoader;
import fun.jyoiko.factory.SingletonFactory;
import fun.jyoiko.hook.ShutDownHook;
import fun.jyoiko.provider.ServiceProvider;
import fun.jyoiko.provider.impl.ServiceProviderImpl;
import fun.jyoiko.transfer.autoCode.RpcDecoder;
import fun.jyoiko.transfer.autoCode.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NettyServer {
    public static final int PORT=9998;
    private final ServiceProvider provider;

    public NettyServer() {
        provider = ExtensionLoader.getExtensionLoader(ServiceProvider.class).getExtension(ServiceCenterEnum.SERVICE_PROVIDER_ENUM.getName());
//        provider= SingletonFactory.getInstance(ServiceProviderImpl.class);
    }
    public void registerService(ServiceConfig config){
        provider.publishService(config);
    }
    @SneakyThrows
    public void start(){
        ShutDownHook.getShutDownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup=new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        //非IO线程池
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors()*2);
        try {
            serverBootstrap.group(bossGroup,workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    .option(ChannelOption.SO_BACKLOG,128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline p= channel.pipeline();
                            p.addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                            p.addLast(new RpcEncoder());
                            p.addLast(new RpcDecoder());
                            p.addLast(serviceHandlerGroup, new ServerChannelHandler());
                        }
                    });
            // 绑定端口，同步等待绑定成功
            ChannelFuture f = serverBootstrap.bind(host, PORT).sync();
            // 同步等待服务端channel关闭
            f.channel().closeFuture().sync();

        } catch (InterruptedException e){
            log.error("occur exception when start server:", e);
        }finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }

    }
}
