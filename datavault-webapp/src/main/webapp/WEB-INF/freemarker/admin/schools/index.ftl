<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

    <div class="container">

        <ol class="breadcrumb">
            <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
            <li class="active"><b>Schools</b></li>
        </ol>

        <#list schools as school>
            <div>
                <a href="${springMacroRequestContext.getContextPath()}/admin/schools/${school.getID()}">${school.name}</a>
            </div>
        </#list>

    </div>
</@layout.vaultLayout>
