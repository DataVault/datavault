<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="content">
    <form>
        <table>

            <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Description</th>
                <th>Size (bytes)</th>
                <th>Timestamp</th>
            </tr>

        <#list vaults as vault>

            <tr>
                <td><a href="${springMacroRequestContext.getRequestUri()}vaults/${vault.getID()}">${vault.getID()}</a></td>
                <td>${vault.name}</td>
                <td>${vault.description}</td>
                <td>${vault.size}</td>
                <td>${vault.getCreationTime()?datetime}</td>
            </tr>

        </#list>

        </table>
    </form>

    <a href="${springMacroRequestContext.getRequestUri()}/vaults/create">Create a new Vault</a>
</div>
</@layout.vaultLayout>
