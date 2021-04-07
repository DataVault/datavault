<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Billing</b></li>
    </ol>

    

    <form id="search-vaults" class="form" role="form" action="" method="get">
        <div class="input-group">
            <input type="text" class="form-control" value="${query?url}" name="query" placeholder="Search for...">
            <div class="input-group-btn">
                <button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-search" aria-hidden="true"></span> Search</button>
            </div>
        </div>
    </form>
    
    <div align="right">
       <form id="search-vaults" class="form" role="form" action="${springMacroRequestContext.getContextPath()}/admin/billing/csv" method="get">
        
            <div class="input-group" align="right">
                <input type="text" class="form-control hidden" value="${query?url}" name="query" placeholder="Search for...">
                <button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-export" aria-hidden="true"></span> Download CSV File</button>
            <div align="right">
           
            </div>
        </form>
</div>
 <#if vaults?has_content> 

        <div class="table-responsive" id="vaultsTable">
            <table class="table table-striped">
                <thead>
                    <tr class="tr">
                       <th><a href="?sort=name&order=${ordername}&query=${query?url}">Vault Name<#if sort == "name"><#if order == "desc"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                       <th><a href="?sort=projectId&order=${orderProjectId}&query=${query?url}">Project Id<#if sort == "projectId"><#if order == "desc"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                      
                       
                       <th>Project Size</th>
                       <th><a href="?sort=vaultSize&order=${ordervaultsize}&query=${query?url}">Vault Size<#if sort == "vaultSize"><#if order == "desc"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                       
                       <th><a href="?sort=reviewDate&order=${orderreviewDate}&query=${query?url}">Review Date<#if sort == "reviewDate"><#if order == "desc"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                        <th><a href="?sort=creationTime&order=${ordercreationtime}&query=${query?url}">Date created<#if sort == "creationTime"><#if order == "desc"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                        <th>Amount to be Billed</th>
                        <th>Amount Billed</th>
                        <th><a href="?sort=vault.user.id&order=${orderuser}&query=${query?url}">Owner<#if sort == "vault.user.id"><#if order == "desc"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                      
                        <th></th>
                    </tr>
                </thead>

                <tbody>
                    <#list vaults as vault>
                        <tr class="tr">
                            <td>
                                <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/">${vault.name?html}</a>
                            </td>
                            <td> <#if vault.getProjectId()??>${vault.getProjectId()}<#else> </#if></td>
                            
                            <td>${vault.getProjectSizeStr()}</td>
                           <td>${vault.getSizeStr()}</td>
                            
                            <td>${vault.reviewDate?string('dd/MM/yyyy')}</td>
                            <td>${vault.getCreationTime()?datetime}</td>
                            <td> <#if vault.getAmountToBeBilled()??>${vault.getAmountToBeBilled()}<#else> </#if></td>
                            <td> <#if vault.getAmountBilled()??>${vault.getAmountBilled()}<#else> </#if></td>
                           
                             <td>
                                 <#if vault.getUserName()??>${vault.getUserName()}</#if>
                            </td>
                            <td>
                             <a href="${springMacroRequestContext.getContextPath()}/admin/billing/${vault.getID()}">Billing Details</a></td>
                             
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
				     	 <a class="page-link" href="${springMacroRequestContext.getContextPath()}/admin/billing?query=${query?url}&sort=${sort}&order=${order}&pageId=${page}" tabindex="-1">${page}</a>
				    	</li>
					</#list>
				 </ul>
			</nav>
        </div>      
            

 </#if> 

</div>
 

</@layout.vaultLayout>
