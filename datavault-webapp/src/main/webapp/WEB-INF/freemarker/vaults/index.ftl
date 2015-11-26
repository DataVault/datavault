<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
<div class="container">

    <#if vaults?has_content>
        <ol class="breadcrumb">
            <li class="active"><b>My Vaults</b></li>
        </ol>

        <div class="table-responsive">
            <table class="table table-striped">

                <thead>
                    <tr class="tr">
                        <th>Vault</th>
                        <th>Description</th>
                        <th>Size</th>
                        <th>Timestamp</th>
                    </tr>
                </thead>

                <tbody>
                    <#list vaults as vault>
                        <tr class="tr">
                            <td>
                                <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/">${vault.name?html}</a>
                            </td>
                            <td>${vault.description?html}</td>
                            <td>${vault.getSizeStr()}</td>
                            <td>${vault.getCreationTime()?datetime}</td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>

        <form>
            <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/vaults/create">
                <span class="glyphicon glyphicon-folder-close" aria-hidden="true"></span> Create new Vault
            </a>
        </form>
    <#else>
        <div class="jumbotron">
            <p>Welcome to the Data Vault!</p>
            <p>Please use the 'Create new Vault' button below to get started...</p>
            <p>
                <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/vaults/create">
                <span class="glyphicon glyphicon-folder-close" aria-hidden="true"></span> Create new Vault
                </a>
            </p>
        </div>
    </#if>



</div>
</@layout.vaultLayout>
