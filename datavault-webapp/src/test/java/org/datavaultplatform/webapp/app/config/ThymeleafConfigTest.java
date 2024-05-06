package org.datavaultplatform.webapp.app.config;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.thymeleaf.DialectConfiguration;
import org.thymeleaf.EngineConfiguration;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@WebMvcTest
@ProfileStandalone
@TestMethodOrder(MethodOrderer.MethodName.class)
@Slf4j
public class ThymeleafConfigTest extends BaseThymeleafTest{

    public static final String HELLO_FIRST_LINE = "<!DOCTYPE html><!--test/hello.html-->";

    @MockBean
    PermissionEvaluator mEvaluator;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private SpringResourceTemplateResolver templateResolver;

    @Autowired
    private ViewResolver viewResolver;

    EngineConfiguration tlConfig;

    MockHttpServletRequest mRequest;

    MockHttpServletResponse mResponse;

    @BeforeEach
    void setup() {
        this.mRequest = new MockHttpServletRequest();
        this.mRequest.setContextPath("/dv");
        this.mResponse = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mRequest));
        assertThat(this.templateEngine.getConfiguration()).isInstanceOf(EngineConfiguration.class);
        tlConfig = (EngineConfiguration) this.templateEngine.getConfiguration();

        lenient().doAnswer(invocation -> {
            Authentication auth = invocation.getArgument(0);
            Serializable targetId = invocation.getArgument(1);
            String targetType = invocation.getArgument(2);
            Object permission = invocation.getArgument(3);
            System.out.printf("hasPermission4[%s][%s][%s][%s]%n",auth.getName(), targetId, targetType, permission);
            return true;
        }).when(mEvaluator).hasPermission(any(), any(), any(), any());
        lenient().doAnswer(invocation -> {
            Authentication auth = invocation.getArgument(0);
            Object targetDomainObject = invocation.getArgument(1);
            Object permission = invocation.getArgument(2);
            System.out.printf("hasPermission3[%s][%s][%s]%n",auth.getName(), targetDomainObject, permission);
            return true;
        }).when(mEvaluator).hasPermission(any(), any(), any());
    }

    private View getView(String viewName) throws Exception {
        return this.viewResolver.resolveViewName(viewName, Locale.getDefault());
    }

    private String viewToHtml(View view, ModelMap modelMap) throws Exception {
        view.render(modelMap, mRequest, mResponse);
        return mResponse.getContentAsString();
    }

    private String getHtml(String viewName, ModelMap modelMap) throws Exception {
        View view = getView(viewName);
        return viewToHtml(view, modelMap);
    }

    @Test
    void testThymeleafConfiguration() {
        Set<DialectConfiguration> dialectConfigs = tlConfig.getDialectConfigurations();
        Set<String> dialectNames = dialectConfigs.stream().map(dc -> dc.getDialect().getName()).collect(Collectors.toSet());
        assertThat(dialectNames).containsAll(Arrays.asList("SpringSecurity", "Layout", "SpringStandard", "java8time"));
        log.info(tlConfig.toString());
        assertThat(tlConfig.getTemplateResolvers()).hasSize(1);
        ITemplateResolver firstTemplateResolver = tlConfig.getTemplateResolvers().iterator().next();
        assertThat(this.templateResolver).isSameAs(firstTemplateResolver);
    }

    @Test
    void testViewResolver() {
        assertThat(this.templateResolver.getCheckExistence()).isTrue();
        assertThat(this.templateResolver.getPrefix()).isEqualTo("classpath:/WEB-INF/templates/");
        assertThat(this.templateResolver.getSuffix()).isEqualTo(".html");
        assertThat(this.templateResolver.getCharacterEncoding()).isEqualTo("UTF-8");
        assertThat(this.templateResolver.isCacheable()).isTrue();
    }

    @Test
    void test00TestHello() throws Exception {
        ClassPathResource helloResource = new ClassPathResource("WEB-INF/templates/test/hello.html");
        assertEquals(HELLO_FIRST_LINE, getFirstLine(helloResource));
        ModelMap modelMap = getModelMap();
        modelMap.put("name", "user101");
        String helloTemplateHtml = getHtml("test/hello.html", modelMap);
        assertEquals(HELLO_FIRST_LINE, getFirstLine(helloTemplateHtml));
        Document doc = Jsoup.parse(helloTemplateHtml);

        noFormFields(doc);

        checkTitle(doc, "Hello user101!");

        outputHtml("test00", doc);
    }


}
