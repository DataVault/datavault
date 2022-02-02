<fieldset>
    <div class="form-card">
        <h2 class="fs-title">Billing</h2>
        Funding for this vault will be from:
        <@spring.bind "vault.billingType" />
        <div id="billing-selection-box" class="row">
            <div class="radio">
                <label>
                    <input type="radio" name="${spring.status.expression}" id="billing-choice-na" class="billing-choice" value="NA" <#if vault.billingType??>${(vault.billingType == 'NA')?then('checked', '')}</#if>>
                    N/A – the data volume will be under the project fee waiver threshold (<100GB). ​
                </label>
            </div>
            <div class="radio">
                <label>
                    <input type="radio" name="${spring.status.expression}" id="billing-choice-grantfunding" value="GRANT_FUNDING" <#if vault.billingType??>${(vault.billingType == 'GRANT_FUNDING')?then('checked', '')}</#if>>
                    Grant funding ​
                </label>
            </div>
            <div class="radio">
                <label>
                    <input type="radio" name="${spring.status.expression}" id="billing-choice-budgetcode" value="BUDGET_CODE" <#if vault.billingType??>${(vault.billingType == 'BUDGET_CODE')?then('checked', '')}</#if>>
                    A budget code. ​
                </label>
            </div>
            <div class="radio">
                <label class="col-sm-2 control-label">
                    <input type="radio" name="${spring.status.expression}" id="billing-choice-slice" value="SLICE" <#if vault.billingType??>${(vault.billingType == 'SLICE')?then('checked', '')}</#if>>
                    A Slice. ​
                </label>
            </div>
        </div>

        <div class="row">
            <div id="slice-form" class="collapse">
                <div class="well">
                    <p>
                        A slice is reserved DataVault storage. If unsure, please check with your PI whether they have reserved a slice.
                        If you do have a slice, please enter the name or code such as 'Slice004'.
                    </p>
                    <div class="form-group">
                        <label class="col-sm-2 control-label">Slice: </label>
                        <@spring.bind "vault.sliceID" />
                        <input type="text" id="sliceID" name="${spring.status.expression}" value="${spring.status.value!""}"/>
                    </div>
                </div>
            </div>
            <div id="grant-billing-form" class="collapse">
                <div class="well">
                    <p>Please provide the details we should use to send your bill (your eIT) to the correct finance team.</p>
                    <div class="form-group required">
                        <label class="col-sm-2 control-label">Authoriser:
                            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                  title="The name of someone in your School/Unit or Sub-Unit who can authorise the payment of the eIT."></span>
                        </label>
                        <@spring.bind "vault.grantAuthoriser" />
                        <input type="text" id="authoriser" name="${spring.status.expression}" value="${spring.status.value!""}"/>
                    </div>
                    <div class="form-group required">
                        <label class="col-sm-2 control-label">School/Unit:</label>
                        <@spring.bind "vault.grantSchoolOrUnit" />
                        <input type="text" id="schoolOrUnit" name="${spring.status.expression}" value="${spring.status.value!""}" />
                    </div>
                    <div class="form-group required">
                        <label class="col-sm-2 control-label">Subunit:</label>
                        <@spring.bind "vault.grantSubunit" />
                        <input type="text" id="subunit" name="${spring.status.expression}" value="${spring.status.value!""}" />
                    </div>
                    <div class="form-group required">
                        <label class="col-sm-2 control-label">Project Title:
                            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                  title="If you are planning to pay the bill from a grant, please enter the Project Title."></span>
                        </label>
                        <@spring.bind "vault.projectTitle" />
                        <input type="text" id="projectTitle" name="${spring.status.expression}" value="${spring.status.value!""}" />
                    </div>
                    <div class="form-group required">
                        <label  for="billingGrantEndDate" class="col-sm-2 control-label">
                            Grant End Date:<span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                                 title="This information will assist the university in ensuring the archive is kept for at least the minimum amount of time required by the funder(s). This field should be left blank if there is no grant associated with the work.&nbsp;"></span>
                        </label>

                        <@spring.bind "vault.billingGrantEndDate" />
                        <input id="billingGrantEndDate" class="form-control date-picker" placeholder="yyyy-mm-dd" name="${spring.status.expression}"
                               value="${spring.status.value!""}"/>
                         <span id="invalid-billing-grant-end-date-span" style="font-size: 1.2em; font: bold; color: #f00; display: inline;"></span>
                    </div>
                </div>
            </div>
            <div id="budget-billing-form" class="collapse">
                <div class="well">
                    <p>Please provide the details we should use to send your bill (your eIT) to the correct finance team.</p>
                    <div class="form-group required">
                        <label class="col-sm-2 control-label">Authoriser:
                            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                  title="The name of someone in your School/Unit or Sub-Unit who can authorise the payment of the eIT."></span>
                        </label>
                        <@spring.bind "vault.budgetAuthoriser" />
                        <input type="text" id="budget-authoriser" name="${spring.status.expression}" value="${spring.status.value!""}"/>
                    </div>
                    <div class="form-group required">
                        <label class="col-sm-2 control-label">School/Unit:</label>
                        <@spring.bind "vault.budgetSchoolOrUnit" />
                        <input type="text" id="budget-schoolOrUnit" name="${spring.status.expression}" value="${spring.status.value!""}" />
                    </div>
                    <div class="form-group required">
                        <label class="col-sm-2 control-label">Subunit:</label>
                        <@spring.bind "vault.budgetSubunit" />
                        <input type="text" id="budget-subunit" name="${spring.status.expression}" value="${spring.status.value!""}" />
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div>
        Select one of the billing option above.
    </div>
    <button type="button" name="previous" class="previous action-button-previous btn btn-default" >&laquo; Previous</button>
    <#if vault.confirmed?c=="false">
    <button type="submit" name="save" value ="Save" class="save action-button-previous btn btn-default" >
        <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
    </button>
    </#if>
    <button type="button" name="next" class="next action-button btn btn-primary" disabled>Next Step &raquo;</button>
</fieldset>
