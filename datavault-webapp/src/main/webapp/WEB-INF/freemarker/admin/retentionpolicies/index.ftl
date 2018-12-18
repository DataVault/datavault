<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Retention Policies</b></li>
    </ol>

    <#if policies?has_content>
        <div class="table-responsive">
            <table class="table table-striped">

                <thead>
                    <tr class="tr">
                        <th>ID</th>
                        <th>Name</th>
                        <th>Description</th>
                        <th>Engine</th>
                        <th>Sort order</th>
                    </tr>
                </thead>

                <tbody>
                    <#list policies as policy>
                        <tr class="tr">
                            <td>${policy.getID()?html}</td>
                            <td>${policy.name?html}</td>
                            <td>${policy.description!?html}</td>
                            <td><span class="label label-default">${policy.engine?html}</span></td>
                            <td>${policy.sort?html}</td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

</div>
</@layout.vaultLayout>
