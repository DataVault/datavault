<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
<div class="container">

    <div class="jumbotron">
        <p>Welcome to the Data Vault!</p>
        <p>Please read the help section <a href="${springMacroRequestContext.getContextPath()}/help">here</a> before continuing.</p>
    </div>

    <#if noFilestores>
        <h4>Ready to get started?</h4>
        <p>We need to know a little about where you intend to archive data from. Please click <a href="${springMacroRequestContext.getContextPath()}/filestores">here</a> to define your storage options.</p>
    <#else>
        <p>You previously defined your storage options but if you would like to review them then click <a href="${springMacroRequestContext.getContextPath()}/filestores">here</a>.</p>

        <#if noVaults>
            <p>You have not yet defined any vaults. Please click <a href="${springMacroRequestContext.getContextPath()}/vaults/create">here</a> to create your first vault.</p>
        </#if>

    </#if>

</div>
</@layout.vaultLayout>
