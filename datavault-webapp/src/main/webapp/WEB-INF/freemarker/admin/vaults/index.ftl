<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="modal fade" id="confirm-removal" tabindex="-1" role="dialog" aria-labelledby="confirmRemovalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="confirmRemovalLabel">Confirm removal of Vault</h4>
            </div>
            <div class="modal-body">
                <p>Are you sure you want to remove ,Vaults should normally not be deleted, only in special circumstances.<b class="remove-vault"></b></p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" class="btn btn-danger btn-ok" id="remove">Remove</button>
            </div>
        </div>
    </div>
</div>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Vaults</b></li>
    </ol>
    
    <a href="${springMacroRequestContext.getContextPath()}/vaults">+Add New Vault</a>
    

    <form id="search-vaults" class="form" role="form" action="" method="get">
        <div class="input-group">
            <input type="text" class="form-control" value="${query}" name="query" placeholder="Search for...">
            <div class="input-group-btn">
                <button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-search" aria-hidden="true"></span> Search</button>
            </div>
        </div>
    </form>
    
    <div align="right">
        <form id="search-vaults" class="form" role="form" action="${springMacroRequestContext.getContextPath()}/admin/vaults/csv" method="get">
            <div class="input-group" align="right">
                <input type="text" class="form-control hidden" value="${query}" name="query" placeholder="Search for...">
                <button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-export" aria-hidden="true"></span> Download CSV File</button>
            </div>
        </form>
</div>
    <#if vaults?has_content>

        <div class="table-responsive">
            <table class="table table-striped">

                <thead>
                    <tr class="tr">
                       <th><a href="?sort=name&order=${ordername}&query=${query?url}">Vault Name<#if sort == "name"><#if ordername == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                       <th>Deposits</th>
                       <th><a href="?sort=vaultSize&order=${ordervaultsize}&query=${query?url}">Vault Size<#if sort == "vaultSize"><#if ordervaultsize == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                       <th><a href="?sort=user&order=${orderuser}&query=${query?url}">Owner(UUN)<#if sort == "user"><#if orderuser == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>                       
                       <th><a href="?sort=groupID&order=${ordergroupID}&query=${query?url}">School<#if sort == "groupID"><#if ordergroupID == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                       <th><a href="?sort=reviewDate&order=${orderreviewDate}&query=${query?url}">Review Date<#if sort == "reviewDate"><#if orderreviewDate == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>                     
                        <th><a href="?sort=creationTime&order=${ordercreationtime}&query=${query?url}">Date created<#if sort == "creationTime"><#if ordercreationtime == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                        <th style="display:none">Actions</th>
                    </tr>
                </thead>

                <tbody>
                    <#list vaults as vault>
                        <tr class="tr">
                            <td>
                                <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}">${vault.name?html}</a>
                            </td>
                            <td>${vault.getNumberOfDeposits()}</td>
                           <td>${vault.getSizeStr()}</td>
                            <td>${vault.getUserName()?html} (${vault.getUserID()?html})</td>                           
                            
                            <td>${vault.groupID?html}</td>
                            <td>${vault.reviewDate?string('dd/MM/yyyy')}</td>
                            <td>${vault.getCreationTime()?datetime}</td>
                            <td style="display:none">
                            <!-- TODO : Hiding the remove button unless the functionality is clear as to what it should do - Soft/Hard delete -->
                            <a class="btn btn-xs btn-danger pull-left" href="#" data-vault="${vault.ID}" data-toggle="modal" data-target="#confirm-removal">
                                <span class="glyphicon glyphicon-remove" aria-hidden="true"></span> Remove
                          </a>
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
    $('.remove-vault', this).text(data.vault);
    $('#remove', this).data('vaultId', data.vault);

});

$("button#remove").click(function(){
    var vaultId = $(this).data('vaultId');
    $.ajax({
        url: '${springMacroRequestContext.getContextPath()}/admin/vaults/' + vaultId,
        type: 'DELETE',
        success: function (data) {
        	console.log("----success---");
            location.reload(true);
        },
        error: function(xhr, ajaxOptions, thrownError) {
            alert('Error: unable to delete vault');
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
