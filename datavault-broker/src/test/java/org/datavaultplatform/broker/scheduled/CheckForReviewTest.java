package org.datavaultplatform.broker.scheduled;

import lombok.SneakyThrows;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.services.LDAPService;
import org.datavaultplatform.common.util.RoleUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CheckForReviewTest {

    private static final String VAULT_1_ID = "vault1-id";
    private static final String VAULT_2_ID = "vault2-id";
    private static final String VAULT_1_NAME = "vault1name";
    private static final String VAULT_2_NAME = "vault2name";

    private static final String VAULT_OWNER_USER_ID = "owner-id";
    private static final String VAULT_OWNER_EMAIL = "owner@example.com";

    private static final String NDM_USER_ID = "ndm-id";
    private static final String NDM_EMAIL = "ndm@example.com";
    private static final String HELP_MAIL = "help@example.com";
    private static final String HOME_URL = "http://localhost:8080";
    private static final String HELP_URL = "http://localhost:8080/help";
    private static final RoleModel roleNominatedDataManager = createRoleModel(RoleUtils.NOMINATED_DATA_MANAGER_ROLE_NAME);
    private static final RoleModel roleDataOwner = createRoleModel(RoleUtils.DATA_OWNER_ROLE_NAME);
    @Captor
    ArgumentCaptor<Map<String, Object>> argEmail1Map;
    @Captor
    ArgumentCaptor<Map<String, Object>> argEmail2Map;
    @Captor
    ArgumentCaptor<Vault> argVault;
    @Mock
    private VaultsService mVaultsService;
    @Mock
    private VaultsReviewService mVaultsReviewService;
    @Mock
    private LDAPService mLdapService;
    @Mock
    private EmailService mEmailService;
    @Mock
    private RolesAndPermissionsService mRolesAndPermissionsService;
    @Mock
    private UsersService mUsersService;
    private Clock clock;
    private CheckForReview checkForReviewSpy;
    private Vault vault1;
    private Vault vault2;
    private User ownerUser;
    private User ndmUser;
    private RoleAssignment ownerRoleAssignment;
    private RoleAssignment ndmRoleAssignment;
    private Group group1;
    
    private LocalDateTime retentionPolicyExpiry1;

    private static RoleModel createRoleModel(String name) {
        RoleModel result = new RoleModel();
        result.setName(name);
        return result;
    }

    @BeforeEach
    void setUp() {

        retentionPolicyExpiry1 = LocalDateTime.of(2026, 4, 4, 16, 42 , 42);
        
        // Initialize the clock for deterministic time
        clock = Clock.fixed(Instant.parse("2023-01-01T10:00:00Z"), ZoneId.systemDefault());

        // Setup mock users
        ownerUser = createUser(VAULT_OWNER_USER_ID, "Owner", "Owner", VAULT_OWNER_EMAIL);
        ndmUser = createUser(NDM_USER_ID, "NDM", "NDM", NDM_EMAIL);
        lenient().when(mUsersService.getUser(VAULT_OWNER_USER_ID)).thenReturn(ownerUser);
        lenient().when(mUsersService.getUser(NDM_USER_ID)).thenReturn(ndmUser);

        this.group1 = new Group("group1id");
        this.group1.setName("group1name");

        Group group2 = new Group("group2id");
        group2.setName("group2name");

        // Setup mock vaults
        vault1 = new Vault(VAULT_1_ID, clock);
        vault1.setName(VAULT_1_NAME);
        vault1.setGroup(group1);

        vault2 = new Vault(VAULT_2_ID, clock);
        vault2.setName(VAULT_2_NAME);
        vault2.setGroup(group2);

        // Setup role assignments
        ownerRoleAssignment = new RoleAssignment();
        ownerRoleAssignment.setUserId(VAULT_OWNER_USER_ID);
        ownerRoleAssignment.setVaultId(VAULT_1_ID);
        ownerRoleAssignment.setRole(roleDataOwner);

        ndmRoleAssignment = new RoleAssignment();
        ndmRoleAssignment.setUserId(NDM_USER_ID);
        ndmRoleAssignment.setVaultId(VAULT_1_ID);
        ndmRoleAssignment.setRole(roleNominatedDataManager);
        
        checkForReviewSpy = spy(new CheckForReview(
                mVaultsService, mVaultsReviewService, mLdapService, mEmailService,
                mRolesAndPermissionsService, mUsersService, HOME_URL, HELP_URL, HELP_MAIL, clock));
    }

    // --- Tests for execute() method ---

    private User createUser(String userID, String first, String last, String email) {
        User result = new User();
        result.setID(userID);
        result.setFirstname(first);
        result.setLastname(last);
        result.setEmail(email);
        return result;
    }

    @Test
    @Order(1)
    @SneakyThrows
    void testExecuteCallsCheckVaultForReviewForEachVault() {
        when(mVaultsService.getVaults()).thenReturn(Arrays.asList(vault1, null, vault2));

        checkForReviewSpy.execute();

        // Verify that checkVaultForReview is called for each non-null vault
        verify(checkForReviewSpy, times(1)).checkVaultForReview(vault1);
        verify(checkForReviewSpy, times(1)).checkVaultForReview(vault2);
        // Ensure nulls are skipped
        verify(checkForReviewSpy, never()).checkVaultForReview(null);
        verify(mVaultsService).getVaults();
        verify(mVaultsReviewService, times(2)).dueForReviewEmail(argVault.capture());
        assertThat(argVault.getAllValues()).containsExactly(vault1, vault2);

        verifyNoMoreInteractions(mVaultsService, mVaultsReviewService, mLdapService, mEmailService, mRolesAndPermissionsService, mUsersService);
    }

    @Test
    @Order(2)
    void testExecuteHandlesEmptyVaultList() throws Exception {
        when(mVaultsService.getVaults()).thenReturn(Collections.emptyList());

        checkForReviewSpy.execute();

        verifyNoMoreInteractions(mVaultsService, mVaultsReviewService, mLdapService, mEmailService, mRolesAndPermissionsService, mUsersService);
    }

    // --- Tests for checkVaultForReview() method ---

    @Test
    @Order(3)
    void testExecuteHandlesNullVaultsList() throws Exception {
        when(mVaultsService.getVaults()).thenReturn(null);

        checkForReviewSpy.execute();

        verifyNoMoreInteractions(mVaultsService, mVaultsReviewService, mLdapService, mEmailService, mRolesAndPermissionsService, mUsersService);
    }

    @Test
    @Order(4)
    void testCheckVaultForReviewWhenNotDueForReview() {
        when(mVaultsReviewService.dueForReviewEmail(vault1)).thenReturn(false);

        checkForReviewSpy.checkVaultForReview(vault1);

        verifyNoMoreInteractions(mVaultsService, mVaultsReviewService, mLdapService, mEmailService, mRolesAndPermissionsService, mUsersService);
    }

    static Stream<Arguments> groupProvider() {
        Group group1 = new Group("group1id");
        group1.setName("group1name");
        return Stream.of(
                Arguments.of(null, ""),
                Arguments.of(group1, "group1name"));
    }

    @ParameterizedTest
    @Order(5)
    @MethodSource("groupProvider") 
    void testCheckVaultForReviewWhenDueForReviewAndNoExistingReviewEmailsOwner(Group group, String expectedGroupString) throws Exception {
        Vault mVault = mock(Vault.class);
        when(mVault.getID()).thenReturn(VAULT_1_ID);
        when(mVault.getName()).thenReturn(VAULT_1_NAME);
        when(mVault.isVaultReviewUnderway()).thenReturn(false);
        when(mVault.getGroup()).thenReturn(group);
        when(mVault.getRetentionPolicyExpiry()).thenReturn(retentionPolicyExpiry1);

        when(mVaultsReviewService.dueForReviewEmail(mVault)).thenReturn(true);
        when(mRolesAndPermissionsService.getRoleAssignmentsForVault(VAULT_1_ID))
                .thenReturn(List.of(ownerRoleAssignment));
        when(mLdapService.getLDAPAttributes(VAULT_OWNER_USER_ID)).thenReturn(Collections.singletonMap("uid", "owner"));
        when(mUsersService.getUser(VAULT_OWNER_USER_ID)).thenReturn(ownerUser);

        checkForReviewSpy.checkVaultForReview(mVault);

        // Verify VaultReview is created
        verify(mVaultsReviewService, times(1)).createVaultReview(mVault);

        // Verify email to support team
        verify(mEmailService, times(1)).sendTemplateMail(eq(HELP_MAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_SUPPORT), argEmail1Map.capture());

        // Verify email to data owner
        verify(mEmailService, times(1)).sendTemplateMail(eq(VAULT_OWNER_EMAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_OWNER), argEmail2Map.capture());

        // Verify no email to NDM if owner is found
        verify(mEmailService, never()).sendTemplateMail(eq(NDM_EMAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_DATA_MANAGER), anyMap());

        verify(mUsersService).getUser(VAULT_OWNER_USER_ID);
        verifyNoMoreInteractions(mVaultsService, mVaultsReviewService, mLdapService, mEmailService, mRolesAndPermissionsService, mUsersService);

        var email1Map = argEmail1Map.getValue();
        var email2Map = argEmail2Map.getValue();
        assertThat(email1Map).isEqualTo(email2Map);
        Map<String, Object> expected = Map.ofEntries(
                entry("vault-review-date", ""), //1
                entry("vault-id", "vault1-id"), //2
                entry("home-page", "http://localhost:8080"), //3
                entry("help-mail", "help@example.com"), //4
                entry("vault-name", "vault1name"), //5
                entry("group-name", expectedGroupString), //6
                entry("help-page", "http://localhost:8080/help"), //7
                entry("vault-review-link", "http://localhost:8080/admin/vaults/vault1-id/reviews"), //8
                entry("retention-policy-expiry-date", "2026-04-04") //9
        );
        assertThat(email1Map).isEqualTo(expected);
    }

    @Test
    @Order(6)
    void testCheckVaultForReviewWhenDueForReviewAndNoExistingReviewEmailsNdmIfNoOwner() throws Exception {
        Vault mVault = mock(Vault.class);
        when(mVault.getID()).thenReturn(VAULT_1_ID);
        when(mVault.getName()).thenReturn(VAULT_1_NAME);
        when(mVault.isVaultReviewUnderway()).thenReturn(false);
        when(mVault.getGroup()).thenReturn(group1);
        when(mVault.getRetentionPolicyExpiry()).thenReturn(retentionPolicyExpiry1);

        when(mVaultsReviewService.dueForReviewEmail(mVault)).thenReturn(true);
        when(mRolesAndPermissionsService.getRoleAssignmentsForVault(VAULT_1_ID))
                .thenReturn(Arrays.asList(null, ndmRoleAssignment));
        when(mLdapService.getLDAPAttributes(NDM_USER_ID)).thenReturn(Collections.singletonMap("uid", "owner"));
        when(mUsersService.getUser(NDM_USER_ID)).thenReturn(ndmUser);

        checkForReviewSpy.checkVaultForReview(mVault);

        // Verify VaultReview is created
        verify(mVaultsReviewService, times(1)).createVaultReview(mVault);

        // Verify email to support team
        verify(mEmailService, times(1)).sendTemplateMail(eq(HELP_MAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_SUPPORT), argEmail1Map.capture());

        // Verify NO email to data owner
        verify(mEmailService, never()).sendTemplateMail(eq(VAULT_OWNER_EMAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_OWNER), anyMap());

        // Verify email to NDM if data owner is NOT found
        verify(mEmailService, times(1)).sendTemplateMail(eq(NDM_EMAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_DATA_MANAGER), argEmail2Map.capture());

        verify(mUsersService).getUser(NDM_USER_ID);
        verifyNoMoreInteractions(mVaultsService, mVaultsReviewService, mLdapService, mEmailService, mRolesAndPermissionsService, mUsersService);

        var email1Map = argEmail1Map.getValue();
        var email2Map = argEmail2Map.getValue();
        assertThat(email1Map).isEqualTo(email2Map);
        Map<String, Object> expected = Map.ofEntries(
                entry("vault-review-date", ""), //1
                entry("vault-id", "vault1-id"), //2
                entry("home-page", "http://localhost:8080"), //3
                entry("help-mail", "help@example.com"), //4
                entry("vault-name", "vault1name"), //5
                entry("group-name", "group1name"), //6
                entry("help-page", "http://localhost:8080/help"), //7
                entry("vault-review-link", "http://localhost:8080/admin/vaults/vault1-id/reviews"), //8
                entry("retention-policy-expiry-date", "2026-04-04") //9
        );
        assertThat(email1Map).isEqualTo(expected);
    }

    @Test
    @Order(7)
    void testCheckVaultForReviewWhenDueForReviewAndNoExistingReviewDontSendEmailsToNdmIfNoOwnerButNdmUserNotFound() throws Exception {
        Vault mVault = mock(Vault.class);
        when(mVault.getID()).thenReturn(VAULT_1_ID);
        when(mVault.getName()).thenReturn(VAULT_1_NAME);
        when(mVault.isVaultReviewUnderway()).thenReturn(false);
        when(mVault.getGroup()).thenReturn(group1);
        when(mVault.getRetentionPolicyExpiry()).thenReturn(retentionPolicyExpiry1);

        when(mVaultsReviewService.dueForReviewEmail(mVault)).thenReturn(true);
        when(mRolesAndPermissionsService.getRoleAssignmentsForVault(VAULT_1_ID))
                .thenReturn(Arrays.asList(null, ndmRoleAssignment));
        when(mLdapService.getLDAPAttributes(NDM_USER_ID)).thenReturn(Collections.singletonMap("uid", "owner"));
        when(mUsersService.getUser(NDM_USER_ID)).thenReturn(null);

        checkForReviewSpy.checkVaultForReview(mVault);

        // Verify VaultReview is created
        verify(mVaultsReviewService, times(1)).createVaultReview(mVault);

        // Verify email to support team
        verify(mEmailService, times(1)).sendTemplateMail(eq(HELP_MAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_SUPPORT), argEmail1Map.capture());

        // Verify NO email to data owner
        verify(mEmailService, never()).sendTemplateMail(eq(VAULT_OWNER_EMAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_OWNER), anyMap());

        // Verify email to NDM if data owner is NOT found
        verify(mEmailService, never()).sendTemplateMail(eq(NDM_EMAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_DATA_MANAGER), anyMap());

        verify(mUsersService).getUser(NDM_USER_ID);
        verifyNoMoreInteractions(mVaultsService, mVaultsReviewService, mLdapService, mEmailService, mRolesAndPermissionsService, mUsersService);

        var email1Map = argEmail1Map.getValue();
        Map<String, Object> expected = Map.ofEntries(
                entry("vault-review-date", ""), //1
                entry("vault-id", "vault1-id"), //2
                entry("home-page", "http://localhost:8080"), //3
                entry("help-mail", "help@example.com"), //4
                entry("vault-name", "vault1name"), //5
                entry("group-name", "group1name"), //6
                entry("help-page", "http://localhost:8080/help"), //7
                entry("vault-review-link", "http://localhost:8080/admin/vaults/vault1-id/reviews"), //8
                entry("retention-policy-expiry-date", "2026-04-04") //9
        );
        assertThat(email1Map).isEqualTo(expected);
    }

    static Stream<Arguments> ldapAttributeMapProvider() {
        return Stream.of(Arguments.of((Map<String, String>) null), Arguments.of(Map.of()));
    }

    /**
     * The ldapAttibutes is null or empty map.
     * @param ldapAttributes the ldapAttributes for the user.
     */
    @ParameterizedTest
    @MethodSource("ldapAttributeMapProvider")
    @Order(8)
    void testCheckVaultForReviewWhenDueForReviewAndNoExistingReviewDontSendEmailsToNdmIfNoOwnerButNdmUserNotInLdap(Map<String,String> ldapAttributes) throws Exception {
        Vault mVault = mock(Vault.class);
        when(mVault.getID()).thenReturn(VAULT_1_ID);
        when(mVault.getName()).thenReturn(VAULT_1_NAME);
        when(mVault.isVaultReviewUnderway()).thenReturn(false);
        when(mVault.getGroup()).thenReturn(group1);
        when(mVault.getRetentionPolicyExpiry()).thenReturn(retentionPolicyExpiry1);

        when(mVaultsReviewService.dueForReviewEmail(mVault)).thenReturn(true);
        when(mRolesAndPermissionsService.getRoleAssignmentsForVault(VAULT_1_ID))
                .thenReturn(Arrays.asList(null, ndmRoleAssignment));
        when(mLdapService.getLDAPAttributes(NDM_USER_ID)).thenReturn(ldapAttributes);

        checkForReviewSpy.checkVaultForReview(mVault);

        // Verify VaultReview is created
        verify(mVaultsReviewService, times(1)).createVaultReview(mVault);

        // Verify email to support team
        verify(mEmailService, times(1)).sendTemplateMail(eq(HELP_MAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_SUPPORT), argEmail1Map.capture());

        // Verify NO email to data owner
        verify(mEmailService, never()).sendTemplateMail(eq(VAULT_OWNER_EMAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_OWNER), anyMap());

        // Verify email to NDM if data owner is NOT found
        verify(mEmailService, never()).sendTemplateMail(eq(NDM_EMAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_DATA_MANAGER), anyMap());

        verifyNoMoreInteractions(mVaultsService, mVaultsReviewService, mLdapService, mEmailService, mRolesAndPermissionsService, mUsersService);

        var email1Map = argEmail1Map.getValue();
        Map<String, Object> expected = Map.ofEntries(
                entry("vault-review-date", ""), //1
                entry("vault-id", "vault1-id"), //2
                entry("home-page", "http://localhost:8080"), //3
                entry("help-mail", "help@example.com"), //4
                entry("vault-name", "vault1name"), //5
                entry("group-name", "group1name"), //6
                entry("help-page", "http://localhost:8080/help"), //7
                entry("vault-review-link", "http://localhost:8080/admin/vaults/vault1-id/reviews"), //8
                entry("retention-policy-expiry-date", "2026-04-04") //9
        );
        assertThat(email1Map).isEqualTo(expected);
    }

    /*
     * RoleAssignment has null userId
     */
    @Test
    @Order(9)
    void testCheckVaultForReviewWhenDueForReviewAndNoExistingReviewDontSendEmailsToNdmIfNoOwnerButRoleAssignmentHasNullUserId() {
        Vault mVault = mock(Vault.class);
        when(mVault.getID()).thenReturn(VAULT_1_ID);
        when(mVault.getName()).thenReturn(VAULT_1_NAME);
        when(mVault.isVaultReviewUnderway()).thenReturn(false);
        when(mVault.getGroup()).thenReturn(group1);
        when(mVault.getRetentionPolicyExpiry()).thenReturn(retentionPolicyExpiry1);

        RoleAssignment ndmRoleAssignmentWithNullUserId = new RoleAssignment();
        ndmRoleAssignmentWithNullUserId.setRole(roleNominatedDataManager);
        
        when(mVaultsReviewService.dueForReviewEmail(mVault)).thenReturn(true);
        when(mRolesAndPermissionsService.getRoleAssignmentsForVault(VAULT_1_ID))
                .thenReturn(Arrays.asList(null, ndmRoleAssignmentWithNullUserId));

        checkForReviewSpy.checkVaultForReview(mVault);

        // Verify VaultReview is created
        verify(mVaultsReviewService, times(1)).createVaultReview(mVault);

        // Verify email to support team
        verify(mEmailService, times(1)).sendTemplateMail(eq(HELP_MAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_SUPPORT), argEmail1Map.capture());

        // Verify NO email to data owner
        verify(mEmailService, never()).sendTemplateMail(eq(VAULT_OWNER_EMAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_OWNER), anyMap());

        // Verify email to NDM if data owner is NOT found
        verify(mEmailService, never()).sendTemplateMail(eq(NDM_EMAIL), anyString(), eq(EmailTemplate.REVIEW_DUE_DATA_MANAGER), anyMap());

        verifyNoMoreInteractions(mVaultsService, mVaultsReviewService, mLdapService, mEmailService, mRolesAndPermissionsService, mUsersService);

        var email1Map = argEmail1Map.getValue();
        Map<String, Object> expected = Map.ofEntries(
                entry("vault-review-date", ""), //1
                entry("vault-id", "vault1-id"), //2
                entry("home-page", "http://localhost:8080"), //3
                entry("help-mail", "help@example.com"), //4
                entry("vault-name", "vault1name"), //5
                entry("group-name", "group1name"), //6
                entry("help-page", "http://localhost:8080/help"), //7
                entry("vault-review-link", "http://localhost:8080/admin/vaults/vault1-id/reviews"), //8
                entry("retention-policy-expiry-date", "2026-04-04") //9
        );
        assertThat(email1Map).isEqualTo(expected);
    }

    @Test
    @Order(10)
    void testCheckVaultForReviewWhenDueForReviewAndExistingReviewThrowsError() {

        Vault mVault = mock(Vault.class);
        when(mVault.getID()).thenReturn(VAULT_1_ID);
        when(mVault.getName()).thenReturn(VAULT_1_NAME);
        when(mVault.isVaultReviewUnderway()).thenReturn(true);

        when(mVaultsReviewService.dueForReviewEmail(mVault)).thenReturn(true);

        // Expect an AssertionError because of the Assert.isTrue check
        var ex = assertThrows(IllegalArgumentException.class, () -> checkForReviewSpy.checkVaultForReview(mVault));
        assertThat(ex).hasMessage("We were about to create a new vault review - but a vault review is already underway");

        // Verify no VaultReview is created and no emails are sent
        verify(mVaultsReviewService, never()).createVaultReview(any(Vault.class));
        verify(mEmailService, never()).sendTemplateMail(anyString(), anyString(), anyString(), anyMap());
    }
}