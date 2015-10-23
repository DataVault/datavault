<#import "*/layout/adminlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
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
                    <h3 class="panel-title">Deposits / Restores In Progress</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="#"><span class="glyphicon glyphicon-chevron-down"></span> ${depositsinprogress}</a> <a href="#"><span class="glyphicon glyphicon-chevron-up"></span> ${restoresinprogress}</a></h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-warning">
                <div class="panel-heading">
                    <h3 class="panel-title">Deposit / Restore Queues</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="#"><span class="glyphicon glyphicon-arrow-down"></span> ${depositqueue}</a> <a href="#"><span class="glyphicon glyphicon-arrow-up"></span> ${restorequeue}</a></h1>
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
                    <h1 class="text-center"><a href="${springMacroRequestContext.getContextPath()}/admin/deposits"><span class="glyphicon glyphicon-save"></span> ${depositcount}</a></h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h3 class="panel-title">Restores</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="${springMacroRequestContext.getContextPath()}/admin/restores"><span class="glyphicon glyphicon-open"></span> ${restorecount}</a></h1>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-success">
                <div class="panel-heading">
                    <h3 class="panel-title">Users</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><a href="${springMacroRequestContext.getContextPath()}/admin/users"><span class="glyphicon glyphicon-user"></span> ${usercount}</a></h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
        </div>
        <div class="col-xs-6 col-md-4">
        </div>
    </div>

</div>
</@layout.vaultLayout>
