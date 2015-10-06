<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li class="active"><b>Vaults</b></li>
    </ol>

    <form id="search-vaults" class="form" role="form" action="" method="post">
        <div class="input-group">
            <input type="text" class="form-control" name="query" placeholder="Search for...">
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
                        <th><a href="?sort=id">ID</a></th>
                        <th><a href="?sort=name">Name</a></th>
                        <th><a href="?sort=description">Description</a></th>
                        <th><a href="?sort=owner">Owner</a></th>
                        <th><a href="?sort=size">Size</a></th>
                        <th><a href="?sort=status">Policy</a></th>
                        <th><a href="?sort=timestamp">Timestamp</a></th>
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
