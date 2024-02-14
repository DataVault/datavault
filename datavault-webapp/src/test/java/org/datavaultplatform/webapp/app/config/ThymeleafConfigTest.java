package org.datavaultplatform.webapp.app.config;

import org.bouncycastle.oer.its.ieee1609dot2.ExplicitCertificate;
import org.checkerframework.checker.units.qual.C;
import org.datavaultplatform.common.io.FileUtils;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.response.*;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;
import org.datavaultplatform.webapp.model.DepositReviewModel;
import org.datavaultplatform.webapp.model.VaultReviewModel;
import org.datavaultplatform.webapp.security.ScopedPermissionEvaluator;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest
@ProfileStandalone
public class ThymeleafConfigTest {

  @MockBean
  ScopedPermissionEvaluator mEvaluatior;

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

  @Test
  void testAdminArchiveStoresIndex() throws Exception {
    ModelMap modelMap = new ModelMap();

    ArchiveStore store1 = getArchiveStore("id-001");
    store1.setLabel("LABEL 1");
    store1.setStorageClass(TivoliStorageManager.class.getName());
    store1.getProperties().put("prop1-1","value1-1");
    store1.getProperties().put("prop1-2","value1-2");

    ArchiveStore store2 = getArchiveStore("id-002");
    store2.setLabel("LABEL 2");
    store2.setStorageClass(TivoliStorageManager.class.getName());
    store2.getProperties().put("prop2-1","value2-1");
    store2.getProperties().put("prop2-2","value2-2");

    modelMap.put("archivestores",Arrays.asList(store1, store2));
    String html = getHtml("admin/archivestores/index.html", modelMap);
    Document doc = Jsoup.parse(html);
    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Archive Stores");
    System.out.println(html);
  }

  @Test
  void testAdminAuditsIndex() throws Exception {
    ModelMap modelMap = new ModelMap();

    AuditInfo audit1 = getAuditInfo("aud-001");
    audit1.setStatus(Audit.Status.IN_PROGRESS);

    AuditInfo audit2 = getAuditInfo("aud-002");
    audit2.setStatus(Audit.Status.COMPLETE);

    AuditChunkStatusInfo info1 = getAuditChunkStatusInfo("acsi-1");
    AuditChunkStatusInfo info2 = getAuditChunkStatusInfo("acsi-2");

    Deposit deposit = getDeposit("dep-001");
    DepositChunk dc1 = getDepositChunk("dc-001");
    DepositChunk dc2 = getDepositChunk("dc-002");

    info1.setStatus(AuditChunkStatus.Status.IN_PROGRESS);
    info1.setArchiveId("arch-id-1");
    info1.setCreationTime(new Date());
    info1.setCompletedTime(new Date());
    info1.setDeposit(deposit);
    info1.setNote("note-1");
    info1.setDepositChunk(dc1);

    info2.setStatus(AuditChunkStatus.Status.IN_PROGRESS);
    info2.setArchiveId("arch-id-1");
    info2.setCreationTime(new Date());
    info2.setCompletedTime(new Date());
    info2.setDeposit(deposit);
    info2.setNote("note-1");
    info2.setDepositChunk(dc1);

    audit1.setAuditChunks(Arrays.asList(info1, info2));
    audit2.setAuditChunks(Arrays.asList(info1, info2));

    modelMap.put("audits", Arrays.asList(audit1, audit2));

    String html = getHtml("admin/audits/index.html", modelMap);
    Document doc = Jsoup.parse(html);
    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Audits");
    System.out.println(html);
  }

  @Test
  //TODO this one is quite hard to setup - complex map structure
  void testAdminAuditsDeposits() throws Exception {
    ModelMap modelMap =  new ModelMap();
    modelMap.put("deposits", Collections.emptyList());
    String html = getHtml("admin/audits/deposits.html", modelMap);
    Document doc = Jsoup.parse(html);
    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin Audits Deposits");
    System.out.println(html);
  }

  @Test
  void testAdminBillingBillingDetails() throws Exception {
    Date now = new Date();
    ModelMap modelMap =  new ModelMap();
    BillingInformation info = getInfo(now);

    modelMap.put("billingDetails", info);
    String html = getHtml("admin/billing/billingDetails.html", modelMap);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin Billing BillingDetails");

    checkTextInputFieldValue(doc, "contactName", "James Bond");
    checkTextInputFieldValue(doc, "school", "Informatics");
    checkTextInputFieldValue(doc, "subUnit", "sub-unit-123");
    checkTextInputFieldValue(doc, "projectId", "ID-123");
    checkTextInputFieldValue(doc, "amountBilled", "123.45");
    checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
    checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

    Element form = lookupElement(doc, "//form");
    assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");
    System.out.println(html);
  }
  @Test
  void testAdminBillingBillingDetailsGrant() throws Exception {
    Date now = new Date();
    ModelMap modelMap =  new ModelMap();
    BillingInformation info = getInfo(now);

    modelMap.put("billingDetails", info);
    String html = getHtml("admin/billing/billingDetailsGrant.html", modelMap);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin Billing BillingDetails (Grant)");

    checkTextInputFieldValue(doc, "contactName", "James Bond");
    checkTextInputFieldValue(doc, "school", "Informatics");
    checkTextInputFieldValue(doc, "subUnit", "sub-unit-123");
    checkTextInputFieldValue(doc, "projectTitle", "MY PROJECT TITLE");
    checkTextInputFieldValue(doc, "amountBilled", "123.45");
    checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
    checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

    Element form = lookupElement(doc, "//form");
    assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");
    System.out.println(html);
  }

  @Test
  void testAdminBillingBillingDetailsNA() throws Exception {
    Date now = new Date();
    ModelMap modelMap =  new ModelMap();
    BillingInformation info = getInfo(now);

    modelMap.put("billingDetails", info);
    String html = getHtml("admin/billing/billingDetailsNA.html", modelMap);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin Billing BillingDetails (NA)");

    checkTextInputFieldValue(doc, "projectId", "ID-123");
    checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
    checkTextInputFieldValue(doc, "amountBilled", "123.45");
    checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
    checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

    Element form = lookupElement(doc, "//form");
    assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");
    System.out.println(html);
  }

  @Test
  void testAdminBillingBillingDetailsWillPay() throws Exception {
    Date now = new Date();
    ModelMap modelMap =  new ModelMap();
    BillingInformation info = getInfo(now);

    modelMap.put("billingDetails", info);
    String html = getHtml("admin/billing/billingDetailsWilLPay.html", modelMap);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin Billing BillingDetails (WILL PAY)");

    checkTextInputFieldValue(doc, "projectId", "ID-123");
    checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
    checkTextInputFieldValue(doc, "amountBilled", "123.45");
    checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
    checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

    Element form = lookupElement(doc, "//form");
    assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");
    System.out.println(html);
  }

  @Test
  void testAdminBillingBillingDetailsBudget() throws Exception {
    Date now = new Date();
    ModelMap modelMap =  new ModelMap();
    BillingInformation info = getInfo(now);

    modelMap.put("billingDetails", info);
    String html = getHtml("admin/billing/billingDetailsBudget.html", modelMap);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin Billing BillingDetails (Budget)");

    checkTextInputFieldValue(doc, "projectId", "ID-123");
    checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
    checkTextInputFieldValue(doc, "amountBilled", "123.45");
    checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
    checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

    Element form = lookupElement(doc, "//form");
    assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");
    System.out.println(html);
  }

  @Test
  void testAdminBillingBillingDetailsBuyNewSlice() throws Exception {
    Date now = new Date();
    ModelMap modelMap =  new ModelMap();
    BillingInformation info = getInfo(now);

    modelMap.put("billingDetails", info);
    String html = getHtml("admin/billing/billingDetailsBuyNewSlice.html", modelMap);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin Billing BillingDetails (SLICE)");

    checkTextInputFieldValue(doc, "projectId", "ID-123");
    checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
    checkTextInputFieldValue(doc, "amountBilled", "123.45");
    checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
    checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

    Element form = lookupElement(doc, "//form");
    assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");
    System.out.println(html);
  }

  @Test
  void testAdminBillingBillingDetailsFundingNoDoNotKnow() throws Exception {
    Date now = new Date();
    ModelMap modelMap =  new ModelMap();
    BillingInformation info = getInfo(now);

    modelMap.put("billingDetails", info);
    String html = getHtml("admin/billing/billingDetailsFundingNoDoNotKNow.html", modelMap);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin Billing BillingDetails (FUNDING: NO OR DO NOT KNOW)");

    checkTextInputFieldValue(doc, "projectId", "ID-123");
    checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
    checkTextInputFieldValue(doc, "amountBilled", "123.45");
    checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
    checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

    Element form = lookupElement(doc, "//form");
    assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");
    System.out.println(html);
  }

  @Test
  void testAdminBillingBillingDetailsBuyFeeWaiver() throws Exception {
    Date now = new Date();
    ModelMap modelMap =  new ModelMap();
    BillingInformation info = getInfo(now);

    modelMap.put("billingDetails", info);
    String html = getHtml("admin/billing/billingDetailsFeeWaiver.html", modelMap);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin Billing BillingDetails (Funding: NO or Don't Know)");

    checkTextInputFieldValue(doc, "projectId", "ID-123");
    checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
    checkTextInputFieldValue(doc, "amountBilled", "123.45");
    checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
    checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

    Element form = lookupElement(doc, "//form");
    assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");
    System.out.println(html);
  }

  @Test
  @WithMockUser(roles="MANAGER")
  void testAdminDepositsIndex() throws Exception {

    Mockito.doAnswer(invocation -> {
      return true;
    }).when(mEvaluatior).hasPermission(Mockito.any(), Mockito.any(), Mockito.any());

    Date now = new Date();
    ModelMap modelMap =  new ModelMap();

    Deposit deposit1 = getDeposit("dep-id-001");

    DepositChunk dc1 = getDepositChunk("dc-001");
    DepositChunk dc2 = getDepositChunk("dc-002");
    dc1.setDeposit(deposit1);
    dc2.setDeposit(deposit1);
    deposit1.setDepositChunks(Arrays.asList(dc1, dc2));

    Deposit deposit2 = getDeposit("dep-id-002");
    DepositChunk dc3 = getDepositChunk("dc-003");
    DepositChunk dc4 = getDepositChunk("dc-004");
    dc3.setDeposit(deposit2);
    dc4.setDeposit(deposit2);
    deposit2.setDepositChunks(Arrays.asList(dc3, dc4));

    dc1.setChunkNum(1);

    dc2.setChunkNum(2);

    dc3.setChunkNum(3);
    dc4.setChunkNum(4);

    deposit1.setName("deposit-one");
    deposit2.setName("deposit-two");

    DepositInfo info1 = getDepositInfo("dep-id-001");
    info1.setName("name1");
    info1.setStatus(Deposit.Status.IN_PROGRESS);
    info1.setDepositChunks(deposit1.getDepositChunks());
    info1.setDatasetID("dataset-id-1");
    info1.setVaultID("vaultID123");
    info1.setVaultName("vaultName1");
    info1.setVaultOwnerID("owner1");
    info1.setGroupID("group1");
    info1.setGroupName("groupName1");
    info1.setUserName("user1");
    info1.setCrisID("CRIS-ID-1");
    info1.setVaultReviewDate(new Date().toString());
    info1.setDescription("DepositOneDescription");
    info1.setCreationTime(new Date());
    info1.setDepositPaths(Collections.emptyList());
    info1.setDepositSize(1234);
    info1.setPersonalDataStatement("personalDataStatement1");
    info1.setShortFilePath("short-file-path-1");
    info1.setHasPersonalData(true);

    DepositInfo info2 = getDepositInfo("dep-id-002");
    info2.setName("name2");
    info2.setStatus(Deposit.Status.COMPLETE);
    info2.setDepositChunks(deposit1.getDepositChunks());
    info2.setDatasetID("dataset-id-2");
    info2.setVaultID("vaultID234");
    info2.setVaultName("vaultName2");
    info2.setVaultOwnerID("owner2");
    info2.setGroupID("group2");
    info2.setGroupName("groupName2");
    info2.setUserName("user2");
    info2.setCrisID("CRIS-ID-2");
    info2.setVaultReviewDate(new Date().toString());
    info2.setDescription("DepositTwoDescription");
    info2.setCreationTime(new Date());
    info2.setDepositPaths(Collections.emptyList());
    info2.setDepositSize(4567);
    info2.setPersonalDataStatement("personalDataStatement2");
    info2.setShortFilePath("short-file-path-2");
    info2.setHasPersonalData(true);
    modelMap.put("deposits", Arrays.asList(info1, info2));
    modelMap.put("totalRecords", 15);
    modelMap.put("totalPages", 1);
    modelMap.put("recordPerPage", 20);
    modelMap.put("offset",0);
    modelMap.put("sort","name");
    modelMap.put("order","asc");
    modelMap.put("orderStatus", Deposit.Status.IN_PROGRESS.name());
    modelMap.put("orderName","order123");
    modelMap.put("page",1);
    modelMap.put("orderDepositSize",123);
    modelMap.put("orderCreationTime","2023-12-31");
    modelMap.put("query","edinburgh university");
    modelMap.put("orderUserID","user123");
    modelMap.put("orderId","order123");
    modelMap.put("orderVaultId","orderVaultIdABC");

    String html = getHtml("admin/deposits/index.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);
  }

  @Test
  @WithMockUser(roles="MANAGER")
  void testAdminEvents() throws Exception {
    ModelMap modelMap =  new ModelMap();

    EventInfo event1 = getEventInfo("event-id-1");
    event1.setDepositID("deposit-123");
    event1.setAgent("agent-1");
    event1.setAgentType("WORKER");
    event1.setRemoteAddress("remote-addr-1");
    event1.setDepositID("message-1");
    event1.setTimestamp(new Date());
    event1.setRemoteAddress("remote-addr-1");
    event1.setEventClass("event-class-1");
    event1.setVaultID("vault-1");
    event1.setMessage("message-1");
    event1.setUserAgent("user-agent-1");
    event1.setUserID("user-1");

    EventInfo event2 = getEventInfo("event-id-2");
    event2.setDepositID("deposit-234");
    event2.setAgent("agent-2");
    event2.setAgentType("WEB");
    event2.setRemoteAddress("remote-addr-2");
    event2.setDepositID("message-2");
    event2.setTimestamp(new Date());
    event2.setRemoteAddress("remote-addr-2");
    event2.setEventClass("event-class-2");
    event2.setVaultID("vault-2");
    event2.setMessage("message-2");
    event2.setUserAgent("user-agent-2");
    event2.setUserID("user-2");

    modelMap.put("events", Arrays.asList(event1,event2));
    String html = getHtml("admin/events/index.html", modelMap);
    System.out.println(html);
  }


  @Test
  @WithMockUser(roles="MANAGER")
  void testAdminPendingVaultsIndex() throws Exception {
    ModelMap modelMap =  new ModelMap();


    modelMap.put("savedVaultsTotal", 123);
    modelMap.put("confirmedVaultsTotal",100);

    String html = getHtml("admin/pendingVaults/index.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Pending Vaults");

    String hrefAdministration = getHrefMatchedOnText(doc, "Administration");
    assertThat(hrefAdministration).isEqualTo("/dv/admin/");

    String hrefConfirmed = getHrefMatchedOnText(doc, "Confirmed");
    assertThat(hrefConfirmed).isEqualTo("/dv/admin/pendingVaults/confirmed/");

    String hrefSaved = getHrefMatchedOnText(doc, "Saved");
    assertThat(hrefSaved).isEqualTo("/dv/admin/pendingVaults/saved/");

  }
  @Test
  @WithMockUser(roles="MANAGER")
  void testAdminPendingVaultsConfirmed() throws Exception {
    ModelMap modelMap =  new ModelMap();

    VaultInfo vault1 = getVaultInfo("vault-info-1");
    vault1.setAffirmed(true);
    vault1.setAuthoriser("vault1-authoriser");
    vault1.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING);
    vault1.setConfirmed(true);
    vault1.setContact("vault1-contact");
    vault1.setCreationTime(new Date());
    vault1.setDataCreators(Arrays.asList("Tom","Dick","Harry"));
    vault1.setDescription("vault1-description");
    vault1.setEstimate(PendingVault.Estimate.UNDER_10TB);
    vault1.setGrantEndDate(new Date());
    vault1.setName("vault1-name");
    vault1.setOwnerId("vault1-owner-id");
    vault1.setProjectSize(1234);
    vault1.setReviewDate(new Date());
    vault1.setUserID("vault1-user-id");
    vault1.setVaultCreatorId("vault1-creator-id");

    VaultInfo vault2 = getVaultInfo("vault-info-2");
    vault2.setAffirmed(true);
    vault2.setAuthoriser("vault2-authoriser");
    vault2.setBillingType(PendingVault.Billing_Type.FEEWAIVER);
    vault2.setConfirmed(true);
    vault2.setContact("vault2-contact");
    vault2.setCreationTime(new Date());
    vault2.setDataCreators(Arrays.asList("Geddy","Neil","Alex"));
    vault2.setDescription("vault2-description");
    vault2.setEstimate(PendingVault.Estimate.OVER_10TB);
    vault2.setGrantEndDate(new Date());
    vault2.setName("vault2-name");
    vault2.setOwnerId("vault2-owner-id");
    vault2.setProjectSize(2345);
    vault2.setReviewDate(new Date());
    vault2.setUserID("vault2-user-id");
    vault2.setVaultCreatorId("vault2-creator-id");

    modelMap.put("recordsInfo","(RECORDS INFO)");
    modelMap.put("numberOfPages", 1);
    modelMap.put("activePageId", 1);
    modelMap.put("query","edinburgh university");
    modelMap.put("sort","name");
    modelMap.put("order","ORDER");
    modelMap.put("ordername","bob");
    modelMap.put("pendingVaults", Arrays.asList(vault1,vault2));

    String html = getHtml("admin/pendingVaults/confirmed.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Confirmed Pending Vaults");
  }

  @Test
  @WithMockUser(roles="MANAGER")
  void testAdminPendingVaultsSaved() throws Exception {
    ModelMap modelMap =  new ModelMap();

    VaultInfo vault1 = getVaultInfo("vault-info-1");
    vault1.setAffirmed(true);
    vault1.setAuthoriser("vault1-authoriser");
    vault1.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING);
    vault1.setConfirmed(true);
    vault1.setContact("vault1-contact");
    vault1.setCreationTime(new Date());
    vault1.setDataCreators(Arrays.asList("Tom","Dick","Harry"));
    vault1.setDescription("vault1-description");
    vault1.setEstimate(PendingVault.Estimate.UNDER_10TB);
    vault1.setGrantEndDate(new Date());
    vault1.setName("vault1-name");
    vault1.setOwnerId("vault1-owner-id");
    vault1.setProjectSize(1234);
    vault1.setReviewDate(new Date());
    vault1.setUserID("vault1-user-id");
    vault1.setVaultCreatorId("vault1-creator-id");

    VaultInfo vault2 = getVaultInfo("vault-info-2");
    vault2.setAffirmed(true);
    vault2.setAuthoriser("vault2-authoriser");
    vault2.setBillingType(PendingVault.Billing_Type.FEEWAIVER);
    vault2.setConfirmed(true);
    vault2.setContact("vault2-contact");
    vault2.setCreationTime(new Date());
    vault2.setDataCreators(Arrays.asList("Geddy","Neil","Alex"));
    vault2.setDescription("vault2-description");
    vault2.setEstimate(PendingVault.Estimate.OVER_10TB);
    vault2.setGrantEndDate(new Date());
    vault2.setName("vault2-name");
    vault2.setOwnerId("vault2-owner-id");
    vault2.setProjectSize(2345);
    vault2.setReviewDate(new Date());
    vault2.setUserID("vault2-user-id");
    vault2.setVaultCreatorId("vault2-creator-id");

    modelMap.put("recordsInfo","(RECORDS INFO)");
    modelMap.put("numberOfPages", 1);
    modelMap.put("activePageId", 1);
    modelMap.put("query","edinburgh university");
    modelMap.put("sort","name");
    modelMap.put("order","ORDER");
    modelMap.put("ordername","bob");
    modelMap.put("pendingVaults", Arrays.asList(vault1,vault2));

    String html = getHtml("admin/pendingVaults/saved.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Saved Pending Vaults");
  }

  @Test
  @WithMockUser(roles="MANAGER")
  void testAdminPendingVaultsSummary() throws Exception {
    ModelMap modelMap =  new ModelMap();

    VaultInfo vault1 = getVaultInfo("vault-info-1");
    vault1.setAffirmed(true);
    vault1.setAuthoriser("vault1-authoriser");
    vault1.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING);
    vault1.setConfirmed(true);
    vault1.setContact("vault1-contact");
    vault1.setCreationTime(new Date());
    vault1.setDataCreators(Arrays.asList("Tom","Dick","Harry"));
    vault1.setDepositorIds(Arrays.asList("Neil","Geddy","Alex"));
    vault1.setDescription("vault1-description");
    vault1.setEstimate(PendingVault.Estimate.UNDER_10TB);
    vault1.setGrantEndDate(new Date());
    vault1.setName("vault1-name");
    vault1.setOwnerId("vault1-owner-id");
    vault1.setPureLink(true);
    vault1.setProjectSize(1234);
    vault1.setReviewDate(new Date());
    vault1.setUserID("vault1-user-id");
    vault1.setVaultCreatorId("vault1-creator-id");
    vault1.setNotes("vault1-notes");

    CreateRetentionPolicy createRetentionPolicy = new CreateRetentionPolicy();
    createRetentionPolicy.setDescription("crp-desc");
    createRetentionPolicy.setId(123);
    createRetentionPolicy.setName("crp-name");
    createRetentionPolicy.setEndDate(new Date().toString());

    Group group = getGroup("group-id-1");
    group.setName("group-name-1");
    group.setEnabled(true);

    modelMap.put("pendingVault",vault1);
    modelMap.put("createRetentionPolicy", createRetentionPolicy);
    modelMap.put("group", group);

    String html = getHtml("admin/pendingVaults/summary.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Pending Vault Summary");
  }
  @Test
  @WithMockUser(roles="IS_ADMIN")
  void testAdminPendingVaultsEditPendingVault() throws Exception {
    ModelMap modelMap =  new ModelMap();

    CreateVault vault = new CreateVault();
    vault.setAffirmed(true);
    vault.setBillingGrantEndDate(new Date().toString());
    vault.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING.name());
    vault.setBudgetAuthoriser("budget-authoriser-1");
    vault.setBudgetSchoolOrUnit("Informatics");
    vault.setBudgetSubunit("SubUnit-1");
    vault.setConfirmed(true);
    vault.setContactPerson("contact-person-1");

    vault.setDatasetID("data-set-id-1");
    vault.setDataCreators(Arrays.asList("creator1","creator2"));
    vault.setDepositors(Arrays.asList("Neil","Geddy","Alex"));
    vault.setDescription("description-1");
    vault.setEstimate("estimate-1");

    vault.setGrantEndDate(String.valueOf(new Date()));
    vault.setGroupID("group-id-one");
    vault.setGrantSubunit("GrantSubUnit-1");
    vault.setGrantAuthoriser("grant-authorizer-1");

    vault.setIsOwner(true);

    vault.setLoggedInAs("user-one");

    vault.setName("vault-name");
    vault.setNominatedDataManagers(Arrays.asList("Tom","Dick","Harry"));
    vault.setNotes("notes-one");

    vault.setPaymentDetails("project-details");
    vault.setPendingID("pending-id-123");
    vault.setPolicyInfo("policy-info-1");
    vault.setProjectTitle("project-title");
    vault.setPureLink(true);
    vault.setReviewDate(new Date().toString());
    vault.setSliceID("slice-id-1");
    vault.setSliceQueryChoice(PendingVault.Slice_Query_Choice.NO_OR_DO_NOT_KNOW.name());
    vault.setVaultCreator("vault-creator-1");
    vault.setVaultOwner("vault-owner-1");

    RetentionPolicy policy1 = new RetentionPolicy();
    policy1.setId(111);
    RetentionPolicy policy2 = new RetentionPolicy();
    policy2.setId(222);

    modelMap.put("errors", Arrays.asList("error1","error2"));
    modelMap.put("vault", vault);
    modelMap.put("vaultID","vault-id-123");
    modelMap.put("policies", Arrays.asList(policy1, policy2));

    String html = getHtml("admin/pendingVaults/edit/editPendingVault.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Edit Pending Vault");
  }

  @Test
  @WithMockUser(roles="USER")
  void testAdminRetentionPoliciesIndex() throws Exception {
    ModelMap modelMap = new ModelMap();

    RetentionPolicy policy1 = new RetentionPolicy();
    policy1.setId(111);
    policy1.setName("policy-one");
    policy1.setDescription("policy1-description");

    RetentionPolicy policy2 = new RetentionPolicy();
    policy2.setId(222);
    policy2.setName("policy-two");
    policy2.setDescription("policy2-description");

    modelMap.put("policies", Arrays.asList(policy1, policy2));

    String html = getHtml("admin/retentionpolicies/index.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Retention Policies");

  }

  @Test
  @WithMockUser(roles="USER")
  void testAdminReviewsIndex() throws Exception {
    ModelMap modelMap =  new ModelMap();

    VaultInfo vault1 = getVaultInfo("vault-info-1");
    vault1.setAffirmed(true);
    vault1.setAuthoriser("vault1-authoriser");
    vault1.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING);
    vault1.setConfirmed(true);
    vault1.setContact("vault1-contact");
    vault1.setCreationTime(new Date());
    vault1.setDataCreators(Arrays.asList("Tom","Dick","Harry"));
    vault1.setDescription("vault1-description");
    vault1.setEstimate(PendingVault.Estimate.UNDER_10TB);
    vault1.setGrantEndDate(new Date());
    vault1.setName("vault1-name");
    vault1.setOwnerId("vault1-owner-id");
    vault1.setProjectSize(1234);
    vault1.setReviewDate(new Date());
    vault1.setUserID("vault1-user-id");
    vault1.setVaultCreatorId("vault1-creator-id");

    VaultInfo vault2 = getVaultInfo("vault-info-2");
    vault2.setAffirmed(true);
    vault2.setAuthoriser("vault2-authoriser");
    vault2.setBillingType(PendingVault.Billing_Type.FEEWAIVER);
    vault2.setConfirmed(true);
    vault2.setContact("vault2-contact");
    vault2.setCreationTime(new Date());
    vault2.setDataCreators(Arrays.asList("Geddy","Neil","Alex"));
    vault2.setDescription("vault2-description");
    vault2.setEstimate(PendingVault.Estimate.OVER_10TB);
    vault2.setGrantEndDate(new Date());
    vault2.setName("vault2-name");
    vault2.setOwnerId("vault2-owner-id");
    vault2.setProjectSize(2345);
    vault2.setReviewDate(new Date());
    vault2.setUserID("vault2-user-id");
    vault2.setVaultCreatorId("vault2-creator-id");

    modelMap.put("vaults", Arrays.asList(vault1,vault2));

    String html = getHtml("admin/reviews/index.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Reviews");
  }

  @Test
  @WithMockUser(roles="USER")
  void testAdminReviewsCreate() throws Exception {
    ModelMap modelMap =  new ModelMap();

    VaultReviewModel vrModel = new VaultReviewModel();
    vrModel.setVaultReviewId("vault-review-id-1");
    vrModel.setNewReviewDate(new Date().toString());
    vrModel.setComment("comment-1");
    vrModel.setActionedDate(new Date());

    DepositReviewModel drm1 = new DepositReviewModel();
    drm1.setDepositId("drm1-depositId1");
    drm1.setDepositReviewId("drm1-reviewId1");
    drm1.setName("drm1-name");
    drm1.setComment("drm1-comment");

    DepositReviewModel drm2 = new DepositReviewModel();
    drm2.setDepositId("drm2-depositId2");
    drm2.setDepositReviewId("drm2-reviewId2");
    drm2.setName("drm2-name");
    drm2.setComment("drm2-comment");

    vrModel.setDepositReviewModels(Arrays.asList(drm1, drm2));

    VaultInfo vault1 = getVaultInfo("vault-info-1");
    vault1.setAffirmed(true);
    vault1.setAuthoriser("vault1-authoriser");
    vault1.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING);
    vault1.setConfirmed(true);
    vault1.setContact("vault1-contact");
    vault1.setCreationTime(new Date());
    vault1.setDataCreators(Arrays.asList("Tom","Dick","Harry"));
    vault1.setDatasetName("dataset-name1");
    vault1.setDescription("vault1-description");
    vault1.setEstimate(PendingVault.Estimate.UNDER_10TB);
    vault1.setGrantEndDate(new Date());
    vault1.setName("vault1-name");
    vault1.setOwnerId("vault1-owner-id");
    vault1.setProjectSize(1234);
    vault1.setReviewDate(new Date());
    vault1.setUserID("vault1-user-id");
    vault1.setVaultCreatorId("vault1-creator-id");

    RoleModel roleModel1 = new RoleModel();
    roleModel1.setName("rm1-name");
    RoleAssignment ra1 = new RoleAssignment();
    ra1.setId(111L);
    ra1.setRole(roleModel1);
    ra1.setUserId("user-id-1");
    ra1.setSchoolId("school-id-1");
    ra1.setVaultId("vault-id-1");

    RoleModel roleModel2 = new RoleModel();
    roleModel2.setName("rm2-name");
    RoleAssignment ra2 = new RoleAssignment();
    ra2.setId(222L);
    ra2.setRole(roleModel2);
    ra2.setUserId("user-id-2");
    ra2.setSchoolId("school-id-2");
    ra2.setVaultId("vault-id-2");

    Group group = getGroup("group-id");
    group.setEnabled(true);
    group.setName("group-name");

    CreateRetentionPolicy createRetentionPolicy = new CreateRetentionPolicy();
    createRetentionPolicy.setName("crp-name");
    createRetentionPolicy.setId(111);
    createRetentionPolicy.setDescription("crp-description");

    modelMap.put("dataManagers", Arrays.asList(ra1, ra2));
    modelMap.put("vault", vault1);
    modelMap.put("vaultReviewModel", vrModel);
    modelMap.put("group", group);
    modelMap.put("createRetentionPolicy", createRetentionPolicy);

    String html = getHtml("admin/reviews/create.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Review");
  }


    @Test
  @WithMockUser(roles="USER")
  void testAdminRetentionPoliciesEdit() throws Exception {
    ModelMap modelMap = new ModelMap();

    RetentionPolicy policy1 = new RetentionPolicy();
    policy1.setId(111);
    policy1.setName("policy-one");
    policy1.setDescription("policy1-description");
    policy1.setUrl("https://info.org/retention-policy-1");
    policy1.setMinDataRetentionPeriod("123");
    policy1.setMinRetentionPeriod(123);
    policy1.setExtendUponRetrieval(true);
    policy1.setInEffectDate(new Date());
    policy1.setEndDate(new Date());
    policy1.setDataGuidanceReviewed(new Date());

    modelMap.put("retentionPolicy", policy1);

    String html = getHtml("admin/retentionpolicies/edit.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Edit Retention Policy");

  }

  @Test
  @WithMockUser(roles="USER")
  void testAdminRetentionPoliciesAdd() throws Exception {
    ModelMap modelMap = new ModelMap();

    RetentionPolicy policy1 = new RetentionPolicy();
    policy1.setId(111);
    policy1.setName("policy-one");
    policy1.setDescription("policy1-description");
    policy1.setUrl("https://info.org/retention-policy-1");
    policy1.setMinDataRetentionPeriod("123");
    policy1.setMinRetentionPeriod(123);
    policy1.setExtendUponRetrieval(true);
    policy1.setInEffectDate(new Date());
    policy1.setEndDate(new Date());
    policy1.setDataGuidanceReviewed(new Date());

    modelMap.put("retentionPolicy", policy1);

    String html = getHtml("admin/retentionpolicies/add.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Add Retention Policy");
  }
  @Test
  @WithMockUser(roles="USER")
  void testAdminRetrievesIndex() throws Exception {
    ModelMap modelMap = new ModelMap();

    Retrieve ret1 = getRetrieve("ret-id-1");
    ret1.setStatus(Retrieve.Status.IN_PROGRESS);
    ret1.setDeposit(getDeposit("deposit-id-1"));
    ret1.setTimestamp(new Date());
    ret1.setNote("note-1");
    ret1.setRetrievePath("/a/b/c");

    Retrieve ret2 = getRetrieve("ret-id-2");
    ret2.setStatus(Retrieve.Status.COMPLETE);
    ret2.setDeposit(getDeposit("deposit-id-2"));
    ret2.setTimestamp(new Date());
    ret2.setNote("note-2");
    ret2.setRetrievePath("/d/e/f");

    modelMap.put("retrieves", Arrays.asList(ret1,ret2));

    String html = getHtml("admin/retrieves/index.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Retrievals");
  }

  @Test
  @WithMockUser(roles="USER")
  void testAdminRolesIndex() throws Exception {
    ModelMap modelMap = new ModelMap();

    RoleModel roleModel1 = new RoleModel();
    roleModel1.setId(1111L);
    roleModel1.setAssignedUserCount(111);
    roleModel1.setStatus("Status-1");
    roleModel1.setName("rm1-name");
    RoleAssignment ra1 = new RoleAssignment();
    ra1.setId(111L);
    ra1.setRole(roleModel1);
    ra1.setUserId("user-id-1");
    ra1.setSchoolId("school-id-1");
    ra1.setVaultId("vault-id-1");

    RoleModel roleModel2 = new RoleModel();
    roleModel2.setId(2222L);
    roleModel2.setAssignedUserCount(222);
    roleModel2.setStatus("Status-2");
    roleModel2.setName("rm2-name");
    RoleAssignment ra2 = new RoleAssignment();
    ra2.setId(222L);
    ra2.setRole(roleModel2);
    ra2.setUserId("user-id-2");
    ra2.setSchoolId("school-id-2");
    ra2.setVaultId("vault-id-2");

    RoleModel roRoleModel1 = new RoleModel();
    roRoleModel1.setName("ro-rm1-name");
    roRoleModel1.setAssignedUserCount(123);
    roRoleModel1.setStatus("STATUS-1");
    RoleAssignment ra3 = new RoleAssignment();
    ra3.setId(333L);
    ra3.setRole(roRoleModel1);
    ra3.setUserId("ro-user-id-1");
    ra3.setSchoolId("ro-school-id-1");
    ra3.setVaultId("ro-vault-id-1");

    RoleModel roRoleModel2 = new RoleModel();
    roRoleModel2.setName("ro-rm2-name");
    roRoleModel2.setAssignedUserCount(234);
    roRoleModel2.setStatus("STATUS-2");
    RoleAssignment ra4 = new RoleAssignment();
    ra4.setId(444L);
    ra4.setRole(roRoleModel2);
    ra4.setUserId("ro-user-id-2");
    ra4.setSchoolId("ro-school-id-2");
    ra4.setVaultId("ro-vault-id-2");

    RoleModel superAdminRoleModel = new RoleModel();
    superAdminRoleModel.setAssignedUserCount(999);
    superAdminRoleModel.setStatus("STATUS-RED");
    superAdminRoleModel.setName("sa-name");
    superAdminRoleModel.setStatus("STATUS-RED");
    RoleAssignment ra5 = new RoleAssignment();
    ra5.setId(555L);
    ra5.setRole(superAdminRoleModel);
    ra5.setUserId("sa-user-id");
    ra5.setSchoolId("sa-school-id");
    ra5.setVaultId("sa-vault-id");

    modelMap.put("superAdminRole", superAdminRoleModel);
    modelMap.put("roles", Arrays.asList(roleModel1, roleModel2));
    modelMap.put("readOnlyRoles", Arrays.asList(roRoleModel1, roRoleModel2));
    modelMap.put("isSuperAdmin", true);
    String html = getHtml("admin/roles/index.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Roles");
  }

  @Test
  @WithMockUser(roles="USER")
  void testAdminSchoolsIndex() throws Exception {
    ModelMap modelMap = new ModelMap();

    Group group1 = getGroup("school-id-1");
    group1.setName("school-name-1");
    group1.setEnabled(true);

    Group group2 = getGroup("school-id-2");
    group2.setName("school-name-2");
    group2.setEnabled(true);

    modelMap.put("schools", Arrays.asList(group1, group2));
    String html = getHtml("admin/schools/index.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Schools");
  }

  @Test
  @WithMockUser(roles="USER")
  void testAdminSchoolsRoles() throws Exception {
    ModelMap modelMap = new ModelMap();

    Group group1 = getGroup("school-id-1");
    group1.setName("school-name-1");
    group1.setEnabled(true);

    RoleModel roleModel1 = new RoleModel();
    roleModel1.setId(1111L);
    roleModel1.setAssignedUserCount(111);
    roleModel1.setStatus("Status-1");
    roleModel1.setName("rm1-name");
    RoleAssignment ra1 = new RoleAssignment();
    ra1.setId(111L);
    ra1.setRole(roleModel1);
    ra1.setUserId("user-id-1");
    ra1.setSchoolId("school-id-1");
    ra1.setVaultId("vault-id-1");

    RoleModel roleModel2 = new RoleModel();
    roleModel2.setId(2222L);
    roleModel2.setAssignedUserCount(222);
    roleModel2.setStatus("Status-2");
    roleModel2.setName("rm2-name");
    RoleAssignment ra2 = new RoleAssignment();
    ra2.setId(222L);
    ra2.setRole(roleModel2);
    ra2.setUserId("user-id-2");
    ra2.setSchoolId("school-id-2");
    ra2.setVaultId("vault-id-2");

    modelMap.put("school", group1);
    modelMap.put("roles", Arrays.asList(roleModel1, roleModel2));
    modelMap.put("roleAssignments", Arrays.asList(ra1, ra2));
    modelMap.put("canManageSchoolRoleAssignments", true);

    String html = getHtml("admin/schools/schoolRoles.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - School Roles");
  }

  @Test
  @WithMockUser(roles="USER")
  void testAdminUsersCreate() throws Exception {
    ModelMap modelMap = new ModelMap();
    User user1 = getUser("user-id-1");
    user1.setFirstname("user1-first");
    user1.setLastname("user1-last");
    user1.setPassword("XXXX");
    user1.setProperties(new HashMap<String,String>(){{
        put("prop-1","value-1");
        put("prop-2","value-2");
    }});
    user1.setEmail("user.one@example.com");
    user1.setID("user-id-111");

    modelMap.put("user", user1);

    String html = getHtml("admin/users/create.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Create User");
  }

  @Test
  @WithMockUser(roles="USER")
  void testAdminUsersEdit() throws Exception {
    ModelMap modelMap = new ModelMap();
    User user1 = getUser("user-id-1");
    user1.setFirstname("user1-first");
    user1.setLastname("user1-last");
    user1.setPassword("XXXX");
    user1.setProperties(new HashMap<String,String>(){{
      put("prop-1","value-1");
      put("prop-2","value-2");
    }});
    user1.setEmail("user.one@example.com");
    user1.setID("user-id-111");

    modelMap.put("user", user1);

    String html = getHtml("admin/users/edit.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Edit Profile");
  }

  @Test
  @WithMockUser(roles="USER")
  void testAdminUsersIndex() throws Exception {
    ModelMap modelMap = new ModelMap();
    User user1 = getUser("user-id-1");
    user1.setFirstname("user1-first");
    user1.setLastname("user1-last");
    user1.setPassword("XXXX");
    user1.setEmail("user.one@example.com");
    user1.setProperties(new HashMap<String,String>(){{
      put("prop-A","value-1");
      put("prop-B","value-2");
    }});

    User user2 = getUser("user-id-2");
    user2.setFirstname("user2-first");
    user2.setLastname("user2-last");
    user2.setPassword("XXXX");
    user2.setEmail("user.two@example.com");
    user2.setProperties(new HashMap<String,String>(){{
      put("prop-C","value-3");
      put("prop-D","value-4");
    }});

    modelMap.put("users", Arrays.asList(user1,user2));
    modelMap.put("query", "admin users");

    String html = getHtml("admin/users/index.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Users");
  }

  @Test
  @WithMockUser(roles="USER")
  void testAdminVaultsIndex() throws Exception {
    ModelMap modelMap = new ModelMap();

    VaultInfo vault1 = getVaultInfo("vault-info-1");
    vault1.setAffirmed(true);
    vault1.setAuthoriser("vault1-authoriser");
    vault1.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING);
    vault1.setConfirmed(true);
    vault1.setContact("vault1-contact");
    vault1.setCreationTime(new Date());
    vault1.setDataCreators(Arrays.asList("Tom","Dick","Harry"));
    vault1.setDescription("vault1-description");
    vault1.setEstimate(PendingVault.Estimate.UNDER_10TB);
    vault1.setGrantEndDate(new Date());
    vault1.setName("vault1-name");
    vault1.setOwnerId("vault1-owner-id");
    vault1.setProjectSize(1234);
    vault1.setReviewDate(new Date());
    vault1.setUserID("vault1-user-id");
    vault1.setVaultCreatorId("vault1-creator-id");

    VaultInfo vault2 = getVaultInfo("vault-info-2");
    vault2.setAffirmed(true);
    vault2.setAuthoriser("vault2-authoriser");
    vault2.setBillingType(PendingVault.Billing_Type.FEEWAIVER);
    vault2.setConfirmed(true);
    vault2.setContact("vault2-contact");
    vault2.setCreationTime(new Date());
    vault2.setDataCreators(Arrays.asList("Geddy","Neil","Alex"));
    vault2.setDescription("vault2-description");
    vault2.setEstimate(PendingVault.Estimate.OVER_10TB);
    vault2.setGrantEndDate(new Date());
    vault2.setName("vault2-name");
    vault2.setOwnerId("vault2-owner-id");
    vault2.setProjectSize(2345);
    vault2.setReviewDate(new Date());
    vault2.setUserID("vault2-user-id");
    vault2.setVaultCreatorId("vault2-creator-id");

    modelMap.put("recordsInfo","(RECORDS INFO)");
    modelMap.put("numberOfPages", 1);
    modelMap.put("activePageId", 1);
    modelMap.put("query","edinburgh university");
    modelMap.put("sort","name");
    modelMap.put("order","ORDER");
    modelMap.put("orderCrisId","cris-id-1");
    modelMap.put("ordervaultsize","orderVaultSize1");
    modelMap.put("orderuser","orderuser1");
    modelMap.put("ordername","bob");
    modelMap.put("orderGroupId","orderGroupId1");
    modelMap.put("orderreviewDate","orderReviewDate1");
    modelMap.put("ordercreationtime","orderCreationTime1");
    modelMap.put("vaults", Arrays.asList(vault1,vault2));

    String html = getHtml("admin/vaults/index.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Vaults");
  }
  @Test
  @WithMockUser(roles="USER")
  void testAdminVaultsVault() throws Exception {
    ModelMap modelMap = new ModelMap();

    VaultInfo vault1 = getVaultInfo("vault-info-1");
    vault1.setAffirmed(true);
    vault1.setAuthoriser("vault1-authoriser");
    vault1.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING);
    vault1.setConfirmed(true);
    vault1.setContact("vault1-contact");
    vault1.setCreationTime(new Date());
    vault1.setDataCreators(Arrays.asList("Tom","Dick","Harry"));
    vault1.setDescription("vault1-description");
    vault1.setEstimate(PendingVault.Estimate.UNDER_10TB);
    vault1.setGrantEndDate(new Date());
    vault1.setName("vault1-name");
    vault1.setOwnerId("vault1-owner-id");
    vault1.setProjectSize(1234);
    vault1.setReviewDate(new Date());
    vault1.setUserID("vault1-user-id");
    vault1.setVaultCreatorId("vault1-creator-id");

    Deposit deposit1 = getDeposit("dep-id-001");

    DepositChunk dc1 = getDepositChunk("dc-001");
    DepositChunk dc2 = getDepositChunk("dc-002");
    dc1.setDeposit(deposit1);
    dc2.setDeposit(deposit1);
    deposit1.setDepositChunks(Arrays.asList(dc1, dc2));

    Deposit deposit2 = getDeposit("dep-id-002");
    DepositChunk dc3 = getDepositChunk("dc-003");
    DepositChunk dc4 = getDepositChunk("dc-004");
    dc3.setDeposit(deposit2);
    dc4.setDeposit(deposit2);
    deposit2.setDepositChunks(Arrays.asList(dc3, dc4));

    dc1.setChunkNum(1);

    dc2.setChunkNum(2);

    dc3.setChunkNum(3);
    dc4.setChunkNum(4);

    deposit1.setName("deposit-one");
    deposit2.setName("deposit-two");

    DepositInfo info1 = getDepositInfo("dep-id-001");
    info1.setName("name1");
    info1.setStatus(Deposit.Status.IN_PROGRESS);
    info1.setDepositChunks(deposit1.getDepositChunks());
    info1.setDatasetID("dataset-id-1");
    info1.setVaultID("vaultID123");
    info1.setVaultName("vaultName1");
    info1.setVaultOwnerID("owner1");
    info1.setGroupID("group1");
    info1.setGroupName("groupName1");
    info1.setUserName("user1");
    info1.setCrisID("CRIS-ID-1");
    info1.setVaultReviewDate(new Date().toString());
    info1.setDescription("DepositOneDescription");
    info1.setCreationTime(new Date());
    info1.setDepositPaths(Collections.emptyList());
    info1.setDepositSize(1234);
    info1.setPersonalDataStatement("personalDataStatement1");
    info1.setShortFilePath("short-file-path-1");
    info1.setHasPersonalData(true);

    DepositInfo info2 = getDepositInfo("dep-id-002");
    info2.setName("name2");
    info2.setStatus(Deposit.Status.COMPLETE);
    info2.setDepositChunks(deposit1.getDepositChunks());
    info2.setDatasetID("dataset-id-2");
    info2.setVaultID("vaultID234");
    info2.setVaultName("vaultName2");
    info2.setVaultOwnerID("owner2");
    info2.setGroupID("group2");
    info2.setGroupName("groupName2");
    info2.setUserName("user2");
    info2.setCrisID("CRIS-ID-2");
    info2.setVaultReviewDate(new Date().toString());
    info2.setDescription("DepositTwoDescription");
    info2.setCreationTime(new Date());
    info2.setDepositPaths(Collections.emptyList());
    info2.setDepositSize(4567);
    info2.setPersonalDataStatement("personalDataStatement2");
    info2.setShortFilePath("short-file-path-2");
    info2.setHasPersonalData(true);

    CreateRetentionPolicy createRetentionPolicy = new CreateRetentionPolicy();
    createRetentionPolicy.setDescription("crp-desc");
    createRetentionPolicy.setId(123);
    createRetentionPolicy.setName("crp-name");
    createRetentionPolicy.setEndDate(new Date().toString());

    Group group = getGroup("group-id-1");
    group.setName("group-name-1");
    group.setEnabled(true);

    modelMap.put("vault", vault1);
    modelMap.put("deposits", Arrays.asList(info1, info2));
    modelMap.put("retentionPolicy", createRetentionPolicy);
    modelMap.put("group", group);

    String html = getHtml("admin/vaults/vault.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin - Vault");
  }
  @Test
  @WithMockUser(roles="USER")
  void testAdminIndex() throws Exception {
      ModelMap modelMap = new ModelMap();
      modelMap.addAttribute("canViewVaultsSize", true);
      modelMap.addAttribute("vaultsize", 123_456_789);
      modelMap.addAttribute("canViewInProgress", true);
      modelMap.addAttribute("depositsinprogress", 101);
      modelMap.addAttribute("retrievesinprogress", 102);
      modelMap.addAttribute("canViewQueues", true);
      modelMap.addAttribute("depositqueue", 103);
      modelMap.addAttribute("retrievequeue", 104);
      modelMap.addAttribute("canManageVaults", true);
      modelMap.addAttribute("vaultcount", 105);
      modelMap.addAttribute("canManagePendingVaults", true);
      modelMap.addAttribute("pendingvaultcount", 106);
      modelMap.addAttribute("canManageDeposits", true);
      modelMap.addAttribute("depositcount", 107);
      modelMap.addAttribute("canViewRetrieves", true);
      modelMap.addAttribute("retrievecount", 108);
      modelMap.addAttribute("canManageReviews", true);
      modelMap.addAttribute("reviewcount", 109);
      modelMap.addAttribute("canManageBillingDetails", true);
      modelMap.addAttribute("canManageSchoolUsers", true);
      modelMap.addAttribute("groupcount", 110);
      modelMap.addAttribute("canManageRoles", true);
      modelMap.addAttribute("rolecount", 111);
      modelMap.addAttribute("canViewEvents", true);
      modelMap.addAttribute("eventcount", 112);
      modelMap.addAttribute("canManageRetentionPolicies", true);
      modelMap.addAttribute("policycount", 113);
      modelMap.addAttribute("canManageArchiveStores", true);
      modelMap.addAttribute("archivestorescount", 114);

    String html = getHtml("admin/index.html", modelMap);
    System.out.println(html);
    Document doc = Jsoup.parse(html);

    //check title
    List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
    String title = titles.get(0).text();
    assertThat(title).isEqualTo("Admin");

  }


  public String getHrefMatchedOnText(Document doc, String linkText){
    List<Element> items = doc.selectXpath("//a[contains(.,'"+linkText+"')]", Element.class);
    Element item = items.get(0);
    String href = item.attr("href");
    return href;
  }

  // SUPPORT METHODS BELOW HERE


    private Element lookupElement(Document doc, String xpath){
    List<Element> items = doc.selectXpath(xpath);
    Element item = items.get(0);
    return item;
  }
  private void checkTextInputFieldValue(Document doc, String name, String expectedValue){
    Element item = lookupElement(doc, "//input[@type='text'][@name = '" + name + "']");
    assertThat(item.attr("value")).isEqualTo(expectedValue);
  }
  private void checkTextAreaFieldValue(Document doc, String name, String expectedValue){
    Element item = lookupElement(doc, "//textarea[@name = '" + name + "']");
    assertThat(item.text()).isEqualTo(expectedValue);
  }

  @NotNull
  private static BillingInformation getInfo(Date now) {
    BillingInformation info = new BillingInformation();

    info.setAmountBilled(new BigDecimal("123.45"));
    info.setAmountToBeBilled(new BigDecimal("234.56"));
    info.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING);
    info.setBudgetCode(true);

    info.setContactName("James Bond");
    info.setCreationTime(now);

    info.setGrantEndDate(now);
    info.setId("billing-id-123");

    info.setPaymentDetails("some-payment-details");
    info.setProjectId("ID-123");
    info.setProjectSize(123);
    info.setProjectTitle("MY PROJECT TITLE");

    info.setReviewDate(now);
    info.setSchool("Informatics");
    info.setSliceID("SLICED-BACKHAND");
    info.setSpecialComments("My Special Comments");
    info.setSubUnit("sub-unit-123");

    info.setUserName("userOne");
    info.setVaultID("vault-id-123");
    info.setVaultName("vault-name");
    return info;
  }


  public ArchiveStore getArchiveStore(String id){
    return new ArchiveStore(){
      @Override
      public String getID() {
        return id;
      }
    };
  }
  public DepositChunk getDepositChunk(String id){
    return new DepositChunk(){
      @Override
      public String getID() {
        return id;
      }
    };
  }
  public Deposit getDeposit(String id){
    return new Deposit(){
      @Override
      public String getID() {
        return id;
      }
    };
  }
  public Audit getAudit(String id){
    return new Audit(){
      @Override
      public String getID() {
        return id;
      }
    };
  }
  public AuditInfo getAuditInfo(String id){
    return new AuditInfo(){
      @Override
      public String getId() {
        return id;
      }
    };
  }
  public AuditChunkStatusInfo getAuditChunkStatusInfo(String id){
    return new AuditChunkStatusInfo(){
      @Override
      public String getID() {
        return id;
      }
    };
  }
  private DepositInfo getDepositInfo(String id) {
    return new DepositInfo(){
      @Override
      public String getID() {
        return id;
      }
    };
  }

  private EventInfo getEventInfo(String id) {
    return new EventInfo(){
      @Override
      public String getId() {
        return id;
      }
    };
  }
  private VaultInfo getVaultInfo(String id) {
    return new VaultInfo(){
      @Override
      public String getID() {
        return id;
      }
    };
  }
  private Group getGroup(String id) {
    return new Group(){
      @Override
      public String getID() {
        return id;
      }
    };
  }
  private Retrieve getRetrieve(String id) {
    return new Retrieve(){
      @Override
      public String getID() {
        return id;
      }
    };
  }
  private User getUser(String id) {
    return new User(){
      @Override
      public String getID() {
        return id;
      }
    };
  }

}
