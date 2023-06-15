package fun.jyoiko;

import fun.jyoiko.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = {"fun.jyoiko"})
public class Client {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(Client.class);
        HelloController helloController = (HelloController)applicationContext.getBean("helloController");
//        AluController aLUController = (AluController) applicationContext.getBean("aluController");

        helloController.hello1();
//        helloController.hello2();

//        int a=10,b=3;
//        aLUController.compute(a,b);
//        aLUController.compute2(a,b);
    }
}