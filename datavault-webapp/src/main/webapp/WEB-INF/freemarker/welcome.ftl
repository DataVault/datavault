<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
<div class="container">

    <div class="jumbotron">
        <p>Welcome to the Data Vault!</p>
        <p>As this may be your first time here, please read the
        <a href="${springMacroRequestContext.getContextPath()}/help">help section</a> before continuing.</p>

        <div <#if filestoresExist>class="text-muted" </#if>>
            <div class="bs-callout bs-callout-info">
                <h4>1. Define your storage options</h4>
                <div class="row">
                    <div class="col-md-10">
                        <p>We need to know a little about where you intend to archive data from. Please
                        <a href="${springMacroRequestContext.getContextPath()}/filestores">define your storage options</a>.</p>
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

        <div <#if datasetsExist>class="text-muted" </#if>>
            <div class="bs-callout bs-callout-info">
                <h4>2. Define a dataset in ${system}</h4>
                <div class="row">
                    <div class="col-md-10">
                    <p>Have you recorded a dataset in <a href="${link}" target="_blank">${system}</a> to record your intended vault/archive?</p>
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

        <div class="bs-callout bs-callout-info">
            <h4>3. Create your first vault</h4>
            <p>You are now ready to <a href="${springMacroRequestContext.getContextPath()}/vaults/create">create your first vault</a>!</p>
        </div>

    </div>

</div>
</@layout.vaultLayout>
