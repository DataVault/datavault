<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <div class="table-responsive">
        <table class="table">

            <tr class="tr">
                <th>ID</th>
                <th>Name</th>
                <th>Description</th>
                <th>Size (bytes)</th>
                <th>Timestamp</th>
            </tr>

            <#list vaults as vault>
                <tbody>
                <tr class="tr">
                    <td>
                        <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/">${vault.getID()}</a>
                    </td>
                    <td>${vault.name?html}</td>
                    <td>${vault.description?html}</td>
                    <td>${vault.size}</td>
                    <td>${vault.getCreationTime()?datetime}</td>
                </tr>
                </tbody>
            </#list>
        </table>

    </div>

    <a href="${springMacroRequestContext.getContextPath()}/vaults/create">Create a new Vault</a>

</div>
</@layout.vaultLayout>
