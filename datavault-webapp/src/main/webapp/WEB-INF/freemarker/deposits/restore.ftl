<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}"><b>My Vaults</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}"><b>Vault:</b> ${vault.name?html}</a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}"><b>Deposit:</b> ${deposit.note?html}</a></li>
        <li class="active">Restore data</li>
    </ol>

    <p class="help-block">Describe the reason for this restore request (who and why) and choose a working directory to restore data from the archive</p>

    <form id="restore-deposit" class="form" role="form" action="" method="post">

        <div class="form-group">
            <label class="control-label">Restore Note:</label>
            <@spring.bind "restore.note" />
            <input type="text"
                   class="form-control"
                   name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
        </div>

        <div class="form-group">
            <label class="control-label">Target directory:</label>
            <@spring.bind "restore.restorePath" />
            <input type="text"
                   style="display:none;"
                   class="form-control file-path"
                   name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
            <div id="tree" class="fancytree-radio tree-box"></div>
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
                checkbox: true,
                selectMode: 1,
                select: function(event, data) {
                    var nodes = data.tree.getSelectedNodes();
                    $(".file-path").val("");
                    nodes.forEach(function(node) {
                        $(".file-path").val(node.key);
                    });
                }
            });
        </script>

        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

        <div class="form-group">
            <button type="submit" name="action" value="submit" class="btn btn-primary"><span class="glyphicon glyphicon-open"></span> Restore data</button>
            <button type="submit" name="action" value="cancel" class="btn btn-danger cancel">Cancel</button>
        </div>

    </form>

</div>

<script>
    $(document).ready(function () {

        $('#restore-deposit').validate({
            ignore: ".ignore",
            rules: {
                note: {
                    required: true
                },
                restorePath: {
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

        $('.policy-select').selectpicker();
    });
</script>

</@layout.vaultLayout>