package zly.rivulet.spring.base.scan.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.util.StringUtils;
import zly.rivulet.base.Rivulet;
import zly.rivulet.base.RivuletManager;

import java.util.List;

public class RivuletTemplateCreatedEventListener implements ApplicationListener<RivuletTemplateCreatedEvent> {

    private String rivuletTemplateBeanName;

    private List<Class<?>> allDescClass;

    @Override
    public void onApplicationEvent(RivuletTemplateCreatedEvent event) {
        Rivulet rivuletTemplate = event.getRivuletTemplate();
        String eventBeanName = event.getRivuletTemplateBeanName();
        if (!StringUtils.hasText(eventBeanName)) {
            return;
        }
        if (StringUtils.hasText(rivuletTemplateBeanName) && !rivuletTemplateBeanName.equals(eventBeanName)) {
            // 和预期名称不符
            return;
        }

        this.putInStorage(rivuletTemplate);
    }

    private void putInStorage(Rivulet rivuletTemplate) {
        RivuletManager rivuletManager = rivuletTemplate.getRivuletManager();
        for (Class<?> descClass : allDescClass) {
            rivuletManager.putInStorage(descClass);
        }

    }

    public void setRivuletTemplateBeanName(String rivuletTemplateBeanName) {
        this.rivuletTemplateBeanName = rivuletTemplateBeanName;
    }

    public void setAllDescClass(List<Class<?>> allDescClass) {
        this.allDescClass = allDescClass;
    }
}
