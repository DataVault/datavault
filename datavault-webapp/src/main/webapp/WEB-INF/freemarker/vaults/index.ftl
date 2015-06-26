<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">
    <div class="row">
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

                <#list vaults as vault>
                    <tbody>
                    <tr class="tr">
                        <td><a href="${springMacroRequestContext.getRequestUri()}vaults/${vault.getID()}/">${vault.getID()}</a></td>
                        <td>${vault.name}</td>
                        <td>${vault.description}</td>
                        <td>${vault.size}</td>
                        <td>${vault.getCreationTime()?datetime}</td>
                    </tr>
                    </tbody>
                </#list>
             </table>
            </div>
        </form>


        <a href="${springMacroRequestContext.getRequestUri()}/vaults/create">Create a new Vault</a>
        </div>
    </div>
</div>
</@layout.vaultLayout>
