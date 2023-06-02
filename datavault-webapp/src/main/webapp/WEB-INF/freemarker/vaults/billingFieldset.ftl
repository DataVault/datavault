<fieldset id="billing-fieldset">
    <div id="billing-section" class="form-card">
        <h2 class="fs-title">Billing</h2>
        <@spring.bind "vault.billingType" />
        <input id="billingType"
            type="hidden"
            name="${spring.status.expression}"
            value="${spring.status.value!''}">
        <div id="slice-query-box" class="row collapse">
            <div class="col-md-8">
                <p>
                    Will funding for this vault be from reserved DataVault storage (aka a 'slice') that
                    has already been purchased? A 'slice' is reserved DataVault storage.
                    If unsure, please ask your PI or the Research Data Support team,
                    details available thorough the Contact button. If you have a grant
                    and not a reserved slice please select 'no'.
                </p>
                <#-- FOR TESTING:  <p>DEBUG VAULT: ${vault?html}
                </p>  -->
            </div>
            <div class="col-md-4 well">
                <@spring.bind "vault.sliceQueryChoice" />
                <div class="radio">
                    <label>
                        <input type="radio"
                            id="slice-query-yes"
                            name="${spring.status.expression}"
                            value="YES"
                            <#if spring.status.value?? && spring.status.value?string=="YES">checked</#if>>
                        Yes
                    </label>
                </div>
                <div class="radio">
                    <label>
                        <input type="radio"
                            id="slice-query-no"
                            name="${spring.status.expression}"
                            value="NO"
                            <#if spring.status.value?? && spring.status.value?string=="NO">checked</#if>>
                        No
                    </label>
                </div>
                <div class="radio">
                    <label>
                        <input type="radio"
                            id="slice-query-do-not-know"
                            name="${spring.status.expression}"
                            value="DO_NOT_KNOW"
                            <#if spring.status.value?? && spring.status.value?string=="DO_NOT_KNOW">checked</#if>>
                        Don't know
                    </label>
                </div>
                <div class="radio">
                    <label>
                        <input type="radio"
                            id="slice-query-buy"
                            name="${spring.status.expression}"
                            value="BUY_NEW_SLICE"
                            <#if spring.status.value?? && spring.status.value?string=="BUY_NEW_SLICE">checked</#if>>
                        I want to buy a new slice of reserved DataVault storage
                    </label>
                </div>
                <button type="button"
                    class="btn btn-danger query-choice-clear"
                    id="slice-query-choice-clear">Clear selection</button>
            </div>
        </div>
        <@spring.bind "vault.fundingQueryChoice" />
        <div id="funding-query-box" class="row collapse">
            <div class="col-md-8">
                <p>
                    Is funding in place for this vault? In other words,
                    will you be paying for your storage? For example,
                    do you have a research grant we can charge against,
                    or a budget such as research centre core funding that
                    you will pay from?
                </p>
            </div>
            <div class="col-md-4 well">
                <div class="radio">
                    <label>
                        <input type="radio"
                            id="funding-query-yes"
                            name="${spring.status.expression}"
                            value="YES"
                            <#if spring.status.value?? && spring.status.value?string=="YES">checked</#if>>
                        Yes
                    </label>
                </div>
                <div class="radio">
                    <label>
                        <input type="radio"
                            id="funding-query-no"
                            name="${spring.status.expression}"
                            value="NO"
                            <#if spring.status.value?? && spring.status.value?string=="NO">checked</#if>>
                        No
                    </label>
                </div>
                <div class="radio">
                    <label>
                        <input type="radio"
                            id="funding-query-do-not-know"
                            name="${spring.status.expression}"
                            value="DO_NOT_KNOW"
                            <#if spring.status.value?? && spring.status.value?string=="DO_NOT_KNOW">checked</#if>>
                        Don't know
                    </label>
                </div>
                <button type="button"
                    class="btn btn-danger my-1 query-choice-clear"
                    id="funding-query-choice-clear">Clear selections and return to the top</button>
            </div>
        </div>
        <@spring.bind "vault.feewaiverQueryChoice" />
        <div id="feewaiver-query-box" class="row collapse">
            <div class="col-md-8">
                <p>
                    Do you wish to use the fee waiver -
                    are you confident this vault and any others
                    from the same project will have a total combined
                    size of under 100 GB?
                </p>
            </div>
            <div class="col-md-4 well">
                <div class="radio">
                    <label>
                        <input type="radio"
                            id="feewaiver-query-yes"
                            name="${spring.status.expression}"
                            value="YES"
                            <#if spring.status.value?? && spring.status.value?string=="YES">checked</#if> />
                        Yes
                    </label>
                </div>
                <div class="radio">
                    <label>
                        <input type="radio"
                            id="feewaiver-query-do-not-know"
                            name="${spring.status.expression}"
                            value="DO_NOT_KNOW"
                            <#if spring.status.value?? && spring.status.value?string=="DO_NOT_KNOW">checked</#if> />
                        Don't know
                    </label>
                </div>
                <button type="button"
                    class="btn btn-danger my-1 query-choice-clear"
                    id="feewaiver-query-choice-clear">Clear selections and return to top</button>
            </div>
        </div>
        <div id="slice-form" class="row collapse">
            <div class="well">
                <p>
                    A slice is reserved DataVault storage. If unsure, please check with your PI whether they have reserved a slice.
                    If you do have a slice, please enter the name or code such as 'Slice004'.
                </p>
                <div class="form-group required">
                    <label class="col-sm-2 control-label">Slice: </label>
                    <@spring.bind "vault.sliceID" />
                    <input type="text" id="sliceID" name="${spring.status.expression}" value="${spring.status.value!''}" />
                </div>
                </label>
            </div>
        </div>
        <div id="payment-details-form" class="row collapse">
            <div class="well">
                <p>Please provide the details we should use to send your bill to the correct finance team.</p>
                <div class="form-group required my-2">
                    <label class="col-sm-4 control-label">Authoriser:
                        <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                            title="The name of someone in your School/Unit or Sub-Unit who can authorise the payment."></span>
                    </label>
                    <@spring.bind "vault.budgetAuthoriser" />
                    <input type="text" class="col-sm-6" id="budget-authoriser" name="${spring.status.expression}" value="${spring.status.value!''}"  />
                </div>
                <div class="form-group my-2">
                    <label class="col-sm-4 control-label">
                        Payment details (if known):<br>
                        (see 'Data Vault - Cost' page on university website)
                    </label>
                    <@spring.bind "vault.paymentDetails" />
                    <textarea id="budget-payment-details" type="text" class="form-control" name="${spring.status.expression}"
                         rows="4" cols="60">
                        <#if vault.paymentDetails??>
                            ${vault.paymentDetails?html}
                        </#if>
                    </textarea>
                </div>
                <div class="form-group my-2">
                    <label for="billingGrantEndDate" class="col-sm-4 control-label">
                        Grant End Date:<span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                            title="This information will assist the university in ensuring the archive is kept for at least the minimum amount of time required by the funder(s). This field should be left blank if there is no grant associated with the work.&nbsp;"></span>
                    </label>
                    <@spring.bind "vault.grantEndDate" />
                    <input id="billingGrantEndDate" class="form-control date-picker col-sm-6" placeholder="yyyy-mm-dd" name="${spring.status.expression}"
                        value="${spring.status.value!''}" />
                    <span id="invalid-billing-grant-end-date-span" style="font-size: 1.2em; font: bold; color:  #AE0F0F; display: inline;"></span>
                </div>
                <button type="button"
                    class="btn btn-danger my-2 query-choice-clear"
                    id="payment-details-form-clear">Clear selection and return to the top</button>
            </div>
        </div>
    </div>
    <button type="button" name="previous" class="previous action-button-previous btn btn-default">&laquo; Previous</button>
    <#if vault.confirmed?c=="false">
        <button type="submit" name="save" value="Save" class="save action-button-previous btn btn-default">
            <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
        </button>
    </#if>
    <button type="button" name="next" class="next action-button btn btn-primary" disabled>Next Step &raquo;</button>
</fieldset>