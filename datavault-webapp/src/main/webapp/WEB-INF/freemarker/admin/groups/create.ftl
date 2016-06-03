<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/groups"><b>Groups</b></a></li>
        <li class="active">Create new Group</li>
    </ol>

    <form id="create-vault" class="form" role="form" action="" method="post">

        <div class="form-group">
            <label class="control-label">Group ID</label>
            <span class="text-muted"><span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" title="A short identifier for the Group e.g. HUM."></span></span>
            <@spring.bind "group.ID" />
            <input type="text"
                   class="form-control"
                   name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
        </div>

        <div class="form-group">
            <label class="control-label">Group Name</label>
            <span class="text-muted"><span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" title="The full name of the Group e.g. Humanities."></span></span>
            <@spring.bind "group.name" />
            <input type="text"
                   class="form-control"
                   name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
        </div>
        
        <input type="hidden" id="submitAction" name="action" value="submit"/>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

        <div class="btn-toolbar">
            <button type="submit" value="submit" class="btn btn-primary"><span class="glyphicon glyphicon-folder-close"></span> Create new Group</button>
            <button type="submit" value="cancel" class="btn btn-danger cancel">Cancel</button>
        </div>

    </form>

</div>

<script>
    $(document).ready(function () {

        $('button[type="submit"]').on("click", function() {
            $('#submitAction').val($(this).attr('value'));
        });
        
        $.validator.addMethod("identifier", function(value, element) {
            return this.optional(element) || /^[a-zA-Z0-9-_/ ]+$/i.test(value);
        }, "Group Identifiers must be made up of uppercase letters, lowercase letters, numbers, dashes or underscores.");

        $('#create-vault').validate({
            rules: {
                ID: {
                    required: true,
                    identifier: true
                },
                name: {
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
                
        $('[data-toggle="tooltip"]').tooltip({
            'placement': 'top'
        });
    });
</script>

</@layout.vaultLayout>