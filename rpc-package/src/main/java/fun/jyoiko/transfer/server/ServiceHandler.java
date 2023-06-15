package fun.jyoiko.transfer.server;

import fun.jyoiko.dto.RpcRequest;
import fun.jyoiko.enums.ServiceCenterEnum;
import fun.jyoiko.extension.ExtensionLoader;
import fun.jyoiko.factory.SingletonFactory;
import fun.jyoiko.provider.ServiceProvider;
import fun.jyoiko.provider.impl.ServiceProviderImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 处理请求，请求传入方法相关的信息，然后服务端提供具体实现
 */
@Slf4j
public class ServiceHandler {
    private final ServiceProvider serviceProvider;

    public ServiceHandler() {
        serviceProvider= ExtensionLoader.getExtensionLoader(ServiceProvider.class)
                .getExtension(ServiceCenterEnum.SERVICE_PROVIDER_ENUM.getName());
//        serviceProvider= SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    public Object handle(RpcRequest request){
        Object service = serviceProvider.getService(request.getRpcServiceName());//这边直接就getService了？
        return invokeTargetMethod(request,service);
    }

    private Object invokeTargetMethod(RpcRequest request,Object service){
        Object result;
        try {
            Method method = service.getClass().getMethod(request.getMethodName(), request.getParamTypes());//获取想要执行的方法及参数类型
            //invoke是怎样执行的： 在service对象上执行该方法，
            // 参数2：方法传入的实参 其实就是：service.method(params)
            result=method.invoke(service,request.getParameters());
            log.info("service:[{}] successful invoke method:[{}]", request.getInterfaceName(), request.getMethodName());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
