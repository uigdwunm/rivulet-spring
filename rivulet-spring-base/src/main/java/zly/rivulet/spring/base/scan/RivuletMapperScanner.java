package zly.rivulet.spring.base.scan;

import org.springframework.aop.scope.ScopedProxyFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.util.StringUtils;
import zly.rivulet.spring.base.proxy.RivuletMapperFactoryBean;

import java.util.Set;

public class RivuletMapperScanner extends ClassPathBeanDefinitionScanner {

    private String rivuletTemplateBeanName;

    public RivuletMapperScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public void setRivuletTemplateBeanName(String rivuletTemplateBeanName) {
        this.rivuletTemplateBeanName = rivuletTemplateBeanName;
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitionHolders = super.doScan(basePackages);
        if (beanDefinitionHolders.isEmpty()) {
            // TODO 可能已经被mybatis扫描成bean了，需要抢回来
            return beanDefinitionHolders;
        }
        for (BeanDefinitionHolder holder : beanDefinitionHolders) {
            // 加工单个mapper
            this.processBeanDefinition((AbstractBeanDefinition) holder.getBeanDefinition());
        }

        return beanDefinitionHolders;
    }

    private void processBeanDefinition(AbstractBeanDefinition definition) {
        // 不知道是啥但是看起来不能丢掉
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
