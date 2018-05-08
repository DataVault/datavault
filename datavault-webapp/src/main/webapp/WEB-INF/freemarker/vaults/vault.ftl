<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>Current Vaults</b></a></li>
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
                    <a class="btn btn-link pull-right" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/create">
                        <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Add Deposit
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
                        <th>Are any of the intended recipients external to the University of Edinburgh?</th>
                        <th>Select Desposit(s) to be Retrieved
                            <a class="btn btn-default pad" data-toggle="popover" data-trigger="hover"
                               data-content="Please make sure you have enough space for the full contents of the vault to be copied to the location you will specify (see 'Size', below)."
                               data-original-title="" title="">?</a>
                        </th>
                    </tr>
                </thead>

                <tbody>
                    <#list deposits as deposit>
                    <tr class="tr">
                        <td>
                            <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}">${deposit.note?html}</a>
                        </td>
                        <td></td>
                        <td>${deposit.getCreationTime()?datetime}</td>
                        <td>${deposit.getSizeStr()}</td>
                        <td></td>
                        <td>
                            <#if deposit.status.name() == "COMPLETE">
                            <a id="retrievebtn" class="btn btn-default"
                               href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}/retrieve">
                                <span class="fa fa-upload fa-rotate-180" aria-hidden="true"></span> Retrieve
                            </a>
                            <#elseif deposit.status.name() == "FAILED">
                            <button id="failedbtn" class="btn btn-danger disabled">
                                <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
                                Failed
                            </button>
                            <#elseif deposit.status.name() == "NOT_STARTED">
                            <button id="inprogressbtn" class="btn btn-info disabled">
                                Not started
                            </button>
                            <#elseif deposit.status.name() == "IN_PROGRESS">
                            <button id="inprogressbtn" class="btn btn-info disabled">
                                <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span>
                                In progress
                            </button>
                            <#else>
                            <button id="inprogressbtn" class="btn btn-info disabled">
                                ${deposit.status}
                            </button>
                            </#if>
                        </td>
                    </tr>
                    </#list>
                </tbody>
            </table>
            </#if>

            <div class="row">
                <div class="col-md-12">
                    <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/create">
                        <i class="fa fa-download fa-rotate-180" aria-hidden="true"></i> Deposit data
                    </a>
                </div>
            </div>

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
                                    <button type="button" class="btn btn-default pull-right">
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
                                <th scope="col">Review Date</th>
                                <td>00/00/0000</td>
                                <td>
                                    <button type="button" class="btn btn-default pull-right">
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
                        <a class="btn btn-default pad"
                           data-toggle="popover"
                           data-trigger="hover"
                           data-content="A vault may have no more than one Owner. In some cases, a vault may have no Owner.&nbsp;In this case, nominated Data Managers, School officers and/or &nbsp;administrators will manage the data on behalf of the university." data-original-title="" title="">?</a>
                    </h4>
                    <table class="table table-bordered">
                        <thead></thead>
                        <tbody>
                            <tr>
                                <td>${vault.userID?html}</td>
                                <td>UUN</td>
                                <td>
                                    <button type="button"
                                            class="btn btn-default pull-right">
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
                                    <button type="button" class="btn btn-default pull-right">Delete</button>
                                </td>
                            </tr>
                            <tr>
                                <td>Name</td>
                                <td>UUN</td>
                                <td>
                                    <button type="button" class="btn btn-default pull-right">Delete</button>
                                </td>
                            </tr>
                            <tr>
                                <td>Name</td>
                                <td>UUN</td>
                                <td>
                                    <button type="button" class="btn btn-default pull-right">Delete</button>
                                </td>
                            </tr>
                            <tr>
                                <td>Name</td>
                                <td>UUN</td>
                                <td>
                                    <button type="button" class="btn btn-default pull-right">Delete</button>
                                </td>
                            </tr>
                            <tr>
                                <td></td>
                                <td></td>
                                <td>
                                    <button type="button" class="btn btn-default pull-right">Add Data Manager +</button>
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
                    <table class="table table-bordered ">
                        <thead>
                        <tr>
                            <th>Retrieved By</th>
                            <th>Date and Time</th>
                            <th>Reason</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>Retrieved By</td>
                            <td>00  00/00/0000</td>
                            <td>For an external user</td>

                        </tr>
                        <tr>
                            <td>Retrieved By</td>
                            <td>00  00/00/0000</td>
                            <td>For an internal user</td>

                        </tr>
                        </tbody>
                    </table>

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
                            <td><button type="button" class="btn btn-default pull-right">View Edit</button></td>

                        </tr>
                        <tr>
                            <td>Edited By</td>
                            <td>00  00/00/0000</td>
                            <td><button type="button" class="btn btn-default pull-right">View Edit</button></td>

                        </tr>
                        <tr>
                            <td>Edited By</td>
                            <td>00  00/00/0000</td>
                            <td><button type="button" class="btn btn-default pull-right">View Edit</button></td>
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
});
</script>

</@layout.vaultLayout>
