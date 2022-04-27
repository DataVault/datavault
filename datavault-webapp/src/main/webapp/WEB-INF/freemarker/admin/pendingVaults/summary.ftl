<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<#import "/spring.ftl" as spring />
<#assign sec=JspTaglibs["http://www.springframework.org/security/tags"] />
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/pendingVaults"><b>Pending Vaults</b></a></li>
        <li class="active"><b>Pending Vault Summary</b></li>
    </ol>
    
     

 <div class="container whitebackground">
    
        
 <h1>Pending Vault Summary</h1>
        
<div class="row">
 <div class="col-md-12">
                
  
  <a name="previous" class="btn btn-primary" 
      href="${springMacroRequestContext.getContextPath()}/admin/pendingVaults">&laquo; Return</a>
</div>
</div>


 <form id="create-vault" class="form" role="form" action="${springMacroRequestContext.getContextPath()}/admin/pendingVaults/upgrade/${pendingVault.ID}" method="get" novalidate="novalidate" _lpchecked="1">
<div class="row">
 <div class="col-md-12">
    <table class="table table-sm">
	  <tbody>
		<tr>
			<th scope="col">
				<h3 class="fs-title">Vault Information</h3></th>
			    <td></td>
		</tr>
		<tr>
			<th scope="col">Affirmation</th>
			<td>
			    <#if (pendingVault.affirmed)??>
                   ${pendingVault.affirmed?c}
                </#if>
            </td>
		</tr>
		<tr>
			<th scope="col">Vault Name</th>
			<td>${pendingVault.name?html}</td>
		</tr>
		<tr>
			<th scope="col">Description</th>
			<td>
			    <#if (pendingVault.description)??>
                   ${pendingVault.description?html}
                </#if>
            </td>
		</tr>
		<tr>
          <th scope="col">Creation Date</th>
          <td>
            <#if (pendingVault.creationTime)??>
             ${pendingVault.creationTime?date}
           </#if>
          </td>
        </tr>
		<tr>
			<th scope="col">Retention Policy</th>
			<td>
			 <!-- Model attribute createRetentionPolicy is used here. -->
			  <span>
               <#if (createRetentionPolicy)??>
                 ${createRetentionPolicy.name?html} (Minimum period:
                 <#if (createRetentionPolicy.minRetentionPeriod)??>
                  ${createRetentionPolicy.minRetentionPeriod?html}
                 <#else>
                   not stated
                 </#if>
                 )</span>
               </#if>
            </td>
		</tr>
		<tr>
			<th scope="col">Grant End Date</th>
			 <td>
                <#if (pendingVault.grantEndDate)??>
                    ${pendingVault.grantEndDate?date}
                </#if>
             </td>
		</tr>
		<tr>
			<th scope="col">School</th>
			<td>
			<!-- Model attribute group used here.-->
              <#if (group.name)??>
                ${group.name?html}
              </#if>
            </td>
		</tr>
		<tr>
			<th scope="col">Review Date</th>
			<td>
			<#if pendingVault.confirmed?c=="true">
                <input class="form-control" id="reviewDate" placeholder="yyyy-mm-dd" name="reviewDate"
                     value="${pendingVault.getReviewDateAsString()}"/>
                <span id="invalid-review-date-span" style="font-size: 1.2em; font: bold; color: #f00; display: inline;"></span>
            <#else>
                <#if (pendingVault.reviewDate)??>
                    ${pendingVault.reviewDate?date}
                </#if>
               
            </#if>
            </td>
		</tr>
		<tr>
			<th scope="col">Estimate of the amount of data</th>
			<td>
			  <#if (pendingVault.estimate)??>
                ${pendingVault.estimate?html}
              </#if>
            </td>
		</tr>
		<tr>
			<th scope="col">Notes regarding data retention</th>
			<td>
              <#if (pendingVault.notes)??>
                ${pendingVault.notes?html}
              </#if>
            </td>
		</tr>
		
		<!-- START OF BILLING SECTION -->
		<tr>
			<th scope="col">
				<h3 class="fs-title">Billing</h3></th>
			<td></td>
		</tr>
		<tr>
			<th scope="col">Funding for this vault will be from</th>
			<td>
			   <#if (pendingVault.billingType)??>
                  ${pendingVault.billingType?html}
               </#if>
            </td>
		</tr>
		
		<#if (pendingVault.billingType)??>
		<#if (pendingVault.billingType != "NA")>
		<!-- BILLTYPE SLICE -->
		<#if (pendingVault.billingType == "SLICE")>
		<tr class="summary-slice-billing-row">
			<th scope="col">Slice</th>
			<td>
              <#if (pendingVault.sliceID)??>
                ${pendingVault.sliceID?html}
              </#if>
            </td>
		</tr>
		<#else>
		<!-- OTHER BILLTYPE  -->
		<tr class="summary-grant-or_budget-billing-row">
			<th scope="col">Authoriser</th>
			<td>
			   <#if (pendingVault.authoriser)??>
                  ${pendingVault.authoriser?html}
               </#if>
            </td>
		</tr>
		<tr class="summary-grant-or_budget-billing-row">
			<th scope="col">School/Unit</th>
			<td>
			   <#if (pendingVault.schoolOrUnit)??>
                  ${pendingVault.schoolOrUnit?html}
               </#if>
            </td>
		</tr>
		<tr class="summary-grant-or_budget-billing-row">
			<th scope="col">Subunit</th>
			<td>
			   <#if (pendingVault.subunit)??>
                  ${pendingVault.subunit?html}
               </#if>
            </td>
		</tr>
		<tr class="summary-grant-or_budget-billing-row">
			<th scope="col">ProjectTitle</th>
			<td>
              <#if (pendingVault.projectTitle)??>
                ${pendingVault.projectTitle?html}
              </#if>
            </td>
		</tr>
		</#if>
		</#if>
		</#if>
		<!-- END OF BILLING SECTION -->
		<tr>
			<th scope="col">
				<h3 class="fs-title">Vault Access</h3></th>
			<td></td>
		</tr>
		<tr>
			<th scope="col">Owner</th>
			<td>
              <#if (pendingVault.ownerId)??>
                ${pendingVault.ownerId?html}
              </#if>
            </td>
		</tr>
		<tr>
			<th scope="col">NDMs</th>
			<td>
			   <#if pendingVault.nominatedDataManagerIds?has_content>
                 <#list pendingVault.nominatedDataManagerIds as ndm>
                     ${ndm} <br>
                 </#list>
               </#if>
			</td>
		</tr>
		<tr>
			<th scope="col">Depositors</th>
			<td>
			   <#if  pendingVault.depositorIds?has_content>
                 <#list pendingVault.depositorIds as dep>
                     ${dep} <br>
                 </#list>
               </#if>
			</td>
		</tr>
		<tr>
			<th scope="col">
				<h3 class="fs-title">Pure Information</h3></th>
			<td></td>
		</tr>
		<tr>
			<th scope="col">Contact Person</th>
			<td>
              <#if (pendingVault.contact)??>
                ${pendingVault.contact?html}
              </#if>
            </td>
		</tr>
		<tr>
			<th scope="col">Data Creators</th>
			<td>
			   <#if  pendingVault.dataCreators?has_content>
                 <#list pendingVault.dataCreators as dc>
                     ${dc} <br>
                 </#list>
               </#if>
			</td>
		</tr>
	</tbody>
</table>
</div>
</div>

<div class="row">
<div class="col-md-12 text-center">
     <#if (pendingVault.ID)??>
          <a name="delete-pending-vault" class="btn btn-default" 
          href="${springMacroRequestContext.getContextPath()}/admin/pendingVaults"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span>Cancel</a>

          <a name="delete-pending-vault" class="btn btn-danger" 
          href="${springMacroRequestContext.getContextPath()}/admin/pendingVaults/${pendingVault.getID()}"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span> Delete Pending Vault</a>
		<#if pendingVault.confirmed?c=="true">
         <button id="create-vault-btn" type="submit" value="submit" class="btn btn-success">
              <span class="glyphicon glyphicon-folder-close"></span>
                                    Create Vault
         </button>
		</#if>
     </#if>
    
  </div>
  </div>
</form>
   
</div>                        
</div>

<!-- Custom javascript -->
<!-- import date-validation-utils.j first -->
<script src="<@spring.url '/resources/application/js/date-validation-utils.js'/>"></script>
<script src="<@spring.url '/resources/application/js/admin-pending-vault-summary.js'/>"></script>

</@layout.vaultLayout>
       
  
 
