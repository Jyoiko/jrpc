package fun.jyoiko.impl.hello;

import fun.jyoiko.HelloService;
import fun.jyoiko.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RpcService(group = "group1",version = "version1")
public class HelloServiceImpl implements HelloService {
    static {
        System.out.println("HelloServiceImpl被创建");
    }
    @Override
    public String sayHello(String name) {
        log.info("Server1收到：{};",name);
        return "HelloImpl1 content is: " + name + ", Hello!";
    }
}

