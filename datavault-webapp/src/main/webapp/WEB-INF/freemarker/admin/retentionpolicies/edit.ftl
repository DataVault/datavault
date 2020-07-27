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

    <form id="add-rentention-policy-form" action="${springMacroRequestContext.getContextPath()}/admin/retentionpolicies/edit/${retentionPolicy.getId()}" method="post">
        <div class="form-rentention-policy">

            <input type="hidden" name="id" value="${retentionPolicy.getId()}">

            <div class="form-group">
                <label for="name">Name</label>
                <@spring.bind "retentionPolicy.name" />
                <input type="text" class="form-control" name="${spring.status.expression}" value="${spring.status.value!""}"/>
            </div>

            <div class="form-group">
                <label for="description">Description</label>
                <@spring.bind "retentionPolicy.description" />
                <textarea class="form-control" rows="3" name="${spring.status.expression}">${spring.status.value!""}</textarea>
            </div>

            <div class="form-group">
                <label for="url">URL</label>
                <@spring.bind "retentionPolicy.url" />
                <input type="url" class="form-control" name="${spring.status.expression}" value="${spring.status.value!""}"/>
            </div>

            <div class="form-group">
                <label  for="minRetentionPeriod" class="control-label">Min Retention Period (in years)</label>
                <@spring.bind "retentionPolicy.minRetentionPeriod" />
                <input type="number" class="form-control" name="${spring.status.expression}" value="${spring.status.value!""}"/>
            </div>

            <div class="form-check">
                <label  for="extendUponRetrieval" class="control-label">Should the retention period be extended if a deposit is retrieved for an external user?</label>
                <@spring.bind "retentionPolicy.extendUponRetrieval" />
                <input type="checkbox" class="form-check-input" id="extendUponRetrieval"
                       name="${spring.status.expression}"
                       value="true" ${retentionPolicy.extendUponRetrieval?then('checked', '')} />
            </div>

            <br/>

            <div class="form-group">
                <label  for="inEffectDate" class="control-label">In Effect Date</label>
                <@spring.bind "retentionPolicy.inEffectDate" />
                <input type="text" class="form-control date-picker" placeholder="yyyy-mm-dd" name="${spring.status.expression}" value='${spring.status.value!""}'/>
            </div>

            <div class="form-group">
                <label  for="endDate" class="control-label">End Date</label>
                <@spring.bind "retentionPolicy.endDate" />
                <input type="text" class="form-control date-picker" placeholder="yyyy-mm-dd" name="${spring.status.expression}" value='${spring.status.value!""}'/>
            </div>

            <div class="form-group">
                <label  for="dateGuidanceReviewed" class="control-label">Date Guidance Review</label>
                <@spring.bind "retentionPolicy.dateGuidanceReviewed" />
                <input type="text" class="form-control date-picker" placeholder="yyyy-mm-dd" name="${spring.status.expression}" value='${spring.status.value!""}'/>
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
