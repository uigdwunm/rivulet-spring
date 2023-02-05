package zly.rivulet.spring.base.scan.listener;

import org.springframework.context.ApplicationEvent;
import zly.rivulet.base.Rivulet;

public class RivuletTemplateCreatedEvent extends ApplicationEvent {
    private final String rivuletTemplateBeanName;

    public RivuletTemplateCreatedEvent(Rivulet rivuletTemplate, String rivuletTemplateBeanName) {
        super(rivuletTemplate);
        this.rivuletTemplateBeanName = rivuletTemplateBeanName;
    }

    public Rivulet getRivuletTemplate() {
        return (Rivulet) this.getSource();
    }

    public String getRivuletTemplateBeanName() {
        return rivuletTemplateBeanName;
    }
}
