<#import "*/layout/adminlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Users</b></li>
    </ol>

    <#if users?has_content>
        <div class="table-responsive">
            <table class="table table-striped">

                <thead>
                    <tr class="tr">
                        <th>ID</th>
                        <th>Name</th>
                    </tr>
                </thead>

                <tbody>
                    <#list users as user>
                        <tr class="tr">
                            <td>${user.id?html}</td>
                            <td>${user.name?html}</td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

</div>
</@layout.vaultLayout>
