<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<#import "/spring.ftl" as spring />
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Retention Policies</b></li>
    </ol>

    <div class="row">
        <div class="col-md-12">
            <a class="btn btn-primary pull-right" href="${springMacroRequestContext.getContextPath()}/admin/retentionpolicies/add"<span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Add Retention Policy </a>
        </div>
    </div>

    <#if policies?has_content>
        <div class="table-responsive">
            <table class="table table-striped">

                <thead>
                    <tr class="tr">
                        <th>Name</th>
                        <th>Description</th>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>

                <tbody>
                    <#list policies as policy>
                        <tr id="policy-row-${policy.getID()}" class="tr">
                            <td>
                                ${policy.name!""}
                            </td>
                            <td>
                                ${policy.description!""}
                            </td>
                            <td><a class="update-btn btn btn-default btn-sm" href="${springMacroRequestContext.getContextPath()}/admin/retentionpolicies/edit/${policy.getID()}"<span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Edit Retention Policy </a></td>
                            <td><button class="delete-btn btn btn-danger btn-sm" data-policyid="${policy.getID()}">Delete Policy</button></td>
                        </tr>
                    </#list>
                </tbody>
            </table>
        </div>
    </#if>

</div>

<script>
$(document).ready(function () {
    $.datepicker.setDefaults({
        dateFormat: "yy-mm-dd",
        changeMonth: true,
        changeYear: true,
        showOtherMonths: true,
        selectOtherMonths: true
    });
    $( ".date-picker" ).datepicker();


    // Add Spring Security CSRF header to ajax requests
    $(function () {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function (e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });


    // Handle delete button
    $('.delete-btn').click(function () {
        var policy_id = $(this).data('policyid');

        $.ajax({
            url: '${springMacroRequestContext.getContextPath()}/admin/retentionpolicies/delete/' + policy_id,
            type: 'DELETE',
            error: function (xhr, ajaxOptions, thrownError) {
                alert('Error: unable to delete policy');
            }
        }).done(function() {
            $("#policy-row-" + policy_id).remove();
        });
    });
});
</script>
</@layout.vaultLayout>
