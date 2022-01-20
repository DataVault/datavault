<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Pending Vaults</b></li>
    </ol>
    
    <h1>Pending Vaults</h1>

    <h3><a href="${springMacroRequestContext.getContextPath()}/admin/pendingVaults/confirmed/">
            Confirmed (${confirmedVaultsTotal})
        </a>
    </h3>
    <h3><a href="${springMacroRequestContext.getContextPath()}/admin/pendingVaults/saved/">
             Saved (${savedVaultsTotal})
         </a>
    </h3>
   
   
</div>
 

</@layout.vaultLayout>
