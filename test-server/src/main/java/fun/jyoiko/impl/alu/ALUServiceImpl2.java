package fun.jyoiko.impl.alu;

import fun.jyoiko.ALUService;

public class ALUServiceImpl2 implements ALUService {
    @Override
    public String add(int a, int b) {
        int r=a+b;
        if (((a ^ r) & (b ^ r)) < 0) {
            return "error params";
        }
        return Integer.toString(r)+" impl2";
    }

    @Override
    public String sub(int a, int b) {
        int r=a-b;
        if (((a ^ r) & (b ^ r)) < 0) {
            return "error params";
        }
        return Integer.toString(r)+" impl2";
    }

    @Override
    public String mul(int a, int b) {
        long r=(long)a*(long)b;
        if ((int)r != r) {
            return "error params";
        }
        return Long.toString(r)+" impl2";
    }

    @Override
    public String div(int a, int b) {
        if(b==0){
            return "error param b";
        }
        int r=a/b;
        return Integer.toString(r)+" impl2";
    }
}
