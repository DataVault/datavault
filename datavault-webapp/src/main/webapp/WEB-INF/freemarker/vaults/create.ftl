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
            <label class="control-label">Vault Name</label>
            <span class="text-muted"><span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" title="A descriptive name for the Vault e.g. the project or experiment."></span></span>
            <@spring.bind "vault.name" />
            <input type="text"
                   class="form-control"
                   name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
        </div>

        <div class="form-group">
            <label class="control-label">Description</label>
            <span class="text-muted"><span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" title="A detailed description of the contents or purpose of this Vault."></span></span>
            <@spring.bind "vault.description" />
            <textarea type="text"
                      class="form-control"
                      name="description"
                      rows="4" cols="60"></textarea>
        </div>

        <div class="form-group">
            <label class="control-label">Relates to</label>
            <span class="text-muted"><span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" title="An external metadata record that describes this Vault. For example, a Dataset record in a CRIS system."></span></span>
            <br>
            <select id="datasetID" name="datasetID" data-width="fit" class="dataset-select selectpicker show-tick">
                <#list datasets as dataset>
                    <option value="${dataset.getID()}">${dataset.name?html}</option>
                </#list>
            </select>
        </div>

        <div class="form-group">
            <label class="control-label">Retention Policy</label>
            <span class="text-muted"><span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" title="The set of rules which govern how long this data should be kept. This may correspond to the requirements of a specific organisation or funder."></span></span>
            <br>
            <select id="policyID" name="policyID" data-width="fit" class="retentionPolicy-select selectpicker show-tick">
                <#list policies as retentionPolicy>
                    <option value="${retentionPolicy.getID()}" data-subtext="${retentionPolicy.description?html}">${retentionPolicy.name?html}</option>
                </#list>
            </select>
        </div>

        <div class="form-group">
            <label class="control-label">Group</label>
            <span class="text-muted"><span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" title="The Group which is associated with this Vault. A Group is used to establish a chain of custody over data in a Vault. A Group administrator will be able to view information about a Vault."></span></span>
            <br>
            <select id="groupID" name="groupID" data-width="fit" class="group-select selectpicker show-tick">
                <#list groups as group>
                    <#if group.enabled>
                        <option value="${group.getID()}">${group.name?html}</option>
                    </#if>
                </#list>
            </select>
        </div>
        
        <input type="hidden" id="submitAction" name="action" value="submit"/>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

        <div class="btn-toolbar">
            <button type="submit" value="submit" class="btn btn-primary"><span class="glyphicon glyphicon-folder-close"></span> Create new Vault</button>
            <button type="submit" value="cancel" class="btn btn-danger cancel">Cancel</button>
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
        $('.retentionPolicy-select').selectpicker();
        $('.group-select').selectpicker();
        
        $('[data-toggle="tooltip"]').tooltip({
            'placement': 'top'
        });
    });
</script>

</@layout.vaultLayout>