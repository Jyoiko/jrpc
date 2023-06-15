package fun.jyoiko.extension;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Holder<T> {
    private volatile T val;
//    public T getVal() {
//        return val;
//    }
//
//    public void setVal(T value) {
//        this.val = value;
//    }

}
