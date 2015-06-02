<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">
<div class="row">
<h1>Vault: ${vault.name} </h1>
<div class="col-sm-12 storage">
    <form>
        <div class="table-responsive">
            <table class="table">
                <tr class="tr">
                    <th>ID</th>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Size (bytes)</th>
                    <th>Timestamp</th>
                </tr>
                <tbody>
                    <tr class="tr">
                        <td><a href="${springMacroRequestContext.getRequestUri()}vaults/${vault.getID()}">${vault.getID()}</a></td>
                        <td>${vault.name}</td>
                        <td>${vault.description}</td>
                        <td>${vault.size}</td>
                        <td>${vault.creationTime?datetime}</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </form>
</div>
</div>
</div>

</@layout.vaultLayout>
