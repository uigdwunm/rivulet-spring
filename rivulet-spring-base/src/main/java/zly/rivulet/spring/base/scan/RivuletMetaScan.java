package zly.rivulet.spring.base.scan;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

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
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * 要扫描的包，包括desc和mapper类
     **/
    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * 多数据源时，指定扫的包归属与哪个RivuletTemplateManager
     */
    String rivuletTemplateBeanName() default "";
}
