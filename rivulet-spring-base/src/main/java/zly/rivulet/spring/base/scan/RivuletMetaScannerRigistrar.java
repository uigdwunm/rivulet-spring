package zly.rivulet.spring.base.scan;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class RivuletMetaScannerRigistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes mapperScanAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(RivuletMetaScan.class.getName()));
        if (mapperScanAttrs == null || mapperScanAttrs.isEmpty()) {
            return;
        }
        String[] basePackages = mapperScanAttrs.getStringArray("basePackages");
        String rivuletTemplateBeanName = mapperScanAttrs.getString("rivuletTemplateBeanName");
//        String basePackagesStr = Arrays.stream(basePackages)
//            .filter(StringUtils::hasText)
//            .collect(Collectors.joining(","));

        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RivuletMapperScannerConfigurer.class);
        beanDefinitionBuilder.addPropertyValue("basePackages", basePackages);
        beanDefinitionBuilder.addPropertyValue("rivuletTemplateBeanName", rivuletTemplateBeanName);
        registry.registerBeanDefinition(this.generateBaseBeanName(importingClassMetadata), beanDefinitionBuilder.getBeanDefinition());
    }

    private String generateBaseBeanName(AnnotationMetadata importingClassMetadata) {
        return importingClassMetadata.getClassName() + "#" + RivuletMetaScannerRigistrar.class.getSimpleName();
    }
}
