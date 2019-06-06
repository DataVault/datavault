<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Vaults</b></li>
    </ol>
    
    <a href="${springMacroRequestContext.getContextPath()}/vaults/redirect">+Add New Vault</a><br>
    

    <form id="search-vaults" class="form" role="form" action="" method="get">
        <div class="input-group">
            <input type="text" class="form-control" value="${query}" name="query" placeholder="Search for...">
            <div class="input-group-btn">
                <button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-search" aria-hidden="true"></span> Search</button>
            </div>
        </div>
    </form>
    
        <form id="search-vaults" class="form" role="form" action="${springMacroRequestContext.getContextPath()}/admin/vaults/csv" method="get">
            <div class="input-group" align="right">
                <input type="text" class="form-control hidden" value="${query}" name="query" placeholder="Search for...">
                <button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-export" aria-hidden="true"></span> Download CSV File</button>
            </div>
        </form>

    <#if vaults?has_content>

        <div class="table-responsive">
            <table class="table table-striped">

                <thead>
                    <tr class="tr">
                       <th><a href="?sort=name&order=${ordername}&query=${query?url}">Vault Name<#if sort == "name"><#if ordername == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                       <th>Deposits</th>
                       <th><a href="?sort=vaultSize&order=${ordervaultsize}&query=${query?url}">Vault Size<#if sort == "vaultSize"><#if ordervaultsize == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                        <th><a href="?sort=user&order=${orderuser}&query=${query?url}">Owner(UUN)<#if sort == "user"><#if orderuser == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>                       
                        
                        <th>School</th>
                        <th>Review Date</th>
                        <th><a href="?sort=creationTime&order=${ordercreationtime}&query=${query?url}">Date created<#if sort == "creationTime"><#if ordercreationtime == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                        
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
                            
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>

    </#if>

</div>
</@layout.vaultLayout>
