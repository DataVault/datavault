<#import "*/layout/adminlayout.ftl" as layout>
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
                                        <li class="list-group-item"><span class="glyphicon glyphicon-user" aria-hidden="true"></span> ${user.name?html} (${user.getID()?html})</li>
                                   </#list>
                                </ul>
                            </td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

</div>
</@layout.vaultLayout>
