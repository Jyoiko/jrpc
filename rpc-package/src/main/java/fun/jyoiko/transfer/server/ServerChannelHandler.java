package fun.jyoiko.transfer.server;

import fun.jyoiko.dto.RpcConstants;
import fun.jyoiko.dto.RpcMessage;
import fun.jyoiko.dto.RpcRequest;
import fun.jyoiko.dto.RpcResponse;
import fun.jyoiko.enums.CompressEnum;
import fun.jyoiko.enums.SerializerEnum;
import fun.jyoiko.factory.SingletonFactory;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private final ServiceHandler serviceHandler;

    public ServerChannelHandler() {
        serviceHandler = SingletonFactory.getInstance(ServiceHandler.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)  {
        try {
            if (msg instanceof RpcMessage) {
                log.info("server receive msg: [{}] ", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializerEnum.KRYO.getCode());
                rpcMessage.setCompress(CompressEnum.GZIP.getCode());
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.pong);
                }else {
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
// Execute the target method (the method the client needs to execute) and return the method result
                    Object result = serviceHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    RpcResponse response=null;
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        response=RpcResponse.success(result, rpcRequest.getRequestId());

                    }else {
                        response =RpcResponse.fail();
                        log.error("not writable now, message dropped");
                    }
                    rpcMessage.setData(response);
                }
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }finally {
            //Ensure that ByteBuf is released, otherwise there may be memory leaks
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            IdleState state=((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        }else
            super.userEventTriggered(ctx, evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
