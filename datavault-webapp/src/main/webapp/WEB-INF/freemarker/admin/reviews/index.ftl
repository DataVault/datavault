<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Reviews</b></li>
    </ol>

    <#if vaults?has_content>

        <div class="table-responsive" id="vaultsTable">
            <table class="table table-striped">
                <thead>
                    <tr class="tr">
                       <th>Vault Name</th>
                       <th>Deposits</th>
                       <th>>Vault Size</th>
                       <th>Owner(UUN)</th>
                       <th>School</th>
                       <th>Review Date</th>
                        <th>Date created</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody>
                    <#list vaults as vault>
                        <tr class="tr">
                            <td>
                                <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/">${vault.name?html}</a>
                            </td>
                            <td>${vault.getNumberOfDeposits()}</td>
                           <td>${vault.getSizeStr()}</td>
                            <td>
                                <#if vault.getUserID()??>
                                <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/${vault.getUserID()}">${vault.getUserName()?html} (${vault.getUserID()?html})</a>
                                </#if>
                            </td>
                            
                            <td>${vault.groupID?html}</td>
                            <td>${vault.reviewDate?string('dd/MM/yyyy')}</td>
                            <td>${vault.getCreationTime()?datetime}</td>
                            <td>
                                <a href="${springMacroRequestContext.getContextPath()}/admin/vaults/${vault.getID()}/reviews">Review</a>
                            </td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>

    </#if>

</div>
 

</@layout.vaultLayout>
