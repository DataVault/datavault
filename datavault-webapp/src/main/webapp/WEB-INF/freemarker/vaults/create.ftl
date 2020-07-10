<#import "*/layout/welcomelayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<#import "/spring.ftl" as spring />
<#assign sec=JspTaglibs["http://www.springframework.org/security/tags"] />
<@layout.vaultLayout>

    <div class="container">
        <div class="row">
            <div class="col-md-12">
                <div class="panel panel-uoe-low">

                    <div class="associated-image">
                        <figure class="uoe-panel-image uoe-panel-image"></figure>
                    </div>

                    <div class="panel-body">
                        <h2>
                            Create new vault
                            <small>
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                      title="Multiple deposits may be made into one vault. Usually one vault will correspond to one Pure record">
                                </span>
                            </small>
                        </h2>

                        <br/>

                        <p class="main">
                            You must record the details of your dataset in <a href="${link}" target="_blank">${system}</a>.
                            If you haven't yet created a Pure dataset record containing the
                            details of the data you're about to deposit in the vault, please do so.
                            If you have any questions about this step please don’t hesitate to contact
                            the Research Data Service using the ‘Contact’ link at the top of this page.
                        </p>
                        <br/>

                        <#if datasets?size == 0>
                            <div class="alert alert-danger" role="alert">
                                <strong>No Dataset available!</strong>
                            </div>
                        </#if>

                        <form id="create-vault" class="form" role="form" action="${springMacroRequestContext.getContextPath()}/vaults/create" method="post" novalidate="novalidate" _lpchecked="1">
                            <p class="main">
                                Select the Pure record describing the data that will be contained in this vault from the list below:
                            </p>

                            <div class="container">
                                <div class="row form-group required">
                                    <div class="col-sm-5">
                                        <select id="datasetID" name="datasetID" class="dataset-select selectpicker show-tick form-control form-control-lg" <#if datasets?size == 0> disabled</#if>>
                                            <option selected disabled data-hidden="true">Please choose a Dataset</option>
                                            <#list datasets as dataset>
                                                <#if dataset.visible == true>
                                                    <option value="${dataset.getID()}">${dataset.name?html}</option>
                                                <#else>
                                                    <option disabled value="${dataset.getID()}">${dataset.name?html}</option>
                                                </#if>
                                            </#list>
                                        </select>
                                    </div>
                                    <label class="control-label">
                                    </label>
                                    <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                          title="Your Pure Dataset records should be listed here. Before you create your vault, the Pure Dataset record must be validated by Research Data Service staff.&nbsp;Please contact us if you have any questions.">
                                    </span>
                                </div>
                                <div class="col-sm-3"></div>

                                <div class="alert alert-info" role="alert">
                                    <p>
                                        The ‘owner’ /PI will be billed for any deposit,
                                        against the Grant ID they provided in relation to the vault.
                                        Rates are advertised on the
                                        <a href="http://www.ed.ac.uk/is/research-support/datavault" target="_blank">Research Services charges page</a>.
                                    </p>
                                    <p>
                                        You will be aware that the data you place in the Vault belongs to the University of Edinburgh
                                        and you will only be able to retrieve the data as long as you are employed by the University of Edinburgh.
                                    </p>
                                </div>
                            </div>
                            <div class="form-group required">
                                <label for="vaultName" class="control-label">Vault Name</label>
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                      title="Maximum 400 characters.">
                                </span>
                                <@spring.bind "vault.name" />
                                <input type="text"
                                       class="form-control"
                                       name="${spring.status.expression}"
                                       value="${spring.status.value!""}"
                                       id="vaultName"
                                       placeholder="Enter a descriptive name for the Vault e.g. the project or experiment." <#if datasets?size == 0> disabled</#if>/>
                            </div>

                            <div class="form-group required">
                                <label for="description" class="control-label">Description</label>
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                      title="This description should contain information to assist you and any other colleagues who will be part of the review process when the vault retention period expires, in deciding whether the data should be retained or deleted. Maximum 6,000 characters.">
                                </span>
                                <@spring.bind "vault.description" />
                                <textarea type="text" class="form-control" name="description" id="description" rows="4" cols="60" <#if datasets?size == 0> disabled</#if>></textarea>
                            </div>

                            <div class="form-group required">
                                <label for="policyID" class="control-label">Retention Policy</label>
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                      title="This field indicates the policy with which we must comply, for the purpose of deciding the minimum amount of time for which this dataset &nbsp;must be archived. In most cases this will be the funder's policy. If there are multiple funders, it should be the one with the longest minimum among them.">
                                </span>
                                <a href="https://www.ed.ac.uk/information-services/research-support/research-data-service/planning-your-data/funder-requirements">
                                    Read more about retention policies
                                </a>
                                <div class="row">
                                    <div class="col-md-12">
                                        <select id="policyID" name="policyID" data-width="auto" class="form-control retentionPolicy-select selectpicker show-tick" <#if datasets?size == 0> disabled</#if>>
                                            <option selected disabled data-hidden="true">Please choose a retention policy</option>
                                            <#list policies as retentionPolicy>
                                                <#if retentionPolicy.minRetentionPeriod == 0>
                                                <option value="${retentionPolicy.getID()}"
                                                        data-subtext="( No minimum period )">${retentionPolicy.name}</option>
                                                <#else>
                                                 <option value="${retentionPolicy.getID()}"
                                                         data-subtext="( Minimum period: ${retentionPolicy.minRetentionPeriod} )">${retentionPolicy.name}</option>
                                                </#if>
                                            </#list>
                                        </select>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group">
                                <label  for="grantEndDate" class="control-label">
                                    <strong>Grant End Date</strong>
                                </label>
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                      title="This information will assist the university in ensuring the archive is kept for at least the minimum amount of time required by the funder(s). This field should be left blank if there is no grant associated with the work.&nbsp;">
                                </span>
                                <@spring.bind "vault.grantEndDate" />
                                <input id="grantEndDate" name="grantEndDate" class="form-control date-picker" placeholder="yyyy-mm-dd" <#if datasets?size == 0> disabled</#if>/>
                            </div>

                            <div class="alert alert-info" role="alert">
                                <p>
                                    You will not be able to add new deposits to this Vault once it is closed.
                                    The Vault will be closed ONE calendar year after the first deposit.
                                    Or, if you specify a Grant End Date, ONE calendar year after the Grant End Date IF that falls later than one year after the first deposit.
                                </p>
                            </div>

                            <div class="form-group required">
                                <label for="groupID" class="control-label">School</label>
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                      title="The School to which the vault belongs. In the absence of the vault Owner, School officers may be consulted on the management of the vault.">
                                </span>
                                <div class="row">
                                    <div class="col-md-12">
                                        <select id="groupID" name="groupID" data-width="auto" class="form-control group-select selectpicker show-tick" <#if datasets?size == 0> disabled</#if>>
                                            <option selected disabled data-hidden="true">Please choose a School</option>
                                            <#list groups as group>
                                                <#if group.enabled>
                                                    <option value="${group.getID()}">${group.name?html}</option>
                                                </#if>
                                            </#list>
                                        </select>
                                    </div>
                                    <div class="col-md-12">
                                        <small><small>
                                                Biomedical Sciences (e.g. Centre for Discovery Brain Sciences),
                                                Clinical Sciences (e.g. MRC Centre for Inflammation Research)
                                                and Molecular, Genetic and Population Health Sciences (e.g. IGMM)
                                                are all ultimately under the stewardship of the Edinburgh Medical School.
                                                We encourage colleagues to select their deanery rather than EMS,
                                                for the ease of identifying the appropriate support staff for the
                                                purposes of the review process and so on.
                                            </small></small>
                                    </div>
                                </div>
                            </div>

                            <div class="form-group required">
                                <label for="reviewDate" class="control-label">
                                    <strong>Review Date (typically ten years from now)</strong>
                                </label>
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                      title="The date by which the vault should be reviewed for decision as to whether it should be deleted or whether there are funds available to support continued storage.&nbsp;If you wish to extend the review date further into the future, please contact the support team to discuss the funding of the storage for the vault.">
                                </span>
                                <@spring.bind "vault.reviewDate" />
                                <input class="form-control" id="reviewDate" name="reviewDate" placeholder="yyyy-mm-dd" <#if datasets?size == 0> disabled</#if>/>
                            </div>

                            <input type="hidden" id="submitAction" name="action" value="submit" />
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

                            <div class="btn-toolbar pull-right">
                                <button type="submit" value="submit" class="btn btn-lg btn-primary"<#if datasets?size == 0> disabled</#if>>
                                    <span class="glyphicon glyphicon-folder-close"></span>
                                    Create new Vault
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
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

            $( "#grantEndDate" ).datepicker();
            $( "#reviewDate" ).datepicker({
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
                "reviewDate",
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

            $('#create-vault').validate({
                debug: true,
                rules: {
                    name: {
                        required: true
                    },
                    description: {
                        required: true
                    },
                    datasetID : {
                        required: true
                    },
                    policyID : {
                        required: true
                    },
                    groupID : {
                        required: true
                    },
                    grantEndDate : {
                        date: true
                    },
                    reviewDate : {
                        required: true,
                        date: true,
                        reviewDate: true
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
                'placement': 'right'
            });
        }); $('[data-toggle="popover"]').popover();
    </script>

</@layout.vaultLayout>