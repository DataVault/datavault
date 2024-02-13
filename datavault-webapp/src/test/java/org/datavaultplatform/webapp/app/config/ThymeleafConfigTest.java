package org.datavaultplatform.webapp.app.config;

import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest
@ProfileStandalone
public class ThymeleafConfigTest {

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
  }

  private View getView(String viewName) throws Exception {
    return this.viewResolver.resolveViewName(viewName, Locale.getDefault());
  }

  private String viewToHtml(View view, ModelMap modelMap) throws Exception {
    view.render(modelMap, mRequest, mResponse);
    String html = mResponse.getContentAsString();
    return html;
  }

  private String getHtml(String viewName, ModelMap modelMap) throws Exception {
    View view = getView(viewName);
    String html = viewToHtml(view, modelMap);
    return html;
  }

  static String HELLO_FIRST_LINE = "<!DOCTYPE html><!--hello.html-->";
  static String ERROR_FIRST_LINE = "<!DOCTYPE html><!--error.html-->";

  @Test
  void testThymeleafHelloPage() throws Exception {
    ClassPathResource helloResource = new ClassPathResource("WEB-INF/templates/test/hello.html");
    assertEquals(HELLO_FIRST_LINE, getFirstLine(helloResource));

    String helloTemplateHtml = getHtml("test/hello.html", new ModelMap());
    assertEquals(HELLO_FIRST_LINE, getFirstLine(helloTemplateHtml));
  }

  @Test
  @WithMockUser(roles="MANAGER")
  void testThymeleafErrorPage() throws Exception {
    ClassPathResource errorResource = new ClassPathResource("WEB-INF/templates/error/error.html");
    assertEquals(ERROR_FIRST_LINE, getFirstLine(errorResource));

    ModelMap modelMap = new ModelMap();
    modelMap.put("message","This is a test error message");
    String errorTemplateHtml = getHtml("error/error.html", modelMap);
    //html is a mix of error 'page' and default template.
    assertThat(errorTemplateHtml).startsWith("<!DOCTYPE html><!--error.html-->\n<!--defaultLayout.html-->");

    Document doc = Jsoup.parse(errorTemplateHtml);
    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Error Page");

    //check 1st css link
    List<Element> linkElements = doc.selectXpath("//link", Element.class);
    Element firstLink = linkElements.get(0);
    assertThat(firstLink.hasAttr("href")).isTrue();
    assertThat(firstLink.attr("href")).isEqualTo("/dv/resources/favicon.ico?v=2");

    //check 1st script tag
    List<Element> scriptElements = doc.selectXpath("//script", Element.class);
    Element firstScript = scriptElements.get(0);
    assertThat(firstScript.hasAttr("src")).isTrue();
    assertThat(firstScript.attr("src")).isEqualTo("/dv/resources/jquery/js/jquery-1.11.3.min.js");

    //check nav value is 'none'
    List<Comment> comments = doc.selectXpath("//div[@id='datavault-header']/comment()", Comment.class);
    Comment firstComment = comments.get(0);
    assertThat(firstComment.getData()).contains("nav is (none)");

    //check error template fragment is placed at correct place within layout template
    List<Element> bodyDivs = doc.selectXpath("//div[@id='datavault-body']", Element.class);
    Element bodyDiv = bodyDivs.get(0);
    Element errorDiv = bodyDiv.child(0);
    assertThat(errorDiv.attr("id")).isEqualTo("error");

    //check the error message gets placed into html
    List<Element> errorMessages = doc.selectXpath("//span[@id='error-message']", Element.class);
    Element errorMessage = errorMessages.get(0);
    assertThat(errorMessage.text()).isEqualTo("This is a test error message");

    System.out.println(errorTemplateHtml);

  }

  public String getFirstLine(ClassPathResource res) throws IOException {
    InputStreamReader rdr = new InputStreamReader(res.getInputStream());
    LineNumberReader lnr = new LineNumberReader(rdr);
    return lnr.readLine();
  }

  public String getFirstLine(String fileContents) throws IOException {
    if (fileContents == null) {
      return null;
    } else {
      return Arrays.stream(fileContents.split("\n")).findFirst().orElse(null);
    }
  }

  @Test
  void testThymeleafConfiguration() {
    Set<DialectConfiguration> dialectConfigs = tlConfig.getDialectConfigurations();
    Set<String> dialectNames = dialectConfigs.stream().map(dc ->dc.getDialect().getName()).collect(Collectors.toSet());
    assertThat(dialectNames).containsAll(Arrays.asList("SpringSecurity","Layout","SpringStandard","java8time"));
    System.out.println(tlConfig);
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
}
