package fun.jyoiko.transfer.client;

import fun.jyoiko.dto.RpcConstants;
import fun.jyoiko.dto.RpcMessage;
import fun.jyoiko.dto.RpcResponse;
import fun.jyoiko.enums.CompressEnum;
import fun.jyoiko.enums.SerializerEnum;
import fun.jyoiko.factory.SingletonFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ClientChannelHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyClient nettyClient;
    public ClientChannelHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyClient=SingletonFactory.getInstance(NettyClient.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("client receive msg: [{}]", msg);
        if(msg instanceof RpcMessage){
            RpcMessage got = (RpcMessage) msg;
            byte messageType = got.getMessageType();
            if (messageType == RpcConstants.RESPONSE_TYPE) {
                RpcResponse data = (RpcResponse) got.getData();
                unprocessedRequests.complete(data);

            } else if (messageType==RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                log.info("heart [{}]", got.getData());
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent) evt).state();
            if (state==IdleState.WRITER_IDLE){
                log.info("idle check happen; {}",ctx.channel().remoteAddress());
                Channel channel = nettyClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage=RpcMessage.builder()
                        .codec(SerializerEnum.KRYO.getCode())
                        .compress(CompressEnum.GZIP.getCode())
                        .messageType(RpcConstants.HEARTBEAT_REQUEST_TYPE)
                        .data(RpcConstants.ping)
                        .build();
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }

        }else
            super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
