<#import "*/layout/welcomelayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<#import "/spring.ftl" as spring />
<@layout.vaultLayout>

    <div class="container">
    
        <h3>
            Current Vaults
            <a class="btn btn-default pad" data-toggle="popover" data-trigger="hover"
               data-content="Vaults of which you are either the Owner or the Nominated Data Manager. If you think a vault is missing from this list, please contact the Research Data Service"
               data-original-title="" title="">?</a>
        </h3>
        <div class="row">
            <div class="col-md-12">
                <a class="btn btn-link pull-right" href="${springMacroRequestContext.getContextPath()}/vaults/create">
                    <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Create new Vault
                </a>
            </div>
        </div>
        
        <div class="row">
            <div class="col-md-12">
                <div class="scrollable">
                    <table class="table table-bordered whitebackground">
                        <thead>
                            <tr>
                                <th>Vault Name</th>
                                <th>Deposit 
                                    <span class="glyphicon glyphicon-question-sign" aria-hidden="true" data-toggle="tooltip" 
                                        title="What is this column?">
                                    </span>
                                </th>
                                <th>Owner Name</th>
                                <th>Date Created</th>
                                <th>Review Date</th>
                                <th>Deposit/Retrieval Completeness
                                    <span class="glyphicon glyphicon-question-sign" aria-hidden="true" data-toggle="tooltip" 
                                        title="Is it the completness of the last deposit/retrieval?">
                                    </span>
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <#list vaults as vault>
                            <tr>
                                <td><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/">${vault.name?html}</a></td>
                                <td>
                                    <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/create"
                                       class="btn btn-primary">Deposit</a>
                                </td>
                                <td>${vault.getUserID()}</td>
                                <td>${vault.getCreationTime()?datetime}</td>
                                <td>00/00/0000</td>
                                <td>
                                    <div class="progress">
                                        <div class="progress-bar" role="progressbar"
                                            aria-valuenow="70" aria-valuemin="0"
                                            aria-valuemax="100" style="width: 70%">
                                        </div>
                                    </div>
                                </td>
                            </tr>
                            </#list>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <br/>
        <br/>
        <div class="row">
            <div class="col-md-12">
                <#if datasets?has_content>
                <form id="create-vault" class="form" role="form" action="vaults/create" method="post">
                    <div class="panel panel-uoe-low">

                        <div class="associated-image">
                            <figure class="uoe-panel-image uoe-panel-image"></figure>
                        </div>
                        
                        <div class="panel-body">

                            <span class="glyphicon glyphicon-question-sign" aria-hidden="true" data-toggle="tooltip"
                                  title="Should we not only create a Vault here? Do we really want to put it on the same page?">
                            </span>
                            <h2>
                                Create new vault

                                <a class="btn btn-default pad" data-toggle="popover" data-trigger="hover"
                                   data-content="Multiple deposits may be made into one vault. Usually one vault will correspond to one Pure record"
                                   data-original-title="" title="">?</a>
                            </h2>
                            
                            <br/>
                            
                            <p class="main">
                                You must record the details of your dataset in <a href="#">PURE</a>. 
                                If you haven't yet created a Pure dataset record containing the 
                                details of the data you're about to deposit in the vault, please do so. 
                            </p>
                            <br>
                            <p class="main">
                                Please select the Pure record describing the data that will be contained in this vault from the list below: 
                            </p>
                            
                            <div class="container">
                                <div class="row">
                                    <div class="col-sm-5">
                                        <select id="datasetID" name="datasetID" class="dataset-select selectpicker show-tick form-control form-control-lg">
                                            <#list datasets as dataset>
                                            <option value="${dataset.getID()}">${dataset.name?html}</option>
                                            </#list>
                                        </select>
                                    </div>
                                    <a class="btn btn-default pad" data-toggle="popover" data-trigger="hover"
                                       data-content="Your Pure Dataset records should be listed here. Before you create your vault, the Pure Dataset record must be validated by Research Data Service staff.&nbsp;Please contact us if you have any questions."
                                       data-original-title="" title="">?</a>
                                    <span class="text-muted">
                                        <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                            title="Your Pure Dataset records should be listed here. Before you create your vault, the Pure Dataset record must be validated by Research Data Service staff.&nbsp;Please contact us if you have any questions.">
                                        </span>
                                    </span>
                                </div>
                                <div class="col-sm-3"></div>
                                
                                <div class="alert alert-info" role="alert">
                                    <p>
                                    The ‘owner’ /PI will be billed for any deposit, 
                                    against the Grant ID they provided in relation to the vault, 
                                    unless they have made other arrangements with Information Services Research Services. 
                                    Rates are advertised on the Research Services charges page.
                                    </p>
                                    <p>
                                    You will be aware that the data you place in the Vault belongs to the University of Edinburgh 
                                    and you will only be able to retrieve the data as long as you are employed by the University of Edinburgh.
                                    </p>
                                </div>
                            </div>
                            <div class="form-group">
                                <label class="control-label">Vault Name</label>
                                <a class="btn btn-default pad" data-toggle="popover" data-trigger="hover"
                                   data-content="Maximum 400 characters."
                                   data-original-title="" title="">?</a>
                                <span class="text-muted">
                                    <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                        title="Maximum 400 characters.">
                                    </span>
                                </span>
                                <@spring.bind "vault.name" />
                                <input type="text"
                                       class="form-control"
                                       name="${spring.status.expression}"
                                       value="${spring.status.value!""}"
                                       placeholder="Enter a descriptive name for the Vault e.g. the project or experiment."/>
                            </div>
                
                            <div class="form-group">
                                <label class="control-label">Description</label>
                                <a class="btn btn-default pad" data-toggle="popover" data-trigger="hover"
                                   data-content="This description should contain information to assist you and any other colleagues who will be part of the review process when the vault retention period expires, in deciding whether the data should be retained or deleted. Maximum 6,000 characters."
                                   data-original-title="" title="">?</a>
                                <span class="text-muted">
                                    <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                        title="This description should contain information to assist you and any other colleagues who will be part of the review process when the vault retention period expires, in deciding whether the data should be retained or deleted. Maximum 6,000 characters.">
                                    </span>
                                </span>
                                <@spring.bind "vault.description" />
                                <textarea type="text" class="form-control" name="description" rows="4" cols="60"></textarea>
                            </div>
                            
                            <span class="glyphicon glyphicon-question-sign" aria-hidden="true" data-toggle="tooltip" 
                                title="Here I removed the 'Dataset is..' as I don't see why it's necessary as it's displayed above?">
                            </span>
                            
                            <div class="form-group">
                                <label for="policyID">Retention Policy</label>
                                <a class="btn btn-default pad" data-toggle="popover" data-trigger="hover"
                                   data-content="This field indicates the policy with which we must comply, for the purpose of deciding the minimum amount of time for which this dataset &nbsp;must be archived. In most cases this will be the funder's policy. If there are multiple funders, it should be the one with the longest minimum among them."
                                   data-original-title="" title="">?</a>
                                <span class="text-muted">
                                    <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                        title="This field indicates the policy with which we must comply, for the purpose of deciding the minimum amount of time for which this dataset &nbsp;must be archived. In most cases this will be the funder's policy. If there are multiple funders, it should be the one with the longest minimum among them.">
                                    </span>
                                </span>
                                <a href="https://www.ed.ac.uk/information-services/research-support/research-data-service/planning-your-data/funder-requirements">
                                    Read more about retention policies
                                </a>
                                
                                <select id="policyID" name="policyID" data-width="fit" 
                                    class="form-control">
                                    <option selected disabled data-hidden="true">Please choose a retention policy</option>
                                    <#list policies as retentionPolicy>
                                    <option value="${retentionPolicy.getID()}"
                                        data-subtext="${retentionPolicy.description?html}">${retentionPolicy.name?html}</option>
                                    </#list>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <label>
                                    <strong>Grant End Date</strong>
                                    <a class="btn btn-default pad" data-toggle="popover" data-trigger="hover"
                                       data-content="This information will assist the university in ensuring the archive is kept for at least the minimum amount of time required by the funder(s). This field should be left blank if there is no grant associated with the work.&nbsp;"
                                       data-original-title="" title="">?</a>
                                    <span class="text-muted">
                                        <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                            title="This information will assist the university in ensuring the archive is kept for at least the minimum amount of time required by the funder(s). This field should be left blank if there is no grant associated with the work.&nbsp;">
                                        </span>
                                    </span>
                                    <span class="glyphicon glyphicon-question-sign" aria-hidden="true" data-toggle="tooltip" 
                                        title="Here I've used a input box of type 'date' instead of having 3 input boxes.">
                                    </span>
                                </label>
                                <input class="form-control" id="endDate" type="date"/>
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
                            
                            <label>Data Manager(s) UUN</label>
                            <span class="text-muted">
                                <a class="btn btn-default pad" data-toggle="popover" data-trigger="hover"
                                   data-content="Each person nominated as a Data Manager on this vault will have the ability to retrieve the vault's contents, deposit more data into the vault, edit the descriptions of deposits or bring forward the review date of the vault. Giving a user Data Manager status does not give them the ability to add or remove the permissions of any other user on the vault.&nbsp;In the absence of the vault Owner, the nominated Data Manager(s) may be consulted on the management of the vault."
                                   data-original-title="" title="">?</a>
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                    title="Each person nominated as a Data Manager on this vault will have the ability to retrieve the vault's contents, deposit more data into the vault, edit the descriptions of deposits or bring forward the review date of the vault. Giving a user Data Manager status does not give them the ability to add or remove the permissions of any other user on the vault.&nbsp;In the absence of the vault Owner, the nominated Data Manager(s) may be consulted on the management of the vault.">
                                </span>
                                <span class="glyphicon glyphicon-question-sign" aria-hidden="true" data-toggle="tooltip" 
                                    title="How is it going to work? what does the 'add button' does?">
                                </span>
                            </span>
                            <div class="input-group">
                                <input class="form-control" aria-label="..." type="text">
                                <div class="input-group-btn">
                                    <button type="button"
                                        class="btn btn-default dropdown-toggle"
                                        data-toggle="dropdown"
                                        aria-haspopup="true"
                                        aria-expanded="false">
                                        + Add <span class="caret"></span>
                                    </button>
                                    <ul class="dropdown-menu dropdown-menu-right">
                                        <li><a href="#">+ Add another</a></li>
                                    </ul>
                                </div>
                            </div>
                            
                            <div class="form-group">
                                <label for="school">School</label>
                                <a class="btn btn-default pad" data-toggle="popover" data-trigger="hover"
                                   data-content="The School to which the vault belongs. In the absence of the vault Owner, School officers may be consulted on the management of the vault."
                                   data-original-title="" title="">?</a>
                                <span class="text-muted">
                                    <span class="glyphicon glyphicon-question-sign" aria-hidden="true" data-toggle="tooltip" 
                                        title="Where do we get that list of school?">
                                    </span>
                                </span>
                                <select class="form-control" id="school">
                                    <option selected="selected">School Name</option>
                                    <option>2</option>
                                    <option>3</option>
                                    <option>4</option>
                                    <option>5</option>
                                </select>
                            </div>
                            
                            <div class="form-group">
                                <div class="alert alert-danger" role="alert">
                                <label>Group</label>
                                <a class="btn btn-default pad" data-toggle="popover" data-trigger="hover"
                                   data-content="The Group which is associated with this Vault. A Group is used to establish a chain of custody over data in a Vault. A Group administrator will be able to view information about a Vault."
                                   data-original-title="" title="">?</a>
                                <span class="text-muted">
                                    <span class="glyphicon glyphicon-question-sign" aria-hidden="true" data-toggle="tooltip" 
                                        title="Doesn't exist in the new UI">
                                    </span>
                                </span>
                                <select id="groupID" name="groupID" data-width="fit" class="group-select selectpicker show-tick">
                                    <option selected disabled data-hidden="true">Please choose a group owner</option>
                                        <#list groups as group>
                                        <#if group.enabled>
                                        <option value="${group.getID()}">${group.name?html}</option>
                                        </#if> 
                                        </#list>
                                </select>
                                </div>
                            </div>
                            
                            <div class="form-group">
                                <label>
                                    <strong>Review Date</strong>
                                    <a class="btn btn-default pad" data-toggle="popover" data-trigger="hover"
                                       data-content="The date by which the vault should be reviewed for decision as to whether it should be deleted or whether there are funds available to support continued storage.&nbsp;If you wish to extend the review date further into the future, please contact the support team to discuss the funding of the storage for the vault. If on the other hand you wish the vault to be deleted, bring the review date forward so that deletion may be considered."
                                       data-original-title="" title="">?</a>
                                    <span class="glyphicon glyphicon-question-sign" aria-hidden="true" data-toggle="tooltip" 
                                        title="Here I've used a input box of type 'date' instead of having 3 input boxes.">
                                    </span>
                                </label>
                                
                                <input class="form-control" id="reviewDate" type="date"/>
                            </div>

                            <input type="hidden" id="submitAction" name="action" value="submit" /> 
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

                            <div class="btn-toolbar pull-right">
                                <button type="submit" value="cancel" class="btn btn-lg btn-link cancel">Cancel</button>
                                <button type="submit" value="submit" class="btn btn-lg btn-primary">
                                    <span class="glyphicon glyphicon-folder-close"></span>
                                    Create new Vault
                                </button>
                            </div>
                        </div>
                    </div>
                </form>
                <#else>
                <div class="alert alert-warning" role="alert">
                    <h4>
                        <span class="glyphicon glyphicon-warning-sign" aria-hidden="true"></span>
                        No datasets found
                    </h4>
                    Please see the help page on <a href="${springMacroRequestContext.getContextPath()}/help#describe">describing your data</a> for more information.
                </div>
                </#if>
            </div>
        </div>
    </div>
    
    <#if datasets?has_content>
    <script>
        $(document).ready(function () {

            $('button[type="submit"]').on("click", function() {
                $('#submitAction').val($(this).attr('value'));
            });

            $('#create-vault').validate({
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
    </#if>

</@layout.vaultLayout>
