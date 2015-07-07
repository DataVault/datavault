<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <div class="table-responsive">
        <table class="table">

            <tr class="tr">
                <th>Vault</th>
                <th>Description</th>
                <th>Size (bytes)</th>
                <th>Timestamp</th>
            </tr>

            <#list vaults as vault>
                <tbody>
                <tr class="tr">
                    <td>
                        <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/">${vault.name?html}</a>
                    </td>
                    <td>${vault.description?html}</td>
                    <td>${vault.size}</td>
                    <td>${vault.getCreationTime()?datetime}</td>
                </tr>
                </tbody>
            </#list>
        </table>

    </div>

    <form>
        <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/vaults/create">
            <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Create a new Vault
        </a>
    </form>

</div>
</@layout.vaultLayout>
