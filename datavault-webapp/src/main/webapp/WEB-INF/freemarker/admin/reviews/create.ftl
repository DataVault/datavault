<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/admin/vaults"><b>Reviews</b></a></li>
        <li class="active"><b>Vault:</b> ${vault.name?html}</li>
    </ol>

    <form id="create-review" action="${springMacroRequestContext.getContextPath()}/admin/vaults/${vault.getID()}" method="post">

    <div class="bs-callout">
        <h2>
            <span class="glyphicon glyphicon-folder-close"></span> ${vault.name?html}
        </h2>
        <h2>
            <small>
                ${vault.description?html}
            </small>
        </h2>
        <hr>
        <p>
            <b>Owner:</b> ${vault.userID?html}<br/>
            <b>Retention Policy:</b> ${retentionPolicy.name?html}<br/>
            <b>Dataset name:</b> ${vault.datasetName?html}<br/>
            <b>Group:</b> ${group.name?html}<br/>
            <b>Created:</b> ${vault.creationTime?datetime}<br/>
            <b>Review date:</b> ${vault.policyExpiry?datetime} (Status: ${vault.policyStatusStr?html})<br/>
            <b>Size:</b> ${vault.getSizeStr()}
        </p>

        <p>Current review date is ${vault.reviewDate?datetime}</p>
        <p>New review date is ${currentReview.newReviewDate?datetime}</p>

        <div class="form-group">
            <label for="newReviewDate" class="control-label">
                <strong>Review Date</strong>
            </label>
            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                  title="The date by which the vault should be reviewed for decision as to whether it should be deleted or whether there are funds available to support continued storage.&nbsp;If you wish to extend the review date further into the future, please contact the support team to discuss the funding of the storage for the vault.">
                                </span>
            <@spring.bind "currentReview.newReviewDate"/>
            <input class="form-control" id="newReviewDate" name="newReviewDate" placeholder="yyyy-mm-dd"/>
        </div>

        <#if deposits?has_content>

            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                        <tr class="tr">
                            <th>Deposit</th>
                            <th>Status</th>

                            <th>Timestamp</th>
                            <th>Delete</th>
                            <th>Comment</th>
                        </tr>
                    </thead>

                    <tbody>
                        <#list deposits as deposit>
                            <tr class="tr">
                                <td>
                                    <a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}">${deposit.name?html}</a>
                                </td>

                                <td>
                                    <div id="deposit-status" class="job-status">
                                        <#if deposit.status.name() == "COMPLETE">
                                            <div class="text-success">
                                                <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>&nbsp;Complete
                                            </div>
                                        <#elseif deposit.status.name() == "FAILED">
                                            <div class="text-danger">
                                                <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>&nbsp;Failed
                                            </div>
                                        <#elseif deposit.status.name() == "NOT_STARTED">&nbsp;Queued
                                        <#elseif deposit.status.name() == "IN_PROGRESS">
                                            <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span>&nbsp;In progress
                                        <#else>
                                            ${deposit.status}
                                        </#if>
                                    </div>
                                </td>

                                <td>${deposit.getCreationTime()?datetime}</td>

                                <td>
                                    <div class="custom-control custom-checkbox">
                                        <input type="checkbox" class="custom-control-input" id="deleteDeposit">
                                    <#--<label class="custom-control-label" for="deleteDeposit">delete</label>-->

                                    </div>
                                </td>
                                <td>
                                    <div class="form-group">
                                        <input type="text" class="form-control" id="comment">
                                    </div>
                                </td>


                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        <#else>
            <p>No deposits</p>
        </#if>


    </div>

        <div class="form-group">
            <button type="submit" name="action" value="save" class="btn btn-info">Save</button>
            <button type="submit" name="action" value="action" class="btn btn-success">Action</button>
            <button type="submit" name="action" value="cancel" class="btn btn-danger">Cancel</button>
        </div>


        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
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

        $( "#newReviewDate" ).datepicker({
            minDate: '+1m'
        });

        $.validator.addMethod(
                "date",
                function(value, element) {
                    // put your own logic here, this is just a (crappy) example

                    if (value){
                        return value.match(/^((1|2)\d\d\d)-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$/);
                    } else {
                        return true;
                    }
                },
                "Please enter a valid date in YYYY-MM-DD format."
        );
        $.validator.addMethod(
                "newReviewDate",
                function(value, element) {
                    // put your own logic here, this is just a (crappy) example

                    if (value){
                        var inAMonth = new Date();
                        inAMonth.setMonth(inAMonth.getMonth() + 1);
                        inAMonth.setHours( 0,0,0,0 );

                        var selectedDate = new Date(value);

                        return selectedDate >= inAMonth;
                    } else {
                        return true;
                    }
                },
                "The review date must be at least one month in the future. The review date is the date at which a decision may be made about potentially deleting the data. If you believe you need the review date to be sooner, please contact the support team using the Contact button at the top of the page."
        );

        $('button[type="submit"]').on("click", function() {
            $('#submitAction').val($(this).attr('value'));
        });

        $('#create-review').validate({
            debug: true,
            rules: {
                newReviewDate : {
                    required: true,
                    date: true,
                    newReviewDate: true
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
            'placement': 'right'
        });
    }); $('[data-toggle="popover"]').popover();
</script>

</@layout.vaultLayout>
