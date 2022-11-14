package org.datavaultplatform.broker.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import org.datavaultplatform.broker.controllers.MetadataController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

public class MetaDataControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  MetadataController controller;

  @Test
  void testGetDataset() {

    when(controller.getDataset(USER_ID_1, "dataset-id-1")).thenReturn(AuthTestData.DATASET_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/metadata/datasets/{datasetid}", "dataset-id-1"),
        AuthTestData.DATASET_1);

    verify(controller).getDataset(USER_ID_1, "dataset-id-1");
  }

  @Test
  void testGetDatasets() {

    when(controller.getDatasets(USER_ID_1)).thenReturn(
        Arrays.asList(AuthTestData.DATASET_1, AuthTestData.DATASET_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/metadata/datasets"),
        Arrays.asList(AuthTestData.DATASET_1, AuthTestData.DATASET_2));

    verify(controller).getDatasets(USER_ID_1);
  }

  @AfterEach
  void securityCheck() {
    checkHasSecurityUserAndClientUserRolesOnly();
  }

}
