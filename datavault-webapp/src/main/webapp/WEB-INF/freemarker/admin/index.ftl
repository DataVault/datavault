<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <div class="row">
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-warning">
                <div class="panel-heading">
                    <h3 class="panel-title"> Vault Size</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><span class="glyphicon glyphicon-dashboard"></span> ${vaultsize}</h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h3 class="panel-title"> Users</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><span class="glyphicon glyphicon-user"></span> <a href="${springMacroRequestContext.getContextPath()}/admin/users">${usercount}</a></h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-success">
                <div class="panel-heading">
                    <h3 class="panel-title"> Vaults</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><span class="glyphicon glyphicon-folder-close"></span> <a href="${springMacroRequestContext.getContextPath()}/admin/vaults">${vaultcount}</a></h1>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h3 class="panel-title"> Deposits</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><span class="glyphicon glyphicon-save"></span> <a href="${springMacroRequestContext.getContextPath()}/admin/deposits">${depositcount}</a></h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-success">
                <div class="panel-heading">
                    <h3 class="panel-title"> Restores</h3>
                </div>
                <div class="panel-body">
                    <h1 class="text-center"><span class="glyphicon glyphicon-open"></span> ${restorecount}</h1>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4"></div>
    </div>


</div>
</@layout.vaultLayout>
