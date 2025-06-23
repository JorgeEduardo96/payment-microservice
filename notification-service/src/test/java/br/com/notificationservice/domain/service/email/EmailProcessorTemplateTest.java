package br.com.notificationservice.domain.service.email;

import br.com.notificationservice.config.SendNotification;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailProcessorTemplateTest {

    @Mock
    private Configuration freemarkerConfig;

    @InjectMocks
    private EmailProcessorTemplate underTest;

    @Test
    void processTemplate() throws Exception {
        SendNotification.Message message = SendNotification.Message.builder()
                .body("template.ftl")
                .variables(Map.of("name", "John"))
                .build();

        Template templateMock = mock(Template.class);

        when(freemarkerConfig.getTemplate("template.ftl")).thenReturn(templateMock);

        try (MockedStatic<FreeMarkerTemplateUtils> mockedStatic = Mockito.mockStatic(FreeMarkerTemplateUtils.class)) {
            mockedStatic.when(() ->
                    FreeMarkerTemplateUtils.processTemplateIntoString(templateMock, message.getVariables())
            ).thenReturn("Hello, John!");

            String result = underTest.processTemplate(message);

            verify(freemarkerConfig).getTemplate("template.ftl");
            assertThat("Hello, John!").isEqualTo(result);
        }
    }
}
