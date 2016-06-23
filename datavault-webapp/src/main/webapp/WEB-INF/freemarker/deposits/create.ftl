<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>My Vaults</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}"><b>Vault:</b> ${vault.name?html}</a></li>
        <li class="active">Create new deposit</li>
    </ol>

    <form id="create-deposit" class="form" role="form" action="" method="post">

        <div class="form-group">
            <label class="control-label">Deposit Note:</label>
            <span class="text-muted"><span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" title="A descriptive name for this particular Deposit to the Vault, to set it apart from other parts of the data."></span></span>
            <@spring.bind "deposit.note" />
            <input type="text"
                   class="form-control"
                   name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
        </div>

        <div class="form-group">
            <label class="control-label">Deposit file or directory:</label>
            <span class="text-muted"><span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" title="Select a single file, or a directory to add to the Vault."></span></span>
            <@spring.bind "deposit.filePath" />
            <input type="text"
                   style="display:none;"
                   class="form-control file-path"
                   name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
            <div id="tree" class="fancytree tree-box"></div>
            <label id="deposit-size" class="text-muted small">No files selected</label>
        </div>
        
        <script>
            var filesizeSeq = 0;
            
            // Create the tree inside the <div id="tree"> element.
            $("#tree").fancytree({
                source: {
                        url: "${springMacroRequestContext.getContextPath()}/files",
                        cache: false
                },
                lazyLoad: function(event, data){
                    var node = data.node;
                    // Load child nodes via ajax GET /files?mode=children&parent=1234
                    data.result = {
                        url: "${springMacroRequestContext.getContextPath()}/files",
                        data: {mode: "children", parent: node.key},
                        cache: false
                    };
                },
                selectMode: 1,
                activate: function(event, data) {
                    var node = data.tree.getActiveNode();
                    filesizeSeq = filesizeSeq + 1;
                    var currentSeq = filesizeSeq;
                    
                    if (node) {
                        $("#deposit-size").text("Deposit size: calculating ...");
                        $(".file-path").val(node.key);
                        $.ajax({
                            url: "${springMacroRequestContext.getContextPath()}/filesize",
                            type: "GET",
                            data: {filepath: node.key},
                            dataType: 'text',
                            success: function (result) {
                                if (currentSeq == filesizeSeq) {
                                    $("#deposit-size").text("Deposit size: " + result);
                                }
                            }
                        });
                    } else {
                        $(".file-path").val("");
                        $("#deposit-size").text("No files selected");
                    }
                }
            });
        </script>

        <div class="btn-toolbar">
            <button type="submit" value="submit" class="btn btn-primary"><i class="fa fa-download fa-rotate-180" aria-hidden="true"></i> Deposit data</button>
            <button type="submit" value="cancel" class="btn btn-danger cancel">Cancel</button>
        </div>

        <input type="hidden" id="submitAction" name="action" value="submit"/>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

    </form>
</div>

<script>
    $(document).ready(function () {

        $('button[type="submit"]').on("click", function() {
            $('#submitAction').val($(this).attr('value'));
        });

        $('#create-deposit').validate({
            ignore: ".ignore",
            rules: {
                note: {
                    required: true
                },
                filePath: {
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