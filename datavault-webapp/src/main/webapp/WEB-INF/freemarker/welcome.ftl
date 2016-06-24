<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
<div class="container">

    <div class="jumbotron">
        <p>Welcome to the Data Vault!</p>
        <p>Please read the help section <a href="${springMacroRequestContext.getContextPath()}/help">here</a> before continuing.</p>
    </div>

    <div <#if filestoresExist>class="text-muted" </#if>>
        <h4>1. Define your storage options</h4>
        <div class="row">
            <div class="col-md-10">
                <p>We need to know a little about where you intend to archive data from. Please click <a href="${springMacroRequestContext.getContextPath()}/filestores">here</a> to define your storage options.</p>
            </div>
            <#if filestoresExist>
                <div class="col-md-2 text-success">
                    <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>&nbsp;Complete
                </div>
            <#else>
                <div class="col-md-2">
                </div>
            </#if>
        </div>
    </div>

    <h4>2. Define a dataset in your CRIS?</h4>
    <p>If your institution has a CRIS eg. PURE, have you defined a dataset to record your intended vault/archive?</p>


    <h4>3. Creat your first vault</h4>
    <p>You have not yet defined any vaults. Please click <a href="${springMacroRequestContext.getContextPath()}/vaults/create">here</a> to create your first vault.</p>

</div>
</@layout.vaultLayout>
