<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
<div class="container">

    <div class="jumbotron">
        <p>Welcome to the Data Vault!</p>
        <p>As this may be your first time here, please read the
        <a href="${springMacroRequestContext.getContextPath()}/help">help section</a> before continuing.</p>

        <div <#if datasetsExist>class="text-muted" </#if>>
            <div class="bs-callout bs-callout-info">
                <h4>1. Define a dataset in ${system}</h4>
                <div class="row">
                    <div class="col-md-10">
                        <p><small>You must record a dataset in <a href="${link}" target="_blank">${system}</a> to record details of your vault.</small></p>
                    </div>
                    <#if datasetsExist>
                        <div class="col-md-2 text-success">
                            <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>&nbsp;Complete
                        </div>
                    <#else>
                        <div class="col-md-2">
                        </div>
                    </#if>
                </div>
            </div>
        </div>

        <div <#if filestoresExist>class="text-muted" </#if>>
            <div class="bs-callout bs-callout-info">
                <h4>2. Define your storage options</h4>
                <div class="row">
                    <div class="col-md-10">
                        <p><small>We need to know a little about where you intend to archive data from. Please
                        <a href="${springMacroRequestContext.getContextPath()}/filestores">define your storage options</a>.</small></p>
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
        </div>

        <div class="bs-callout bs-callout-info">
            <h4>3. Create your first vault</h4>
            <p><small>You have not yet defined any vaults. Please click <a href="${springMacroRequestContext.getContextPath()}/vaults/create">here</a> to create your first vault.</small></p>
        </div>

    </div>

</div>
</@layout.vaultLayout>
