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
                                <th>Owner Name</th>
                                <th>Role</th>
                                <th>Vault Size</th>
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
                               
                                </td>
                                <td>${vault.getUserName()?html} (${vault.getUserID()?html})</td>
                                <td></td>
                                <td>${vault.getVaultSize()}</td>
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
        
       
    

</@layout.vaultLayout>
