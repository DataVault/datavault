<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="none">

<@layout.vaultLayout>

<div class="container">

    <div class="alert alert-danger" role="alert">
        <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
        <span class="sr-only">Error:</span>
        An error has occured!<br/><br/>
        <#if message?has_content>
            <span>${message}</span>
        </#if>
    </div>

</div>

</@layout.vaultLayout>