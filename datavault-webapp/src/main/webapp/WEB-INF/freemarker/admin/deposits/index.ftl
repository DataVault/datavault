<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li class="active"><b>Deposits</b></li>
    </ol>

    <#if deposits?has_content>
        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                <tr class="tr">
                    <th>Deposit</th>
                    <th>Status</th>
                    <th>Size</th>
                    <th>File Path</th>
                   <th>Timestamp</th>
                </tr>
                </thead>

                <tbody>
                    <#list deposits as deposit>
                    <tr class="tr">
                        <td>${deposit.note?html}</a></td>
                        <td>${deposit.status}</td>
                        <td>${deposit.getSizeStr()}</td>
                        <td>${deposit.filePath?html}</td>
                        <td>${deposit.getCreationTime()?datetime}</td>
                    </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

</div>
</@layout.vaultLayout>
