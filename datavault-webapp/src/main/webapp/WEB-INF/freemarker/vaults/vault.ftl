<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">
    <h1>Vault: ${vault.name} </h1>

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
                <td><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}">${vault.getID()}</a>
                </td>
                <td>${vault.name?html}</td>
                <td>${vault.description?html}</td>
                <td>${vault.size}</td>
                <td>${vault.creationTime?datetime}</td>
            </tr>
            </tbody>
        </table>
    </div>

    <h3>Deposits</h3>

    <div class="table-responsive">
        <table class="table">
            <tr class="tr">
                <th>ID</th>
                <th>Note</th>
                <th>File Path</th>
                <th>Timestamp</th>
            </tr>

            <#list deposits as deposit>
                <tbody>
                <tr class="tr">
                    <td>
                        <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}">${deposit.getID()}</a>
                    </td>
                    <td>${deposit.note?html}</td>
                    <td>${deposit.filePath?html}</td>
                    <td>${deposit.getCreationTime()?datetime}</td>
                </tr>
                </tbody>
            </#list>
        </table>
    </div>

    <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/create">Add a deposit</a>

</div>

</@layout.vaultLayout>
