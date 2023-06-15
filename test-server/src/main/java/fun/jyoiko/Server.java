package fun.jyoiko;

import fun.jyoiko.annotation.RpcScan;
import fun.jyoiko.dto.ServiceConfig;
import fun.jyoiko.impl.hello.HelloServiceImpl;
import fun.jyoiko.impl.hello.HelloServiceImpl2;
import fun.jyoiko.transfer.server.NettyServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = {"fun.jyoiko"})
public class Server {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(Server.class);
        NettyServer nettyServer = (NettyServer) applicationContext.getBean("nettyServer");

        HelloService helloService2=new HelloServiceImpl2();
        ServiceConfig serviceConfig = ServiceConfig.builder()
                .version("version2").group("group2").service(helloService2).build();
        nettyServer.registerService(serviceConfig);
        nettyServer.start();

    }
}