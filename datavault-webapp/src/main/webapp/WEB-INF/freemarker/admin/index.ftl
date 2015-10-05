<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <div class="row">
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-warning">
                <div class="panel-heading">
                    <h3 class="panel-title glyphicon glyphicon-dashboard"> Size</h3>
                </div>
                <div class="panel-body">
                    <h4>Vault size: ${vaultsize}</h4>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h3 class="panel-title glyphicon glyphicon-user"> Users</h3>
                </div>
                <div class="panel-body">
                    <h4>Users: <a href="${springMacroRequestContext.getContextPath()}/admin/users">${usercount}</a></h4>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-success">
                <div class="panel-heading">
                    <h3 class="panel-title glyphicon glyphicon-dashboard"> Vaults</h3>
                </div>
                <div class="panel-body">
                    <h4>Vaults: <a href="${springMacroRequestContext.getContextPath()}/admin/vaults">${vaultcount}</a></h4>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h3 class="panel-title glyphicon glyphicon-save"> Deposits</h3>
                </div>
                <div class="panel-body">
                    <h4>Deposits: <a href="${springMacroRequestContext.getContextPath()}/admin/deposits">${depositcount}</a></h4>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-success">
                <div class="panel-heading">
                    <h3 class="panel-title glyphicon glyphicon-open"> Restores</h3>
                </div>
                <div class="panel-body">
                    <h4>Restores: ${restorecount}</h4>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4"></div>
    </div>


</div>
</@layout.vaultLayout>
