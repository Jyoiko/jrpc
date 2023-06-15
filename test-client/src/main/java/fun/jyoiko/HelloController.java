package fun.jyoiko;


import fun.jyoiko.annotation.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class HelloController {
    @RpcReference(version = "version1",group = "group1")
    private HelloService helloService;

//    @RpcReference(version = "version2",group = "group2")
//    private HelloService helloService2;

    public void hello1() throws InterruptedException {
        String s = helloService.sayHello("server");
        assert "Hello content is: server, Hello!".equals(s);
        Thread.sleep(4000);
        for (int i = 0; i < 10; i++) {
            System.out.println(helloService.sayHello("looping"));
        }
    }

//    public void hello2() throws InterruptedException {
//        String s = helloService2.sayHello("server2");
//        assert "Hello content is: server2, Hello!".equals(s);
//        Thread.sleep(4000);
//        for (int i = 0; i < 10; i++) {
//            System.out.println(helloService2.sayHello("looping2"));
//        }
//    }
}
