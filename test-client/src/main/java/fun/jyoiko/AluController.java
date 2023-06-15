package fun.jyoiko;

import fun.jyoiko.ALUService;
import fun.jyoiko.annotation.RpcReference;
import org.springframework.stereotype.Component;

@Component
public class AluController {
//    @RpcReference(version = "version1",group = "group1")
//    ALUService aluService;

    @RpcReference(version = "version2",group = "group2")
    ALUService aluService2;
//    public void compute(int a,int b){
//        String add = (String)aluService.add(a, b);
//        String div = (String)aluService.div(a, b);
//        String mul = (String) aluService.mul(a, b);
//        String sub = (String) aluService.sub(a, b);
//        System.out.printf("(1) For input %d, %d, compute results:\n ADD: %s\n DIV: %s\nMUL: %s\nSUB: %s\n%n",a,b,add,div,mul,sub );
//
//    }
    public void compute2(int a,int b){
        String add = (String)aluService2.add(a, b);
        String div = (String)aluService2.div(a, b);
        String mul = (String) aluService2.mul(a, b);
        String sub = (String) aluService2.sub(a, b);
        System.out.printf("(2) For input %d, %d, compute results:\n ADD: %s\n DIV: %s\nMUL: %s\nSUB: %s\n%n",a,b,add,div,mul,sub );

    }
}
