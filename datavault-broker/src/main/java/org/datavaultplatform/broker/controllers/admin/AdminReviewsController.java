package org.datavaultplatform.broker.controllers.admin;

import org.datavaultplatform.broker.services.DepositsReviewService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.broker.services.VaultsReviewService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.model.User;
import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiHeader;
import org.jsondoc.core.annotation.ApiHeaders;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.pojo.ApiVerb;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Api(name="AdminReviews", description = "Administrator Review functions")
public class AdminReviewsController {

    private VaultsService vaultsService;
    private VaultsReviewService vaultsReviewService;
    private DepositsReviewService depositsReviewService;

    public void setVaultsService(VaultsService vaultsService) {
        this.vaultsService = vaultsService;
    }

    public void setVaultsReviewService(VaultsReviewService vaultsReviewService) {
        this.vaultsReviewService = vaultsReviewService;
    }

    public void setDepositsReviewService(DepositsReviewService depositsReviewService) {
        this.depositsReviewService = depositsReviewService;
    }


}
