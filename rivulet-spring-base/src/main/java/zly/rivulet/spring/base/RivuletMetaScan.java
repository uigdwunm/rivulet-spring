package zly.rivulet.spring.base;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RivuletMetaScannerRigistrar.class)
public @interface RivuletMetaScan {

    /**
     * 要扫描的包，包括desc和mapper类
     * 效果同下面的 basePackages
     **/
    String[] value() default {};

    /**
     * 要扫描的包，包括desc和mapper类
     **/
    String[] basePackages() default {};
}
