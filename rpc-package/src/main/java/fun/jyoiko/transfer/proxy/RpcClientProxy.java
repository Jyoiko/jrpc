package fun.jyoiko.transfer.proxy;

import fun.jyoiko.dto.RpcRequest;
import fun.jyoiko.dto.RpcResponse;
import fun.jyoiko.dto.ServiceConfig;
import fun.jyoiko.enums.ResponseEnum;
import fun.jyoiko.transfer.client.NettyClient;
import fun.jyoiko.transfer.client.RequestTransport;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RpcClientProxy implements InvocationHandler {

    private final ServiceConfig rpcServiceConfig;
    private final RequestTransport rpcRequestTransport;
    public RpcClientProxy(ServiceConfig serviceConfig, RequestTransport rpcRequestTransport) {
        rpcServiceConfig = serviceConfig;
        this.rpcRequestTransport = rpcRequestTransport;
    }

    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz){
        return (T)Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz},this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        log.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());
        RpcRequest request = RpcRequest.builder()
                .parameters(args)
                .methodName(method.getName())
                .requestId(UUID.randomUUID().toString())
                .paramTypes(method.getParameterTypes())
                .interfaceName(method.getDeclaringClass().getName())
                .version(rpcServiceConfig.getVersion())
                .group(rpcServiceConfig.getGroup())
                .build();
        RpcResponse response=null;
        if(rpcRequestTransport instanceof NettyClient){
            CompletableFuture<RpcResponse> resultFuture= (CompletableFuture<RpcResponse>) rpcRequestTransport.sendRpcRequest(request);
            response=resultFuture.get();
        }
        check(response,request);
        return response.getData();
    }

    private void check(RpcResponse response,RpcRequest request){
        String INTERFACE_NAME = "interfaceName";
        if (response == null) {
            throw new RuntimeException("调用服务失败,"+INTERFACE_NAME + ":" + request.getInterfaceName());
        }
        if(!request.getRequestId().equals(response.getRequestId())){
            throw new RuntimeException("调用服务不匹配,"+INTERFACE_NAME + ":" + request.getInterfaceName());
        }
        if (response.getStatusCode()==null||!response.getStatusCode().equals(ResponseEnum.SUCCESS.getCode())){
            throw new RuntimeException("调用服务失败,"+INTERFACE_NAME + ":" + request.getInterfaceName());
        }
    }
}
