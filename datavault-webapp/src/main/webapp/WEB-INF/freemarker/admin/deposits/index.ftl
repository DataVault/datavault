<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
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
                    <th>Deposit Name</th>
                    <th><a href="?sort=depositSize&query=${query?url}">Size</a></th>
                    <th><a href="?sort=creationTime&query=${query?url}">Date deposited</a></th>
                    <th><a href="?sort=status&query=${query?url}">Status</a></th>
                    <th>Depositor</th>
                    <th>Vault Name</th>
                    <th>Pure Record ID</th>
                    <th>School</th>
                    <th>Deposit ID</th>
                    <th>Vault ID</th>
                    <th>Vault Owner</th>
                    <th>Vault Review Date</th>
                    <th>Actions</th>
                </tr>
                </thead>

                <tbody>
                    <#list deposits as deposit>
                    <tr class="tr">
                        <td>${deposit.name?html}</td>
                        <td>${deposit.getSizeStr()}</td>
                        <td>${deposit.getCreationTime()?datetime}</td>
                        <td>${deposit.status}</td>
                        <td>${deposit.userName} (${deposit.userID})</td>
                        <td>${deposit.vaultName}</td>
                        <td>${deposit.datasetID}</td>
                        <td>${deposit.groupName}</td>
                        <td><a href="${springMacroRequestContext.getContextPath()}/vaults/${deposit.vaultID}/deposits/${deposit.getID()?html}">${deposit.getID()?html}</a></td>
                        <td><a href="${springMacroRequestContext.getContextPath()}/vaults/${deposit.vaultID}">${deposit.vaultID?html}</a></td>
                        <td>${deposit.vaultOwnerName} (${deposit.vaultOwnerID})</td>
                        <td>${deposit.vaultReviewDate}</td>
                        <td>
                            <#if deposit.status == "FAILED">
                                <a class="restart-deposit-btn btn btn-default btn-sm"
                                   href="${springMacroRequestContext.getContextPath()}/vaults/${deposit.vaultID}/deposits/${deposit.getID()?html}/restart">
                                    Restart
                                </a>
                            </#if>
                        </td>
                    </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

</div>
</@layout.vaultLayout>
