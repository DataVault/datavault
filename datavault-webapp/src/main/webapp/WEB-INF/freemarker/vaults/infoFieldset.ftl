<fieldset>
    <div class="form-card">
        <h2 class="fs-title">Vault Information</h2>

        <div class="form-group required">
            <label for="vaultName" class="control-label">Vault Name</label>
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                  title="Maximum 400 characters.">
                                                                </span>
            <@spring.bind "vault.name" />
            <input type="text"
                   class="form-control"
                   name="${spring.status.expression}"
                   value="${spring.status.value!""}"
                   id="vaultName"
                   placeholder="Enter a descriptive name for the Vault e.g. the project or experiment."/>
        </div>

        <div class="form-group required">
            <label for="description" class="control-label">Description</label>
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                  title="Maximum 6,000 characters.">
                                                                </span>
            <@spring.bind "vault.description" />
            <textarea type="text" class="form-control" name="${spring.status.expression}" value="${spring.status.value!""}" id="description" rows="4" cols="60"><#if vault.description??>${vault.description?html}</#if></textarea>
        </div>

        <div class="form-group">
            <label  for="grantEndDate" class="control-label">
                <strong>Grant End Date</strong>
            </label>
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                  title="This information will assist the university in ensuring the archive is kept for at least the minimum amount of time required by the funder(s). This field should be left blank if there is no grant associated with the work.&nbsp;">
            </span>
            <@spring.bind "vault.grantEndDate" />
            <input id="grantEndDate" class="form-control date-picker" placeholder="yyyy-mm-dd" name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
             <span id="invalid-grant-end-date-span" style="font-size: 1.2em; font: bold; color: #f00; display: inline;"></span>
        </div>

        <div class="alert alert-info" role="alert">
            <p>
                You will not be able to add new deposits to this Vault once it is closed.
                The Vault will be closed ONE calendar year after the first deposit.
                Or, if you specify a Grant End Date, ONE calendar year after the Grant End Date IF that falls later than one year after the first deposit.
            </p>
        </div>

        <div class="form-group required">
            <label for="policyInfo" class="control-label">Retention Policy (tell us how long we must keep the data)</label>
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                  title="Tell us which funder's policy we must comply with. This tells us the minimum amount of time we must keep the data. This information is important when deciding on the Review Date. If there is no funder, choose the University of Edinburgh retention policy. If there are multiple funders, choose the one with the longest minimum retention period. ">
                                                                </span>
            <a href="https://www.ed.ac.uk/information-services/research-support/research-data-service/planning-your-data/funder-requirements">
                Read more about retention policies
            </a>
            <div class="row">
                <div class="col-md-12">
                    <select id="policyInfo" name="policyInfo" data-width="auto" class="form-control retentionPolicy-select selectpicker show-tick">
                        <option selected disabled data-hidden="true">Please choose a retention policy</option>
                        <#list policies as retentionPolicy>
                            <option value="${retentionPolicy.getID()}-${retentionPolicy.minRetentionPeriod}" 
                            <#if vault.policyInfo??>${(vault.policyInfo == retentionPolicy.getPolicyInfo())?then('selected', 'true')}</#if>
                            >${retentionPolicy.name?html} (Minimum period: ${retentionPolicy.minRetentionPeriod?html})</option>
                        </#list>
                    </select>
                </div>
            </div>
        </div>

        <div class="form-group required">
            <label for="reviewDate" class="control-label">
                <strong>Review Date (tell us how long you want us to keep the data)</strong>
            </label>
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                  title="The date by which the vault should be reviewed for decision as to whether it should be deleted or whether there are funds available to support continued storage.&nbsp;If you wish to extend the review date further into the future, please contact the support team to discuss the funding of the storage for the vault.">
                                                                </span>
            <@spring.bind "vault.reviewDate" />
            <input class="form-control" id="reviewDate" placeholder="yyyy-mm-dd" name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
                    <span id="updated-review-date-span" style="font-size: 1.2em; font: bold; color: #006400; display: inline;"></span>
                   <span id="invalid-review-date-span" style="font-size: 1.2em; font: bold; color: #f00; display: inline;"></span>
        </div>

        <div class="form-group required">
            <label for="groupID" class="control-label">School</label>
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                  title="The School to which the vault belongs. In the absence of the vault Owner, School officers may be consulted on the management of the vault.">
                                                                </span>
            <div class="row">
                <div class="col-md-12">
                    <select id="groupID" name="groupID" data-width="auto" class="form-control group-select selectpicker show-tick">
                        <option selected disabled data-hidden="true">Please choose a School</option>
                        <#list groups as group>
                            <#if group.enabled>
                                <option value="${group.getID()}" <#if vault.groupID??>${(vault.groupID == group.getID())?then('selected', 'true')}</#if>>${group.name?html}</option>
                            </#if>
                        </#list>
                    </select>
                </div>
                <div class="col-md-12">
                    <small><small>
                            Biomedical Sciences (e.g. Centre for Discovery Brain Sciences),
                            Clinical Sciences (e.g. MRC Centre for Inflammation Research)
                            and Molecular, Genetic and Population Health Sciences (e.g. IGMM)
                            are all ultimately under the stewardship of the Edinburgh Medical School.
                            We encourage colleagues to select their deanery rather than EMS,
                            for the ease of identifying the appropriate support staff for the
                            purposes of the review process and so on.
                        </small></small>
                </div>
            </div>
        </div>

        <div class="form-group" required>
            <label class="control-label">Rough estimate of the amount of data:</label>
            <div class="radio">
                <label>
                    <input type="radio" name="estimate" value="UNDER_100GB" <#if vault.estimate??>${(vault.estimate == 'UNDER_100GB')?then('checked', '')}</#if>> Under 100 GB
                </label>
            </div>
            <div class="radio">
                <label>
                    <input type="radio" name="estimate" value="UNDER_10TB" <#if vault.estimate??>${(vault.estimate == 'UNDER_10TB')?then('checked', '')}</#if>> Between 100 GB and 10 TB
                </label>
            </div>
            <div class="radio">
                <label>
                    <input type="radio" name="estimate" value="OVER_10TB" <#if vault.estimate??>${(vault.estimate == 'OVER_10TB')?then('checked', '')}</#if>> Over 10 TB
                </label>
            </div>
            <div class="radio">
                <label>
                    <input type="radio" name="estimate" value="UNKNOWN" <#if vault.estimate??>${(vault.estimate == 'UNKNOWN')?then('checked', '')}</#if>> Don’t know
                </label>
            </div>
        </div>

        <div class="form-group">
            <label for="notes" class="control-label">Notes regarding data retention and possible sharing</label>
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                  title="Information to assist you and any other colleagues who will be part of the review process when the vault retention period expires, in deciding whether the data should be retained or deleted. Please also use this field to explain whether requests for access to the data should be refused on a vault-wide basis, or whether certain criteria should be applied and/or any Data Sharing Agreement signed before any requests are granted. Please add a blank copy of the Data Sharing Agreement to your Vault as a separate deposit.">
            </span>
            <@spring.bind "vault.notes" />
            <textarea type="text" class="form-control" name="notes" id="notes" name="${spring.status.expression}" value="${spring.status.value!""}" rows="4" cols="60"><#if vault.notes??>${vault.notes?html}</#if></textarea>
        </div>
    </div>

    <button type="button" name="previous" class="previous action-button-previous btn btn-default" >&laquo; Previous</button>
    <#if vault.confirmed?c=="false">
    <button type="submit" name="save" value="Save" class="save action-button-previous btn btn-default" >
        <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
    </button>
    </#if>
    <button type="button" name="next" class="next action-button btn btn-primary" disabled>Next Step &raquo;</button>
</fieldset>