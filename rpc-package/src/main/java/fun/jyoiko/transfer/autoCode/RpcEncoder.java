package fun.jyoiko.transfer.autoCode;

import fun.jyoiko.compress.Compress;
import fun.jyoiko.dto.RpcConstants;
import fun.jyoiko.dto.RpcMessage;
import fun.jyoiko.enums.CompressEnum;
import fun.jyoiko.enums.SerializerEnum;
import fun.jyoiko.extension.ExtensionLoader;
import fun.jyoiko.serialize.Serialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.concurrent.atomic.AtomicInteger;

public class RpcEncoder extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf byteBuf) throws Exception  {
        byteBuf.writeInt(RpcConstants.CAFE_BABE);
        byteBuf.writeByte(RpcConstants.VERSION);
        //跳过full_length字段
        byteBuf.writerIndex(byteBuf.writerIndex()+4);
        byte messageType = rpcMessage.getMessageType();
        byteBuf.writeByte(messageType);
        byteBuf.writeByte(rpcMessage.getCodec());
        byteBuf.writeByte(rpcMessage.getCompress());
        byteBuf.writeInt(ATOMIC_INTEGER.getAndIncrement());
        //header部分完成
        //body部分构建
        byte[] body=null;
        int full_length=RpcConstants.HEAD_LENGTH;
        if (messageType == RpcConstants.RESPONSE_TYPE || messageType == RpcConstants.REQUEST_TYPE) {
            Serialize serialize = ExtensionLoader.getExtensionLoader(Serialize.class)
                    .getExtension(SerializerEnum.getNameFromCode(rpcMessage.getCodec()));
            body= serialize.serialize(rpcMessage.getData());

            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
                    .getExtension(CompressEnum.getNameFromCode(rpcMessage.getCompress()));
            body = compress.compress(body);
            full_length+=body.length;
        }
        if (body != null) {
            byteBuf.writeBytes(body);
        }
        int index = byteBuf.writerIndex();
        byteBuf.writerIndex(index-full_length+4+1);//也就是version之后的位置
        byteBuf.writeInt(full_length);
        byteBuf.writerIndex(index);
    }
}
