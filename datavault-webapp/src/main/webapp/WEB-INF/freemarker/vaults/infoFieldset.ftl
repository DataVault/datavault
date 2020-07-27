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
                  title="This description should contain information to assist you and any other colleagues who will be part of the review process when the vault retention period expires, in deciding whether the data should be retained or deleted. Maximum 6,000 characters.">
                                                                </span>
            <@spring.bind "vault.description" />
            <textarea type="text" class="form-control" name="description" id="description" rows="4" cols="60"></textarea>
        </div>

        <div class="form-group required">
            <label for="policyID" class="control-label">Retention Policy</label>
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                  title="This field indicates the policy with which we must comply, for the purpose of deciding the minimum amount of time for which this dataset &nbsp;must be archived. In most cases this will be the funder's policy. If there are multiple funders, it should be the one with the longest minimum among them.">
                                                                </span>
            <a href="https://www.ed.ac.uk/information-services/research-support/research-data-service/planning-your-data/funder-requirements">
                Read more about retention policies
            </a>
            <div class="row">
                <div class="col-md-12">
                    <select id="policyID" name="policyID" data-width="auto" class="form-control retentionPolicy-select selectpicker show-tick">
                        <option selected disabled data-hidden="true">Please choose a retention policy</option>
                        <#list policies as retentionPolicy>
                            <option value="${retentionPolicy.getID()}"
                                    data-subtext="( Minimum period: ${retentionPolicy.minDataRetentionPeriod?html} )">${retentionPolicy.name?html}</option>
                        </#list>
                    </select>
                </div>
            </div>
        </div>

        <div class="form-group">
            <label  for="grantEndDate" class="control-label">
                <strong>Grant End Date</strong>
            </label>
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                  title="This information will assist the university in ensuring the archive is kept for at least the minimum amount of time required by the funder(s). This field should be left blank if there is no grant associated with the work.&nbsp;">
                                                                </span>
            <@spring.bind "vault.grantEndDate" />
            <input id="grantEndDate" name="grantEndDate" class="form-control date-picker" placeholder="yyyy-mm-dd"/>
        </div>

        <div class="alert alert-info" role="alert">
            <p>
                You will not be able to add new deposits to this Vault once it is closed.
                The Vault will be closed ONE calendar year after the first deposit.
                Or, if you specify a Grant End Date, ONE calendar year after the Grant End Date IF that falls later than one year after the first deposit.
            </p>
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
                                <option value="${group.getID()}">${group.name?html}</option>
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

        <div class="form-group required">
            <label for="reviewDate" class="control-label">
                <strong>Review Date (typically ten years from now)</strong>
            </label>
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                  title="The date by which the vault should be reviewed for decision as to whether it should be deleted or whether there are funds available to support continued storage.&nbsp;If you wish to extend the review date further into the future, please contact the support team to discuss the funding of the storage for the vault.">
                                                                </span>
            <@spring.bind "vault.reviewDate" />
            <input class="form-control" id="reviewDate" name="reviewDate" placeholder="yyyy-mm-dd"/>
        </div>

        <div class="form-group" required>
            <label class="control-label">Rough estimate of the amount of data:</label>
            <div class="radio">
                <label>
                    <input type="radio" name="isOwner" value="true"> Under 100 GB
                </label>
            </div>
            <div class="radio">
                <label>
                    <input type="radio" name="isOwner" value="false"> Between 100 GB and 10 TB
                </label>
            </div>
            <div class="radio">
                <label>
                    <input type="radio" name="isOwner" value="false"> Over 10 TB
                </label>
            </div>
            <div class="radio">
                <label>
                    <input type="radio" name="isOwner" value="false"> Don’t know
                </label>
            </div>
        </div>

        <div class="form-group">
            <label for="description" class="control-label">Notes regarding data retention</label>
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                  title="Please note anything that would inform a future data owner , more specific information can be added with each deposit.">
                                                                </span>
            <textarea type="text" class="form-control" name="description" id="description" rows="4" cols="60"></textarea>
        </div>
    </div>

    <button type="button" name="previous" class="previous action-button-previous btn btn-default" >&laquo; Previous</button>
    <button type="button" name="save" class="save action-button-previous btn btn-default" >
        <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
    </button>
    <button type="button" name="next" class="next action-button btn btn-primary">Next Step &raquo;</button>
</fieldset>