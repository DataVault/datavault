<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}"><b>My Vaults</b></a></li>
        <li class="active"><b>Vault:</b> ${vault.name?html}</li>
    </ol>

    <div class="table-responsive">
        <table class="table table-striped">
            <thead>
                <tr class="tr">
                    <th>Description</th>
                    <th>Policy</th>
                    <th>Size (bytes)</th>
                    <th>Timestamp</th>
                </tr>
            </thead>
            <tbody>
                <tr class="tr">
                    <td>${vault.description?html}</td>
                    <td>${policy.name}</td>
                    <td>${vault.size}</td>
                    <td>${vault.creationTime?datetime}</td>
                </tr>
            </tbody>
        </table>
    </div>

    <#if deposits?has_content>

        <h3>Deposits</h3>

        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                    <tr class="tr">
                        <th>Deposit</th>
                        <th>File Path</th>
                        <th>Timestamp</th>
                    </tr>
                </thead>

                <tbody>
                    <#list deposits as deposit>
                        <tr class="tr">
                            <td>
                                <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}">${deposit.note?html}</a>
                            </td>
                            <td>${deposit.filePath?html}</td>
                            <td>${deposit.getCreationTime()?datetime}</td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

    <form>
        <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/create">
            <span class="glyphicon glyphicon-save" aria-hidden="true"></span> Deposit data
        </a>
    </form>

</div>

</@layout.vaultLayout>
