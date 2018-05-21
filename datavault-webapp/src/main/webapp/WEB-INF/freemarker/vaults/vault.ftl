<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>Home</b></a></li>
        <li class="active"><b>Vault:</b> ${vault.name?html}</li>
    </ol>

    <div class="panel panel-uoe-low">
        <div class="associated-image">
            <figure class="uoe-panel-image uoe-panel-image"></figure>
        </div>

        <div class="panel-body">

            <h2>Summary of Vault Metadata</h2>
            <br/>

            <#if deposits?has_content>
            <h4><strong>Deposit and Retrieve</strong></h4>
            <div class="row">
                <div class="col-md-12">
                    <a class="btn btn-primary pull-right" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/create">
                        <i class="fa fa-download fa-rotate-180" aria-hidden="true"></i> Deposit data
                    </a>
                </div>
            </div>
            <table class="table table-bordered">
                <thead>
                    <tr class="tr">
                        <th>Deposit Name</th>
                        <th>Deposited By</th>
                        <th>Date and Time</th>
                        <th>Total Size of Selected Deposits</th>
                        <th>Select Desposit(s) to be Retrieved
                            <span class="glyphicon glyphicon-info-sign text-muted" aria-hidden="true" data-toggle="tooltip" 
                                title="Please make sure you have enough space for the full contents of the vault to be copied to the location you will specify (see 'Size', below).">
                            </span>
                        </th>
                    </tr>
                </thead>

                <tbody>
                    <#list deposits as deposit>
                    <tr class="tr">
                        <td>
                            <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}">
                                ${deposit.name?html}
                            </a>
                        </td>
                        <td>${deposit.getUserID()}</td>
                        <td>${deposit.getCreationTime()?datetime}</td>
                        <td>${deposit.getSizeStr()}</td>
                        <td>
                            <#if deposit.status.name() == "COMPLETE">
                            <a id="retrievebtn" class="btn btn-default pull-right"
                               href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}/retrieve">
                                <span class="fa fa-upload fa-rotate-180" aria-hidden="true"></span> Retrieve
                            </a>
                            <#elseif deposit.status.name() == "FAILED">
                            <span class="label label-danger pull-right">Failed</span>
                            <#elseif deposit.status.name() == "NOT_STARTED">
                            <span class="label label-info pull-right">Not started</span>
                            <#elseif deposit.status.name() == "IN_PROGRESS">
                            <span class="label label-info pull-right">In progress</span>
                            <#else>
                            <span class="label label-info pull-right">${deposit.status}</span>
                            </#if>
                        </td>
                    </tr>
                    </#list>
                </tbody>
            </table>
            <#else>
            <div class="row">
                <div class="col-md-12">
                    <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/create">
                        <i class="fa fa-download fa-rotate-180" aria-hidden="true"></i> Deposit data
                    </a>
                </div>
            </div>
            </#if>

            <div id="accordion">
                <h4 class="accordion-toggle">
                    ‹› &nbsp;Summary of Full Vault Metadata
                </h4>
                <div class="accordion-content">
                    <table class="table table-sm">

                        <tbody>
                            <tr>
                                <th scope="col">Vault Name</th>
                                <td>${vault.name?html}</td>
                                <td></td>
                            </tr>
                            <tr>
                                <th scope="col">Description</th>
                                <td>${vault.description?html}</td>
                                <td>
                                    <button type="button" class="btn btn-default pull-right" disabled>
                                        Edit
                                    </button>
                                </td>
                            </tr>
                            <tr>
                                <th scope="col">Size</th>
                                <td>${vault.getSizeStr()}</td>
                                <td></td>
                            </tr>
                            <tr>
                                <th scope="col">PURE ID</th>
                                <td>${vault.datasetName?html}</td>
                                <td></td>
                            </tr>
                            <tr>
                                <th scope="col">Grant End Date</th>
                                <td>${vault.grantEndDate?string('dd/MM/yyyy')}</td>
                                <td>
                                    <button type="button" class="btn btn-default pull-right" disabled>
                                        Edit review date
                                    </button>
                                </td>
                            </tr>
                            <tr>
                                <th scope="col">Review Date</th>
                                <td>${vault.reviewDate?string('dd/MM/yyyy')}</td>
                                <td>
                                    <button type="button" class="btn btn-default pull-right" disabled>
                                        Edit review date
                                    </button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    <div class="alert alert-info" role="alert">
                        <p>
                            All vaults are automatically closed so no more deposits can be added.
                            ONE calendar year after the grant end date or one year after the first
                            deposit, whichever is later.
                        </p>
                    </div>
                </div>

                <h4 class="accordion-toggle">
                    ‹› &nbsp;Data Managers
                </h4>
                <div class="accordion-content">
                    <h4>
                        <strong>Owner</strong>
                        <small>
                            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                title="A vault may have no more than one Owner. In some cases, a vault may have no Owner.&nbsp;In this case, nominated Data Managers, School officers and/or &nbsp;administrators will manage the data on behalf of the university.">
                            </span>
                        </small>
                    </h4>
                    <table class="table table-bordered">
                        <thead></thead>
                        <tbody>
                            <tr>
                                <td>User Name</td>
                                <td>${vault.userID?html}</td>
                                <td>
                                    <button type="button" class="btn btn-default pull-right" disabled>
                                        Edit
                                    </button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                    
                    <h4><strong>Nominated Data Managers</strong></h4>
                    <table class="table table-bordered ">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>UUN</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>Name</td>
                                <td>UUN</td>
                                <td>
                                    <button type="button" class="btn btn-default pull-right" disabled>Delete</button>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="3">
                                    <button type="button" class="btn btn-default pull-right" disabled>Add Data Manager +</button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <h4 class="accordion-toggle">
                    ‹› &nbsp;Vault History
                </h4>
                <div class="accordion-content">
                    <h4><strong>Retrievals</strong></h4>
                    <div class="scrollable">
                        <table class="table table-bordered ">
                            <thead>
                            <tr>
                                <th>Deposit Name</th>
                                <th>Retrieved By</th>
                                <th>For an external user</th>
                                <th>Date and Time</th>
                                <th>Status</th>
                            </tr>
                            </thead>
                            <tbody>
                            <#list retrievals?keys as depositName>
                                <#list retrievals[depositName] as retrieve>
                                <tr>
                                    <td>${depositName}</td>
                                    <td>${retrieve.user.getID()?html}</td>
                                    <td><#if retrieve.hasExternalRecipients>Yes<#else>No</#if></td>
                                    <td>${retrieve.timestamp?datetime}</td>
                                    <td>
                                        <div class="job-status">
                                            <#if retrieve.status.name() == "COMPLETE">
                                            <div class="text-success">
                                                <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>
                                                Complete
                                            </div>
                                            <#elseif retrieve.status.name() == "FAILED">
                                            <div class="text-danger">
                                                <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
                                                Failed
                                            </div>
                                            <#elseif retrieve.status.name() == "NOT_STARTED">
                                            Not started
                                            <#elseif retrieve.status.name() == "IN_PROGRESS">
                                            <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span>
                                            In progress
                                            <#else>
                                            ${retrieve.status}
                                            </#if>
                                        </div>
                                    </td>
                                </tr>
                                </#list> 
                            </#list> 
                            </tbody>
                        </table>
                    </div>

                    <h4><strong>Edits</strong></h4>
                    <table class="table table-bordered ">
                        <thead>
                        <tr>
                            <th>Edited By</th>
                            <th>Date and Time</th>
                            <th>View Edit</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>Edited By</td>
                            <td>00  00/00/0000</td>
                            <td><button type="button" class="btn btn-default pull-right" disabled>View Edit</button></td>
                        </tr>
                        </tbody>
                    </table>

                    <h4><strong>Vault Metadata</strong></h4>
                    <h5>
                        <strong>
                            PURE metadata - Vault description and other metadata captured from associated PURE dataset record at a time.
                        </strong>
                    </h5>

                    <div class="form-group">
                        <textarea class="form-control" id="exampleFormControlTextarea1" rows="3" disabled="disabled">
                            Not editable as could be lengthy
                        </textarea>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
$(document).ready(function(){
    $('[data-toggle="popover"]').popover();

    $('#accordion').find('.accordion-toggle').click(function(){

      //Expand or collapse this panel
      $(this).next().slideToggle('fast');

      //Hide the other panels
      $(".accordion-content").not($(this).next()).slideUp('fast');

    });
    
    $('[data-toggle="tooltip"]').tooltip({
        'placement': 'right'
    });
});
</script>

</@layout.vaultLayout>
