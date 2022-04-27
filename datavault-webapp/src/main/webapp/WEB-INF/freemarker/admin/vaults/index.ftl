<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Vaults</b></li>
    </ol>
    
    <a href="${springMacroRequestContext.getContextPath()}/vaults">+Add New Vault</a>
    

    <form id="search-vaults" class="form" role="form" action="" method="get">
        <div class="input-group">
            <input type="text" class="form-control" value="${query?url}" name="query" placeholder="Search for...">
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

        <div class="table-responsive" id="vaultsTable">
            <table class="table table-striped">
                <thead>
                    <tr class="tr">
                       <th><a href="?sort=name&order=${ordername}&query=${query?url}">Vault Name<#if sort == "name"><#if order == "desc"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                       <th>Deposits</th>
                       <th>Project ID</th>
                        <th><a href="?sort=crisID&order=${orderCrisId}&query=${query?url}">Cris ID<#if sort == "crisID"><#if order == "desc"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                       <th>Project Size</th>
                       <th><a href="?sort=vaultSize&order=${ordervaultsize}&query=${query?url}">Vault Size<#if sort == "vaultSize"><#if order == "desc"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                       <th><a href="?sort=user&order=${orderuser}&query=${query?url}">Owner(UUN)<#if sort == "user"><#if order == "desc"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                       <th><a href="?sort=groupID&order=${orderGroupId}&query=${query?url}">School<#if sort == "groupID"><#if order == "desc"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                       <th><a href="?sort=reviewDate&order=${orderreviewDate}&query=${query?url}">Review Date<#if sort == "reviewDate"><#if order == "desc"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                        <th><a href="?sort=creationTime&order=${ordercreationtime}&query=${query?url}">Date created<#if sort == "creationTime"><#if order == "desc"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                        <th style="display:none">Actions</th>
                    </tr>
                </thead>

                <tbody>
                    <#list vaults as vault>
                        <tr class="tr">
                            <td>
                                <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/">${vault.name?html}</a>
                            </td>
                            <td>${vault.getNumberOfDeposits()}</td>
                            <td> <#if vault.getProjectId()??>${vault.getProjectId()}<#else> </#if></td>
                            <td> <#if vault.getCrisID()??>${vault.getCrisID()}<#else> </#if></td>
                            <td>${vault.getProjectSizeStr()}</td>
                           <td>${vault.getSizeStr()}</td>
                            <td>
                                <#if vault.getOwnerId()??>
                                    <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/${vault.getOwnerId()}">${vault.getOwnerName()?html} (${vault.getOwnerId()?html})</a>
                                <#else> 
                                   User missing
                                </#if>
                            </td>
                            
                            <td>${vault.groupID?html}</td>
                            <td>${vault.reviewDate?string('dd/MM/yyyy')}</td>
                            <td>${vault.getCreationTime()?datetime}</td>
                            <td style="display:none">
                            <!-- TODO : Hiding the remove button unless the functionality is clear as to what it should do - Soft/Hard delete -->
                           
                            </td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
            
            <div align="center">
            	<p class="text-info">${recordsInfo}</p>
            </div>
            <div align="center">
            <nav aria-label="...">
			    <ul class="pagination pagination-lg" id="paginationButton">
                    <#list 1..numberOfPages as page>
				    	<li <#if page == activePageId>class="page-item active"<#else>class="page-item"</#if> id="${page}">
				     	 <a class="page-link" href="${springMacroRequestContext.getContextPath()}/admin/vaults?sort=${sort}&order=${order}&pageId=${page}&query=${query}" tabindex="-1">${page}</a>
				    	</li>
					</#list>
				 </ul>
			</nav>
        </div>

    </#if>

</div>
 

</@layout.vaultLayout>
