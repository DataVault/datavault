<#import "*/layout/welcomelayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<#import "/spring.ftl" as spring />
<@layout.vaultLayout>

    <div class="container">
    
        <#if vaults?has_content>
        <h3>
            Current Vaults
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                title="Vaults of which you are either the Owner or the Nominated Data Manager. If you think a vault is missing from this list, please contact the Research Data Service">
        </h3>
        
        <div class="row">
            <div class="col-md-12">
                <div class="scrollable">
                    <table class="table table-bordered whitebackground">
                        <thead>
                            <tr>
                                <th>Vault Name</th>
                                <th>Deposit</th>
                                <th>Owner Name</th>
                                <th>Date Created</th>
                                <th class="text-muted">Review Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            <#list vaults as vault>
                            <tr>
                                <td>
                                    <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/">
                                        ${vault.name?html}
                                    </a>
                                </td>
                                <td>
                                    <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/create" class="btn btn-primary">
                                       <i class="fa fa-download fa-rotate-180" aria-hidden="true"></i> Deposit
                                   </a>
                                </td>
                                <td>${vault.getUserID()}</td>
                                <td>${vault.getCreationTime()?datetime}</td>
                                <td class="text-muted">${vault.getReviewDate()?string('dd/MM/yyyy')}</td>
                            </tr>
                            </#list>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <br/>
        <br/>
        </#if>
        
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
                            </small>
                        </h2>
                        
                        <br/>
                        
                        <p class="main">
                            You must record the details of your dataset in <a href="${link}" target="_blank">${system}</a>. 
                            If you haven't yet created a Pure dataset record containing the 
                            details of the data you're about to deposit in the vault, please do so. 
                        </p>
                        <br/>

                        <#if datasets?size == 0>
                        <div class="alert alert-danger" role="alert">
                            <strong>No Dataset available!</strong>
                        </div>
                        </#if>

                        <form id="create-vault" class="form" role="form" action="${springMacroRequestContext.getContextPath()}/vaults/create" method="post" novalidate="novalidate" _lpchecked="1">
                            <p class="main">
                                Select the Pure record describing the data that will be contained in this vault from the list below: 
                            </p>
                            
                            <div class="container">
                                <div class="row form-group required">
                                    <div class="col-sm-5">
                                        <select id="datasetID" name="datasetID" class="dataset-select selectpicker show-tick form-control form-control-lg" <#if datasets?size == 0> disabled</#if>>
                                            <option selected disabled data-hidden="true">Please choose a Dataset</option>
                                            <#list datasets as dataset>
                                            <option value="${dataset.getID()}">${dataset.name?html}</option>
                                            </#list>
                                        </select>
                                    </div>
                                    <label class="control-label">
                                    </label>
                                    <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                        title="Your Pure Dataset records should be listed here. Before you create your vault, the Pure Dataset record must be validated by Research Data Service staff.&nbsp;Please contact us if you have any questions.">
                                    </span>
                                </div>
                                <div class="col-sm-3"></div>
                                
                                <div class="alert alert-info" role="alert">
                                    <p>
                                    The ‘owner’ /PI will be billed for any deposit, 
                                    against the Grant ID they provided in relation to the vault, 
                                    unless they have made other arrangements with Information Services Research Services. 
                                    Rates are advertised on the 
                                    <a href="http://www.ed.ac.uk/is/research-support/datavault" target="_blank">Research Services charges page</a>.
                                    </p>
                                    <p>
                                    You will be aware that the data you place in the Vault belongs to the University of Edinburgh 
                                    and you will only be able to retrieve the data as long as you are employed by the University of Edinburgh.
                                    </p>
                                </div>
                            </div>
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
                                       placeholder="Enter a descriptive name for the Vault e.g. the project or experiment." <#if datasets?size == 0> disabled</#if>/>
                            </div>
                
                            <div class="form-group required">
                                <label for="description" class="control-label">Description</label>
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                    title="This description should contain information to assist you and any other colleagues who will be part of the review process when the vault retention period expires, in deciding whether the data should be retained or deleted. Maximum 6,000 characters.">
                                </span>
                                <@spring.bind "vault.description" />
                                <textarea type="text" class="form-control" name="description" id="description" rows="4" cols="60" <#if datasets?size == 0> disabled</#if>></textarea>
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
                                        <select id="policyID" name="policyID" data-width="auto" class="form-control retentionPolicy-select selectpicker show-tick" <#if datasets?size == 0> disabled</#if>>
                                            <option selected disabled data-hidden="true">Please choose a retention policy</option>
                                            <#list policies as retentionPolicy>
                                            <option value="${retentionPolicy.getID()}" 
                                                data-subtext="( Minimum period: ${retentionPolicy.minDataRetentionPeriod?html} )">${retentionPolicy.name?html}</option>
                                            </#list>
                                        </select>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="form-group required">
                                <label  for="grantEndDate" class="control-label">
                                    <strong>Grant End Date</strong>
                                </label>
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                     title="This information will assist the university in ensuring the archive is kept for at least the minimum amount of time required by the funder(s). This field should be left blank if there is no grant associated with the work.&nbsp;">
                                </span>
                                <@spring.bind "vault.grantEndDate" />
                                <input id="grantEndDate" name="grantEndDate" class="form-control" type="date" <#if datasets?size == 0> disabled</#if>/>
                            </div>
                            
                            <div class="alert alert-info" role="alert">
                                <p>
                                Vault will be closed so no more
                                deposits can be added, ONE calendar
                                year after the grant end date or one
                                year after the first deposit,
                                whichever is later.
                                </p>
                            </div>
                            
                            <div class="form-group required">
                                <label for="groupID" class="control-label">School</label>
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                    title="The School to which the vault belongs. In the absence of the vault Owner, School officers may be consulted on the management of the vault.">
                                </span>
                                <div class="row">
                                    <div class="col-md-12">
                                        <select id="groupID" name="groupID" data-width="auto" class="form-control group-select selectpicker show-tick" <#if datasets?size == 0> disabled</#if>>
                                            <option selected disabled data-hidden="true">Please choose a School</option>
                                            <#list groups as group>
                                            <#if group.enabled>
                                            <option value="${group.getID()}">${group.name?html}</option>
                                            </#if> 
                                            </#list>
                                        </select>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="form-group required">
                                <label for="reviewDate" class="control-label">
                                    <strong>Review Date</strong>
                                </label>
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                        title="The date by which the vault should be reviewed for decision as to whether it should be deleted or whether there are funds available to support continued storage.&nbsp;If you wish to extend the review date further into the future, please contact the support team to discuss the funding of the storage for the vault. If on the other hand you wish the vault to be deleted, bring the review date forward so that deletion may be considered.">
                                </span>
                                <@spring.bind "vault.reviewDate" />
                                <input class="form-control" id="reviewDate" name="reviewDate" type="date" <#if datasets?size == 0> disabled</#if>/>
                            </div>

                            <input type="hidden" id="submitAction" name="action" value="submit" /> 
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

                            <div class="btn-toolbar pull-right">
                                <button type="submit" value="submit" class="btn btn-lg btn-primary"<#if datasets?size == 0> disabled</#if>>
                                    <span class="glyphicon glyphicon-folder-close"></span>
                                    Create new Vault
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="//cdn.jsdelivr.net/webshim/1.14.5/polyfiller.js"></script>
    <script>
        webshims.setOptions('forms-ext', {types: 'date'});
        webshims.polyfill('forms forms-ext');

        $(document).ready(function () {

            $('button[type="submit"]').on("click", function() {
                $('#submitAction').val($(this).attr('value'));
            });

            $('#create-vault').validate({
            	debug: true,
                rules: {
                    name: {
                        required: true
                    },
                    description: {
                        required: true
                    },
                    datasetID : {
                        required: true
                    },
                    policyID : {
                        required: true
                    },
                    groupID : {
                        required: true
                    },
                    grantEndDate : {
                        required: true
                    },
                    reviewDate : {
                        required: true
                    }
                },
                highlight: function (element) {
                    $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
                },
                success: function (element) {
                    element.addClass('valid')
                        .closest('.form-group').removeClass('has-error').addClass('has-success');
                },
                submitHandler: function (form) {
                    $('button[type="submit"]').prop('disabled', true);
                    form.submit();
                }
            });


            $('.dataset-select').selectpicker();
            $('.retentionPolicy-select').selectpicker();
            $('.group-select').selectpicker();

            $('[data-toggle="tooltip"]').tooltip({
                'placement': 'right'
            });
        }); $('[data-toggle="popover"]').popover();
    </script>

</@layout.vaultLayout>
