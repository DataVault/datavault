<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<#import "/spring.ftl" as spring />
<#assign sec=JspTaglibs["http://www.springframework.org/security/tags"] />

<@layout.vaultLayout>
    
    <link href="/datavault-webapp/resources/application/stylesheets/create-prototype.css" rel="stylesheet" type="text/css">
    <script src="/datavault-webapp/resources/application/js/create-prototype.js"></script>
    

    <div class="container">
        <div class="row">
            <div class="col-md-12">
                <div class="panel panel-uoe-low">

                    <div class="associated-image">
                        <figure class="uoe-panel-image uoe-panel-image"></figure>
                    </div>

                    <div class="panel-body">
                        <h2>
                            Create new vault
                            <small>
                            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                  title="Multiple deposits may be made into one vault. Usually one vault will correspond to one Pure record">
                            </span>
                            </small>
                        </h2>

                        <br/>

                        <!-- MultiStep Form -->
                        <div class="container">
                            <div class="row justify-content-center mt-0">
                                <div class="col-10 text-center p-0 mt-3 mb-2">
                                    <div class="card px-0 pt-4 pb-0 mt-3 mb-3">
                                        <div class="row">
                                            <div class="col-md-12 mx-0">
                                                <form id="vault-creation-form">
                                                    <!-- progressbar -->
                                                    <ul id="progressbar">
                                                        <li class="active" id="affirmation"><strong>Affirmation</strong></li>
                                                        <li id="billing"><strong>Billing</strong></li>
                                                        <li id="vault-info"><strong>Vault Info</strong></li>
                                                        <li id="vault-access"><strong>Vault Access</strong></li>
                                                        <li id="vault-summary"><strong>Summary</strong></li>
                                                        <li id="vault-pending"><strong>Pending</strong></li>
                                                    </ul>
                                                    <!-- fieldsets -->
                                                    <fieldset>
                                                        <div class="form-card">
                                                            <h2 class="fs-title">Affirmation</h2>
                                                            <p>
                                                                In order to create a Vault you'll have to answer several questions.
                                                                You can go through each steps at your own pace and
                                                                come back to previous steps whenever you want.
                                                                <strong>
                                                                Don't forget to save your progress if your leaving the website
                                                                to make sure everything is there when you come back.
                                                                </strong>
                                                            </p>
                                                            <p>
                                                                I understand that the ‘owner’ /PI will be billed for any deposit
                                                                (rates are advertised on the Research Services charges page).
                                                                I understand that any files I deposit in the Vault belong to
                                                                the University of Edinburgh. I understand that access is for
                                                                authorised University of Edinburgh users only.
                                                                Therefore I will only be able to retrieve the data as long as
                                                                I am employed by the University of Edinburgh (as long as my
                                                                University login is active).
                                                            </p>
                                                            <div class="checkbox">
                                                                <label>
                                                                    <input id="affirmation-check" type="checkbox"> Accept
                                                                </label>
                                                            </div>
                                                        </div>
                                                        <button type="button" name="save" class="save action-button-previous btn btn-default" >
                                                            <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
                                                        </button>
                                                        <button type="button" name="next" class="next action-button btn btn-primary" disabled>Next Step &raquo;</button>
                                                    </fieldset>
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
                                                    <fieldset>
                                                        <div class="form-card">
                                                            <h2 class="fs-title text-center">Vault Users</h2> <br><br>

                                                            <h4>Vault Access</h4>

                                                            <div class="form-group" required>
                                                                <label class="control-label">Are you the owner of the vault:</label>
                                                                <div class="radio-inline">
                                                                    <label>
                                                                        <input type="radio" name="isOwner" value="true"> Yes
                                                                    </label>
                                                                </div>
                                                                <div class="radio-inline">
                                                                    <label>
                                                                        <input type="radio" name="isOwner" value="false" checked> No
                                                                    </label>
                                                                </div>
                                                            </div>
                                                            <div class="form-group" required>
                                                                <label class="col-sm-2 control-label">Owner UUN: </label>
                                                                <input id="owner-uun" type="text"  placeholder="autofilled uun with ldap"/>
                                                            </div>

                                                            <div class="form-group">
                                                                <label class="col-sm-2 control-label">NDMs: </label>
                                                                <input type="text" placeholder="autofilled uun with ldap" />
                                                                <button type="button" id="add-ndm-btn" class="btn btn-default btn-sm">Add a NDM</button>
                                                                <div id="extra-ndm-list"></div>
                                                                <div class="example-ndm hidden col-sm-offset-2">
                                                                    <input name="ndm-uun" class="ndm" type="text"  placeholder="autofilled uun with ldap"/>
                                                                    <button type="button" class="remove-ndm-btn btn btn-danger btn-xs">Remove</button>
                                                                </div>
                                                            </div>

                                                            <div id="depositors-form-group" class="form-group">
                                                                <label class="col-sm-2 control-label">Depositors: </label>
                                                                <input name="depositor-uun" class="depositor" type="text" placeholder="autofilled uun with ldap"/>
                                                                <button type="button" id="add-depositor-btn" class="btn btn-default btn-sm">Add a Depositor</button>
                                                                <div id="extra-depositor-list"></div>
                                                                <div class="example-depositor hidden col-sm-offset-2">
                                                                    <input name="depositor-uun" class="depositor" type="text"  placeholder="autofilled uun with ldap"/>
                                                                    <button type="button" class="remove-depositor-btn btn btn-danger btn-xs">Remove</button>
                                                                </div>
                                                            </div>

                                                            <h4>Pure Information</h4>

                                                            <div id="contact-form-group" class="form-group">
                                                                <label class="col-sm-2 control-label">Contact person: </label>
                                                                <input name="contact-uun" class="contact" type="text" placeholder="autofilled uun with ldap"/>
                                                            </div>

                                                            <div id="creators-form-group" class="form-group">
                                                                <label class="col-sm-2 control-label">Data Creator: </label>
                                                                <input name="creator-uun" class="creator" type="text" placeholder="autofilled uun with ldap"/>
                                                                <button type="button" id="add-creator-btn" class="btn btn-default btn-sm">Add a Data Creator</button>
                                                                <div id="extra-creator-list"></div>
                                                                <div class="example-creator hidden col-sm-offset-2">
                                                                    <input name="creator-uun" class="creator" type="text"  placeholder="autofilled uun with ldap"/>
                                                                    <button type="button" class="remove-creator-btn btn btn-danger btn-xs">Remove</button>
                                                                </div>
                                                            </div>

                                                            <div class="well">
                                                                A statement that encourages them to go to Pure later,
                                                                saying they can go to Pure to edit the dataset record
                                                                we will create automatically to describe this vault,
                                                                so they can link the dataset record to their papers and multiple projects.
                                                                <div class="alert alert-info">
                                                                    <strong>We recommend you review the Pure metadata record for accuracy and add links to research outputs and other projects/ people.</strong>
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <button type="button" name="previous" class="previous action-button-previous btn btn-default" >&laquo; Previous</button>
                                                        <button type="button" name="save" class="save action-button-previous btn btn-default" >
                                                            <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
                                                        </button>
                                                        <button type="button" name="next" class="next action-button btn btn-primary">Next Step &raquo;</button>
                                                    </fieldset>
                                                    <fieldset>
                                                        <div class="form-card">
                                                            <h2 class="fs-title">Summary</h2>
                                                            <div>
                                                                <table class="table table-sm">

                                                                    <tbody>
                                                                    <tr>
                                                                        <th scope="col">Vault Name</th>
                                                                        <td>Vault #1</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Description</th>
                                                                        <td>Created just for demo purpuses</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Estimate Size</th>
                                                                        <td>
                                                                            Over 100GB
                                                                        </td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">PURE ID</th>
                                                                        <td>67d7sad6asd69ad</td>
                                                                        <td></td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Grant End Date</th>
                                                                        <td>27 Septembre 2030</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Review Date</th>
                                                                        <td>01 August 2025</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Billing</th>
                                                                        <td>Slice [Slice008]</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Owner</th>
                                                                        <td>wpetit</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Named Data Manager</th>
                                                                        <td>wpetit, dspeed2</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Data Creators</th>
                                                                        <td>wpetit, dspeed2</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Depositors</th>
                                                                        <td>wpetit, dspeed2</td>
                                                                    </tr>
                                                                    </tbody>
                                                                </table>
                                                            </div>
                                                            <div>
                                                                Please make sure all information above are correct and click on confirm to process to your vault creation.
                                                            </div>
                                                            <div class="alert alert-warning">
                                                                <strong>PLEASE NOTE – this information will be public, and will be linked to the PI’s Pure profile.</strong>
                                                                <div class="checkbox">
                                                                    <label>
                                                                        <input id="affirmation-check" type="checkbox"> I agree
                                                                    </label>
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <button type="button" name="previous" class="previous action-button-previous btn btn-default" >&laquo; Previous</button>
                                                        <button type="button" name="save" class="save action-button-previous btn btn-default" >
                                                            <span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save
                                                        </button>
                                                        <button type="button" name="next" class="next action-button btn btn-success">
                                                            <span class="glyphicon glyphicon-ok" aria-hidden="true"></span> Confirm
                                                        </button>
                                                    </fieldset>
                                                    <fieldset>
                                                        <div class="form-card">
                                                            <h2 class="fs-title">Pending</h2>
                                                            <div>
                                                                <table class="table table-sm">

                                                                    <tbody>
                                                                    <tr>
                                                                        <th scope="col">Vault Name</th>
                                                                        <td>Vault #1</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Description</th>
                                                                        <td>Created just for demo purpuses</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Estimate Size</th>
                                                                        <td>
                                                                            Over 100GB
                                                                        </td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">PURE ID</th>
                                                                        <td>67d7sad6asd69ad</td>
                                                                        <td></td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Grant End Date</th>
                                                                        <td>27 Septembre 2030</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Review Date</th>
                                                                        <td>01 August 2025</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Billing</th>
                                                                        <td>Slice [Slice008]</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Owner</th>
                                                                        <td>wpetit</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Named Data Manager</th>
                                                                        <td>wpetit, dspeed2</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Data Creators</th>
                                                                        <td>wpetit, dspeed2</td>
                                                                    </tr>
                                                                    <tr>
                                                                        <th scope="col">Depositors</th>
                                                                        <td>wpetit, dspeed2</td>
                                                                    </tr>
                                                                    </tbody>
                                                                </table>
                                                            </div>
                                                            <div class="alert alert-info">
                                                                Creation Pending. Please wait for a member of our staff to validate your request.
                                                                You'll receive an email when it's done, blablabla...
                                                            </div>
                                                        </div>
                                                    </fieldset>
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
<#--                        </div>-->
                    </div>
                </div>
            </div>
        </div>
    </div>
   


</@layout.vaultLayout>