<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<h1>Vault details</h1>

<form>
    <table>

        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Description</th>
            <th>Size (bytes)</th>
            <th>Timestamp</th>
        </tr>

        <tr>
            <td><a href="${springMacroRequestContext.getRequestUri()}vaults/${vault.getID()}">${vault.getID()}</a></td>
            <td>${vault.name}</td>
            <td>${vault.description}</td>
            <td>${vault.size}</td>
            <td>${vault.creationTime?datetime}</td>
        </tr>

    </table>
</form>
</@layout.vaultLayout>
