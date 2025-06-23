package br.com.notificationservice.domain.service.email;

import br.com.notificationservice.config.SendNotification;
import br.com.notificationservice.domain.exception.EmailException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Component
@RequiredArgsConstructor
public class EmailProcessorTemplate {

    private final Configuration freemarkerConfig;

    public String processTemplate(SendNotification.Message message) {
        try {
            Template template = freemarkerConfig.getTemplate(message.getBody());

            return FreeMarkerTemplateUtils.processTemplateIntoString(template, message.getVariables());
        } catch (Exception ex) {
            throw new EmailException(ex.getMessage());
        }
    }
}
