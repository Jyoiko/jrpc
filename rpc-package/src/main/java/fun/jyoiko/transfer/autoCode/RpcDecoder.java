package fun.jyoiko.transfer.autoCode;

import fun.jyoiko.compress.Compress;
import fun.jyoiko.dto.RpcConstants;
import fun.jyoiko.dto.RpcMessage;
import fun.jyoiko.dto.RpcRequest;
import fun.jyoiko.dto.RpcResponse;
import fun.jyoiko.enums.CompressEnum;
import fun.jyoiko.enums.SerializerEnum;
import fun.jyoiko.extension.ExtensionLoader;
import fun.jyoiko.serialize.Serialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class RpcDecoder extends LengthFieldBasedFrameDecoder {
    public RpcDecoder(){
        this(RpcConstants.MAX_LENGTH,5,4,-9,0);
    }
/**
 *
 * @param maxFrameLength  帧的最大长度
 * @param lengthFieldOffset length字段偏移的地址 magicCode+version
 * @param lengthFieldLength length字段所占的字节长 full_length: 4B
 * @param lengthAdjustment 修改帧数据长度字段中定义的值，可以为负数 因为有时候我们习惯把头部记入长度,若为负数,则说明要推后多少个字段
 *                         （默认是从full_length字段之后开始算的，想算全长就前移）
 * @param initialBytesToStrip 解析时候跳过多少个长度 手动解析头部，因此不跳过

*/
    public RpcDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        //在这里调用父类的方法,实现指得到想要的部分,我在这里全部都要,也可以只要body部分
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf) {
            ByteBuf byteBuf = (ByteBuf) decode;
            if(byteBuf.readableBytes()>=RpcConstants.HEAD_LENGTH){
                try {
                    return decodeFrame(byteBuf);
                }catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                }finally {
                    byteBuf.release();
                }
            }else {
                log.error("Decode Frame Error");
            }
        }else {
            log.info("Not ByteBuf");
        }
        return decode;
    }

    private Object decodeFrame(ByteBuf in){
        //check magicCode & version
        int magicCode = in.readInt();
        if (magicCode != RpcConstants.CAFE_BABE) {
            throw new IllegalArgumentException("Unknown magic code: " + Integer.toHexString(magicCode));
        }
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }

        int full_length = in.readInt();
        byte messageType = in.readByte();
        byte codeType= in.readByte();
        byte compressType=in.readByte();
        int messageId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .compress(compressType)
                .messageType(messageType)
                .messageId(messageId)
                .codec(codeType).build();

        if(messageType==RpcConstants.HEARTBEAT_REQUEST_TYPE){
            rpcMessage.setData(RpcConstants.ping);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.pong);
            return rpcMessage;
        }
        int body_length=full_length-RpcConstants.HEAD_LENGTH;
        if (body_length > 0) {
            byte[] bytes = new byte[body_length];
            in.readBytes(bytes);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(CompressEnum.getNameFromCode(compressType));
            Serialize serialize = ExtensionLoader.getExtensionLoader(Serialize.class).getExtension(SerializerEnum.getNameFromCode(codeType));

            byte[] decompressed = compress.decompress(bytes);
            if (messageType==RpcConstants.RESPONSE_TYPE){
                RpcResponse rpcResponse = serialize.deserialize(decompressed, RpcResponse.class);
                rpcMessage.setData(rpcResponse);
            }else {
                RpcRequest rpcRequest = serialize.deserialize(decompressed, RpcRequest.class);
                rpcMessage.setData(rpcRequest);
            }

        }

        return rpcMessage;
    }
}
