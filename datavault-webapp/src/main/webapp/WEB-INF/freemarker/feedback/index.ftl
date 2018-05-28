<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="feedback">
<@layout.vaultLayout>
<#import "/spring.ftl" as spring />

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>Home</b></a></li>
        <li class="active"><b>Feedback</b></li>
    </ol>

    <div class="panel panel-uoe-low">
        <div class="associated-image">
            <figure class="uoe-panel-image uoe-panel-image"></figure>
        </div>

        <div class="panel-body">

            <h2>Feedback</h2>
            <br/>

            <form id="feedback" class="form-horizontal role="form" action="" method="post">

                <div class="form-group">
                    <label class="col-sm-2 control-label" for="name">Name:</label>
                    <div class="col-sm-10">
                        <input type="text"
                               class="form-control"
                               name="name"
                               value="${principal}"
                               disabled />
                    </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-2 control-label" for="email">Email:</label>
                    <div class="col-sm-10">
                        <input type="text"
                               class="form-control"
                               name="email"
                               value=""/>
                   </div>
                </div>

                <div class="form-group">
                    <label class="col-sm-2 control-label" for="feedback">Feedback:</label>
                    <div class="col-sm-10">
                        <textarea type="text"
                                  class="form-control"
                                  name="feedback"
                                  rows="4" cols="60"></textarea>
                    </div>
                </div>

                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                <div class="form-group">
                    <label class="col-sm-2 control-label"></label>
                    <div class="col-sm-10">
                        <div class="btn-toolbar">
                            <button type="submit" name="action" value="submit" class="btn btn-primary"><span class="glyphicon glyphicon-bullhorn"></span> Send feedback</button>
                            <button type="submit" name="action" value="cancel" class="btn btn-danger cancel">Cancel</button>
                        </div>
                    </div>
                </div>

            </form>

        </div>
    </div>

</div>

<script>
    $(document).ready(function () {

        $('#feedback').validate({
            rules: {
                email: {
                    required: true
                },
                feedback: {
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

        $('.retentionPolicy-select').selectpicker();
        $('.group-select').selectpicker();
    });
</script>

</@layout.vaultLayout>