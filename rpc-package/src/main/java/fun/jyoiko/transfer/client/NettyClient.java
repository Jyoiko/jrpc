package fun.jyoiko.transfer.client;

import fun.jyoiko.dto.RpcConstants;
import fun.jyoiko.dto.RpcMessage;
import fun.jyoiko.dto.RpcRequest;
import fun.jyoiko.dto.RpcResponse;
import fun.jyoiko.enums.CompressEnum;
import fun.jyoiko.enums.SerializerEnum;
import fun.jyoiko.enums.ServiceCenterEnum;
import fun.jyoiko.extension.ExtensionLoader;
import fun.jyoiko.factory.SingletonFactory;
import fun.jyoiko.registry.ServiceDiscovery;
import fun.jyoiko.transfer.autoCode.RpcDecoder;
import fun.jyoiko.transfer.autoCode.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在这里与JavaGuide不同的地方是，把ChannelProvider部分集成进来了，
 * 在一开始的书写中，因为希望把getChannel放在ChannelProvider中，而getChannel
 * 的Connect方法又需要NettyClient对象，这就导致了循环依赖，使得程序阻塞
 */
@Slf4j
public class NettyClient implements RequestTransport{

    private final Bootstrap bootstrap;
    private final ServiceDiscovery discovery;
    private final UnprocessedRequests unprocessedRequests;
    private final Map<String, Channel> channelMap;
    public NettyClient()  {
        channelMap=new ConcurrentHashMap<>();
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline p=socketChannel.pipeline();
                        p.addLast(new IdleStateHandler(0,5,0));
                        p.addLast(new RpcEncoder());
                        p.addLast(new RpcDecoder());
                        p.addLast(new ClientChannelHandler());
                    }
                });
        discovery= ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceCenterEnum.SERVICE_DISCOVERY_ENUM.getName());
        unprocessedRequests=SingletonFactory.getInstance(UnprocessedRequests.class);
    }

    @SneakyThrows
    public Channel Connect(InetSocketAddress inetSocketAddress){
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener)future -> {
            if(future.isSuccess()){
                log.info("The client has connected [{}] successful!", inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            } else {
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();

    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        // build return value
        CompletableFuture<RpcResponse> resultFuture = new CompletableFuture<>();
        // get server address
        InetSocketAddress inetSocketAddress = discovery.lookupService(rpcRequest);
        // get  server address related channel
        Channel channel = getChannel(inetSocketAddress);
        if (channel.isActive()) {
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            RpcMessage rpcMessage=RpcMessage.builder()
                    .data(rpcRequest)
                    .codec(SerializerEnum.KRYO.getCode())
                    .compress(CompressEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE)
                    .build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcMessage);
                }else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("Send failed:", future.cause());
                }
            });
        }else {
            throw new IllegalStateException();
        }

        return resultFuture;
    }

    public Channel get(InetSocketAddress address){
        String key = address.toString();
        if(channelMap.containsKey(key)){
            Channel channel=channelMap.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            }else {
                channelMap.remove(key);
            }
        }
        return null;
    }

    public void set(InetSocketAddress address, Channel channel){
        channelMap.put(address.toString(),channel);
    }

    public Channel getChannel(InetSocketAddress address){
        Channel channel = get(address);
        if (channel == null) {
            channel=Connect(address);
            set(address,channel);
        }
        return channel;
    }
}
