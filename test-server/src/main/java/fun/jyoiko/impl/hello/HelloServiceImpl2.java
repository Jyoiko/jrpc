package fun.jyoiko.impl.hello;

import fun.jyoiko.HelloService;
import fun.jyoiko.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RpcService(group = "group1",version = "version1")
public class HelloServiceImpl2 implements HelloService {
    static {
        System.out.println("HelloServiceImpl2被创建");
    }
    @Override
    public String sayHello(String name) {
        log.info("Server2收到：{};",name);
        return "HelloImpl2 content is: " + name + ", Hello!";
    }
}
