<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/vaults"><b>Vaults</b></a></li>
        <li class="active"><b>Vault:</b> ${vault.name?html}</li>
    </ol>

    <div class="table-responsive">
        <table class="table table-striped">
            <thead>
                <tr class="tr">
                    <th>Description</th>
                    <th>Group</th>
                    <th>Size</th>
                    <th>Timestamp</th>
                </tr>
            </thead>
            <tbody>
                <tr class="tr">
                    <td>${vault.description?html}</td>
                    <td>${group.name?html}</td>
                    <td>${vault.getSizeStr()}</td>
                    <td>${vault.creationTime?datetime}</td>
                </tr>
            </tbody>
        </table>
    </div>

    <div class="table-responsive">
        <table class="table table-striped">
            <thead>
                <tr class="tr">
                    <th>Policy</th>
                    <th>Last checked</th>
                    <th>Status</th>
                    <th></th>
                </tr>
            </thead>
            <tbody>
                <tr class="tr">
                    <td>${policy.name?html}</td>
                    <td><#if vault.policyLastChecked??>${vault.policyLastChecked?datetime}<#else>Never</#if></td>
                    <td>${vault.policyStatusString?html}</td>
                    <td>
                        <form action="${springMacroRequestContext.getContextPath()}/admin/vaults/${vault.getID()}/checkpolicy" method="post">
                            <button type="submit" class="btn btn-default"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span> Update</button>
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        </form>
                    </td>
                </tr>
            </tbody>
        </table>
    </div>

    <#if deposits?has_content>

        <h3>Deposits</h3>

        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                    <tr class="tr">
                        <th>Deposit</th>
                        <th>Origin</th>
                        <th>File Path</th>
                        <th>Status</th>
                        <th>Size</th>
                        <th>Timestamp</th>
                    </tr>
                </thead>

                <tbody>
                    <#list deposits as deposit>
                        <tr class="tr">
                            <td>
                                <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}">${deposit.note?html}</a>
                            </td>
                            <td>${deposit.fileOrigin?html}</td>
                            <td>${deposit.shortFilePath?html}</td>
                            <td>${deposit.status?html}</td>
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
