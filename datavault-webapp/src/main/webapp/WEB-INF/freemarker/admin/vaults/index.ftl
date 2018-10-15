<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
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
                        <th><a href="?sort=id&order=${orderid}&query=${query?url}">Vault ID<#if sort == "id"><#if orderid == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                        <th><a href="?sort=name&order=${ordername}&query=${query?url}">Vault Name<#if sort == "name"><#if ordername == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                        <th><a href="?sort=user&order=${orderuser}&query=${query?url}">Owner Name (UUN)<#if sort == "user"><#if orderuser == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                        <th><a href="?sort=vaultSize&order=${ordervaultsize}&query=${query?url}">Size<#if sort == "vaultSize"><#if ordervaultsize == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                        <th><a href="?sort=retentionPolicy&order=${orderpolicy}&query=${query?url}">Policy<#if sort == "retentionPolicy"><#if orderpolicy == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                        <th>School</th>
                        <th><a href="?sort=creationTime&order=${ordercreationtime}&query=${query?url}">Date created<#if sort == "creationTime"><#if ordercreationtime == "dec"><span class="dropup"><span class="caret"></span></span><#else><span class="caret"></span></#if></#if></a></th>
                        <th>Review Date</th>
                    </tr>
                </thead>

                <tbody>
                    <#list vaults as vault>
                        <tr class="tr">
                            <td>
                                <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}">${vault.getID()?html}</a>
                            </td>
                            <td>${vault.name?html}</td>
                            <td>${vault.getUserName()?html} (${vault.getUserID()?html})</td>
                            <td>${vault.getSizeStr()}</td>
                            <td>${vault.policyID?html}</td>
                            <td>${vault.groupID?html}</td>
                            <td>${vault.getCreationTime()?datetime}</td>
                            <td>${vault.reviewDate?string('dd/MM/yyyy')}</td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>

        <form id="search-vaults" class="form" role="form" action="${springMacroRequestContext.getContextPath()}/admin/vaults/csv" method="get">
            <div class="input-group">
                <input type="text" class="form-control hidden" value="${query}" name="query" placeholder="Search for...">
                <button class="btn btn-primary" type="submit"><span class="glyphicon glyphicon-export" aria-hidden="true"></span> Export to CSV</button>
            </div>
        </form>
    </#if>

</div>
</@layout.vaultLayout>
