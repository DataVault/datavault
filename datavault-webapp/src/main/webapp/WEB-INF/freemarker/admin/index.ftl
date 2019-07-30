<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>Home</b></a></li>
        <li class="active"><b>Administration</b></li>
    </ol>

    <div class="row">
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-warning">
                <div class="panel-heading">
                    <h3 class="panel-title">Vault Size</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="#"><span class="glyphicon glyphicon-dashboard"></span> ${vaultsize}</a></h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-warning">
                <div class="panel-heading">
                    <h3 class="panel-title">Deposits / Retrievals In Progress</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="#"><span class="glyphicon glyphicon-chevron-down"></span> ${depositsinprogress}</a> <a href="#"><span class="glyphicon glyphicon-chevron-up"></span> ${retrievesinprogress}</a></h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-warning">
                <div class="panel-heading">
                    <h3 class="panel-title">Deposit / Retrieval Queues</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="#"><span class="glyphicon glyphicon-arrow-down"></span> ${depositqueue}</a> <a href="#"><span class="glyphicon glyphicon-arrow-up"></span> ${retrievequeue}</a></h1>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h3 class="panel-title">Vaults</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="${springMacroRequestContext.getContextPath()}/admin/vaults"><span class="glyphicon glyphicon-folder-close"></span> ${vaultcount}</a></h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h3 class="panel-title">Deposits</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="${springMacroRequestContext.getContextPath()}/admin/deposits"><i class="fa fa-download fa-rotate-180" aria-hidden="true"></i> ${depositcount}</a></h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h3 class="panel-title">Retrieves</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="${springMacroRequestContext.getContextPath()}/admin/retrieves"><i class="fa fa-upload fa-rotate-180" aria-hidden="true"></i> ${retrievecount}</a></h1>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-success">
                <div class="panel-heading">
                    <h3 class="panel-title">Billing</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="${springMacroRequestContext.getContextPath()}/admin/billing"><i class="fa fa-money" aria-hidden="true"></i> </a></h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-success">
                <div class="panel-heading">
                    <h3 class="panel-title">Groups</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="${springMacroRequestContext.getContextPath()}/admin/groups"><i class="fa fa-user" aria-hidden="true"></i> ${groupcount}</a></h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-success">
                <div class="panel-heading">
                    <h3 class="panel-title">Roles</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center">
                        <a href="${springMacroRequestContext.getContextPath()}/admin/roles">
                            <!-- TODO this hack should be changed to show fa-user-cog once font-awesome has been upgrade to version 5 -->
                            <i style="mask: url('${springMacroRequestContext.getContextPath()}/resources/theme/images/user-cog-solid.svg') no-repeat center; width: 1em; height: 1em; display: inline-block; background-color: rgb(52, 109, 191);"></i>
                            <span style="vertical-align: top;">${rolecount}</span>
                        </a>
                    </h1>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">Events</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="${springMacroRequestContext.getContextPath()}/admin/events"><span class="glyphicon glyphicon-time"></span> ${eventcount}</a></h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">Retention Policies</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="${springMacroRequestContext.getContextPath()}/admin/retentionpolicies"><span class="glyphicon glyphicon-bell"></span> ${policycount}</a></h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-default">
                <div class="panel-heading">
                    <h3 class="panel-title">Archive Stores</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="${springMacroRequestContext.getContextPath()}/admin/archivestores"><span class="glyphicon glyphicon-upload"></span> ${archivestorescount}</a></h1>
                </div>
            </div>
        </div>
    </div>

</div>
</@layout.vaultLayout>
