package zly.rivulet.spring.base.scan;

import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.util.StringUtils;
import zly.rivulet.spring.base.proxy.RivuletMapperFactoryBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RivuletMapperScanner extends ClassPathBeanDefinitionScanner {

    private String rivuletTemplateBeanName;
    private final Map<String, Set<BeanDefinition>> candidateComponentsMap = new HashMap<>();

    private BeanNameGenerator beanNameGenerator;

    public RivuletMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
        beanNameGenerator = new AnnotationBeanNameGenerator();
        this.setBeanNameGenerator(beanNameGenerator);
    }

    @Override
    public void setBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
        this.beanNameGenerator = beanNameGenerator;
        super.setBeanNameGenerator(beanNameGenerator);
    }

    public void setRivuletTemplateBeanName(String rivuletTemplateBeanName) {
        this.rivuletTemplateBeanName = rivuletTemplateBeanName;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    @Override
    public Set<BeanDefinition> findCandidateComponents(String basePackage) {
        Set<BeanDefinition> candidateComponents = this.candidateComponentsMap.get(basePackage);
        if (candidateComponents == null) {
            candidateComponents = super.findCandidateComponents(basePackage);
            this.candidateComponentsMap.put(basePackage, candidateComponents);
        }
        return candidateComponents;
    }

    @Override
    protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
        BeanDefinitionRegistry registry = getRegistry();
        if (registry.containsBeanDefinition(beanName)) {
            // ????????????????????????
            // ??????????????????RivuletMapper???Bean?????????????????????????????????
            registry.removeBeanDefinition(beanName);
        }
        // TODO ?????????????????????
        return true;
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        if (beanDefinitionHolders.isEmpty()) {
            return beanDefinitionHolders;
        }
        for (BeanDefinitionHolder holder : beanDefinitionHolders) {
            // ????????????mapper
            this.processBeanDefinition((AbstractBeanDefinition) holder.getBeanDefinition());
        }

        return beanDefinitionHolders;
    }

    private void processBeanDefinition(AbstractBeanDefinition definition) {
        // ??????????????????????????????????????????
        if (ScopedProxyFactoryBean.class.getName().equals(definition.getBeanClassName())) {
            BeanDefinitionHolder beanDefinitionHolder = ((RootBeanDefinition) definition).getDecoratedDefinition();
            if (beanDefinitionHolder == null) {
                throw new IllegalStateException("The target bean definition of scoped proxy bean not found. Root bean definition[" + definition + "]");
            }
            definition = (AbstractBeanDefinition) beanDefinitionHolder.getBeanDefinition();
        }
        String beanClassName = definition.getBeanClassName();
        definition.getConstructorArgumentValues().addGenericArgumentValue(beanClassName);
        try {
            // for spring-native
            definition.getPropertyValues().add("mapperInterface", Class.forName(beanClassName));
        } catch (ClassNotFoundException ignore) {
            // ignore
        }
        definition.setBeanClass(RivuletMapperFactoryBean.class);
        if (StringUtils.hasText(rivuletTemplateBeanName)) {
            definition.getPropertyValues().add("rivuletTemplate", new RuntimeBeanReference(this.rivuletTemplateBeanName));
        } else {
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }

    }
}
