<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">

    <div class="row">
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-primary">
                <div class="panel-heading">
                    <h3 class="panel-title glyphicon glyphicon-user"> Users</h3>
                </div>
                <div class="panel-body">
                    <ul>
                        <li>Users: ${usercount} (<a href="users">List users</a>)</li>
                    </ul>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-success">
                <div class="panel-heading">
                    <h3 class="panel-title glyphicon glyphicon-dashboard"> Vaults</h3>
                </div>
                <div class="panel-body">
                    <ul>
                        <li>Vaults: ${vaultcount}</li>
                    </ul>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h3 class="panel-title glyphicon glyphicon-save"> Deposits</h3>
                </div>
                <div class="panel-body">
                    <ul>
                        <li>Deposits: ${depositcount}</li>
                    </ul>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-xs-6 col-md-4">
            <div class="panel panel-warning">
                <div class="panel-heading">
                    <h3 class="panel-title glyphicon glyphicon-dashboard"> Size</h3>
                </div>
                <div class="panel-body">
                    <ul>
                        <li>Vault size: ${vaultsize}</li>
                    </ul>
                </div>
            </div>
        </div>
        <div class="col-xs-6 col-md-4"></div>
        <div class="col-xs-6 col-md-4"></div>
    </div>


</div>
</@layout.vaultLayout>
