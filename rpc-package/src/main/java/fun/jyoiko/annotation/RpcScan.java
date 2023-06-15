package fun.jyoiko.annotation;

import fun.jyoiko.spring.CustomScannerRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CustomScannerRegistrar.class)
public @interface RpcScan {
    String[] basePackage();
}
