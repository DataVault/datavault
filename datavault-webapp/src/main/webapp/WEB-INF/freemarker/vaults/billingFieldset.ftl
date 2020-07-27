<fieldset>
    <div class="form-card">
        <h2 class="fs-title">Billing</h2>
        Funding for this vault will be from:
        <div id="billing-selection-box" class="row">
            <div class="radio">
                <label>
                    <input type="radio" name="billing-choices" id="billing-choice-na" class="billing-choice" value="na">
                    N/A – the data volume will be under the project fee waiver threshold (<100GB). ​
                </label>
            </div>
            <div class="radio">
                <label>
                    <input type="radio" name="billing-choices" id="billing-choice-grantfunding" value="grantfunding">
                    Grant funding [open dialog to select/specify a project and provide the grant end date for the purposes of when the eIT must be sent by.]​
                </label>
            </div>
            <div class="radio">
                <label>
                    <input type="radio" name="billing-choices" id="billing-choice-budgetcode" value="budgetcode">
                    A budget code. ​
                </label>
            </div>
            <div class="radio">
                <label class="col-sm-2 control-label">
                    <input type="radio" name="billing-choices" id="billing-choice-slice" value="slice">
                    A Slice. ​
                </label>
            </div>
        </div>

        <div class="row">
            <div id="slice-form" class="collapse">
                <div class="well">
                    <p>
                        A slice is when you have reserved a DataVault storage by paying before creating the vault.
                        Usually called something like 'Slice008' or 'CSMB2020'.
                        If you have a slice, fill in the follow field with the name or code of the Slice.
                    </p>
                    <div class="form-group">
                        <label class="col-sm-2 control-label">Slice: </label>
                        <input type="text" />
                    </div>
                </div>
            </div>
            <div id="billing-form" class="collapse">
                <div class="well">
                    <p>Please provide the details we should use to send your bill (your eIT) to the correct finance team.</p>
                    <div class="form-group">
                        <label class="col-sm-2 control-label">Athoriser*:
                            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                  title="The name of someone in your School/Unit or Sub-Unit who can authorise the payment of the eIT."></span>
                        </label>
                        <input type="text" />
                    </div>
                    <div class="form-group">
                        <label class="col-sm-2 control-label">School/Unit*:</label> <input type="text" />
                    </div>
                    <div class="form-group">
                        <label class="col-sm-2 control-label">Subunit*:</label> <input type="text" />
                    </div>
                    <div class="form-group">
                        <label class="col-sm-2 control-label">ProjectId:
                            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                  title="If you are planning to pay the bill from a grant, please select the Project from this list."></span>
                        </label> <input type="text" />
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div>
        Select one of the billing option above.
    </div>
    <button type="button" name="previous" class="previous action-button-previous btn btn-default" >&laquo; Previous</button>
    <button type="button" name="save" class="save action-button-previous btn btn-default" >
        <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
    </button>
    <button type="button" name="next" class="next action-button btn btn-primary" disabled>Next Step &raquo;</button>
</fieldset>