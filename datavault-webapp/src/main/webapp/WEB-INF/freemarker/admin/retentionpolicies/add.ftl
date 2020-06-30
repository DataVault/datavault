<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<#import "/spring.ftl" as spring />
<@layout.vaultLayout>


<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li class="active"><b>Add Retention Policies</b></li>
    </ol>

        <form id="add-rentention-policy-form" action="${springMacroRequestContext.getContextPath()}/admin/retentionpolicies/add" method="post">
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
                    <label for="url">URL</label>
                            <@spring.bind "retentionPolicy.url" />
                    <input type="url" class="form-control" id="url" name="url"/>
                </div>

                <div class="form-group">
                    <label  for="minRetentionPeriod" class="control-label">
                        <strong>Min Retention Period (in years)</strong>
                    </label>
                            <@spring.bind "retentionPolicy.minRetentionPeriod" />
                    <input type=="number" class="form-control" id="minRetentionPeriod" name="minRetentionPeriod"/>
                </div>

                <div class="form-check">
                    <label  for="extendUponRetrieval" class="control-label">
                        <strong>Should the retention period be extended if a deposit is retrieved for an external user? </strong>
                    </label>
                    <@spring.bind "retentionPolicy.extendUponRetrieval" />
                    <input type="checkbox" class="form-check-input" id="extendUponRetrieval"
                           name="${spring.status.expression}"
                           value="true" ${retentionPolicy.extendUponRetrieval?then('checked', '')} />
                </div>

                <br/>

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

                <div class="form-group">
                    <button type="submit" name="action" value="submit" class="btn btn-primary"><span class="glyphicon glyphicon-save"></span>Save</button>
                    <button type="submit" name="action" value="cancel" class="btn btn-danger cancel">Cancel</button>
                </div>

                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            </div>
        </form>

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

});
</script>
</@layout.vaultLayout>
