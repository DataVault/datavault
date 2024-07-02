package org.datavaultplatform.webapp.controllers.admin;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.response.ReviewInfo;
import org.datavaultplatform.common.response.VaultInfo;
import org.datavaultplatform.common.response.VaultsData;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.model.DepositReviewModel;
import org.datavaultplatform.webapp.model.VaultReviewModel;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.hibernate.validator.constraints.ModCheck;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@SpringBootTest(classes = DataVaultWebApp.class)
@AutoConfigureMockMvc
@ProfileDatabase
@TestPropertySource(properties = "logging.level.org.springframework.security=DEBUG")
@AddTestProperties
public class AdminReviewsControllerTest {

    public static final String TEST_VAULT_REVIEW_ID = "test-vault-review-id";
    private static final String TEST_GROUP_ID = "test-group-id";
    private static final String TEST_VAULT_ID_1 = "vaultinfo-id-1";
    private static final String TEST_VAULT_ID_2 = "vaultinfo-id-2";
    private static final String TEST_VAULT_NAME_1 = "vaultinfo-name-1";
    private static final String TEST_VAULT_NAME_2 = "vaultinfo-name-2";
    private static final String TEST_DEPOSIT_ID_1 = "test-deposit-id-1";
    private static final String TEST_DEPOSIT_ID_2 = "test-deposit-id-2";
    private static final String TEST_DEPOSIT_REVIEW_1_ID = "test-deposit-review-1-id";
    private static final String TEST_DEPOSIT_REVIEW_1_COMMENT = "test-deposit-review-1-comment";
    private static final String TEST_DEPOSIT_REVIEW_2_ID = "test-deposit-review-2-id";
    private static final String TEST_DEPOSIT_REVIEW_2_COMMENT = "test-deposit-review-2-comment";
    private static final String TEST_DEPOSIT_1_NAME = "test-deposit-1-name";
    private static final String TEST_DEPOSIT_2_NAME = "test-deposit-2-name";
    private static final int TEST_RETENTION_POLICY_ID_1 = 123456;
    private static final int TEST_RETENTION_POLICY_ID_2 = 98765;

    private static final String REVIEW_DATE_NOTIFICATION_MESSAGE = "If some deposits are to be retained then a new Review Date must be entered";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestService restService;

    @Mock
    private VaultsData vaultsData;


    @Mock
    private VaultInfo mVaultInfo1;

    @Mock
    private VaultInfo mVaultInfo2;

    @Mock
    private RoleAssignment mRoleAssignment1;

    @Mock
    private RoleAssignment mRoleAssignment2;

    @Mock
    private RoleModel mRoleModel1;

    @Mock
    private RoleModel mRoleModel2;

    @Mock
    private CreateRetentionPolicy mCreateRetentionPolicy1;

    @Mock
    private CreateRetentionPolicy mCreateRetentionPolicy2;

    @Mock
    private ReviewInfo reviewInfo1;

    @Mock
    private ReviewInfo reviewInfo2;

    @Mock
    private VaultReview currentReview1;

    @Mock
    private VaultReview currentReview2;

    @Mock
    private VaultReviewModel vaultReviewModel1;

    @Mock
    private VaultReviewModel vaultReviewModel2;

    @Mock
    private DepositInfo depositInfo1;

    @Mock
    private DepositInfo depositInfo2;

    @Mock
    private DepositReview depositReview1;

    @Mock
    private DepositReview depositReview2;

    @Mock
    Group mGroup;
    
    @Mock
    ReviewInfo mReviewInfo;

    @Mock
    VaultReview mVaultReview;

    @Mock
    VaultReviewModel mVaultReviewModel;

    @Mock
    DepositInfo mDepositInfo1;

    @Mock
    DepositReview mDepositReview1;

    @Mock
    DepositReview mOriginalDepositReview1;

    @Mock
    DepositReviewModel mDepositReviewModel1;

    @Mock
    DepositInfo mDepositInfo2;

    @Mock
    DepositReview mDepositReview2;

    @Mock
    DepositReview mOriginalDepositReview2;

    @Mock
    DepositReviewModel mDepositReviewModel2;
    
    @Mock
    Date mDate1;
    @Mock
    Date mDate2;
    private List<RoleAssignment>  roleAssignments = new ArrayList<>();

    private List<VaultInfo> vaultsInfo = new ArrayList<>();

    
    @BeforeEach
    void setup() {
        // VaultsInfo
        vaultsInfo.add(mVaultInfo1);
        vaultsInfo.add(mVaultInfo2);

        when(vaultsData.getData()).thenReturn(vaultsInfo);

        Mockito.lenient().when(mVaultInfo1.getID()).thenReturn(TEST_VAULT_ID_1);
        Mockito.lenient().when(mVaultInfo1.getName()).thenReturn(TEST_VAULT_NAME_1);

        Mockito.lenient().when(mVaultInfo2.getID()).thenReturn(TEST_VAULT_ID_2);
        Mockito.lenient().when(mVaultInfo2.getName()).thenReturn(TEST_VAULT_NAME_2);

        // RoleAssignments
        roleAssignments.add(mRoleAssignment1);
        roleAssignments.add(mRoleAssignment2);

        Mockito.lenient().when(mRoleModel1.getName()).thenReturn("Nominated Data Manager");
        Mockito.lenient().when(mRoleModel1.getName()).thenReturn("Data Owner");

        Mockito.lenient().when(mRoleAssignment1.getRole()).thenReturn(mRoleModel1);
        Mockito.lenient().when(mRoleAssignment2.getRole()).thenReturn(mRoleModel2);

        Mockito.lenient().when(mCreateRetentionPolicy1.getID()).thenReturn(TEST_RETENTION_POLICY_ID_1);
        Mockito.lenient().when(mCreateRetentionPolicy2.getID()).thenReturn(TEST_RETENTION_POLICY_ID_2);

        Mockito.lenient().when(mCreateRetentionPolicy1.getMinRetentionPeriod()).thenReturn(10);
        Mockito.lenient().when(mCreateRetentionPolicy2.getMinRetentionPeriod()).thenReturn(5);

        // RestService
        when(restService.getVaultsForReview()).thenReturn(vaultsData);

        when(restService.getVault(TEST_VAULT_ID_1)).thenReturn(mVaultInfo1);
        when(restService.getVault(TEST_VAULT_ID_2)).thenReturn(mVaultInfo2);

        when(restService.getRoleAssignmentsForVault(TEST_VAULT_ID_1)).thenReturn(roleAssignments);
        when(restService.getRoleAssignmentsForVault(TEST_VAULT_ID_2)).thenReturn(roleAssignments);

        when(restService.getRetentionPolicy(String.valueOf(TEST_RETENTION_POLICY_ID_1))).thenReturn(mCreateRetentionPolicy1);
        when(restService.getRetentionPolicy(String.valueOf(TEST_RETENTION_POLICY_ID_2))).thenReturn(mCreateRetentionPolicy1);
    }

    @DisplayName("Test getVaultsForReview() and expect no error.")
    @Test
    @WithMockUser(roles = {"ADMIN_REVIEWS"})
    void testGetVaultsForReview() throws Exception {
        // Act
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/admin/reviews")
                .contentType(MediaType.TEXT_HTML)
                .accept(MediaType.TEXT_HTML);

        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String html = mvcResult.getResponse().getContentAsString();

        // Assert
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("admin/reviews/index");

        Document doc = Jsoup.parse(html);

        Elements elems1 = doc.selectXpath("//a[@href='/vaults/vaultinfo-id-1/']");
        assertThat(elems1.size()).isEqualTo(1);
        Element elem1 = elems1.get(0);
        assertThat(elem1.text()).isEqualTo("vaultinfo-name-1");

        Elements elems2 = doc.selectXpath("//a[@href='/vaults/vaultinfo-id-2/']");
        assertThat(elems2.size()).isEqualTo(1);
        Element elem2 = elems2.get(0);
        assertThat(elem2.text()).isEqualTo("vaultinfo-name-2");

    }

    @DisplayName("Test showReview() with no Review Date errors.")
    @Test
    @WithMockUser(roles = {"ADMIN_VAULTS"})
    void testShowReview_WithNoReviewDateError() throws Exception {
        // Arrange
        mocksForShowAndProcessReviewTests();
        
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/admin/vaults/"+TEST_VAULT_ID_1+"/reviews")
                .contentType(MediaType.TEXT_HTML)
                .accept(MediaType.TEXT_HTML);
        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        // Assert
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("admin/reviews/create");
        ModelMap modelMap = mvcResult.getModelAndView().getModelMap();
        VaultReviewModel vrm = (VaultReviewModel) modelMap.get("vaultReviewModel");
        List<DepositReviewModel> drms = vrm.getDepositReviewModels();
        assertThat(drms.size()).isEqualTo(2);
        DepositReviewModel drm1 = drms.get(0);

        assertThat(drm1.getDepositReviewId()).isEqualTo(TEST_DEPOSIT_REVIEW_1_ID);
        assertThat(drm1.getDeleteStatus()).isEqualTo(1);
        assertThat(drm1.getComment()).isEqualTo(TEST_DEPOSIT_REVIEW_1_COMMENT);
        assertThat(drm1.getDepositId()).isEqualTo(TEST_DEPOSIT_ID_1);
        assertThat(drm1.getName()).isEqualTo(TEST_DEPOSIT_1_NAME);
        assertThat(drm1.getStatusName()).isEqualTo(Audit.Status.IN_PROGRESS.name());
        assertThat(drm1.getCreationTime()).isEqualTo(mDate1);

        DepositReviewModel drm2 = drms.get(1);
        assertThat(drm2.getDepositReviewId()).isEqualTo(TEST_DEPOSIT_REVIEW_2_ID);
        assertThat(drm2.getDeleteStatus()).isEqualTo(2);
        assertThat(drm2.getComment()).isEqualTo(TEST_DEPOSIT_REVIEW_2_COMMENT);
        assertThat(drm2.getDepositId()).isEqualTo(TEST_DEPOSIT_ID_2);
        assertThat(drm2.getName()).isEqualTo(TEST_DEPOSIT_2_NAME);
        assertThat(drm2.getStatusName()).isEqualTo(Audit.Status.COMPLETE.name());
        assertThat(drm2.getCreationTime()).isEqualTo(mDate2);

        // No error key in modelMap
        assertThat((String) modelMap.get("error")).isBlank();

        System.out.println("mvcResult: " + mvcResult);
        String html = mvcResult.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);
        // No error alert on page
        Elements elems1 = doc.selectXpath("//div[@role='alert']");
        assertThat(elems1.size()).isEqualTo(0);

    }

    @DisplayName("Test showReview() with Review Date error.")
    @Test
    @WithMockUser(roles = {"ADMIN_VAULTS"})
    void testShowReview_WithReviewDateError_ThenNotificationDisplaysError() throws Exception {
        // Arrange
        mocksForShowAndProcessReviewTests();

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/admin/vaults/"+TEST_VAULT_ID_1+"/reviews")
                .queryParam("error", "reviewdate")
                .contentType(MediaType.TEXT_HTML)
                .accept(MediaType.TEXT_HTML);
        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();

        // Assert
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("admin/reviews/create");

        ModelMap modelMap = mvcResult.getModelAndView().getModelMap();

        // Error key in modelMap
        assertThat((String) modelMap.get("error")).isEqualTo(REVIEW_DATE_NOTIFICATION_MESSAGE);

        String html = mvcResult.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);

        // Error alert on page
        Elements elems1 = doc.selectXpath("//div[@role='alert']");
        assertThat(elems1.size()).isEqualTo(1);
        Element elem1 = elems1.get(0);
        assertThat(elem1.text()).isEqualTo(REVIEW_DATE_NOTIFICATION_MESSAGE);

    }

    // In tests below we add the ModelAttribute vaultReviewModel to the RequestBuilder by using flashAttr.
    // Reference: https://www.baeldung.com/spring-web-flash-attributes
    @DisplayName("Test processReview() with action Cancel.")
    @Test
    @WithMockUser(roles = {"ADMIN_VAULTS"})
    void testProcessReview_ActionCancel() throws Exception {
        // Arrange
        mocksForShowAndProcessReviewTests();

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/admin/vaults/"+TEST_VAULT_ID_1+"/reviews/" + TEST_VAULT_REVIEW_ID)
                .queryParam("action", "Cancel")
                .flashAttr("vaultReviewModel", mVaultReviewModel)
                .contentType(MediaType.TEXT_HTML)
                .accept(MediaType.TEXT_HTML)
                .with(csrf());
        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        //Assert
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("redirect:/admin/reviews");
    }

    @DisplayName("Test processReview() with action Save.")
    @Test
    @WithMockUser(roles = {"ADMIN_VAULTS"})
    void testProcessReview_ActionSave() throws Exception {
        // Arrange
        mocksForShowAndProcessReviewTests();

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/admin/vaults/"+TEST_VAULT_ID_1+"/reviews/" + TEST_VAULT_REVIEW_ID)
                .queryParam("action", "Save")
                .flashAttr("vaultReviewModel", mVaultReviewModel)
                .contentType(MediaType.TEXT_HTML)
                .accept(MediaType.TEXT_HTML)
                .with(csrf());
        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        //Assert
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("redirect:/admin/reviews");

    }

    @DisplayName("Test processReview() with action Submit with no retained deposits and no review date.")
    @Test
    @WithMockUser(roles = {"ADMIN_VAULTS"})
    void testProcessReview_Submit_WithNoReviewDate_AndNoRetainedDeposits() throws Exception {
        // Arrange
        mocksForShowAndProcessReviewTests();
        // ReviewDate not set
        when(mVaultReviewModel.getNewReviewDate()).thenReturn(null);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/admin/vaults/"+TEST_VAULT_ID_1+"/reviews/" + TEST_VAULT_REVIEW_ID)
                .queryParam("action", "Submit")
                .flashAttr("vaultReviewModel", mVaultReviewModel)
                .contentType(MediaType.TEXT_HTML)
                .accept(MediaType.TEXT_HTML)
                .with(csrf());
        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        //Assert
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("redirect:/admin/reviews");

    }

    @DisplayName("Test processReview() with action Submit with no retained deposits and a review date not null.")
    @Test
    @WithMockUser(roles = {"ADMIN_VAULTS"})
    void testProcessReview_Submit_WithNoRetainedDeposits_AndReviewDateNotNull() throws Exception {
        // Arrange
        mocksForShowAndProcessReviewTests();

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/admin/vaults/"+TEST_VAULT_ID_1+"/reviews/" + TEST_VAULT_REVIEW_ID)
                .queryParam("action", "Submit")
                .flashAttr("vaultReviewModel", mVaultReviewModel)
                .contentType(MediaType.TEXT_HTML)
                .accept(MediaType.TEXT_HTML)
                .with(csrf());
        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        //Assert
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("redirect:/admin/reviews");

    }

    @DisplayName("Test processReview() with action Submit with a retained deposit and a review date null.")
    @Test
    @WithMockUser(roles = {"ADMIN_VAULTS"})
    void testProcessReview_Submit_WithNoReviewDate_AndRetainedDeposits_ThenError() throws Exception {
        // Arrange
        mocksForShowAndProcessReviewTests();

        // Override ReviewDate and set RETAIN delete status for one Deposit
        when(mVaultReviewModel.getNewReviewDate()).thenReturn(null);
        when(mDepositReviewModel1.getDeleteStatus()).thenReturn(DepositReviewDeleteStatus.RETAIN);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/admin/vaults/"+TEST_VAULT_ID_1+"/reviews/" + TEST_VAULT_REVIEW_ID)
                .queryParam("action", "Submit")
                .flashAttr("vaultReviewModel", mVaultReviewModel)
                .contentType(MediaType.TEXT_HTML)
                .accept(MediaType.TEXT_HTML)
                .with(csrf());
        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        // Assert
        assertThat(mvcResult.getModelAndView().getViewName()).isEqualTo("redirect:/admin/vaults/"+TEST_VAULT_ID_1+"/reviews" );

        ModelMap modelMap = mvcResult.getModelAndView().getModelMap();

        // Error key in modelMap
        assertThat((String) modelMap.get("error")).isEqualTo("reviewdate");
    }


    private void mocksForShowAndProcessReviewTests() {
        // Arrange
        when(restService.getVault(Mockito.any(String.class))).thenReturn(mVaultInfo1);

        when(mVaultInfo1.getGroupID()).thenReturn(TEST_GROUP_ID);

        when(restService.getGroup(TEST_GROUP_ID)).thenReturn(mGroup);
        when(restService.getCurrentReview(TEST_VAULT_ID_1)).thenReturn(mReviewInfo);
        when(mReviewInfo.getVaultReviewId()).thenReturn(TEST_VAULT_REVIEW_ID);
        when(restService.getVaultReview(TEST_VAULT_REVIEW_ID)).thenReturn(mVaultReview);
        when(mReviewInfo.getDepositIds()).thenReturn(List.of(TEST_DEPOSIT_ID_1, TEST_DEPOSIT_ID_2));
        when(restService.getDepositReview(TEST_DEPOSIT_ID_1)).thenReturn(mDepositReview1);
        when(restService.getDepositReview(TEST_DEPOSIT_ID_2)).thenReturn(mDepositReview2);
        when(restService.getDeposit(TEST_DEPOSIT_ID_1)).thenReturn(mDepositInfo1);
        when(restService.getDeposit(TEST_DEPOSIT_ID_2)).thenReturn(mDepositInfo2);

        when(mDepositReview1.getId()).thenReturn(TEST_DEPOSIT_REVIEW_1_ID);
        when(mDepositReview1.getComment()).thenReturn(TEST_DEPOSIT_REVIEW_1_COMMENT);
        when(mDepositReview1.getDeleteStatus()).thenReturn(DepositReviewDeleteStatus.ONREVIEW);
        when(mDepositInfo1.getID()).thenReturn(TEST_DEPOSIT_ID_1);
        when(mDepositInfo1.getName()).thenReturn(TEST_DEPOSIT_1_NAME);
        when(mDepositInfo1.getStatus()).thenReturn(Deposit.Status.IN_PROGRESS);
        when(mDepositInfo1.getCreationTime()).thenReturn(mDate1);

        when(mDepositReview2.getId()).thenReturn(TEST_DEPOSIT_REVIEW_2_ID);
        when(mDepositReview2.getComment()).thenReturn(TEST_DEPOSIT_REVIEW_2_COMMENT);
        when(mDepositReview2.getDeleteStatus()).thenReturn(DepositReviewDeleteStatus.ONEXPIRY);
        when(mDepositInfo2.getID()).thenReturn(TEST_DEPOSIT_ID_2);
        when(mDepositInfo2.getName()).thenReturn(TEST_DEPOSIT_2_NAME);
        when(mDepositInfo2.getStatus()).thenReturn(Deposit.Status.COMPLETE);
        when(mDepositInfo2.getCreationTime()).thenReturn(mDate2);
        List<DepositReviewModel> drm = new ArrayList<>();
        drm.add(mDepositReviewModel1);
        drm.add(mDepositReviewModel2);
        when(restService.getDepositReview(mDepositReviewModel1.getDepositReviewId())).thenReturn(mOriginalDepositReview1);
        when(restService.getDepositReview(mDepositReviewModel2.getDepositReviewId())).thenReturn(mOriginalDepositReview2);
        // Default no retained delete status deposits, we override this in some tests
        when(mDepositReviewModel1.getDeleteStatus()).thenReturn(DepositReviewDeleteStatus.ONEXPIRY);
        when(mDepositReviewModel2.getDeleteStatus()).thenReturn(DepositReviewDeleteStatus.ONREVIEW);
        when(mVaultReviewModel.getDepositReviewModels()).thenReturn(drm);
        // Default ReviewDate set, this overriden in some tests
        when(mVaultReviewModel.getNewReviewDate()).thenReturn(new Date());
    }
}
