<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <div class="table-responsive">
        <table class="table table-striped">

            <thead>
                <tr class="tr">
                    <th>Vault</th>
                    <th>Description</th>
                    <th>Size (bytes)</th>
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
                        <td>${vault.size}</td>
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

</div>
</@layout.vaultLayout>
