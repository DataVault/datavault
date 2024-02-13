<#import "*/layout/welcomelayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<#import "/spring.ftl" as spring />
<#assign sec=JspTaglibs["http://www.springframework.org/security/tags"] />
<@layout.vaultLayout>

    <div class="container">
    
        <#if vaults?has_content>
        <h3>
            Current Vaults
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                title="Vaults of which you are either the Owner or the Nominated Data Manager. If you think a vault is missing from this list, please contact the Research Data Service">
            </span>
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
                                    <#assign viewDepositsSecurityExpression = "hasPermission('${vault.ID}', 'vault', 'VIEW_DEPOSITS_AND_RETRIEVES') or hasPermission('${vault.groupID}', 'GROUP', 'MANAGE_SCHOOL_VAULT_DEPOSITS')">
                                    <@sec.authorize access=viewDepositsSecurityExpression>
                                    <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/create" class="btn btn-primary">
                                       <i class="fa fa-download fa-rotate-180" aria-hidden="true"></i> Deposit
                                    </a>
                                    </@sec.authorize>
                                </td>
                                <td><#if vault.getOwnerId()??>${vault.getOwnerId()}</#if></td>
                                <td>${vault.getCreationTime()?datetime}</td>
                                <td class="text-muted">${vault.getReviewDateAsString()}</td>
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
        <#if pendingVaults?has_content>
            <h3>
                Pending Vaults
                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                      title="Vaults of which are pending. If you think a vault is missing from this list, please contact the Research Data Service">
                </span>
            </h3>

            <div class="row">
                <div class="col-md-12">
                    <div class="scrollable">
                        <table class="table table-bordered whitebackground">
                            <thead>
                            <tr>
                                <th>Vault Name</th>
                                <th>Owner Name</th>
                                <th>Date Created</th>
                            </tr>
                            </thead>
                            <tbody>
                            <#list pendingVaults as pendingVault>
                                <tr>
                                    <td>
                                        <a href="${springMacroRequestContext.getContextPath()}/pendingVaults/${pendingVault.getID()}/">
                                            ${pendingVault.name?html}
                                        </a>
                                    </td>
                                    <td><#if pendingVault.getOwnerId()??>${pendingVault.getOwnerId()}</#if></td>
                                    <td>${pendingVault.getCreationTime()?datetime}</td>
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
            <a href="${springMacroRequestContext.getContextPath()}/vaults/buildsteps" class="btn btn-lg btn-primary">
                <span class="glyphicon glyphicon-folder-close"></span>
                Create vault
            </a>
        </div>
</@layout.vaultLayout>
