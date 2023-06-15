package fun.jyoiko.spring;

import fun.jyoiko.annotation.RpcReference;
import fun.jyoiko.annotation.RpcService;
import fun.jyoiko.dto.ServiceConfig;
import fun.jyoiko.enums.ServiceCenterEnum;
import fun.jyoiko.enums.TransportEnum;
import fun.jyoiko.extension.ExtensionLoader;
import fun.jyoiko.factory.SingletonFactory;
import fun.jyoiko.provider.ServiceProvider;
import fun.jyoiko.provider.impl.ServiceProviderImpl;
import fun.jyoiko.transfer.client.RequestTransport;
import fun.jyoiko.transfer.proxy.RpcClientProxy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Slf4j
@Component
public class BeanPostprocessor implements BeanPostProcessor {
    private final RequestTransport rpcClient;
    private final ServiceProvider serviceProvider;
    public BeanPostprocessor() {
        rpcClient = ExtensionLoader.getExtensionLoader(RequestTransport.class).getExtension(TransportEnum.NETTY.getName());
//        serviceProvider= SingletonFactory.getInstance(ServiceProviderImpl.class);
        serviceProvider = ExtensionLoader.getExtensionLoader(ServiceProvider.class).getExtension(ServiceCenterEnum.SERVICE_PROVIDER_ENUM.getName());
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            ServiceConfig serviceConfig = ServiceConfig.builder().service(bean).group(rpcService.group()).version(rpcService.version()).build();
            serviceProvider.publishService(serviceConfig);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        Field[] declaredFields = beanClass.getDeclaredFields();
        for (Field field:
             declaredFields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                ServiceConfig serviceConfig = ServiceConfig.builder()
                        .version(rpcReference.version())
                        .group(rpcReference.group())
                        .build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(serviceConfig, rpcClient);
                Object proxy = rpcClientProxy.getProxy(field.getType());
                field.setAccessible(true);
                try {
                    field.set(bean,proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return bean;
    }
}
