package org.datavaultplatform.webapp.app.config;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateDeposit;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.common.request.CreateVault;
import org.datavaultplatform.common.response.*;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;
import org.datavaultplatform.webapp.model.DepositReviewModel;
import org.datavaultplatform.webapp.model.VaultReviewHistoryModel;
import org.datavaultplatform.webapp.model.VaultReviewModel;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.View;
import org.thymeleaf.DialectConfiguration;
import org.thymeleaf.EngineConfiguration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolution;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

@WebMvcTest
@ProfileStandalone
public class ThymeleafConfigTest {

    public static final String HELLO_FIRST_LINE = "<!DOCTYPE html><!--test/hello.html-->";

    public static final String ERROR_FIRST_LINE = "<!DOCTYPE html><!--error/error.html-->";

    private final Date now = new Date();

    final Logger log = LoggerFactory.getLogger(getClass());

    @MockBean
    PermissionEvaluator mEvaluator;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private SpringResourceTemplateResolver templateResolver;

    @Autowired
    private ThymeleafViewResolver viewResolver;

    EngineConfiguration tlConfig;

    MockHttpServletRequest mRequest;

    MockHttpServletResponse mResponse;

    @BeforeEach
    void setup() {
        this.mRequest = new MockHttpServletRequest();
        this.mRequest.setContextPath("/dv");
        this.mRequest.setRequestURI("/dv/test");
        this.mResponse = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mRequest));
        assertThat(this.templateEngine.getConfiguration()).isInstanceOf(EngineConfiguration.class);
        tlConfig = (EngineConfiguration) this.templateEngine.getConfiguration();

        lenient().doAnswer(invocation -> true).when(mEvaluator).hasPermission(any(), any(), any(), any());
        lenient().doAnswer(invocation -> true).when(mEvaluator).hasPermission(any(), any(), any());
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
        assertThat(dialectNames).containsAll(List.of("SpringSecurity", "Layout", "SpringStandard"));
        log.info(tlConfig.toString());
        assertThat(tlConfig.getTemplateResolvers()).hasSize(1);
        ITemplateResolver firstTemplateResolver = tlConfig.getTemplateResolvers().iterator().next();
        assertThat(this.templateResolver).isSameAs(firstTemplateResolver);
    }

    @Test
    void testTemplateResolver() {
        assertThat(this.templateResolver.getCheckExistence()).isTrue();
        assertThat(this.templateResolver.getPrefix()).isEqualTo("classpath:/WEB-INF/templates/");
        assertThat(this.templateResolver.getSuffix()).isEqualTo(".html");
        assertThat(this.templateResolver.getCharacterEncoding()).isEqualTo("UTF-8");
        assertThat(this.templateResolver.isCacheable()).isTrue();

        String template = "test/hello";
        TemplateResolution templateResolution = templateResolver.resolveTemplate(this.tlConfig, template, template, Collections.emptyMap());
        assertThat(templateResolution.getTemplateResource().exists()).isTrue();
        assertThat(templateResolution.isTemplateResourceExistenceVerified()).isTrue();
        assertThat(templateResolution.getTemplateResource().getDescription()).isEqualTo("class path resource [WEB-INF/templates/test/hello.html]");
        System.out.println(templateResolution);
    }

    @Test
    void test00TestHello() throws Exception {
        ClassPathResource helloResource = new ClassPathResource("WEB-INF/templates/test/hello.html");
        assertEquals(HELLO_FIRST_LINE, getFirstLine(helloResource));
        ModelMap modelMap = new ModelMap();
        modelMap.put("name", "user101");
        String helloTemplateHtml = getHtml("test/hello", modelMap);
        assertEquals(HELLO_FIRST_LINE, getFirstLine(helloTemplateHtml));
        Document doc = Jsoup.parse(helloTemplateHtml);
        checkTitle(doc, "Hello user101!");
    }

    @Test
    void test01AdminArchiveStoresIndex() throws Exception {
        ModelMap modelMap = new ModelMap();

        ArchiveStore store1 = getArchiveStore("id-001");
        store1.setLabel("LABEL 1");
        store1.setStorageClass(TivoliStorageManager.class.getName());
        store1.getProperties().put("prop1-1", "value1-1");
        store1.getProperties().put("prop1-2", "value1-2");

        ArchiveStore store2 = getArchiveStore("id-002");
        store2.setLabel("LABEL 2");
        store2.setStorageClass(TivoliStorageManager.class.getName());
        store2.getProperties().put("prop2-1", "value2-1");
        store2.getProperties().put("prop2-2", "value2-2");

        modelMap.put("archivestores", List.of(store1, store2));
        String html = getHtml("admin/archivestores/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Archive Stores");
        outputHtml(html);
    }

    @Test
        //TODO this one is quite hard to setup - complex map structure
    void test02AdminAuditsDeposits() throws Exception {
        ModelMap modelMap = new ModelMap();
        modelMap.put("deposits", Collections.emptyList());
        String html = getHtml("admin/audits/deposits", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin Audits Deposits");
        outputHtml(html);
    }

    @Test
    void test03AdminAuditsIndex() throws Exception {
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
        info1.setCreationTime(now);
        info1.setCompletedTime(now);
        info1.setDeposit(deposit);
        info1.setNote("note-1");
        info1.setDepositChunk(dc1);

        info2.setStatus(AuditChunkStatus.Status.IN_PROGRESS);
        info2.setArchiveId("arch-id-2");
        info2.setCreationTime(now);
        info2.setCompletedTime(now);
        info2.setDeposit(deposit);
        info2.setNote("note-2");
        info2.setDepositChunk(dc2);

        audit1.setAuditChunks(List.of(info1, info2));
        audit2.setAuditChunks(List.of(info1, info2));

        modelMap.put("audits", List.of(audit1, audit2));

        String html = getHtml("admin/audits/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Audits");
        outputHtml(html);
    }

    @Test
    void test04AdminBillingBillingDetails() throws Exception {
        ModelMap modelMap = new ModelMap();
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetails", modelMap);
        Document doc = Jsoup.parse(html);


        checkTextInputFieldValue(doc, "contactName", "James Bond");
        checkTextInputFieldValue(doc, "school", "Informatics");
        checkTextInputFieldValue(doc, "subUnit", "sub-unit-123");
        checkTextInputFieldValue(doc, "projectId", "ID-123");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details");
        outputHtml(html);
    }

    @Test
    void test05AdminBillingBillingDetailsBudget() throws Exception {
        
        ModelMap modelMap = new ModelMap();
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsBudget", modelMap);
        Document doc = Jsoup.parse(html);


        checkTextInputFieldValue(doc, "projectId", "ID-123");
        checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details (Budget)");
        outputHtml(html);
    }


    @Test
    void test06AdminBillingBillingDetailsBuyNewSlice() throws Exception {
        
        ModelMap modelMap = new ModelMap();
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsBuyNewSlice", modelMap);
        Document doc = Jsoup.parse(html);

        checkTextInputFieldValue(doc, "projectId", "ID-123");
        checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details (SLICE)");
        outputHtml(html);
    }

    @Test
    void test07AdminBillingBillingDetailsBuyFeeWaiver() throws Exception {
        
        ModelMap modelMap = new ModelMap();
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsFeeWaiver", modelMap);
        Document doc = Jsoup.parse(html);


        checkTextInputFieldValue(doc, "projectId", "ID-123");
        checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details (Funding: NO or Don't Know)");
        outputHtml(html);
    }

    @Test
    void test08AdminBillingBillingDetailsFundingNoDoNotKnow() throws Exception {
        
        ModelMap modelMap = new ModelMap();
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsFundingNoDoNotKNow", modelMap);
        Document doc = Jsoup.parse(html);


        checkTextInputFieldValue(doc, "projectId", "ID-123");
        checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details (FUNDING: NO OR DO NOT KNOW)");
        outputHtml(html);
    }


    @Test
    void test09AdminBillingBillingDetailsGrant() throws Exception {
        
        ModelMap modelMap = new ModelMap();
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsGrant", modelMap);
        Document doc = Jsoup.parse(html);


        checkTextInputFieldValue(doc, "contactName", "James Bond");
        checkTextInputFieldValue(doc, "school", "Informatics");
        checkTextInputFieldValue(doc, "subUnit", "sub-unit-123");
        checkTextInputFieldValue(doc, "projectTitle", "MY PROJECT TITLE");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details (Grant)");
        outputHtml(html);
    }

    @Test
    void test10AdminBillingBillingDetailsNA() throws Exception {
        ModelMap modelMap = new ModelMap();
        
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsNA", modelMap);
        Document doc = Jsoup.parse(html);


        checkTextInputFieldValue(doc, "projectId", "ID-123");
        checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details (NA)");
        outputHtml(html);
    }

    @Test
    void test11AdminBillingDetailsSlice() throws Exception {
        ModelMap modelMap = new ModelMap();
        
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsSlice", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Billing Details (SLICE)");
        outputHtml(html);
    }

    @Test
    void test12AdminBillingBillingDetailsWillPay() throws Exception {
        ModelMap modelMap = new ModelMap();
        
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsWilLPay", modelMap);
        Document doc = Jsoup.parse(html);


        checkTextInputFieldValue(doc, "projectId", "ID-123");
        checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details (WILL PAY)");
        outputHtml(html);
    }

    @Test
    void test13BillingDetailsIndex() throws Exception {
        ModelMap modelMap = new ModelMap();
        
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Billing Details");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void test14AdminDepositsIndex() throws Exception {

        
        ModelMap modelMap = new ModelMap();

        Deposit deposit1 = getDeposit("dep-id-001");

        DepositChunk dc1 = getDepositChunk("dc-001");
        DepositChunk dc2 = getDepositChunk("dc-002");
        dc1.setDeposit(deposit1);
        dc2.setDeposit(deposit1);
        deposit1.setDepositChunks(List.of(dc1, dc2));

        Deposit deposit2 = getDeposit("dep-id-002");
        DepositChunk dc3 = getDepositChunk("dc-003");
        DepositChunk dc4 = getDepositChunk("dc-004");
        dc3.setDeposit(deposit2);
        dc4.setDeposit(deposit2);
        deposit2.setDepositChunks(List.of(dc3, dc4));

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
        info1.setVaultReviewDate(now.toString());
        info1.setDescription("DepositOneDescription");
        info1.setCreationTime(now);
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
        info2.setVaultReviewDate(now.toString());
        info2.setDescription("DepositTwoDescription");
        info2.setCreationTime(now);
        info2.setDepositPaths(Collections.emptyList());
        info2.setDepositSize(4567);
        info2.setPersonalDataStatement("personalDataStatement2");
        info2.setShortFilePath("short-file-path-2");
        info2.setHasPersonalData(true);
        modelMap.put("deposits", List.of(info1, info2));
        modelMap.put("totalRecords", 15);
        modelMap.put("totalPages", 1);
        modelMap.put("recordPerPage", 20);
        modelMap.put("offset", 0);
        modelMap.put("sort", "name");
        modelMap.put("order", "asc");
        modelMap.put("orderStatus", Deposit.Status.IN_PROGRESS.name());
        modelMap.put("orderName", "order123");
        modelMap.put("page", 1);
        modelMap.put("orderDepositSize", 123);
        modelMap.put("orderCreationTime", "2023-12-31");
        modelMap.put("query", "edinburgh university");
        modelMap.put("orderUserID", "user123");
        modelMap.put("orderId", "order123");
        modelMap.put("orderVaultId", "orderVaultIdABC");

        String html = getHtml("admin/deposits/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Deposits");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void test15AdminEventsIndex() throws Exception {
        ModelMap modelMap = new ModelMap();

        EventInfo event1 = getEventInfo1();

        EventInfo event2 = getEventInfo2();

        modelMap.put("events", List.of(event1, event2));
        String html = getHtml("admin/events/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Events");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "IS_ADMIN")
    void test16AdminPendingVaultsEditPendingVault() throws Exception {
        ModelMap modelMap = new ModelMap();

        CreateVault vault = getCreateVault();
        RetentionPolicy policy1 = getRetentionPolicy1();
        RetentionPolicy policy2 = getRetentionPolicy2();

        modelMap.put("errors", List.of("error1", "error2"));
        modelMap.put("vault", vault);
        modelMap.put("vaultID", "vault-id-123");
        modelMap.put("policies", List.of(policy1, policy2));

        String html = getHtml("admin/pendingVaults/edit/editPendingVault", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Edit Pending Vault");
        outputHtml(html);
    }


    @Test
    @WithMockUser(roles = "MANAGER")
    void test17AdminPendingVaultsConfirmed() throws Exception {
        ModelMap modelMap = new ModelMap();

        VaultInfo vault1 = getVaultInfo1();
        VaultInfo vault2 = getVaultInfo2();

        modelMap.put("recordsInfo", "(RECORDS INFO)");
        modelMap.put("numberOfPages", 1);
        modelMap.put("activePageId", 1);
        modelMap.put("query", "edinburgh university");
        modelMap.put("sort", "name");
        modelMap.put("order", "ORDER");
        modelMap.put("ordername", "bob");
        modelMap.put("pendingVaults", List.of(vault1, vault2));

        String html = getHtml("admin/pendingVaults/confirmed", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Confirmed Pending Vaults");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void test18AdminPendingVaultsIndex() throws Exception {
        ModelMap modelMap = new ModelMap();


        modelMap.put("savedVaultsTotal", 123);
        modelMap.put("confirmedVaultsTotal", 100);

        String html = getHtml("admin/pendingVaults/index", modelMap);
        Document doc = Jsoup.parse(html);


        String hrefAdministration = getHrefMatchedOnText(doc, "Administration");
        assertThat(hrefAdministration).isEqualTo("/dv/admin/");

        String hrefConfirmed = getHrefMatchedOnText(doc, "Confirmed");
        assertThat(hrefConfirmed).isEqualTo("/dv/admin/pendingVaults/confirmed/");

        String hrefSaved = getHrefMatchedOnText(doc, "Saved");
        assertThat(hrefSaved).isEqualTo("/dv/admin/pendingVaults/saved/");

        //check title
        checkTitle(doc, "Admin - Pending Vaults");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void test19AdminPendingVaultsSaved() throws Exception {
        ModelMap modelMap = new ModelMap();

        VaultInfo vault1 = getVaultInfo1();

        VaultInfo vault2 = getVaultInfo2();

        modelMap.put("recordsInfo", "(RECORDS INFO)");
        modelMap.put("numberOfPages", 1);
        modelMap.put("activePageId", 1);
        modelMap.put("query", "edinburgh university");
        modelMap.put("sort", "name");
        modelMap.put("order", "ORDER");
        modelMap.put("ordername", "bob");
        modelMap.put("pendingVaults", List.of(vault1, vault2));

        String html = getHtml("admin/pendingVaults/saved", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Saved Pending Vaults");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void test20AdminPendingVaultsSummary() throws Exception {
        ModelMap modelMap = new ModelMap();

        VaultInfo vault1 = getVaultInfo1();

        CreateRetentionPolicy createRetentionPolicy = new CreateRetentionPolicy();
        createRetentionPolicy.setDescription("crp-desc");
        createRetentionPolicy.setId(123);
        createRetentionPolicy.setName("crp-name");
        createRetentionPolicy.setEndDate(now.toString());

        Group group = getGroup("group-id-1");
        group.setName("group-name-1");
        group.setEnabled(true);

        modelMap.put("pendingVault", vault1);
        modelMap.put("createRetentionPolicy", createRetentionPolicy);
        modelMap.put("group", group);

        String html = getHtml("admin/pendingVaults/summary", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Pending Vault Summary");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test21AdminRetentionPoliciesAdd() throws Exception {
        ModelMap modelMap = new ModelMap();

        RetentionPolicy policy1 = getRetentionPolicy1();

        modelMap.put("retentionPolicy", policy1);

        String html = getHtml("admin/retentionpolicies/add", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Add Retention Policy");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test22AdminRetentionPoliciesEdit() throws Exception {
        ModelMap modelMap = new ModelMap();

        RetentionPolicy policy1 = getRetentionPolicy1();

        modelMap.put("retentionPolicy", policy1);

        String html = getHtml("admin/retentionpolicies/edit", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Edit Retention Policy");
        outputHtml(html);
    }


    @Test
    @WithMockUser(roles = "USER")
    void test23AdminRetentionPoliciesIndex() throws Exception {
        ModelMap modelMap = new ModelMap();

        RetentionPolicy policy1 = getRetentionPolicy1();
        RetentionPolicy policy2 = getRetentionPolicy2();

        modelMap.put("policies", List.of(policy1, policy2));

        String html = getHtml("admin/retentionpolicies/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Retention Policies");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test24AdminRetrievesIndex() throws Exception {
        ModelMap modelMap = new ModelMap();

        Retrieve ret1 = getRetrieve("ret-id-1");
        ret1.setStatus(Retrieve.Status.IN_PROGRESS);
        ret1.setDeposit(getDeposit("deposit-id-1"));
        ret1.setTimestamp(now);
        ret1.setNote("note-1");
        ret1.setRetrievePath("/a/b/c");

        Retrieve ret2 = getRetrieve("ret-id-2");
        ret2.setStatus(Retrieve.Status.COMPLETE);
        ret2.setDeposit(getDeposit("deposit-id-2"));
        ret2.setTimestamp(now);
        ret2.setNote("note-2");
        ret2.setRetrievePath("/d/e/f");

        modelMap.put("retrieves", List.of(ret1, ret2));

        String html = getHtml("admin/retrieves/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Retrievals");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test25AdminReviewsCreate() throws Exception {
        ModelMap modelMap = new ModelMap();

        VaultReviewModel vrModel = new VaultReviewModel();
        vrModel.setVaultReviewId("vault-review-id-1");
        vrModel.setNewReviewDate(now.toString());
        vrModel.setComment("comment-1");
        vrModel.setActionedDate(now);

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

        vrModel.setDepositReviewModels(List.of(drm1, drm2));

        VaultInfo vault1 = getVaultInfo1();

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

        modelMap.put("dataManagers", List.of(ra1, ra2));
        modelMap.put("vault", vault1);
        modelMap.put("vaultReviewModel", vrModel);
        modelMap.put("group", group);
        modelMap.put("createRetentionPolicy", createRetentionPolicy);

        String html = getHtml("admin/reviews/create", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Review");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test26AdminReviewsIndex() throws Exception {
        ModelMap modelMap = new ModelMap();

        VaultInfo vault1 = getVaultInfo1();
        VaultInfo vault2 = getVaultInfo2();

        modelMap.put("vaults", List.of(vault1, vault2));

        String html = getHtml("admin/reviews/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Reviews");
        outputHtml(html);
    }

    @Test
    void test27AdminIRolesAdminIndex() throws Exception {

        ModelMap modelMap = new ModelMap();

        String html = getHtml("admin/roles/isadmin/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - IS Admin");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test28AdminRolesIndex() throws Exception {
        ModelMap modelMap = new ModelMap();

        String html = getHtml("admin/roles/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Roles");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test29AdminSchoolsIndex() throws Exception {
        ModelMap modelMap = new ModelMap();

        Group group1 = getGroup("school-id-1");
        group1.setName("school-name-1");
        group1.setEnabled(true);

        Group group2 = getGroup("school-id-2");
        group2.setName("school-name-2");
        group2.setEnabled(true);

        modelMap.put("schools", List.of(group1, group2));
        String html = getHtml("admin/schools/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Schools");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test30AdminSchoolsRoles() throws Exception {
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
        modelMap.put("roles", List.of(roleModel1, roleModel2));
        modelMap.put("roleAssignments", List.of(ra1, ra2));
        modelMap.put("canManageSchoolRoleAssignments", true);

        String html = getHtml("admin/schools/schoolRoles", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - School Roles");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test31AdminUsersCreate() throws Exception {
        ModelMap modelMap = new ModelMap();
        User user1 = getUser("user-id-1");
        user1.setFirstname("user1-first");
        user1.setLastname("user1-last");
        user1.setPassword("XXXX");
        user1.setProperties(new HashMap<>() {{
            put("prop-1", "value-1");
            put("prop-2", "value-2");
        }});
        user1.setEmail("user.one@example.com");
        user1.setID("user-id-111");

        modelMap.put("user", user1);

        String html = getHtml("admin/users/create", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Create User");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test32AdminUsersEdit() throws Exception {
        ModelMap modelMap = new ModelMap();
        User user1 = getUser("user-id-1");
        user1.setFirstname("user1-first");
        user1.setLastname("user1-last");
        user1.setPassword("XXXX");
        user1.setProperties(new HashMap<>() {{
            put("prop-1", "value-1");
            put("prop-2", "value-2");
        }});
        user1.setEmail("user.one@example.com");
        user1.setID("user-id-111");

        modelMap.put("user", user1);

        String html = getHtml("admin/users/edit", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Edit Profile");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test33AdminUsersIndex() throws Exception {
        ModelMap modelMap = new ModelMap();
        User user1 = getUser("user-id-1");
        user1.setFirstname("user1-first");
        user1.setLastname("user1-last");
        user1.setPassword("XXXX");
        user1.setEmail("user.one@example.com");
        user1.setProperties(new HashMap<>() {{
            put("prop-A", "value-1");
            put("prop-B", "value-2");
        }});

        User user2 = getUser("user-id-2");
        user2.setFirstname("user2-first");
        user2.setLastname("user2-last");
        user2.setPassword("XXXX");
        user2.setEmail("user.two@example.com");
        user2.setProperties(new HashMap<>() {{
            put("prop-C", "value-3");
            put("prop-D", "value-4");
        }});

        modelMap.put("users", List.of(user1, user2));
        modelMap.put("query", "admin users");

        String html = getHtml("admin/users/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Users");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test34AdminVaultsIndex() throws Exception {
        ModelMap modelMap = new ModelMap();

        VaultInfo vault1 = getVaultInfo1();
        VaultInfo vault2 = getVaultInfo2();

        modelMap.put("recordsInfo", "(RECORDS INFO)");
        modelMap.put("numberOfPages", 1);
        modelMap.put("activePageId", 1);
        modelMap.put("query", "edinburgh university");
        modelMap.put("sort", "name");
        modelMap.put("order", "ORDER");
        modelMap.put("orderCrisId", "cris-id-1");
        modelMap.put("ordervaultsize", "orderVaultSize1");
        modelMap.put("orderuser", "orderuser1");
        modelMap.put("ordername", "bob");
        modelMap.put("orderGroupId", "orderGroupId1");
        modelMap.put("orderreviewDate", "orderReviewDate1");
        modelMap.put("ordercreationtime", "orderCreationTime1");
        modelMap.put("vaults", List.of(vault1, vault2));

        String html = getHtml("admin/vaults/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Vaults");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test35AdminVaultsVault() throws Exception {
        ModelMap modelMap = new ModelMap();

        VaultInfo vault1 = getVaultInfo1();

        Deposit deposit1 = getDeposit("dep-id-001");

        DepositChunk dc1 = getDepositChunk("dc-001");
        DepositChunk dc2 = getDepositChunk("dc-002");
        dc1.setDeposit(deposit1);
        dc2.setDeposit(deposit1);
        deposit1.setDepositChunks(List.of(dc1, dc2));

        Deposit deposit2 = getDeposit("dep-id-002");
        DepositChunk dc3 = getDepositChunk("dc-003");
        DepositChunk dc4 = getDepositChunk("dc-004");
        dc3.setDeposit(deposit2);
        dc4.setDeposit(deposit2);
        deposit2.setDepositChunks(List.of(dc3, dc4));

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
        info1.setVaultReviewDate(now.toString());
        info1.setDescription("DepositOneDescription");
        info1.setCreationTime(now);
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
        info2.setVaultReviewDate(now.toString());
        info2.setDescription("DepositTwoDescription");
        info2.setCreationTime(now);
        info2.setDepositPaths(Collections.emptyList());
        info2.setDepositSize(4567);
        info2.setPersonalDataStatement("personalDataStatement2");
        info2.setShortFilePath("short-file-path-2");
        info2.setHasPersonalData(true);

        CreateRetentionPolicy createRetentionPolicy = new CreateRetentionPolicy();
        createRetentionPolicy.setDescription("crp-desc");
        createRetentionPolicy.setId(123);
        createRetentionPolicy.setName("crp-name");
        createRetentionPolicy.setEndDate(now.toString());

        Group group = getGroup("group-id-1");
        group.setName("group-name-1");
        group.setEnabled(true);

        modelMap.put("vault", vault1);
        modelMap.put("deposits", List.of(info1, info2));
        modelMap.put("retentionPolicy", createRetentionPolicy);
        modelMap.put("group", group);

        String html = getHtml("admin/vaults/vault", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin - Vault");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test36dminIndex() throws Exception {
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

        String html = getHtml("admin/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Admin");
        outputHtml(html);
    }

    @Test
    void test37AuthConfirmation() throws Exception {
        ModelMap modelMap = new ModelMap();

        String html = getHtml("auth/confirmation", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Authentication - Confirmation");
        outputHtml(html);
    }

    @Test
    void test38AuthDenied() throws Exception {
        ModelMap modelMap = new ModelMap();

        String html = getHtml("auth/denied", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Authentication - Access Denied");
        outputHtml(html);
    }

    @Test
    void test39AuthLogin() throws Exception {
        ModelMap modelMap = new ModelMap();

        modelMap.put("welcome", "Welcome Message");
        modelMap.put("success", "Success Message");

        String html = getHtml("auth/login", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Authentication - Login");
        outputHtml(html);
    }

    @Test
    void test40DepositsCreate() throws Exception {
        ModelMap modelMap = new ModelMap();


        CreateDeposit deposit = new CreateDeposit();
        deposit.setFileUploadHandle("file-upload-handle-1");
        deposit.setName("deposit-name-1");
        deposit.setDepositPaths(List.of("path1", "path2", "path3"));
        deposit.setVaultID("vault-id-1");
        deposit.setDescription("deposit-description");
        deposit.setHasPersonalData("hasPersonalData1");
        deposit.setPersonalDataStatement("personalDataStatement1");

        VaultInfo vault1 = getVaultInfo1();

        modelMap.put("deposit", deposit);
        modelMap.put("vault", vault1);

        String html = getHtml("deposits/create", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Deposits - Create");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "IS_ADMIN")
    public void test41DepositsDeposit() throws Exception {

        ModelMap modelMap = new ModelMap();

        User user1 = getUser("user-id-1");

        EventInfo event1 = getEventInfo1();

        EventInfo event2 = getEventInfo2();

        Deposit deposit1 = getDeposit("dep-id-001");

        DepositChunk dc1 = getDepositChunk("dc-001");
        DepositChunk dc2 = getDepositChunk("dc-002");
        dc1.setDeposit(deposit1);
        dc2.setDeposit(deposit1);
        deposit1.setDepositChunks(List.of(dc1, dc2));
        deposit1.setStatus(Deposit.Status.COMPLETE);

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
        info1.setVaultReviewDate(now.toString());
        info1.setDescription("DepositOneDescription");
        info1.setCreationTime(now);
        info1.setDepositPaths(Collections.emptyList());
        info1.setDepositSize(1234);
        info1.setPersonalDataStatement("personalDataStatement1");
        info1.setShortFilePath("short-file-path-1");
        info1.setHasPersonalData(true);
        info1.setStatus(Deposit.Status.COMPLETE);

        VaultInfo vault1 = getVaultInfo1();

        Retrieve ret1 = getRetrieve("ret-id-1");
        ret1.setStatus(Retrieve.Status.IN_PROGRESS);
        ret1.setDeposit(getDeposit("deposit-id-1"));
        ret1.setTimestamp(now);
        ret1.setNote("note-1");
        ret1.setRetrievePath("/a/b/c");
        ret1.setUser(user1);
        ret1.setHasExternalRecipients(true);

        Retrieve ret2 = getRetrieve("ret-id-2");
        ret2.setStatus(Retrieve.Status.COMPLETE);
        ret2.setDeposit(getDeposit("deposit-id-2"));
        ret2.setTimestamp(now);
        ret2.setNote("note-2");
        ret2.setRetrievePath("/d/e/f");
        ret2.setUser(user1);
        ret2.setHasExternalRecipients(true);

        modelMap.put("vault", vault1);
        modelMap.put("deposit", info1);
        modelMap.put("events", List.of(event1, event2));
        modelMap.put("retrieves", List.of(ret1, ret2));

        String html = getHtml("deposits/deposit", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Deposits - Deposit");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "IS_ADMIN")
    public void test42DepositsRetrieve() throws Exception {

        ModelMap modelMap = new ModelMap();

        User user1 = getUser("user-id-1");

        Deposit deposit1 = getDeposit("dep-id-001");

        DepositChunk dc1 = getDepositChunk("dc-001");
        DepositChunk dc2 = getDepositChunk("dc-002");
        dc1.setDeposit(deposit1);
        dc2.setDeposit(deposit1);
        deposit1.setDepositChunks(List.of(dc1, dc2));
        deposit1.setStatus(Deposit.Status.COMPLETE);

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
        info1.setVaultReviewDate(now.toString());
        info1.setDescription("DepositOneDescription");
        info1.setCreationTime(now);
        info1.setDepositPaths(Collections.emptyList());
        info1.setDepositSize(1234);
        info1.setPersonalDataStatement("personalDataStatement1");
        info1.setShortFilePath("short-file-path-1");
        info1.setHasPersonalData(true);
        info1.setStatus(Deposit.Status.COMPLETE);

        VaultInfo vault1 = getVaultInfo1();

        Retrieve ret1 = getRetrieve("ret-id-1");
        ret1.setStatus(Retrieve.Status.IN_PROGRESS);
        ret1.setDeposit(getDeposit("deposit-id-1"));
        ret1.setTimestamp(now);
        ret1.setNote("note-1");
        ret1.setRetrievePath("/a/b/c");
        ret1.setUser(user1);
        ret1.setHasExternalRecipients(true);

        modelMap.put("vault", vault1);
        modelMap.put("deposit", info1);
        modelMap.put("retrieve", ret1);

        String html = getHtml("deposits/retrieve", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Deposits - Retrieve");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void test43ErrorError() throws Exception {
        ClassPathResource errorResource = new ClassPathResource("WEB-INF/templates/error/error.html");
        assertEquals(ERROR_FIRST_LINE, getFirstLine(errorResource));

        ModelMap modelMap = new ModelMap();
        modelMap.put("message", "This is a test error message");
        String errorTemplateHtml = getHtml("error/error", modelMap);
        //html is a mix of error 'page' and default template.
        assertThat(errorTemplateHtml).startsWith("<!DOCTYPE html><!--error/error.html-->\n<!--layout/defaultLayout.html-->");

        outputHtml(errorTemplateHtml);

        Document doc = Jsoup.parse(errorTemplateHtml);

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
        Comment secondComment = comments.get(1);
        assertThat(secondComment.getData()).contains("nav is (none)");

        //check error template fragment is placed at correct place within layout template
        List<Element> bodyDivs = doc.selectXpath("//div[@id='datavault-body']", Element.class);
        Element bodyDiv = bodyDivs.get(0);
        Element errorDiv = bodyDiv.child(0);
        assertThat(errorDiv.attr("id")).isEqualTo("error");

        //check the error message gets placed into html
        List<Element> errorMessages = doc.selectXpath("//span[@id='error-message']", Element.class);
        Element errorMessage = errorMessages.get(0);
        assertThat(errorMessage.text()).isEqualTo("This is a test error message");

        //check title
        checkTitle(doc, "Error Page");
        System.out.println(errorTemplateHtml);
    }


    @Test
    void test44FeedbackIndex() throws Exception {
        ModelMap modelMap = new ModelMap();

        String html = getHtml("feedback/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Feedback");
        outputHtml(html);
    }

    @Test
    void test45FeedbackSent() throws Exception {
        ModelMap modelMap = new ModelMap();

        String html = getHtml("feedback/sent", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Feedback - Sent");
        outputHtml(html);
    }

    @Test
    void test46FilestoresIndex() throws Exception {
        ModelMap modelMap = new ModelMap();

        User user = getUser("user-id-1");
        user.setFirstname("first");
        user.setLastname("last");
        user.setEmail("user.1@example.com");
        user.setPassword("XXXXX");

        modelMap.put("activeDir", "activeDir1");
        modelMap.put("sftpHost", "sftpHost2");
        modelMap.put("sftpPort", "sftpPort3");
        modelMap.put("sftpRootPath", "sftpRootPath4");

        FileStore fs1 = getFileStore("fs-id-1");
        fs1.setProperties(new HashMap<>() {{
            put("host", "host1");
            put("port", "port1");
            put("rootPath", "rootPath1");
            put("publicKey", "public-key-1");
        }});
        fs1.setLabel("fs-label-1");
        fs1.setStorageClass("storage-class-1");
        fs1.setUser(user);

        FileStore fs2 = getFileStore("fs-id-2");
        fs2.setProperties(new HashMap<>() {{
            put("host", "host2");
            put("port", "port2");
            put("rootPath", "rootPath2");
            put("publicKey", "public-key-2");
        }});
        fs2.setLabel("fs-label-2");
        fs2.setStorageClass("storage-class-2");
        fs2.setUser(user);

        modelMap.put("filestoresLocal", List.of(fs1, fs2));
        modelMap.put("filestoresSFTP", List.of(fs1, fs2));

        String html = getHtml("filestores/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Filestores");
        outputHtml(html);
    }

    @Test
    void test47GroupsIndex() throws Exception {
        ModelMap modelMap = new ModelMap();

        Group group1 = getGroup("group-id-1");
        group1.setName("group-name-1");
        group1.setEnabled(true);

        Group group2 = getGroup("group-id-2");
        group2.setName("group-name-2");
        group2.setEnabled(true);

        VaultInfo vault1 = getVaultInfo1();
        VaultInfo vault2 = getVaultInfo2();

        modelMap.put("vaults", List.of(vault1, vault2));
        modelMap.put("groups", List.of(group1, group2));

        String html = getHtml("groups/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Group Vaults");
        outputHtml(html);
    }

    @Test
    void test48HelpIndex() throws Exception {
        ModelMap modelMap = new ModelMap();

        modelMap.put("system", "system-01");
        modelMap.put("link", "link-01");

        String html = getHtml("help/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Help");
        outputHtml(html);
    }

    @Test
    void test49VaultsConfirmed() throws Exception {
        ModelMap modelMap = new ModelMap();

        String html = getHtml("vaults/confirmed", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Vaults - Confirmed");
        outputHtml(html);
    }

    @Test
    void test50VaultsCreate() throws Exception {
        ModelMap modelMap = new ModelMap();

        CreateVault vault = getCreateVault();

        Dataset dataset1 = getDataset1();
        Dataset dataset2 = getDataset2();

        RetentionPolicy retPol1 = getRetentionPolicy1();

        RetentionPolicy retPol2 = getRetentionPolicy2();

        modelMap.put("datasets", List.of(dataset1, dataset2));
        modelMap.put("vault", vault);
        modelMap.put("policies", List.of(retPol1, retPol2));//RetentionPolicy[]


        String html = getHtml("vaults/create", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Vaults - Create");
        outputHtml(html);
    }


    @Test
    @WithMockUser(roles = "IS_ADMIN")
    void test51VaultsIndex() throws Exception {
        ModelMap modelMap = new ModelMap();

        VaultInfo vault1 = getVaultInfo1();
        VaultInfo vault2 = getVaultInfo2();
        Dataset dataset1 = getDataset1();
        Dataset dataset2 = getDataset2();

        RetentionPolicy retPol1 = getRetentionPolicy1();

        RetentionPolicy retPol2 = getRetentionPolicy2();

        Group group1 = getGroup("group-id-1");
        group1.setName("group-name-1");
        group1.setEnabled(true);

        Group group2 = getGroup("group-id-2");
        group2.setName("group-name-2");
        group2.setEnabled(true);

        modelMap.addAttribute("vaults", List.of(vault1, vault2));//VaultInfo[]
        modelMap.addAttribute("pendingVaults", List.of(vault1, vault2));//VaultInfo[]

        // pass the view an empty Vault since the form expects it
        modelMap.addAttribute("vault", getCreateVault());

        modelMap.addAttribute("datasets", List.of(dataset1, dataset2));//Dataset[]

        modelMap.addAttribute("policies", List.of(retPol1, retPol2));//RetentionPolicy[]

        modelMap.addAttribute("groups", List.of(group1, group2));//Group[]

        modelMap.put("system", "system-01");
        modelMap.put("link", "link-01");

        modelMap.addAttribute("welcome", "WelcomeMessage01");


        String html = getHtml("vaults/index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Vaults");
        outputHtml(html);
    }

    @Test
    void test52VaultsNewCreatePrototype() throws Exception {
        ModelMap modelMap = new ModelMap();

        Group group1 = getGroup("group-id-1");
        group1.setName("group-name-1");
        group1.setEnabled(true);

        Group group2 = getGroup("group-id-2");
        group2.setName("group-name-2");
        group2.setEnabled(true);

        modelMap.put("groups", List.of(group1, group2));
        modelMap.put("vault", getCreateVault());
        modelMap.put("errors", List.of("error1", "error2"));
        RetentionPolicy retPol1 = getRetentionPolicy1();
        RetentionPolicy retPol2 = getRetentionPolicy2();
        modelMap.put("errors", List.of(retPol1, retPol2));
        modelMap.put("loggedInAs", "loggedInAs1");

        String html = getHtml("vaults/newCreatePrototype", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Vaults - Create");
        outputHtml(html);
    }


    @Test
    void test53VaultsUserVaults() throws Exception {
        ModelMap modelMap = new ModelMap();

        VaultInfo vault1 = getVaultInfo1();
        VaultInfo vault2 = getVaultInfo2();
        modelMap.put("vaults", List.of(vault1, vault2));

        String html = getHtml("vaults/userVaults", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Vaults - User Vaults");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "IS_ADMIN")
    void test54VaultsVault() throws Exception {

        ModelMap modelMap = new ModelMap();
        RoleModel roleModel1 = new RoleModel();
        RoleModel roleModel2 = new RoleModel();

        RoleAssignment roleAssignment1 = new RoleAssignment();
        RoleAssignment roleAssignment2 = new RoleAssignment();
        CreateRetentionPolicy retentionPolicy = new CreateRetentionPolicy();
        Group group = new Group();

        modelMap.addAttribute("vault", getVaultInfo1());
        modelMap.addAttribute("roles", List.of(roleModel1, roleModel2));
        modelMap.addAttribute("roleAssignments", List.of(roleAssignment1, roleAssignment2));
        modelMap.addAttribute("retentionPolicy", retentionPolicy);//String
        modelMap.addAttribute("group", group);

        DepositInfo deposit1 = getDepositInfo("deposit-id-1");

        Retrieve deposit1Retrieve = getRetrieve("deposit1-ret-1");
        Retrieve deposit2Retrieve = getRetrieve("deposit1-ret-2");

        DepositInfo deposit2 = getDepositInfo("deposit-id-2");
        modelMap.put("deposit", List.of(deposit1, deposit2));

        Map<String, Retrieve[]> depositNameToRetrievalsMap = new HashMap<>();
        depositNameToRetrievalsMap.put(deposit1.getName(), new Retrieve[]{deposit1Retrieve});
        depositNameToRetrievalsMap.put(deposit1.getName(), new Retrieve[]{deposit2Retrieve});
        modelMap.put("retrievals", depositNameToRetrievalsMap);

        User user1 = getUser1();
        User user2 = getUser2();
        modelMap.put("dataManages", List.of(user1, user2));

        EventInfo eventInfo1 = getEventInfo1();
        EventInfo eventInfo2 = getEventInfo2();

        modelMap.put("roleEvents", List.of(eventInfo1, eventInfo2));

        DepositReviewModel drm1 = new DepositReviewModel();
        drm1.setComment("drm1-comment");
        drm1.setName("drm1-name");
        drm1.setDeleteStatus(1);
        drm1.setCreationTime(now);
        drm1.setStatusName("drm1-status-name");
        drm1.setDepositReviewId("drm1-deposit-review-id");
        drm1.setDepositId("drm1-deposit-id");
        drm1.setToBeDeleted(true);

        DepositReviewModel drm2 = new DepositReviewModel();
        drm2.setComment("drm2-comment");
        drm2.setName("drm2-name");
        drm2.setDeleteStatus(1);
        drm2.setCreationTime(now);
        drm2.setStatusName("drm2-status-name");
        drm2.setDepositReviewId("drm2-deposit-review-id");
        drm2.setDepositId("drm2-deposit-id");
        drm2.setToBeDeleted(true);

        VaultReviewModel vrm1 = new VaultReviewModel();
        vrm1.setActionedDate(now);
        vrm1.setComment("vrm1 - comment");
        vrm1.setVaultReviewId("vault-review-id-1");
        vrm1.setNewReviewDate(now.toString());
        vrm1.setDepositReviewModels(List.of(drm1, drm2));

        VaultReviewModel vrm2 = new VaultReviewModel();
        vrm2.setActionedDate(now);
        vrm2.setComment("vrm2 - comment");
        vrm2.setVaultReviewId("vault-review-id-2");
        vrm2.setNewReviewDate(now.toString());
        vrm2.setDepositReviewModels(List.of(drm1, drm2));

        VaultReviewHistoryModel vrhm = new VaultReviewHistoryModel();
        vrhm.setVaultReviewModels(List.of(vrm1, vrm2));

        modelMap.put("vrhm", vrhm);

        String html = getHtml("vaults/vault", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Vaults - Vault");
        outputHtml(html);
    }

    @Test
    @WithMockUser(roles = "IS_ADMIN")
    void test55Index() throws Exception {
        ModelMap modelMap = new ModelMap();

        String html = getHtml("index", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Index");
        outputHtml(html);
    }

    @Test
    @WithMockUser()
    void test56Secure() throws Exception {
        ModelMap modelMap = new ModelMap();

        String html = getHtml("secure", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Secure Page");
        outputHtml(html);
    }

    @Test
    void test57Welcome() throws Exception {
        ModelMap modelMap = new ModelMap();
        modelMap.put("filestoresExist",true);
        modelMap.put("datasetsExist",true);
        modelMap.put("link","link01");
        modelMap.put("system","system01");

        String html = getHtml("welcome", modelMap);
        Document doc = Jsoup.parse(html);

        //check title
        checkTitle(doc, "Welcome");
        outputHtml(html);
    }

    public String getHrefMatchedOnText(Document doc, String linkText) {
        List<Element> items = doc.selectXpath("//a[contains(.,'" + linkText + "')]", Element.class);
        Element item = items.get(0);
        return item.attr("href");
    }

    private Element lookupElement(Document doc, String xpath) {
        List<Element> items = doc.selectXpath(xpath);
        return items.get(0);
    }

    private void checkTextInputFieldValue(Document doc, String name, String expectedValue) {
        Element item = lookupElement(doc, "//input[@type='text'][@name = '" + name + "']");
        assertThat(item.attr("value")).isEqualTo(expectedValue);
    }

    private void checkTextAreaFieldValue(Document doc, String name, String expectedValue) {
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


    public ArchiveStore getArchiveStore(String id) {
        return new ArchiveStore() {
            @Override
            public String getID() {
                return id;
            }
        };
    }

    public DepositChunk getDepositChunk(String id) {
        return new DepositChunk() {
            @Override
            public String getID() {
                return id;
            }
        };
    }

    public Deposit getDeposit(String id) {
        return new Deposit() {
            @Override
            public String getID() {
                return id;
            }
        };
    }

    public Audit getAudit(String id) {
        return new Audit() {
            @Override
            public String getID() {
                return id;
            }
        };
    }

    public AuditInfo getAuditInfo(String id) {
        return new AuditInfo() {
            @Override
            public String getId() {
                return id;
            }
        };
    }

    public AuditChunkStatusInfo getAuditChunkStatusInfo(String id) {
        return new AuditChunkStatusInfo() {
            @Override
            public String getID() {
                return id;
            }
        };
    }

    private DepositInfo getDepositInfo(String id) {
        return new DepositInfo() {
            @Override
            public String getID() {
                return id;
            }
        };
    }

    private EventInfo getEventInfo(String id) {
        return new EventInfo() {
            @Override
            public String getId() {
                return id;
            }
        };
    }

    private VaultInfo getVaultInfo(String id) {
        return new VaultInfo() {
            @Override
            public String getID() {
                return id;
            }
        };
    }

    private Group getGroup(String id) {
        return new Group() {
            @Override
            public String getID() {
                return id;
            }
        };
    }

    private Retrieve getRetrieve(String id) {
        return new Retrieve() {
            @Override
            public String getID() {
                return id;
            }
        };
    }

    private User getUser(String id) {
        return new User() {
            @Override
            public String getID() {
                return id;
            }
        };
    }

    private FileStore getFileStore(String id) {
        return new FileStore() {
            @Override
            public String getID() {
                return id;
            }
        };
    }

    private VaultInfo getVaultInfo1() {
        VaultInfo vault1 = getVaultInfo("vault-info-1");
        vault1.setAffirmed(true);
        vault1.setAuthoriser("vault1-authoriser");
        vault1.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING);
        vault1.setConfirmed(true);
        vault1.setContact("vault1-contact");
        vault1.setCreationTime(now);
        vault1.setDataCreators(List.of("Tom", "Dick", "Harry"));
        vault1.setDescription("vault1-description");
        vault1.setEstimate(PendingVault.Estimate.UNDER_10TB);
        vault1.setGrantEndDate(now);
        vault1.setName("vault1-name");
        vault1.setOwnerId("vault1-owner-id");
        vault1.setProjectSize(1234);
        vault1.setReviewDate(now);
        vault1.setUserID("vault1-user-id");
        vault1.setVaultCreatorId("vault1-creator-id");
        vault1.setUserName("vault-username-1");
        return vault1;
    }

    private VaultInfo getVaultInfo2() {
        VaultInfo vault2 = getVaultInfo("vault-info-2");
        vault2.setAffirmed(true);
        vault2.setAuthoriser("vault2-authoriser");
        vault2.setBillingType(PendingVault.Billing_Type.FEEWAIVER);
        vault2.setConfirmed(true);
        vault2.setContact("vault2-contact");
        vault2.setCreationTime(now);
        vault2.setDataCreators(List.of("Geddy", "Neil", "Alex"));
        vault2.setDescription("vault2-description");
        vault2.setEstimate(PendingVault.Estimate.OVER_10TB);
        vault2.setGrantEndDate(now);
        vault2.setName("vault2-name");
        vault2.setOwnerId("vault2-owner-id");
        vault2.setProjectSize(2345);
        vault2.setReviewDate(now);
        vault2.setUserID("vault2-user-id");
        vault2.setVaultCreatorId("vault2-creator-id");
        vault2.setUserName("vault-username-2");
        return vault2;
    }

    private Dataset getDataset1() {
        Dataset result = new Dataset();
        result.setID("ds-id-1");
        result.setName("ds-name-1");
        result.setCrisId("cris-id-1");
        result.setVisible(true);
        result.setContent("ds-content-1");
        return result;
    }

    private Dataset getDataset2() {
        Dataset result = new Dataset();
        result.setID("ds-id-2");
        result.setName("ds-name-2");
        result.setCrisId("cris-id-2");
        result.setVisible(true);
        result.setContent("ds-content-2");
        return result;
    }

    private User getUser1() {
        User user = getUser("user-1-id");
        user.setFirstname("user1-first");
        user.setLastname("user1-last");
        user.setPassword("XXXX");
        user.setProperties(new HashMap<>() {{
            put("prop-A", "value-1");
            put("prop-B", "value-2");
        }});
        user.setEmail("user.one@example.com");
        return user;
    }

    private User getUser2() {
        User user = getUser("user-2-id");
        user.setFirstname("user2-first");
        user.setLastname("user2-last");
        user.setPassword("XXXX");
        user.setProperties(new HashMap<>() {{
            put("prop-C", "value-3");
            put("prop-D", "value-4");
        }});
        user.setEmail("user.two@example.com");
        return user;
    }

    private EventInfo getEventInfo1() {
        EventInfo event1 = getEventInfo("event-id-1");
        event1.setDepositID("deposit-123");
        event1.setAgent("agent-1");
        event1.setAgentType("WORKER");
        event1.setRemoteAddress("remote-addr-1");
        event1.setDepositID("message-1");
        event1.setTimestamp(now);
        event1.setRemoteAddress("remote-addr-1");
        event1.setEventClass("event-class-1");
        event1.setVaultID("vault-1");
        event1.setMessage("message-1");
        event1.setUserAgent("user-agent-1");
        event1.setUserID("user-1");
        return event1;
    }

    private EventInfo getEventInfo2() {
        EventInfo event2 = getEventInfo("event-id-2");
        event2.setDepositID("deposit-234");
        event2.setAgent("agent-2");
        event2.setAgentType("WEB");
        event2.setRemoteAddress("remote-addr-2");
        event2.setDepositID("message-2");
        event2.setTimestamp(now);
        event2.setRemoteAddress("remote-addr-2");
        event2.setEventClass("event-class-2");
        event2.setVaultID("vault-2");
        event2.setMessage("message-2");
        event2.setUserAgent("user-agent-2");
        event2.setUserID("user-2");
        return event2;
    }

    private RetentionPolicy getRetentionPolicy1() {
        RetentionPolicy policy1 = new RetentionPolicy();
        policy1.setId(111);
        policy1.setName("policy-one");
        policy1.setDescription("policy1-description");
        policy1.setUrl("https://info.org/retention-policy-1");
        policy1.setMinDataRetentionPeriod("123");
        policy1.setMinRetentionPeriod(123);
        policy1.setExtendUponRetrieval(true);
        policy1.setInEffectDate(now);
        policy1.setEndDate(now);
        policy1.setDataGuidanceReviewed(now);
        return policy1;
    }

    private RetentionPolicy getRetentionPolicy2() {
        RetentionPolicy policy2 = new RetentionPolicy();
        policy2.setId(111);
        policy2.setName("policy-two");
        policy2.setDescription("policy2-description");
        policy2.setUrl("https://info.org/retention-policy-2");
        policy2.setMinDataRetentionPeriod("234");
        policy2.setMinRetentionPeriod(234);
        policy2.setExtendUponRetrieval(true);
        policy2.setInEffectDate(now);
        policy2.setEndDate(now);
        policy2.setDataGuidanceReviewed(now);
        return policy2;
    }

    private CreateVault getCreateVault() {
        CreateVault vault = new CreateVault();
        vault.setAffirmed(true);
        vault.setBillingGrantEndDate(now.toString());
        vault.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING.name());
        vault.setBudgetAuthoriser("budget-authoriser-1");
        vault.setBudgetSchoolOrUnit("Informatics");
        vault.setBudgetSubunit("SubUnit-1");
        vault.setConfirmed(true);
        vault.setContactPerson("contact-person-1");

        vault.setDatasetID("data-set-id-1");
        vault.setDataCreators(List.of("creator1", "creator2"));
        vault.setDepositors(List.of("Neil", "Geddy", "Alex"));
        vault.setDescription("description-1");
        vault.setEstimate("estimate-1");

        vault.setGrantEndDate(String.valueOf(now));
        vault.setGroupID("group-id-one");
        vault.setGrantSubunit("GrantSubUnit-1");
        vault.setGrantAuthoriser("grant-authorizer-1");

        vault.setIsOwner(true);

        vault.setLoggedInAs("user-one");

        vault.setName("vault-name");
        vault.setNominatedDataManagers(List.of("Tom", "Dick", "Harry"));
        vault.setNotes("notes-one");

        vault.setPaymentDetails("project-details");
        vault.setPendingID("pending-id-123");
        vault.setPolicyInfo("policy-info-1");
        vault.setProjectTitle("project-title");
        vault.setPureLink(true);
        vault.setReviewDate(now.toString());
        vault.setSliceID("slice-id-1");
        vault.setSliceQueryChoice(PendingVault.Slice_Query_Choice.NO_OR_DO_NOT_KNOW.name());
        vault.setVaultCreator("vault-creator-1");
        vault.setVaultOwner("vault-owner-1");
        return vault;
    }

    private void checkTitle(Document doc, String expectedTitle) {
        List<TextNode> titles = doc.selectXpath("//head/title[1]/text()", TextNode.class);
        String title = titles.get(0).text();
        assertThat(title).isEqualTo(expectedTitle);
    }

    public String getFirstLine(ClassPathResource res) throws IOException {
        InputStreamReader rdr = new InputStreamReader(res.getInputStream());
        LineNumberReader lnr = new LineNumberReader(rdr);
        return lnr.readLine();
    }

    public String getFirstLine(String fileContents) {
        if (fileContents == null) {
            return null;
        } else {
            return Arrays.stream(fileContents.split("\n")).findFirst().orElse(null);
        }
    }

    void outputHtml(String html) {
        outputHtml(html, true);
    }

    @SuppressWarnings("SameParameterValue")
    void outputHtml(String html, boolean output) {
        if (output) {
            log.info(html);
        }
    }
}
