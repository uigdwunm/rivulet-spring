package zly.rivulet.spring.base.scan;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;
import zly.rivulet.base.definer.annotations.RivuletDesc;
import zly.rivulet.spring.base.scan.listener.RivuletTemplateCreatedEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RivuletMapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor {

    private String[] basePackages;

    private String rivuletTemplateBeanName;

    public void setBasePackages(String[] basePackages) {
        this.basePackages = basePackages;
    }

    public void setRivuletTemplateBeanName(String rivuletTemplateBeanName) {
        this.rivuletTemplateBeanName = rivuletTemplateBeanName;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        // 先扫描desc
        ClassPathScanningCandidateComponentProvider rivuletDescScanner = new ClassPathScanningCandidateComponentProvider(false);
        // 过滤出desc
        rivuletDescScanner.addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
                AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
                return annotationMetadata.hasAnnotatedMethods(RivuletDesc.class.getName());
            }
        });
        List<Class<?>> allDescClass = new ArrayList<>();
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = rivuletDescScanner.findCandidateComponents(basePackage);
            List<Class<?>> descClass = candidateComponents.stream()
                .map(BeanDefinition::getBeanClassName)
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).collect(Collectors.toList());
            allDescClass.addAll(descClass);
        }
        // 整一个监听器，指定的template生成时，把这个属性注入进去
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RivuletTemplateCreatedEventListener.class);
        beanDefinitionBuilder.addPropertyValue("allDescClass", allDescClass);
        beanDefinitionBuilder.addPropertyValue("rivuletTemplateBeanName", rivuletTemplateBeanName);
        beanDefinitionRegistry.registerBeanDefinition(this.generateListenerBeanName(rivuletTemplateBeanName), beanDefinitionBuilder.getBeanDefinition());

        // 扫描mapper
        RivuletMapperScanner rivuletMapperScanner = new RivuletMapperScanner(beanDefinitionRegistry);
        rivuletMapperScanner.addIncludeFilter(new AnnotationTypeFilter(RivuletMapper.class));
        rivuletMapperScanner.setRivuletTemplateBeanName(rivuletTemplateBeanName);

        rivuletMapperScanner.scan(basePackages);
    }

    private String generateListenerBeanName(String rivuletTemplateBeanName) {
        if (StringUtils.hasText(rivuletTemplateBeanName)) {
            return rivuletTemplateBeanName + "" + RivuletTemplateCreatedEventListener.class.getSimpleName();
        }
        return RivuletTemplateCreatedEventListener.class.getName();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {
    }
}
