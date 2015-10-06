<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li class="active"><b>Restores</b></li>
    </ol>

    <#if restores?has_content>
        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                <tr class="tr">
                    <th>Deposit</th>
                    <th>Timestamp</th>
                    <th>Restore path</th>
                    <th>Restore note</th>
                </tr>
                </thead>

                <tbody>
                    <#list restores as restore>
                    <tr class="tr">
                        <td>${restore.getDeposit().getID()?html}</td>
                        <td>${restore.timestamp?datetime}</td>
                        <td>${restore.restorePath?html}</td>
                        <td>${restore.note?html}</td>
                    </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

</div>
</@layout.vaultLayout>
