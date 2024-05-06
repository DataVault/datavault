package org.datavaultplatform.webapp.controllers.admin;

import lombok.SneakyThrows;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.datavaultplatform.webapp.services.RestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ui.ModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AdminRetentionPoliciesControllerTest {
    
    RestService mRestService = Mockito.mock(RestService.class);
    
    AdminRetentionPoliciesController controller  = new AdminRetentionPoliciesController(mRestService);
    
    
    @Test
    @SneakyThrows
    void editRetentionPolicyIdsDoMatch() {

        CreateRetentionPolicy createRetentionPolicy = new CreateRetentionPolicy();
        createRetentionPolicy.setId(222);

        String result = controller.editRetentionPolicy(createRetentionPolicy, new ModelMap(), "222", "notCancel");

        Mockito.verify(mRestService).editRetentionPolicy(createRetentionPolicy);
        assertEquals("redirect:/admin/retentionpolicies", result);
    }

    @Test
    @SneakyThrows
    void editRetentionPolicyIdsDoNotMatch() {

        CreateRetentionPolicy createRetentionPolicy = new CreateRetentionPolicy();
        createRetentionPolicy.setId(222);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            controller.editRetentionPolicy(createRetentionPolicy, new ModelMap(), "111", "notCancel");
        });
        assertThat(ex).hasMessage("retentionPolicyId mismatch URL[111] form[222]");
    }
}