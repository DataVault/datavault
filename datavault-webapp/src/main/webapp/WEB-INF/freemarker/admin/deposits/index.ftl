<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li class="active"><b>Deposits</b></li>
    </ol>

    <form id="search-vaults" class="form" role="form" action="" method="get">
        <div class="input-group">
            <input type="text" class="form-control" name="query" value="${query}" placeholder="Search for...">
            <div class="input-group-btn">
                <button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-search" aria-hidden="true"></span> Search</button>
            </div>
        </div>
    </form>

    <#if deposits?has_content>
        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                <tr class="tr">
                    <th><a href="?sort=note&query=${query?url}">Deposit</a></th>
                    <th><a href="?sort=status&query=${query?url}">Status</a></th>
                    <th><a href="?sort=filePath&query=${query?url}">File Path</a></th>
                    <th><a href="?sort=depositSize&query=${query?url}">Size</a></th>
                    <th><a href="?sort=creationTime&query=${query?url}">Timestamp</a></th>
                </tr>
                </thead>

                <tbody>
                    <#list deposits as deposit>
                    <tr class="tr">
                        <td>${deposit.note?html}</a></td>
                        <td>${deposit.status}</td>
                        <td>${deposit.filePath?html}</td>
                        <td>${deposit.getSizeStr()}</td>
                        <td>${deposit.getCreationTime()?datetime}</td>
                    </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

</div>
</@layout.vaultLayout>
