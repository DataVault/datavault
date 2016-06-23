<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>My Vaults</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}"><b>Vault:</b> ${vault.name?html}</a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}"><b>Deposit:</b> ${deposit.note?html}</a></li>
        <li class="active">Retrieve data</li>
    </ol>

    <p class="help-block">Describe the reason for this retrieve request (who and why) and choose a working directory to retrieve data from the archive</p>

    <form id="retrieve-deposit" class="form" role="form" action="" method="post">

        <div class="form-group">
            <label class="control-label">Retrieve Note:</label>
            <span class="text-muted"><span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" title="Explain the reason why this Deposit is being retrieved."></span></span>
            <@spring.bind "retrieve.note" />
            <input type="text"
                   class="form-control"
                   name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
        </div>

        <div class="form-group">
            <label class="control-label">Target directory:</label>
            <span class="text-muted"><span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" title="Select the destination directory where you wish the data to be saved to."></span></span>
            <@spring.bind "retrieve.retrievePath" />
            <input type="text"
                   style="display:none;"
                   class="form-control file-path"
                   name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
            <div id="tree" class="fancytree tree-box"></div>
        </div>

        <script>
            // Create the tree inside the <div id="tree"> element.
            $("#tree").fancytree({
                source: {
                        url: "${springMacroRequestContext.getContextPath()}/dir",
                        cache: false
                },
                lazyLoad: function(event, data){
                    var node = data.node;
                    // Load child nodes via ajax GET /dir?mode=children&parent=1234
                    data.result = {
                        url: "${springMacroRequestContext.getContextPath()}/dir",
                        data: {mode: "children", parent: node.key},
                        cache: false
                    };
                },
                selectMode: 1,
                activate: function(event, data) {
                    var node = data.tree.getActiveNode();

                    if (node) {
                        $(".file-path").val(node.key);
                    } else {
                        $(".file-path").val("");
                    }
                }
            });
        </script>

        <input type="hidden" id="submitAction" name="action" value="submit"/>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

        <div class="btn-toolbar">
            <button type="submit" value="submit" class="btn btn-primary"><i class="fa fa-upload fa-rotate-180" aria-hidden="true"></i> Retrieve data</button>
            <button type="submit" value="cancel" class="btn btn-danger cancel">Cancel</button>
        </div>

    </form>

</div>

<script>
    $(document).ready(function () {

        $('button[type="submit"]').on("click", function() {
            $('#submitAction').val($(this).attr('value'));
        });

        $('#retrieve-deposit').validate({
            ignore: ".ignore",
            rules: {
                note: {
                    required: true
                },
                retrievePath: {
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