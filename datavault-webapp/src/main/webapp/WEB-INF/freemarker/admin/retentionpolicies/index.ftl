<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<#import "/spring.ftl" as spring />
<@layout.vaultLayout>

<div class="modal fade" id="add-rentention-policy-modal" tabindex="-1" role="dialog" aria-labelledby="addRetentionPolicyLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <h4 class="modal-title" id="addRetentionPolicyLabel">Add Retention Policy</h4>
            </div>
            <div class="modal-body">
                <form id="add-rentention-policy-form" action="${springMacroRequestContext.getContextPath()}/admin/retentionpolicies/create" method="post">
                    <div class="form-rentention-policy">
                        <div class="form-group">
                            <label for="name">Name</label>
                            <@spring.bind "retentionPolicy.name" />
                            <input type="text" class="form-control" id="name" name="name"/>
                        </div>
                        <div class="form-group">
                            <label for="description">Description</label>
                            <@spring.bind "retentionPolicy.description" />
                            <textarea class="form-control" id="description" name="description" rows="3"></textarea>
                        </div>
                        <div class="form-group">
                            <label for="engine">Engine</label>
                            <@spring.bind "retentionPolicy.engine" />
                            <input class="form-control" id="engine" name="engine" rows="3"
                                   value="${spring.status.value!"org.datavaultplatform.common.retentionpolicy.impl.DefaultRetentionPolicy"}"/>
                        </div>
                        <div class="form-group">
                            <label for="url">URL</label>
                            <@spring.bind "retentionPolicy.url" />
                            <input type="url" class="form-control" id="url" name="url"/>
                        </div>
                        <div class="form-group">
                            <label for="sort">Sort Order</label>
                            <@spring.bind "retentionPolicy.sort" />
                            <input type="number" class="form-control" id="sort" name="sort"/>
                        </div>
                        <div class="form-group">
                            <label  for="minDataRetentionPeriod" class="control-label">
                                <strong>Min Date Retention Period</strong>
                            </label>
                            <@spring.bind "retentionPolicy.minDataRetentionPeriod" />
                            <input id="minDataRetentionPeriod" name="minDataRetentionPeriod" class="form-control date-picker" placeholder="yyyy-mm-dd"/>
                        </div>
                        <div class="form-group">
                            <label  for="inEffectDate" class="control-label">
                                <strong>In Effect Date</strong>
                            </label>
                            <@spring.bind "retentionPolicy.inEffectDate" />
                            <input id="inEffectDate" name="inEffectDate" class="form-control date-picker" placeholder="yyyy-mm-dd"/>
                        </div>
                        <div class="form-group">
                            <label  for="endDate" class="control-label">
                                <strong>End Date</strong>
                            </label>
                            <@spring.bind "retentionPolicy.endDate" />
                            <input id="endDate" name="endDate" class="form-control date-picker" placeholder="yyyy-mm-dd"/>
                        </div>
                        <div class="form-group">
                            <label  for="dateGuidanceReviewed" class="control-label">
                                <strong>Date Guidance Review</strong>
                            </label>
                            <@spring.bind "retentionPolicy.dateGuidanceReviewed" />
                            <input id="dateGuidanceReviewed" name="dateGuidanceReviewed" class="form-control date-picker" placeholder="yyyy-mm-dd"/>
                        </div>

                        <input type="hidden" id="submitAction" name="action" value="submit" />
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button form="add-rentention-policy-form" type="submit" value="submit" class="btn btn-primary">Submit</button>
            </div>
        </div>
    </div>
</div>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Retention Policies</b></li>
    </ol>

    <div class="row">
        <div class="col-md-12">
            <a class="btn btn-primary pull-right" href="#" data-toggle="modal" data-target="#add-rentention-policy-modal">
                <span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Add Retention Policy
            </a>
        </div>
    </div>

    <#if policies?has_content>
        <div class="table-responsive">
            <table class="table table-striped">

                <thead>
                    <tr class="tr">
                        <th>ID</th>
                        <th>Name</th>
                        <th>Description</th>
                        <th>Engine</th>
                        <th>Sort order</th>
                        <th></th>
                        <th></th>
                    </tr>
                </thead>

                <tbody>
                    <#list policies as policy>
                        <tr id="policy-row-${policy.getID()}" class="tr">
                            <td>${policy.getID()?html}</td>
                            <td>
                                <input id="${policy.getID()}-name" value="${policy.name?html}"/>
                            </td>
                            <td>
                                <textarea id="${policy.getID()}-description" class="form-control" name="${policy.getID()}-description" rows="3">${policy.description!?html}</textarea>
                            </td>
                            <td><span class="label label-default">${policy.engine?html}</span></td>
                            <td>${policy.sort?html}</td>
                            <td><button class="update-btn btn btn-default btn-sm" data-policyid="${policy.getID()}">Update Policy</button></td>
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
    $( '#minDataRetentionPeriod' ).datepicker();

    // Add Spring Security CSRF header to ajax requests
    $(function () {
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        $(document).ajaxSend(function (e, xhr, options) {
            xhr.setRequestHeader(header, token);
        });
    });

    // Handle description update
    $('.update-btn').click(function () {
        var policy_id = $(this).data('policyid');

        var new_name = $("#" + policy_id + "-name").val();
        var new_description = $("#" + policy_id + "-description").val();

        $.ajax({
            url: '${springMacroRequestContext.getContextPath()}/admin/retentionpolicies/' + policy_id + '/update',
            type: 'POST',
            data: {
                name: new_name,
                description: new_description
            },
            error: function (xhr, ajaxOptions, thrownError) {
                alert('Error: unable to update policy name');
            }
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
