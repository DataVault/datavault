<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">
    <div class="row">
        <div class="col-sm-12 storage">
        <h1>Create New Vault</h1>

            <form class="form-horizontal" role="form">
                <div class="form-group">
                    <label class="col-sm-4 control-label">Vault ID:</label>
                    <div class="col-sm-6"><input type="text" name="id" /></div>
                    <label class="col-sm-4 control-label">Vault Name:</label>
                        <div class="col-sm-6"><input type="text" name="name" /></div>
                    <label class="col-sm-4 control-label">Description:</label>
                            <div class="col-sm-6"><textarea type="text" name="description" rows="6" cols="60"></textarea></div>
                </div>
                <input type="submit" value="Submit" />

                <div class="modal-footer">
                    <input type="submit" class="btn btn-primary" value="Submit"/>
                </div>

            </form>
        </div>
    </div>
</@layout.vaultLayout>