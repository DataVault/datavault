package org.datavaultplatform.webapp.app.ajp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.common.services.LDAPService;
import org.datavaultplatform.common.util.DockerUtils;
import org.datavaultplatform.webapp.authentication.shib.ShibAuthenticationListener;
import org.datavaultplatform.webapp.authentication.shib.ShibGrantedAuthorityService;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.test.ProfileShib;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.SocketUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.io.Files;
import org.testcontainers.utility.MountableFile;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ProfileShib
@TestPropertySource(properties = {"tomcat.ajp.enabled=true"})
@Testcontainers(disabledWithoutDocker = true)
@Slf4j
class AjpConnectorIT {

  static int springBootAppPort = SocketUtils.findAvailableTcpPort();
  static int springBootAjpPort = SocketUtils.findAvailableTcpPort();
  @Container
  static GenericContainer<?> httpdContainer;
  static File tempHttpdConf;

  static {

    if(DockerUtils.isRunningInsideDocker()) {
      org.testcontainers.Testcontainers.exposeHostPorts(springBootAppPort);
    }

    initTempHttpdConf();

    MountableFile mountableFile = MountableFile.forHostPath(tempHttpdConf.getAbsolutePath());

    httpdContainer = new GenericContainer<>("httpd:2.4")
        .withExposedPorts(80)
        .withCopyFileToContainer(mountableFile, "/usr/local/apache2/conf/httpd.conf");
  }

  @MockBean
  ShibAuthenticationListener mAuthListener;
  @MockBean
  RestService mRestService;
  @MockBean
  LDAPService mLdapService;
  @MockBean
  ShibGrantedAuthorityService mGrantedAuthorityService;
  @Mock
  User mUser;
  @Captor
  ArgumentCaptor<AuthenticationSuccessEvent> argAuthSuccessEvent;
  private String dockerHost;

  @SneakyThrows
  static void initTempHttpdConf() {
    tempHttpdConf = new File(Files.createTempDir(), "httpd.conf");

    ClassPathResource resource = new ClassPathResource("httpd.conf");
    InputStream is = resource.getInputStream();
    String content = IOUtils.toString(is, "UTF-8");
    content = content.replaceAll("8009", String.valueOf(springBootAjpPort));
    if (DockerUtils.isRunningInsideDocker()) {
      content = content.replaceAll("host.docker.internal", "host.testcontainers.internal");
    }

    try (OutputStream os = new FileOutputStream(tempHttpdConf)) {
      IOUtils.write(content, os, "UTF-8");
    }
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    System.out.printf("springBootAppPort [%s]%n", springBootAppPort);
    System.out.printf("springBootAjpPort [%s]%n", springBootAjpPort);
    registry.add("server.port", () -> springBootAppPort);
    registry.add("tomcat.ajp.port", () -> springBootAjpPort);
  }

  @BeforeEach
  @SneakyThrows
  void setup() {

    ArgumentCaptor<String> argId = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<User> argUser = ArgumentCaptor.forClass(User.class);
    ArgumentCaptor<ValidateUser> argValidateUser = ArgumentCaptor.forClass(ValidateUser.class);

    when(mRestService.userExists(argValidateUser.capture())).thenReturn(false);

    when(mRestService.addUser(argUser.capture())).thenReturn(mUser);

    HashMap<String, String> ldapInfo = new HashMap<>();
    when(mLdapService.getLDAPAttributes(argId.capture())).thenReturn(ldapInfo);

    doNothing().when(mAuthListener).onApplicationEvent(argAuthSuccessEvent.capture());

  }

  @Test
  @SneakyThrows
  void testConnectionUsingTomcatDirect() {
    checkUserIdFromHttpResponse(springBootAppPort);
  }

  @Test
  void testViaApacheHttpdAndAjp() {
    int httpdPort = httpdContainer.getMappedPort(80);
    checkUserIdFromHttpResponse(httpdPort);
  }

  void checkUserIdFromHttpResponse(int port) {
    String dockerHost = httpdContainer.getHost();
    log.info("DOCKER HOST[{}]", dockerHost);
    boolean runningInsideDocker = DockerUtils.isRunningInsideDocker();
    String host = runningInsideDocker ? dockerHost : "localhost";
    log.info("runningInsideDocker[{}] => HOST[{}]", runningInsideDocker, host);

    RestTemplate rt = new RestTemplateBuilder().rootUri(String.format("http://%s:%s", host, port))
        .defaultHeader("uid", "u123")
        .build();
    String response = rt.getForObject("/test/auth", String.class);
    DocumentContext ctx = JsonPath.parse(response);
    assertEquals("u123", ctx.read("$.principal"));
  }

  @TestConfiguration
  static class TestConfig {

    @RestController
    public class TestAuthController {

      @GetMapping("/test/auth")
      public AuthUserDetails admin(Authentication authentication) {
        return new AuthUserDetails((PreAuthenticatedAuthenticationToken) authentication);
      }

    }
  }

}
