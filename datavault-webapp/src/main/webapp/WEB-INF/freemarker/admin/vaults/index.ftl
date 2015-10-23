<#import "*/layout/adminlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Vaults</b></li>
    </ol>

    <form id="search-vaults" class="form" role="form" action="" method="get">
        <div class="input-group">
            <input type="text" class="form-control" value="${query}" name="query" placeholder="Search for...">
            <div class="input-group-btn">
                <button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-search" aria-hidden="true"></span> Search</button>
            </div>
        </div>
    </form>

    <#if vaults?has_content>

        <div class="table-responsive">
            <table class="table table-striped">

                <thead>
                    <tr class="tr">
                        <th><a href="?sort=id&query=${query?url}">ID</a></th>
                        <th><a href="?sort=name&query=${query?url}">Name</a></th>
                        <th><a href="?sort=description&query=${query?url}">Description</a></th>
                        <th><a href="?sort=user&query=${query?url}">Owner</a></th>
                        <th><a href="?sort=vaultSize&query=${query?url}">Size</a></th>
                        <th><a href="?sort=policy&query=${query?url}">Policy</a></th>
                        <th><a href="?sort=creationTime&query=${query?url}">Timestamp</a></th>
                    </tr>
                </thead>

                <tbody>
                    <#list vaults as vault>
                        <tr class="tr">
                            <td>
                                <a href="${springMacroRequestContext.getContextPath()}/admin/vaults/${vault.getID()}/">${vault.getID()?html}</a>
                            </td>
                            <td>${vault.name?html}</td>
                            <td>${vault.description?html}</td>
                            <td>${vault.getUser().getID()?html}</td>
                            <td>${vault.getSizeStr()}</td>
                            <td>${vault.policyID?html}</td>
                            <td>${vault.getCreationTime()?datetime}</td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

</div>
</@layout.vaultLayout>
