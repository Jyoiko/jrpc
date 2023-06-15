package fun.jyoiko.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fun.jyoiko.dto.RpcRequest;
import fun.jyoiko.dto.RpcResponse;
import fun.jyoiko.serialize.Serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoSerializer implements Serialize {
    /**
     * Because Kryo is not thread safe. So, use ThreadLocal to store Kryo objects
     */
    ThreadLocal<Kryo> kryoThreadLocal=ThreadLocal.withInitial(()->{
        Kryo kryo = new Kryo();
        kryo.register(RpcRequest.class);
        kryo.register(RpcResponse.class);
        return kryo;
    });
    @Override
    public byte[] serialize(Object obj) {
        try(Output out=new Output(new ByteArrayOutputStream())){
            Kryo kryo=kryoThreadLocal.get();
            kryo.writeObject(out,obj);

            return out.toBytes();
        }catch (Exception e) {
            throw new RuntimeException("Serialization failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(Input input=new Input(new ByteArrayInputStream(bytes))){
            Kryo kryo = kryoThreadLocal.get();
            Object o = kryo.readObject(input,clazz);
            kryoThreadLocal.remove();
            return clazz.cast(o);
        }catch (Exception e) {
            throw new RuntimeException("Serialization failed");
        }
    }
}
