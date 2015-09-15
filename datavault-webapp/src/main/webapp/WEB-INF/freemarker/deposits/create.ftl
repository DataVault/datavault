<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}"><b>My Vaults</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}"><b>Vault:</b> ${vault.name?html}</a></li>
        <li class="active">Create new deposit</li>
    </ol>

    <form id="create-deposit" class="form" role="form" action="" method="post">

        <div class="form-group">
            <label class="control-label">Deposit Note:</label>
            <@spring.bind "deposit.note" />
            <input type="text"
                   class="form-control"
                   name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
        </div>

        <div class="form-group" style="display:none;">
            <label class="control-label">Filepath:</label>
            <@spring.bind "deposit.filePath" />
            <input type="text"
                    class="form-control file-path"
                    name="${spring.status.expression}"
                    value="${spring.status.value!""}"/>
        </div>

        <div class="form-group">
            <label class="control-label">Deposit file or directory:</label>
            <div id="tree" class="fancytree-radio tree-box"></div>
        </div>

        <script>
            // Create the tree inside the <div id="tree"> element.
            $("#tree").fancytree({
                source: {
                        url: "/datavault-webapp/files",
                        cache: false
                },
                lazyLoad: function(event, data){
                    var node = data.node;
                    // Load child nodes via ajax GET /files?mode=children&parent=1234
                    data.result = {
                        url: "/datavault-webapp/files",
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
        
        <div>
            <button type="submit" class="btn btn-primary">Submit</button>
        </div>


    </form>
</div>

<script>
    $(document).ready(function () {

        $('#create-deposit').validate({
            rules: {
                note: {
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
    });
</script>

</@layout.vaultLayout>