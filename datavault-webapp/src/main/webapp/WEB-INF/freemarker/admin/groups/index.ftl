<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Groups</b></li>
    </ol>

    <#if groups?has_content>
        <div class="table-responsive">
            <table class="table table-striped">

                <thead>
                    <tr class="tr">
                        <th>ID</th>
                        <th>Name</th>
                        <th>Vaults</th>
                        <th>Owners</th>
                    </tr>
                </thead>

                <tbody>
                    <#assign counter = 0 >
                    <#list groups as group>
                        <tr class="tr">
                            <td>${group.ID?html}</td>
                            <td>${group.name?html}</td>
                            <td><span class="badge">${vaultCounts[counter]}<#assign counter = counter + 1></span></td>
                            <td>
                                <ul class="list-group">
                                    <#list group.getOwners() as user>
                                        <li class="list-group-item"><span class="glyphicon glyphicon-user" aria-hidden="true"></span> ${user.name?html} (${user.getID()?html})
                                            <a class="btn btn-xs btn-danger pull-right" href="${springMacroRequestContext.getContextPath()}/admin/groups/${group.ID}/remove/${user.getID()}">
                                                <span class="glyphicon glyphicon-remove" aria-hidden="true"></span> Remove
                                            </a>
                                        </li>
                                   </#list>
                                </ul>
                                <form>
                                    <a class="btn btn-default" href="${springMacroRequestContext.getContextPath()}/admin/groups/${group.ID}/add">
                                        <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Add Owner
                                    </a>
                                </form>
                            </td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

    <form>
        <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/admin/groups/create">
            <span class="glyphicon glyphicon-education" aria-hidden="true"></span> Create new Group
        </a>
    </form>

</div>
</@layout.vaultLayout>
