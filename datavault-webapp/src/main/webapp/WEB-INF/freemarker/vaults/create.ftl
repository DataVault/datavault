<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>My Vaults</b></a></li>
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
            <label class="control-label">Dataset:</label>
            <select id="datasetID" name="datasetID" class='dataset-select'>
                <#list datasets as dataset>
                    <option value="${dataset.getID()}">${dataset.name?html}</option>
                </#list>
            </select>
        </div>

        <div class="form-group">
            <label class="control-label">Policy:</label>
            <select id="policyID" name="policyID" class='policy-select'>
                <#list policies as policy>
                    <option value="${policy.getID()}">${policy.name?html}</option>
                </#list>
            </select>
        </div>

        <div class="form-group">
            <label class="control-label">Group:</label>
            <select id="groupID" name="groupID" class='group-select'>
                <#list groups as group>
                    <option value="${group.getID()}">${group.name?html}</option>
                </#list>
            </select>
        </div>

        <input type="hidden" id="submitAction" name="action" value="submit"/>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

        <div class="form-group">
            <button type="submit" name="action" value="submit" class="btn btn-primary"><span class="glyphicon glyphicon-folder-close"></span> Create new Vault</button>
            <button type="submit" name="action" value="cancel" class="btn btn-danger cancel">Cancel</button>
        </div>

    </form>

</div>

<script>
    $(document).ready(function () {

        $('button[type="submit"]').on("click", function() {
            $('#submitAction').val($(this).attr('value'));
        });
        
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
            },
            submitHandler: function (form) {
                $('button[type="submit"]').prop('disabled', true);
                form.submit();
            }
        });
        
        $('.dataset-select').selectpicker();
        $('.policy-select').selectpicker();
        $('.group-select').selectpicker();
    });
</script>

</@layout.vaultLayout>