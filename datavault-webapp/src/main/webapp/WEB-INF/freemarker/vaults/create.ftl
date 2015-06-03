<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />
<div class="container">
    <div class="row">
        <div class="col-xs-12 storage">
        <h1>Create New Vault</h1>

            <form class="form-horizontal" role="form" action="" method="post">
                <div class="form-group">
                    <!--
                    <label class="col-sm-4 control-label">Vault ID:</label>
                    <div class="col-sm-6">
                        <input type="text" name="id" />
                    </div>
                    -->


                    <label class="col-xs-6 control-label">Vault Name:</label>
                    <div class="col-xs-6">
                        <@spring.bind "vault.name" />
                        <input type="text"
                               name="${spring.status.expression}"
                               value="${spring.status.value!""}"/>
                    </div>

                    <label class="col-xs-6 control-label">Description:</label>
                    <div class="col-xs-6">
                        <@spring.bind "vault.description" />
                        <textarea type="text" name="description" rows="6" cols="60"></textarea>
                    </div>

                </div>

                <div class="modal-footer">
                    <input type="submit" class="btn btn-primary" value="Submit"/>
                </div>

            </form>
        </div>
    </div>
</@layout.vaultLayout>