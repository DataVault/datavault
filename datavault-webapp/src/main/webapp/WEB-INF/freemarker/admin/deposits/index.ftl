<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<#assign sec=JspTaglibs["http://www.springframework.org/security/tags"] />
<@layout.vaultLayout>

    <div class="modal fade" id="confirm-removal" tabindex="-1" role="dialog" aria-labelledby="confirmRemovalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                    <h4 class="modal-title" id="confirmRemovalLabel">Confirm removal of Deposit</h4>
                </div>
                <div class="modal-body">
                    <p>You have selected to delete the files in this deposit.<br> Do you have the authorisation of the Owner / Data Manager and Head of School or College, as per the review process? (In the case of an ‘orphan’ vault, permission of the Head of School and College is necessary).</p>

                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-danger btn-ok" id="remove">Yes</button>
                </div>
            </div>
        </div>
    </div>

    <div class="container">

        <ol class="breadcrumb">
            <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
            <li class="active"><b>Deposits</b></li>
        </ol>

        <form id="search-vaults" class="form" role="form" action="" method="get">
            <div class="input-group">
                <input type="text" class="form-control" name="query" value="${query}" placeholder="Search for...">
                <div class="input-group-btn">
                    <button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-search" aria-hidden="true"></span> Search</button>
                </div>
            </div>
        </form>

        <div align="right">
            <form id="search-vaults" class="form" role="form" action="${springMacroRequestContext.getContextPath()}/admin/deposits/csv" method="get">
                <div class="input-group" align="right">
                    <input type="text" class="form-control hidden" value="${query}" name="query" placeholder="Search for...">
                    <button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-export" aria-hidden="true"></span> Download CSV File</button>
                </div>
            </form>
        </div>
        <#if deposits?has_content>
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                    <tr class="tr">
                        <th>Deposit Name</th>
                        <th><a href="?sort=depositSize&query=${query?url}">Size</a></th>
                        <th><a href="?sort=creationTime&query=${query?url}">Date deposited</a></th>
                        <th><a href="?sort=status&query=${query?url}">Status</a></th>
                        <th>Depositor</th>
                        <th>Vault Name</th>
                        <th>Pure Record ID</th>
                        <th>School</th>
                        <th>Deposit ID</th>
                        <th>Vault ID</th>
                        <th>Vault Owner</th>
                        <th>Vault Review Date</th>
                        <th>Actions</th>

                    </tr>
                    </thead>

                    <tbody>
                    <#list deposits as deposit>
                        <tr class="tr">
                            <td>${deposit.name?html}</td>
                            <td>${deposit.getSizeStr()}</td>
                            <td>${deposit.getCreationTime()?datetime}</td>
                            <td>${deposit.status}</td>
                            <td><#if deposit.getUserName()??>${deposit.getUserName()}<#else> </#if> (${deposit.userID})</td>
                            <td><#if deposit.getVaultName()??>${deposit.getVaultName()}<#else> </#if></td>
                            <td><#if deposit.getCrisID()??>${deposit.getCrisID()}<#else> </#if> </td>
                            <td>${deposit.groupName}</td>
                            <td><a href="${springMacroRequestContext.getContextPath()}/vaults/${deposit.vaultID}/deposits/${deposit.getID()?html}">${deposit.getID()?html}</a></td>
                            <td><a href="${springMacroRequestContext.getContextPath()}/vaults/${deposit.vaultID}">${deposit.vaultID?html}</a></td>
                            <td><#if deposit.vaultOwnerID??>${deposit.vaultOwnerName} (${deposit.vaultOwnerID})</#if></td>
                            <td>${deposit.vaultReviewDate}</td>
                            <td>
                                <#assign removeDepositSecurityExpression = "hasPermission('${deposit.getGroupID()}', 'GROUP', 'DELETE_SCHOOL_VAULT_DEPOSITS')">
                                <@sec.authorize access=removeDepositSecurityExpression>
                                <#if deposit.status != "DELETED">
                                    <a class="btn btn-xs btn-danger pull-left" href="#" data-vault="${deposit.getVaultID()}" data-deposit="${deposit.getID()}" data-toggle="modal" data-target="#confirm-removal">
                                        <span class="glyphicon glyphicon-remove" aria-hidden="true"></span> Remove
                                    </a>
                                    <br>
                                </#if>
                                </@sec.authorize>
                                <#if deposit.status == "FAILED">
                                    <a class="restart-deposit-btn btn btn-default btn-sm"
                                       href="${springMacroRequestContext.getContextPath()}/vaults/${deposit.vaultID}/deposits/${deposit.getID()?html}/restart">
                                        Restart
                                    </a>

                                </#if>
                            </td>


                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
        </#if>

    </div>
    <script>
        //Bind properties to the user removal confirmation dialog
        $('#confirm-removal').on('show.bs.modal', function(e) {
            var data = $(e.relatedTarget).data();
            $('#remove', this).data('depositId', data.deposit);
            $('#remove', this).data('vaultId', data.vault);

        });

        $("button#remove").click(function(){
            var depositId = $(this).data('depositId');
            var vaultId = $(this).data('vaultId');
            $.ajax({
                url: '${springMacroRequestContext.getContextPath()}/admin/deposits/' + depositId+'?vaultId='+vaultId,
                type: 'DELETE',
                success: function (data) {
                    console.log("----success---");
                    window.location.href = '${springMacroRequestContext.getContextPath()}/vaults/'+vaultId+'/deposits/'+depositId;

                },
                error: function(xhr, ajaxOptions, thrownError) {
                    //alert('Error: unable to delete Deposit');
                }
            });
        });

        //Add Spring Security CSRF header to ajax requests
        $(function () {
            var token = $("meta[name='_csrf']").attr("content");
            var header = $("meta[name='_csrf_header']").attr("content");
            $(document).ajaxSend(function(e, xhr, options) {
                xhr.setRequestHeader(header, token);
            });
        });
				
    </script>
</@layout.vaultLayout>