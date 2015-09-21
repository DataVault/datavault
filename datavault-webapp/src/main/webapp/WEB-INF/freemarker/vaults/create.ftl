<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}"><b>My Vaults</b></a></li>
        <li class="active">Create new vault</li>
    </ol>

    <form id="create-vault" class="form" role="form" action="" method="post">

        <div class="form-group">
            <label class="control-label">Vault Name:</label>
            <@spring.bind "vault.name" />
            <input type="text"
                   class="form-control"
                   name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
        </div>

        <div class="form-group">
            <label class="control-label">Description:</label>
            <@spring.bind "vault.description" />
            <textarea type="text"
                      class="form-control"
                      name="description"
                      rows="6" cols="60"></textarea>
        </div>

        <div class="form-group">
            <label class="control-label">Policy:</label>
            <@spring.bind "policyMap" />
            <@spring.formSingleSelect "vault.policyID", policyMap, "class='policy-select'" />
        </div>

        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

        <div class="pull-left">
            <button type="submit" name="action" value="submit" class="btn btn-primary"><span class="glyphicon glyphicon-folder-close"></span> Create new Vault</button>
        </div>

        <div class="pull-right">
            <button type="submit" name="action" value="cancel" class="btn btn-danger cancel">Cancel</button>
        </div>

    </form>

</div>

<script>
    $(document).ready(function () {

        $('#create-vault').validate({
            rules: {
                name: {
                    required: true
                },
                description: {
                    required: true
                }
            },
            highlight: function (element) {
                $(element).closest('.form-group').removeClass('has-success').addClass('has-error');
            },
            success: function (element) {
                element.addClass('valid')
                    .closest('.form-group').removeClass('has-error').addClass('has-success');
            }
        });

        $('.policy-select').selectpicker();
    });
</script>

</@layout.vaultLayout>