package org.datavaultplatform.webapp.app.config;

import org.apache.commons.lang3.StringUtils;
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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ProfileStandalone
@TestMethodOrder(MethodOrderer.MethodName.class)
@AutoConfigureMockMvc
public class ThymeleafTemplateTest extends BaseThymeleafTest {

    private static final ThreadLocal<ModelMap> TL_MODEL_MAP = ThreadLocal.withInitial(ModelMap::new);

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setupTL() {
        TL_MODEL_MAP.remove();
    }

    @AfterEach
    void tearDownTL() {
        TL_MODEL_MAP.remove();
    }

    public static final String HELLO_FIRST_LINE = "<!DOCTYPE html><!--hello.ftl-->";

    public static final  String ERROR_FIRST_LINE = "<!DOCTYPE html><!--error/error.ftl-->";

    private final Date now = new Date();

    final Logger log = LoggerFactory.getLogger(getClass());

    enum ListState {
        NULL, EMPTY, NON_EMPTY
    }

    @MockBean
    PermissionEvaluator mEvaluator;


    @BeforeEach
    void setup() {

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

    @SuppressWarnings("UnnecessaryLocalVariable")
    private String getHtml(String template, ModelMap modelMap) throws Exception {
        TL_MODEL_MAP.set(modelMap);
        String html = mockMvc.perform(get("/dv/test/"+template).contextPath("/dv"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        StringBuilder sb = new StringBuilder();
        sb.append("<!-- template[");
        sb.append("datavault-webapp/src/main/webapp/WEB-INF/freemarker/");
        sb.append(template);
        sb.append(".ftl");
        sb.append("] -->");
        sb.append("\n");
        sb.append(html);
        return sb.toString();
    }

    @Test
    void test00TestHello() throws Exception {
        ClassPathResource helloResource = new ClassPathResource("WEB-INF/freemarker/test/hello.ftl");
        assertEquals(HELLO_FIRST_LINE, getFirstLine(helloResource));
        ModelMap modelMap = getModelMap();
        modelMap.put("name", "user101");
        String helloTemplateHtml = getHtml("test/hello", modelMap);
        assertEquals("<!-- template[datavault-webapp/src/main/webapp/WEB-INF/freemarker/test/hello.ftl] -->", getFirstLine(helloTemplateHtml));
        Document doc = getDocument(helloTemplateHtml);

        noFormFields(doc);

        checkTitle(doc, "Hello user101!");
    }

    @Test
    void test01AdminArchiveStoresIndex() throws Exception {
        ModelMap modelMap = getModelMap();

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

        modelMap.put("archivestores", Arrays.asList(store1, store2));
        String html = getHtml("admin/archivestores/index", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc,"add-archivestoreLocal-form");
        //check title
        checkTitle(doc, "Admin - Archive Stores");
        outputHtml("test01",doc);
    }

    @Test
        //TODO this one is quite hard to setup - complex map structure
    void test02AdminAuditsDeposits() throws Exception {
        ModelMap modelMap = getModelMap();
        modelMap.put("deposits", Collections.emptyList());
        String html = getHtml("admin/audits/deposits", modelMap);
        Document doc = getDocument(html);
        displayFormFields(doc, "");

        //check title
        checkTitle(doc, "Admin Audits Deposits");
        outputHtml("test02",doc);
    }

    @Test
    void test03AdminAuditsIndex() throws Exception {
        ModelMap modelMap = getModelMap();

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

        audit1.setAuditChunks(Arrays.asList(info1, info2));
        audit2.setAuditChunks(Arrays.asList(info1, info2));

        modelMap.put("audit", audit1);
        modelMap.put("audits", Arrays.asList(audit1, audit2));

        String html = getHtml("admin/audits/index", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Admin - Audits");
        outputHtml("test03", doc);
    }

    @Test
    void test04AdminBillingBillingDetails() throws Exception {
        ModelMap modelMap = getModelMap();
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetails", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc,"update-billingDetails-form");

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
        outputHtml("test04", doc);
    }

    @Test
    void test05AdminBillingBillingDetailsBudget() throws Exception {

        ModelMap modelMap = getModelMap();
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsBudget", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc,"update-billingDetails-form");

        checkTextInputFieldValue(doc, "projectId", "ID-123");
        checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details (Budget)");
        outputHtml("test05", doc);
    }


    @Test
    void test06AdminBillingBillingDetailsBuyNewSlice() throws Exception {

        ModelMap modelMap = getModelMap();
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsBuyNewSlice", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc,"update-billingDetails-form");

        checkTextInputFieldValue(doc, "projectId", "ID-123");
        checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details (SLICE)");
        outputHtml("test06", doc);
    }

    @Test
    void test07AdminBillingBillingDetailsBuyFeeWaiver() throws Exception {

        ModelMap modelMap = getModelMap();
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsFeeWaiver", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc,"update-billingDetails-form");

        checkTextInputFieldValue(doc, "projectId", "ID-123");
        checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details (Funding: NO or Don't Know)");
        outputHtml("test07", doc);
    }

    @Test
    void test08AdminBillingBillingDetailsFundingNoDoNotKnow() throws Exception {

        ModelMap modelMap = getModelMap();
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsFundingNoDoNotKNow", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc,"update-billingDetails-form");

        checkTextInputFieldValue(doc, "projectId", "ID-123");
        checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details (FUNDING: NO OR DO NOT KNOW)");
        outputHtml("test08", doc);
    }


    @Test
    void test09AdminBillingBillingDetailsGrant() throws Exception {

        ModelMap modelMap = getModelMap();
        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsGrant", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc,"update-billingDetails-form");

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
        outputHtml("test09", doc);
    }

    @Test
    void test10AdminBillingBillingDetailsNA() throws Exception {
        ModelMap modelMap = getModelMap();

        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsNA", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "update-billingDetails-form");

        checkTextInputFieldValue(doc, "projectId", "ID-123");
        checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details (NA)");
        outputHtml("test10", doc);
    }

    @Test
    void test11AdminBillingDetailsSlice() throws Exception {
        ModelMap modelMap = getModelMap();

        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsSlice", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "update-billingDetails-form");

        //check title
        checkTitle(doc, "Admin - Billing Details (SLICE)");
        outputHtml("test11", doc);
    }

    @Test
    void test12AdminBillingBillingDetailsWillPay() throws Exception {
        ModelMap modelMap = getModelMap();

        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        String html = getHtml("admin/billing/billingDetailsWilLPay", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "update-billingDetails-form");

        checkTextInputFieldValue(doc, "projectId", "ID-123");
        checkTextInputFieldValue(doc, "amountToBeBilled", "234.56");
        checkTextInputFieldValue(doc, "amountBilled", "123.45");
        checkTextAreaFieldValue(doc, "paymentDetails", "some-payment-details");
        checkTextAreaFieldValue(doc, "specialComments", "My Special Comments");

        Element form = lookupElement(doc, "//form");
        assertThat(form.attr("action")).isEqualTo("/dv/admin/billing/updateBillingDetails");

        //check title
        checkTitle(doc, "Admin - Billing Details (WILL PAY)");
        outputHtml("test12", doc);
    }

    @Test
    void test13BillingDetailsIndex() throws Exception {
        ModelMap modelMap = getModelMap();

        VaultInfo vault1 = getVaultInfo1();
        VaultInfo vault2 = getVaultInfo2();

        modelMap.put("vaults", Arrays.asList(vault1, vault2));

        BillingInformation info = getInfo(now);

        modelMap.put("billingDetails", info);
        modelMap.put("numberOfPages",2);
        //ADDED 24MAR2024
        modelMap.put("sort","blah");
        modelMap.put("query","blah");
        modelMap.put("ordername","blah");
        modelMap.put("orderProjectId","blah");
        modelMap.put("ordervaultsize","1234");
        modelMap.put("ordergrantEndDate","blah");
        modelMap.put("ordercreationtime","blah");
        modelMap.put("orderreviewDate", new Date().toString());
        modelMap.put("orderuser","bob");
        modelMap.put("recordsInfo","info");
        modelMap.put("activePageId",1);
        modelMap.put("order","desc");

        String html = getHtml("admin/billing/index", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "search-vaults");

        //check title
        checkTitle(doc, "Admin - Billing Details");
        outputHtml("test13", doc);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void test14AdminDepositsIndex() throws Exception {


        ModelMap modelMap = getModelMap();

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
        info1.setVaultReviewDate(now.toString());
        info1.setDescription("DepositOneDescription");
        info1.setCreationTime(now);
        info1.setDepositPaths(Collections.emptyList());
        info1.setDepositSize(1234);
        info1.setPersonalDataStatement("personalDataStatement1");
        info1.setShortFilePath("short-file-path-1");
        info1.setHasPersonalData(true);
        //added 24mar2024
        info1.setUserID("dep1-user-id");
        info1.setVaultOwnerName("dep1-vault-owner-name");

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
        // added 24mar2024
        info2.setUserID("dep2-user-id");
        info2.setVaultOwnerName("dep2-vault-owner-name");

        modelMap.put("deposits", Arrays.asList(info1, info2));
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
        //added 24mar2024
        modelMap.put("deposit", info1);
        modelMap.put("activePageId",1);

        String html = getHtml("admin/deposits/index", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "search-vaults");

        //check title
        checkTitle(doc, "Admin - Deposits");
        outputHtml("test14", doc);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void test15AdminEventsIndex() throws Exception {
        ModelMap modelMap = getModelMap();

        EventInfo event1 = getEventInfo1();

        EventInfo event2 = getEventInfo2();

        modelMap.put("events", Arrays.asList(event1, event2));
        String html = getHtml("admin/events/index", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Admin - Events");
        outputHtml("test15", doc);
    }

    @WithMockUser(roles = "IS_ADMIN")
    @ParameterizedTest
    @EnumSource(value = ListState.class, names  = {"NULL","NON_EMPTY"})
    void test16AdminPendingVaultsEditPendingVault(ListState listState) throws Exception {
        ModelMap modelMap = getModelMap();

        CreateVault vault = getCreateVault();
        RetentionPolicy policy1 = getRetentionPolicy1();
        RetentionPolicy policy2 = getRetentionPolicy2();

        switch (listState) {
            case NULL:
                vault.setDataCreators(null);
                vault.setDepositors(null);
                vault.setNominatedDataManagers(null);
                break;
            case EMPTY:
                vault.setDataCreators(Collections.emptyList());
                vault.setDepositors(Collections.emptyList());
                vault.setNominatedDataManagers(Collections.emptyList());
                break;
            default:
        }

        modelMap.put("errors", Arrays.asList("error1", "error2"));
        modelMap.put("vault", vault);
        modelMap.put("vaultID", "vault-id-123");
        modelMap.put("policies", Arrays.asList(policy1, policy2));
        modelMap.put("groups", Arrays.asList(getGroup("1"), getGroup("2")));

        String html = getHtml("admin/pendingVaults/edit/editPendingVault", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "pendingvault-edit-form");

        //check title
        checkTitle(doc, "Admin - Edit Pending Vault");
        checkPolicyInfoOptions(doc);
        outputHtml("test16", doc);

    }

    @SuppressWarnings("unused")
    private void displayFormFields(Document doc) {
        displayFormFields(doc, null);
    }

    private void displayFormFields(Document doc, String expectedFormId) {
        //the old freemarker templates had hidden false values for checkboxes - no need for this with Thymeleaf
        Elements hiddenFalseValues = doc.selectXpath("//form[1]//input[@type='hidden'][@value='false']");
        //assertThat(hiddenFalseValues.size()).isZero();

        Elements forms = doc.selectXpath("//form[1]");

        if(forms.isEmpty()){
            assertThat(expectedFormId.equals(""));
            return;
        }
        Element form = forms.get(0);
        String formMethod = form.attr("method");
        String formAction = form.attr("action");
        String formId = form.attr("id");
        if (expectedFormId != null) {
            assertThat(formId).isEqualTo(expectedFormId);
        } else {
            System.out.println("WE HAVE A FORM NOT EXPECTED WITT ID [" + formId  + "]");
        }
        if(StringUtils.isNotBlank(formAction)){
            assertThat(formAction).startsWith("/dv");
        }
        System.out.printf("FORM method[%s] id[%s] action[%s]%n", formMethod, formId, formAction);

        Elements inputs = doc.selectXpath("//form[1]//input");

        for (Element input : inputs) {
            System.out.printf("input type[%s] name[%s] value[%s] id[%s] %n", input.attr("type"), input.attr("name"), input.attr("value"), input.id());
        }

        //select tag and options
        Elements selects = doc.selectXpath("//form[1]//select");
        for (Element select : selects) {
            System.out.printf("select name[%s] id[%s] %n", select.attr("name"), select.id());
            Elements options = select.getElementsByTag("option");
            for (Element option : options) {
                boolean selected = option.hasAttr("selected");
                System.out.printf("select-option value[%s] selected[%s] %n", option.attr("value"), selected);
            }
        }

        //datalist tag and options
        Elements datalists = doc.selectXpath("//form[1]//datalist");
        for (Element datalist : datalists) {
            System.out.printf("datalist name[%s] id[%s] %n", datalist.attr("name"), datalist.id());
            Elements options = datalist.getElementsByTag("option");
            for (Element option : options) {
                boolean selected = option.hasAttr("selected");
                System.out.printf("datalist-option value[%s] selected[%s] %n", option.attr("value"), selected);
            }
        }

        //textareas
        Elements textareas = doc.selectXpath("//form[1]//textarea");
        for (Element textarea : textareas) {
            System.out.printf("textarea name[%s] id[%s] text[%s]%n", textarea.attr("name"), textarea.id(), textarea.text());
        }
    }


    @Test
    @WithMockUser(roles = "MANAGER")
    void test17AdminPendingVaultsConfirmed() throws Exception {
        ModelMap modelMap = getModelMap();

        VaultInfo vault1 = getVaultInfo1();
        VaultInfo vault2 = getVaultInfo2();

        modelMap.put("recordsInfo", "(RECORDS INFO)");
        modelMap.put("numberOfPages", 1);
        modelMap.put("activePageId", 1);
        modelMap.put("query", "edinburgh university");
        modelMap.put("sort", "name");
        modelMap.put("order", "ORDER");
        modelMap.put("ordername", "bob");
        modelMap.put("pendingVaults", Arrays.asList(vault1, vault2));

        String html = getHtml("admin/pendingVaults/confirmed", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "search-pendingvaults");

        //check title
        checkTitle(doc, "Admin - Confirmed Pending Vaults");
        outputHtml("test17", doc);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void test18AdminPendingVaultsIndex() throws Exception {
        ModelMap modelMap = getModelMap();


        modelMap.put("savedVaultsTotal", 123);
        modelMap.put("confirmedVaultsTotal", 100);

        String html = getHtml("admin/pendingVaults/index", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        String hrefAdministration = getHrefMatchedOnText(doc, "Administration");
        assertThat(hrefAdministration).isEqualTo("/dv/admin/");

        String hrefConfirmed = getHrefMatchedOnText(doc, "Confirmed");
        assertThat(hrefConfirmed).isEqualTo("/dv/admin/pendingVaults/confirmed/");

        String hrefSaved = getHrefMatchedOnText(doc, "Saved");
        assertThat(hrefSaved).isEqualTo("/dv/admin/pendingVaults/saved/");

        //check title
        checkTitle(doc, "Admin - Pending Vaults");
        outputHtml("test18", doc);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void test19AdminPendingVaultsSaved() throws Exception {
        ModelMap modelMap = getModelMap();

        VaultInfo vault1 = getVaultInfo1();

        VaultInfo vault2 = getVaultInfo2();

        modelMap.put("recordsInfo", "(RECORDS INFO)");
        modelMap.put("numberOfPages", 1);
        modelMap.put("activePageId", 1);
        modelMap.put("query", "edinburgh university");
        modelMap.put("sort", "name");
        modelMap.put("order", "ORDER");
        modelMap.put("ordername", "bob");
        modelMap.put("pendingVaults", Arrays.asList(vault1, vault2));

        String html = getHtml("admin/pendingVaults/saved", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "search-pendingvaults");

        //check title
        checkTitle(doc, "Admin - Saved Pending Vaults");
        outputHtml("test19",doc);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void test20AdminPendingVaultsSummary() throws Exception {
        ModelMap modelMap = getModelMap();

        VaultInfo vault1 = getVaultInfo1();

        CreateRetentionPolicy createRetentionPolicy = new CreateRetentionPolicy();
        createRetentionPolicy.setDescription("crp-desc");
        createRetentionPolicy.setId(123);
        createRetentionPolicy.setName("crp-name");
        createRetentionPolicy.setEndDate(getNowValue());

        Group group = getGroup("group-id-1");
        group.setName("group-name-1");
        group.setEnabled(true);

        modelMap.put("pendingVault", vault1);
        modelMap.put("createRetentionPolicy", createRetentionPolicy);
        modelMap.put("group", group);

        String html = getHtml("admin/pendingVaults/summary", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "create-vault");

        //check title
        checkTitle(doc, "Admin - Pending Vault Summary");
        outputHtml("test20", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test21AdminRetentionPoliciesAdd() throws Exception {
        ModelMap modelMap = getModelMap();

        RetentionPolicy policy1 = getRetentionPolicy1();

        modelMap.put("retentionPolicy", policy1);

        String html = getHtml("admin/retentionpolicies/add", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "add-rentention-policy-form");

        //check title
        checkTitle(doc, "Admin - Add Retention Policy");
        outputHtml("test21", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test22AdminRetentionPoliciesEdit() throws Exception {
        ModelMap modelMap = getModelMap();

        RetentionPolicy policy1 = getRetentionPolicy1();
        modelMap.put("retentionPolicy", policy1);

        String html = getHtml("admin/retentionpolicies/edit", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc,"add-rentention-policy-form");

        //check title
        checkTitle(doc, "Admin - Edit Retention Policy");
        outputHtml("test22", doc);
    }


    @Test
    @WithMockUser(roles = "USER")
    void test23AdminRetentionPoliciesIndex() throws Exception {
        ModelMap modelMap = getModelMap();

        RetentionPolicy policy1 = getRetentionPolicy1();
        RetentionPolicy policy2 = getRetentionPolicy2();

        modelMap.put("policies", Arrays.asList(policy1, policy2));

        String html = getHtml("admin/retentionpolicies/index", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Admin - Retention Policies");
        outputHtml("test23", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test24AdminRetrievesIndex() throws Exception {
        ModelMap modelMap = getModelMap();

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

        modelMap.put("retrieves", Arrays.asList(ret1, ret2));

        String html = getHtml("admin/retrieves/index", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Admin - Retrievals");
        outputHtml("test24", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test25AdminReviewsCreate() throws Exception {
        ModelMap modelMap = getModelMap();

        VaultReviewModel vrModel = new VaultReviewModel();
        vrModel.setVaultReviewId("vault-review-id-1");
        vrModel.setNewReviewDate(getNowValue());
        vrModel.setComment("comment-1");
        vrModel.setActionedDate(now);

        DepositReviewModel drm1 = new DepositReviewModel();
        drm1.setDepositId("drm1-depositId1");
        drm1.setDepositReviewId("drm1-reviewId1");
        drm1.setName("drm1-name");
        drm1.setComment("drm1-comment");
        drm1.setCreationTime(new Date());
        drm1.setStatusName("NOT_STARTED");

        DepositReviewModel drm2 = new DepositReviewModel();
        drm2.setDepositId("drm2-depositId2");
        drm2.setDepositReviewId("drm2-reviewId2");
        drm2.setName("drm2-name");
        drm2.setComment("drm2-comment");
        drm2.setCreationTime(new Date());
        drm2.setStatusName("IN_PROGRESS");

        vrModel.setDepositReviewModels(Arrays.asList(drm1, drm2));

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

        modelMap.put("dataManagers", Arrays.asList(ra1, ra2));
        modelMap.put("vault", vault1);
        modelMap.put("vaultReviewModel", vrModel);
        modelMap.put("group", group);
        modelMap.put("createRetentionPolicy", createRetentionPolicy);

        String html = getHtml("admin/reviews/create", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "create-review");

        //check title
        checkTitle(doc, "Admin - Review");
        outputHtml("test25", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test26AdminReviewsIndex() throws Exception {
        ModelMap modelMap = getModelMap();

        VaultInfo vault1 = getVaultInfo1();
        VaultInfo vault2 = getVaultInfo2();

        modelMap.put("vaults", Arrays.asList(vault1, vault2));

        String html = getHtml("admin/reviews/index", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Admin - Reviews");
        outputHtml("test26", doc);
    }

    @Test
    void test27AdminIRolesAdminIndex() throws Exception {

        ModelMap modelMap = getModelMap();

        RoleModel superAdminRole = new RoleModel();
        superAdminRole.setName("Super-Admin");
        ArrayList<User> users = new ArrayList<>();
        User user1 = getUser("one");
        User user2 = getUser("two");
        users.add(user1);
        users.add(user2);
        modelMap.put("role",superAdminRole);
        modelMap.put("users", users);
        String html = getHtml("admin/roles/isadmin/index", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "create-form");

        //check title
        checkTitle(doc, "Admin - IS Admin");
        outputHtml("test27", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test28AdminRolesIndex() throws Exception {
        ModelMap modelMap = getModelMap();

        RoleModel superAdminRole = new RoleModel();
        superAdminRole.setName("Super-Admin");
        superAdminRole.setStatus("SUPER_STATUS");


        RoleModel ror1 = new RoleModel();
        ror1.setName("ror1");
        ror1.setStatus("ror1-status");
        RoleModel ror2 = new RoleModel();
        ror2.setName("ror2");
        ror2.setStatus("ror2-status");
        modelMap.put("readOnlyRoles",Arrays.asList(ror1, ror2));
        RoleModel role1 = new RoleModel();
        role1.setName("role1");
        role1.setStatus("role1-status");
        RoleModel role2 = new RoleModel();
        role2.setName("role1");
        role2.setStatus("role1-status");
        modelMap.put("roles", Arrays.asList(role1,role2));
        modelMap.put("isSuperAdmin",true);
        modelMap.put("superAdminRole", superAdminRole);

        String html = getHtml("admin/roles/index", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "create-role");

        //check title
        checkTitle(doc, "Admin - Roles");
        outputHtml("test28", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test29AdminSchoolsIndex() throws Exception {
        ModelMap modelMap = getModelMap();

        Group group1 = getGroup("school-id-1");
        group1.setName("school-name-1");
        group1.setEnabled(true);

        Group group2 = getGroup("school-id-2");
        group2.setName("school-name-2");
        group2.setEnabled(true);

        modelMap.put("schools", Arrays.asList(group1, group2));
        String html = getHtml("admin/schools/index", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Admin - Schools");
        outputHtml("test29", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test30AdminSchoolsRoles() throws Exception {
        ModelMap modelMap = getModelMap();

        Group group1 = getGroup("school-id-1");
        group1.setName("school-name-1");
        group1.setEnabled(true);

        RoleModel roleModel1 = new RoleModel();
        roleModel1.setId(1111L);
        roleModel1.setAssignedUserCount(111);
        roleModel1.setStatus("Status-1");
        roleModel1.setName("rm1-name");
        roleModel1.setDescription("rm1-description");

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
        roleModel2.setDescription("rm2-description");

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

        String html = getHtml("admin/schools/schoolRoles", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "create-form");

        //check title
        checkTitle(doc, "Admin - School Roles");
        outputHtml("test30", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test31AdminUsersCreate() throws Exception {
        ModelMap modelMap = getModelMap();
        User user1 = getUser("user-id-1");
        user1.setFirstname("user1-first");
        user1.setLastname("user1-last");
        user1.setPassword("XXXX");
        user1.setProperties(new HashMap<String,String>() {{
            put("prop-1", "value-1");
            put("prop-2", "value-2");
        }});
        user1.setEmail("user.one@example.com");
        user1.setID("user-id-111");

        modelMap.put("user", user1);

        String html = getHtml("admin/users/create", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "create-user");

        //check title
        checkTitle(doc, "Admin - Create User");
        outputHtml("test31", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test32AdminUsersEdit() throws Exception {
        ModelMap modelMap = getModelMap();
        User user1 = getUser("user-id-1");
        user1.setFirstname("user1-first");
        user1.setLastname("user1-last");
        user1.setPassword("XXXX");
        user1.setProperties(new HashMap<String,String>() {{
            put("prop-1", "value-1");
            put("prop-2", "value-2");
        }});
        user1.setEmail("user.one@example.com");
        user1.setID("user-id-111");

        modelMap.put("user", user1);

        String html = getHtml("admin/users/edit", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc,"edit-user");

        //check title
        checkTitle(doc, "Admin - Edit Profile");
        outputHtml("test32", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test33AdminUsersIndex() throws Exception {
        ModelMap modelMap = getModelMap();
        User user1 = getUser("user-id-1");
        user1.setFirstname("user1-first");
        user1.setLastname("user1-last");
        user1.setPassword("XXXX");
        user1.setEmail("user.one@example.com");
        user1.setProperties(new HashMap<String,String>() {{
            put("prop-A", "value-1");
            put("prop-B", "value-2");
        }});

        User user2 = getUser("user-id-2");
        user2.setFirstname("user2-first");
        user2.setLastname("user2-last");
        user2.setPassword("XXXX");
        user2.setEmail("user.two@example.com");
        user2.setProperties(new HashMap<String,String>() {{
            put("prop-C", "value-3");
            put("prop-D", "value-4");
        }});

        modelMap.put("users", Arrays.asList(user1, user2));
        modelMap.put("query", "admin users");

        String html = getHtml("admin/users/index", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "search-users");

        //check title
        checkTitle(doc, "Admin - Users");
        outputHtml("test33", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test34AdminVaultsIndex() throws Exception {
        ModelMap modelMap = getModelMap();

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
        modelMap.put("vaults", Arrays.asList(vault1, vault2));

        String html = getHtml("admin/vaults/index", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "search-vaults");

        //check title
        checkTitle(doc, "Admin - Vaults");
        outputHtml("test34", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test35AdminVaultsVault() throws Exception {
        ModelMap modelMap = getModelMap();

        VaultInfo vault1 = getVaultInfo1();

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
        info1.setVaultReviewDate(now.toString());
        info1.setDescription("DepositOneDescription");
        info1.setCreationTime(now);
        info1.setDepositPaths(Collections.emptyList());
        info1.setDepositSize(1234);
        info1.setPersonalDataStatement("personalDataStatement1");
        info1.setShortFilePath("short-file-path-1");
        info1.setHasPersonalData(true);
        info1.setFileOrigin("file-origin-1");
        info1.setUserID("dep1-user-id");

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
        info2.setFileOrigin("file-origin-2");
        info2.setUserID("dep2-user-id");

        CreateRetentionPolicy createRetentionPolicy = new CreateRetentionPolicy();
        createRetentionPolicy.setDescription("crp-desc");
        createRetentionPolicy.setId(123);
        createRetentionPolicy.setName("crp-name");
        createRetentionPolicy.setEndDate(getNowValue());

        Group group = getGroup("group-id-1");
        group.setName("group-name-1");
        group.setEnabled(true);

        modelMap.put("vault", vault1);
        modelMap.put("deposits", Arrays.asList(info1, info2));
        modelMap.put("retentionPolicy", createRetentionPolicy);
        modelMap.put("group", group);

        String html = getHtml("admin/vaults/vault", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "");

        //check title
        checkTitle(doc, "Admin - Vault");
        outputHtml("test35", doc);
    }

    @Test
    @WithMockUser(roles = "USER")
    void test36adminIndex() throws Exception {
        ModelMap modelMap = getModelMap();
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
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Admin");
        outputHtml("test36", doc);
    }

    @Test
    void test37AuthConfirmation() throws Exception {
        ModelMap modelMap = getModelMap();

        String html = getHtml("auth/confirmation", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Authentication - Confirmation");
        outputHtml("test37", doc);
    }

    @Test
    void test38AuthDenied() throws Exception {
        ModelMap modelMap = getModelMap();

        String html = getHtml("auth/denied", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Authentication - Access Denied");
        outputHtml("test38", doc);
    }

    @Test
    void test39AuthLogin() throws Exception {
        ModelMap modelMap = getModelMap();

        modelMap.put("welcome", "Welcome Message");
        modelMap.put("success", "Success Message");

        String html = getHtml("auth/login", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "");

        //check title
        checkTitle(doc, "Authentication - Login");
        outputHtml("test39", doc);
    }

    @Test
    void test40DepositsCreate() throws Exception {
        ModelMap modelMap = getModelMap();


        CreateDeposit deposit = new CreateDeposit();
        deposit.setFileUploadHandle("file-upload-handle-1");
        deposit.setName("deposit-name-1");
        deposit.setDepositPaths(Arrays.asList("path1", "path2", "path3"));
        deposit.setVaultID("vault-id-1");
        deposit.setDescription("deposit-description");
        deposit.setHasPersonalData("hasPersonalData1");
        deposit.setPersonalDataStatement("personalDataStatement1");

        VaultInfo vault1 = getVaultInfo1();

        modelMap.put("deposit", deposit);
        modelMap.put("vault", vault1);

        String html = getHtml("deposits/create", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc,"add-from-storage-form");

        //check title
        checkTitle(doc, "Deposits - Create");
        outputHtml("test40", doc);
    }

    @Test
    @WithMockUser(roles = "IS_ADMIN")
    public void test41DepositsDeposit() throws Exception {

        ModelMap modelMap = getModelMap();

        User user1 = getUser("user-id-1");

        EventInfo event1 = getEventInfo1();

        EventInfo event2 = getEventInfo2();

        Deposit deposit1 = getDeposit("dep-id-001");

        DepositChunk dc1 = getDepositChunk("dc-001");
        DepositChunk dc2 = getDepositChunk("dc-002");
        dc1.setDeposit(deposit1);
        dc2.setDeposit(deposit1);
        deposit1.setDepositChunks(Arrays.asList(dc1, dc2));
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
        info1.setUserID("deposit1-user-id");

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
        modelMap.put("events", Arrays.asList(event1, event2));
        modelMap.put("retrieves", Arrays.asList(ret1, ret2));

        String html = getHtml("deposits/deposit", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Deposits - Deposit");
        outputHtml("test41", doc);
    }

    @Test
    @WithMockUser(roles = "IS_ADMIN")
    public void test42DepositsRetrieve() throws Exception {

        ModelMap modelMap = getModelMap();

        User user1 = getUser("user-id-1");

        Deposit deposit1 = getDeposit("dep-id-001");

        DepositChunk dc1 = getDepositChunk("dc-001");
        DepositChunk dc2 = getDepositChunk("dc-002");
        dc1.setDeposit(deposit1);
        dc2.setDeposit(deposit1);
        deposit1.setDepositChunks(Arrays.asList(dc1, dc2));
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
        Document doc = getDocument(html);

        displayFormFields(doc, "retrieve-deposit");

        //check title
        checkTitle(doc, "Deposits - Retrieve");
        outputHtml("test42", doc);
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void test43ErrorError() throws Exception {
        ClassPathResource errorResource = new ClassPathResource("WEB-INF/freemarker/error/error.ftl");
        assertEquals("<!--error.ftl-->", getFirstLine(errorResource));

        ModelMap modelMap = getModelMap();
        modelMap.put("message", "This is a test error message");
        String errorTemplateHtml = getHtml("error/error", modelMap);
        //html is a mix of error 'page' and default template.
        assertThat(errorTemplateHtml).startsWith("<!-- template[datavault-webapp/src/main/webapp/WEB-INF/freemarker/error/error.ftl] -->\n" +
                "<!--error.ftl-->\n" +
                "\n" +
                "<!DOCTYPE html>");

        Document doc = getDocument(errorTemplateHtml);

        noFormFields(doc);

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
        //List<Comment> comments = doc.selectXpath("//div[@id='datavault-header']/comment()", Comment.class);
        //Comment secondComment = comments.get(1);
        //assertThat(secondComment.getData()).contains("nav is (none)");

        //check error template fragment is placed at correct place within layout template
        //List<Element> bodyDivs = doc.selectXpath("//div[@id='datavault-body']", Element.class);
        //Element bodyDiv = bodyDivs.get(0);
        //Element errorDiv = bodyDiv.child(0);
        //assertThat(errorDiv.attr("id")).isEqualTo("error");

        //check the error message gets placed into html
        List<Element> errorMessages = doc.selectXpath("//span[@id='error-message']", Element.class);
        Element errorMessage = errorMessages.get(0);
        assertThat(errorMessage.text()).isEqualTo("This is a test error message");

        //check title
        checkTitle(doc, "Error Page");
        outputHtml("test43", doc);
    }


    @Test
    void test44FeedbackIndex() throws Exception {
        ModelMap modelMap = getModelMap();

        String html = getHtml("feedback/index", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc,"feedback");

        //check title
        checkTitle(doc, "Feedback");
        outputHtml("test44", doc);
    }

    @Test
    void test45FeedbackSent() throws Exception {
        ModelMap modelMap = getModelMap();

        String html = getHtml("feedback/sent", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Feedback - Sent");
        outputHtml("test45", doc);
    }

    @Test
    void test46FilestoresIndex() throws Exception {
        ModelMap modelMap = getModelMap();

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
        fs1.setProperties(new HashMap<String,String>() {{
            put("host", "host1");
            put("port", "port1");
            put("rootPath", "rootPath1");
            put("publicKey", "public-key-1");
        }});
        fs1.setLabel("fs-label-1");
        fs1.setStorageClass("storage-class-1");
        fs1.setUser(user);

        FileStore fs2 = getFileStore("fs-id-2");
        fs2.setProperties(new HashMap<String,String>() {{
            put("host", "host2");
            put("port", "port2");
            put("rootPath", "rootPath2");
            put("publicKey", "public-key-2");
        }});
        fs2.setLabel("fs-label-2");
        fs2.setStorageClass("storage-class-2");
        fs2.setUser(user);

        modelMap.put("filestoresLocal", Arrays.asList(fs1, fs2));
        modelMap.put("filestoresSFTP", Arrays.asList(fs1, fs2));

        String html = getHtml("filestores/index", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "add-filestoreSFTP-form");

        //check title
        checkTitle(doc, "Filestores");
        outputHtml("test46", doc);
    }

    @Test
    void test47GroupsIndex() throws Exception {
        ModelMap modelMap = getModelMap();

        Group group1 = getGroup("group-id-1");
        group1.setName("group-name-1");
        group1.setEnabled(true);

        Group group2 = getGroup("group-id-2");
        group2.setName("group-name-2");
        group2.setEnabled(true);

        VaultInfo vault1 = getVaultInfo1();
        VaultInfo vault2 = getVaultInfo2();


        ArrayList<VaultInfo[]> vaults = new ArrayList<>();
        vaults.add(new VaultInfo[]{vault1, vault2});
        vaults.add(new VaultInfo[]{vault1, vault2});
        modelMap.put("vaults", vaults);
        modelMap.put("groups", Arrays.asList(group1, group2));

        String html = getHtml("groups/index", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Group Vaults");
        outputHtml("test47", doc);
    }

    @Test
    void test48HelpIndex() throws Exception {
        ModelMap modelMap = getModelMap();

        modelMap.put("system", "system-01");
        modelMap.put("link", "link-01");

        String html = getHtml("help/index", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Help");
        outputHtml("test48", doc);
    }

    @Test
    void test49VaultsConfirmed() throws Exception {
        ModelMap modelMap = getModelMap();

        String html = getHtml("vaults/confirmed", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Vaults - Confirmed");
        outputHtml("test49", doc);
    }

    @Test
    void test50VaultsCreate() throws Exception {
        ModelMap modelMap = getModelMap();

        CreateVault vault = getCreateVault();

        Dataset dataset1 = getDataset1();
        Dataset dataset2 = getDataset2();

        RetentionPolicy retPol1 = getRetentionPolicy1();

        RetentionPolicy retPol2 = getRetentionPolicy2();

        modelMap.put("datasets", Arrays.asList(dataset1, dataset2));
        modelMap.put("vault", vault);
        modelMap.put("policies", Arrays.asList(retPol1, retPol2));//RetentionPolicy[]
        modelMap.put("link", "https://www.ed.ac.uk");
        modelMap.put("system", "DataVault");
        modelMap.put("groups", Arrays.asList(getGroup("1"), getGroup("2")));

        String html = getHtml("vaults/create", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc, "create-vault");

        //check title
        checkTitle(doc, "Vaults - Create");
        outputHtml("test50", doc);
    }


    @Test
    @WithMockUser(roles = "IS_ADMIN")
    void test51VaultsIndex() throws Exception {
        ModelMap modelMap = getModelMap();

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

        modelMap.addAttribute("vaults", Arrays.asList(vault1, vault2));//VaultInfo[]
        modelMap.addAttribute("pendingVaults", Arrays.asList(vault1, vault2));//VaultInfo[]

        // pass the view an empty Vault since the form expects it
        modelMap.addAttribute("vault", getCreateVault());

        modelMap.addAttribute("datasets", Arrays.asList(dataset1, dataset2));//Dataset[]

        modelMap.addAttribute("policies", Arrays.asList(retPol1, retPol2));//RetentionPolicy[]

        modelMap.addAttribute("groups", Arrays.asList(group1, group2));//Group[]

        modelMap.put("system", "system-01");
        modelMap.put("link", "link-01");

        modelMap.addAttribute("welcome", "WelcomeMessage01");


        String html = getHtml("vaults/index", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Vaults");
        outputHtml("test51", doc);
    }

    @WithMockUser(roles = "IS_ADMIN")
    @ParameterizedTest
    @EnumSource(value = ListState.class, names = {"NULL","NON_EMPTY"})
    void test52VaultsNewCreatePrototype(ListState listState) throws Exception {
        ModelMap modelMap = getModelMap();

        Group group1 = getGroup("group-id-1");
        group1.setName("group-name-1");
        group1.setEnabled(true);

        Group group2 = getGroup("group-id-2");
        group2.setName("group-name-2");
        group2.setEnabled(true);

        CreateVault vault = getCreateVault();
        switch (listState) {
            case NULL:
                vault.setDataCreators(null);
                vault.setDepositors(null);
                vault.setNominatedDataManagers(null);
                break;
            case EMPTY:
                vault.setDataCreators(Collections.emptyList());
                vault.setDepositors(Collections.emptyList());
                vault.setNominatedDataManagers(Collections.emptyList());
                break;
            default:
        }
        RetentionPolicy retPol1 = getRetentionPolicy1();
        RetentionPolicy retPol2 = getRetentionPolicy2();
        modelMap.put("policies", Arrays.asList(retPol1, retPol2));
        modelMap.put("groups", Arrays.asList(group1, group2));
        vault.setPolicyInfo("111-123");
        vault.setGroupID("group-id-1");
        modelMap.put("vault", vault);
        modelMap.put("errors", Arrays.asList("error-message-1", "error-message-2"));
        modelMap.put("loggedInAs", "loggedInAs1");

        String html = getHtml("vaults/newCreatePrototype", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc,"vault-creation-form");
        checkPolicyInfoOptions(doc);

        //check title
        checkTitle(doc, "Vaults - Create");
        outputHtml("test52", doc);
    }


    @Test
    void test53VaultsUserVaults() throws Exception {
        ModelMap modelMap = getModelMap();

        VaultInfo vault1 = getVaultInfo1();
        VaultInfo vault2 = getVaultInfo2();
        modelMap.put("vaults", Arrays.asList(vault1, vault2));

        String html = getHtml("vaults/userVaults", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Vaults - User Vaults");
        outputHtml("test53", doc);
    }

    @Test
    @WithMockUser(roles = "IS_ADMIN")
    void test54VaultsVault() throws Exception {

        ModelMap modelMap = getModelMap();

        RoleModel roleModel1 = new RoleModel();
        roleModel1.setId(1111L);
        roleModel1.setName("role1");
        roleModel1.setDescription("role1desc");

        RoleModel roleModel2 = new RoleModel();
        roleModel2.setId(2222L);
        roleModel2.setName("role2");
        roleModel2.setDescription("role2desc");

        RoleAssignment roleAssignment1 = new RoleAssignment();
        roleAssignment1.setId(1010L);
        roleAssignment1.setUserId("user1");
        roleAssignment1.setRole(roleModel1);

        RoleAssignment roleAssignment2 = new RoleAssignment();
        roleAssignment2.setId(2020L);
        roleAssignment2.setUserId("user2");
        roleAssignment2.setRole(roleModel2);
        CreateRetentionPolicy retentionPolicy = new CreateRetentionPolicy();

        Group group = new Group();

        modelMap.addAttribute("vault", getVaultInfo1());
        modelMap.addAttribute("roles", Arrays.asList(roleModel1, roleModel2));
        modelMap.addAttribute("roleAssignments", Arrays.asList(roleAssignment1, roleAssignment2));
        modelMap.addAttribute("retentionPolicy", retentionPolicy);//String
        modelMap.addAttribute("group", group);

        DepositInfo deposit1 = getDepositInfo("deposit-id-1");
        deposit1.setCreationTime(new Date());
        deposit1.setUserID("deposit1-user-id");
        deposit1.setStatus(Deposit.Status.IN_PROGRESS);
        deposit1.setHasPersonalData(false);

        Retrieve deposit1Retrieve = getRetrieve("deposit1-ret-1");
        Retrieve deposit2Retrieve = getRetrieve("deposit1-ret-2");

        DepositInfo deposit2 = getDepositInfo("deposit-id-2");
        deposit2.setCreationTime(new Date());
        deposit2.setUserID("deposit2-user-id");
        deposit2.setStatus(Deposit.Status.COMPLETE);
        deposit2.setHasPersonalData(true);

        modelMap.put("deposit", Arrays.asList(deposit1, deposit2));

        Map<String, Retrieve[]> depositNameToRetrievalsMap = new HashMap<>();
        depositNameToRetrievalsMap.put(deposit1.getName(), new Retrieve[]{deposit1Retrieve});
        depositNameToRetrievalsMap.put(deposit2.getName(), new Retrieve[]{deposit2Retrieve});
        modelMap.put("retrievals", depositNameToRetrievalsMap);

        User user1 = getUser1();
        User user2 = getUser2();
        modelMap.put("dataManages", Arrays.asList(user1, user2));

        EventInfo eventInfo1 = getEventInfo1();
        EventInfo eventInfo2 = getEventInfo2();

        modelMap.put("roleEvents", Arrays.asList(eventInfo1, eventInfo2));

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
        vrm1.setNewReviewDate(getNowValue());
        vrm1.setDepositReviewModels(Arrays.asList(drm1, drm2));

        VaultReviewModel vrm2 = new VaultReviewModel();
        vrm2.setActionedDate(now);
        vrm2.setComment("vrm2 - comment");
        vrm2.setVaultReviewId("vault-review-id-2");
        vrm2.setNewReviewDate(getNowValue());
        vrm2.setDepositReviewModels(Arrays.asList(drm1, drm2));

        VaultReviewHistoryModel vrhm = new VaultReviewHistoryModel();
        vrhm.setVaultReviewModels(Arrays.asList(vrm1, vrm2));

        modelMap.put("vrhm", vrhm);
        modelMap.put("deposits", Arrays.asList(deposit1, deposit2));

        String html = getHtml("vaults/vault", modelMap);
        Document doc = getDocument(html);

        displayFormFields(doc,"add-data-manager-form");

        //check title
        checkTitle(doc, "Vaults - Vault");
        outputHtml("test54", doc);
    }

    @Test
    @WithMockUser(roles = "IS_ADMIN")
    void test55Index() throws Exception {
        ModelMap modelMap = getModelMap();

        String html = getHtml("index", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Index");
        outputHtml("test55", doc);
    }

    @Test
    @WithMockUser()
    void test56Secure() throws Exception {
        ModelMap modelMap = getModelMap();

        String html = getHtml("secure", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Secure Page");
        outputHtml("test56", doc);
    }

    @Test
    void test57Welcome() throws Exception {
        ModelMap modelMap = getModelMap();
        modelMap.put("filestoresExist",true);
        modelMap.put("datasetsExist",true);
        modelMap.put("link","link01");
        modelMap.put("system","system01");

        String html = getHtml("welcome", modelMap);
        Document doc = getDocument(html);

        noFormFields(doc);

        //check title
        checkTitle(doc, "Welcome");
        outputHtml("test57", doc);
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

    @SuppressWarnings("unused")
    public Audit getAudit(String id) {
        return new Audit() {
            @Override
            public String getID() {
                return id;
            }
        };
    }

    public AuditInfo getAuditInfo(String id) {
        AuditInfo result = new AuditInfo() {
            @Override
            public String getId() {
                return id;
            }
        };
        result.setCreationTime(new Date());
        return result;
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
            @Override
            public String getName(){
                return "name-"+id;
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

    private Group getGroup(String groupId){
        Group group = new Group();
        group.setID(groupId);
        group.setName("name-"+groupId);
        group.setEnabled(true);
        return group;
    }


    private Retrieve getRetrieve(String id) {
        Retrieve result =  new Retrieve() {
            @Override
            public String getID() {
                return id;
            }
        };
        result.setHasExternalRecipients(true);
        result.setUser(getUser("ret-user-"+id));
        result.setNote("ret-note-"+id);
        result.setRetrievePath("retrieve-path-"+id);
        result.setTimestamp(new Date());
        result.setStatus(Retrieve.Status.COMPLETE);
        result.setDeposit(getDeposit("ret-"+id));
        return result;
    }

    private User getUser(String id) {
        User result = new User() {
            @Override
            public String getID() {
                return id;
            }
        };
        result.setFirstname("first-"+id);
        result.setLastname("last-"+id);
        return result;
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
        vault1.setAuthoriser("vault1-authoriser");
        vault1.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING);
        vault1.setConfirmed(true);
        vault1.setContact("vault1-contact");
        vault1.setCreationTime(now);
        vault1.setDataCreators(Arrays.asList("Tom", "Dick", "Harry"));
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
        vault1.setPolicyID("policy-id-1");
        vault1.setPolicyExpiry(now);
        vault1.setPolicyLastChecked(now);
        vault1.setGroupID("group-id-1");
        vault1.setOwnerName("vault-owner-name-1");
        vault1.setDatasetName("vault-data-set-name-1");
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
        vault2.setDataCreators(Arrays.asList("Geddy", "Neil", "Alex"));
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
        vault2.setPolicyID("policy-id-2");
        vault2.setGroupID("group-id-2");
        vault2.setOwnerName("vault-owner-name-2");
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
        user.setProperties(new HashMap<String,String>() {{
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
        user.setProperties(new HashMap<String,String>() {{
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
        assertThat(policy1.getPolicyInfo()).isEqualTo("111-123");
        return policy1;
    }

    private RetentionPolicy getRetentionPolicy2() {
        RetentionPolicy policy2 = new RetentionPolicy();
        policy2.setId(222);
        policy2.setName("policy-two");
        policy2.setDescription("policy2-description");
        policy2.setUrl("https://info.org/retention-policy-2");
        policy2.setMinDataRetentionPeriod("234");
        policy2.setMinRetentionPeriod(234);
        policy2.setExtendUponRetrieval(true);
        policy2.setInEffectDate(now);
        policy2.setEndDate(now);
        policy2.setDataGuidanceReviewed(now);
        assertThat(policy2.getPolicyInfo()).isEqualTo("222-234");
        return policy2;
    }

    private CreateVault getCreateVault() {
        CreateVault vault = new CreateVault();
        vault.setAffirmed(true);
        vault.setBillingGrantEndDate(getNowValue());
        vault.setBillingType(PendingVault.Billing_Type.GRANT_FUNDING.name());
        vault.setBudgetAuthoriser("budget-authoriser-1");
        vault.setBudgetSchoolOrUnit("Informatics");
        vault.setBudgetSubunit("SubUnit-1");
        vault.setConfirmed(true);
        vault.setContactPerson("contact-person-1");

        vault.setDatasetID("data-set-id-1");
        ArrayList creators = new ArrayList();
        creators.add("creator1");
        creators.add("creator2");
        vault.setDataCreators(creators);

        ArrayList depositors = new ArrayList();
        depositors.add("Neil");
        depositors.add("Geddy");
        depositors.add("Alex");
        vault.setDepositors(depositors);
        vault.setDescription("description-1");
        vault.setEstimate("estimate-1");

        vault.setGrantEndDate(getNowValue());
        vault.setGroupID("group-id-one");
        vault.setGrantSubunit("GrantSubUnit-1");
        vault.setGrantAuthoriser("grant-authorizer-1");

        vault.setIsOwner(true);

        vault.setLoggedInAs("user-one");

        vault.setName("vault-name");
        ArrayList ndms = new ArrayList();
        ndms.add("ndm1");
        ndms.add("ndm2");
        vault.setNominatedDataManagers(ndms);
        vault.setNotes("notes-one");

        vault.setPaymentDetails("project-details");
        vault.setPendingID("pending-id-123");
        vault.setPolicyInfo("111-123");
        vault.setProjectTitle("project-title");
        vault.setPureLink(true);
        vault.setReviewDate(getNowValue());
        vault.setSliceID("slice-id-1");
        vault.setSliceQueryChoice(PendingVault.Slice_Query_Choice.NO_OR_DO_NOT_KNOW.name());
        vault.setVaultCreator("vault-creator-1");
        vault.setVaultOwner("vault-owner-1");
        return vault;
    }

    /** This test controller will render the template with all spring's converters in operation too **/
    @TestConfiguration
    @Controller
    static class TestController {

        @GetMapping(value = "/test/{*path}", produces = MediaType.TEXT_HTML_VALUE)
        public String getTemplate(Model model, @PathVariable("path") String path) {
            ModelMap tl = TL_MODEL_MAP.get();//merge in the values from the model map
            model.mergeAttributes(tl);
            return path;
        }
    }

    private Document getDocument(String html) {
        Document doc = Jsoup.parse(html);
        checkCssLinks(doc);
        checkScriptTags(doc);
        return doc;
    }

    void checkCssLinks(Document doc) {
        Elements links = doc.selectXpath("//link");
        for(Element link : links) {
            String type = link.attr("type");
            assertThat(type).containsAnyOf("text/css","image/ico");
            if(type.equals("text/css")){
                String href = link.attr("href");
                assertThat(href).endsWith("css");
                assertThat(link.hasAttr("src")).isFalse();
            }
        }
    }
    void checkScriptTags(Document doc) {
        /*
        Elements scripts = doc.selectXpath("//script");
        for(Element script : scripts) {

            boolean hasType = script.hasAttr("type");
            if (hasType) {
                String type = script.attr("type");
                assertThat(type).containsAnyOf("text/javascript","application/javascript");
            }
            boolean hasSource = script.hasAttr("src");
            if (hasSource) {
                String src = script.attr("src");
                assertThat(src).endsWith(".js");
            } else {
                assertThat(hasType).isTrue();
            }
        }
         */
    }

    void checkPolicyInfoOptions(Document doc){
        Elements policyInfoSelects = doc.selectXpath("//select[@id='policyInfo']");
        assertThat(policyInfoSelects.size()).isOne();
        Element policyInfoSelect = policyInfoSelects.first();
        Elements options = policyInfoSelect.children();
        for(int i=0;i<options.size();i++){
            Element option = options.get(i);
            if(i==1){
                checkOption(option, "111-123",true);
            }
            else if(i==2){
                checkOption(option, "222-234",false);
            }
        }

    }
    void checkOption(Element option, String value, boolean selected){
        assertThat(option.tag().getName().equals("option"));
        assertThat(option.val().equals(value));
        boolean isSelected = option.hasAttr("selected");
        assertThat(isSelected).isEqualTo(selected);
    }

    private String getNowValue() {
        return now.toString();
    }

}
