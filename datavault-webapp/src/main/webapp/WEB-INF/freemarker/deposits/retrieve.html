<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>Home</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/"><b>Vault:</b> ${vault.name?html}</a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}"><b>Deposit:</b> ${deposit.name?html}</a></li>
        <li class="active">Retrieve data</li>
    </ol>
    
    <div class="row">
        <div class="col-md-12">
            <div class="panel panel-uoe-low">

                <div class="associated-image">
                    <figure class="uoe-panel-image uoe-panel-image"></figure>
                </div>
                
                <div class="panel-body">
                    <h2>
                        Retrieve Data
                    </h2>
                    
                    <br/>

                    <#if deposit.hasPersonalData == true>
                        <div class="alert alert-info" role="alert"><strong>Contains personal data!</strong><br/>Statement: ${deposit.getPersonalDataStatement()}</div>
                    </#if>

                    <form id="retrieve-deposit" class="form" role="form" action="" method="post">
                        
                        <div class="form-group">
                            <label>Are any of the intended recipients external to the University of Edinburgh?</label>
                            <span class="text-muted">
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                      title="'External' means they are neither a member of staff nor a student currently at the university. We must record this information to for compliance with funder requirements, with respect to how long a deposit is kept">
                                </span>
                            </span>
                            <br/>
                            <div class="radio-inline">
                                <label>
                                    <@spring.bind "retrieve.hasExternalRecipients" />
                                    <input type="radio" name="hasExternalRecipients" value="true">
                                    Yes
                                </label>
                            </div>
                            <div class="radio-inline">
                                <label>
                                    <@spring.bind "retrieve.hasExternalRecipients" />
                                    <input type="radio" name="hasExternalRecipients" value="false">
                                    No
                                </label>
                            </div>
                        </div>
                        
                        <div class="form-group hidden">
                            <label class="control-label">Retrieve Note:</label>
                            <span class="text-muted">
                                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                    title="Explain the reason why this Deposit is being retrieved.">
                                </span>
                            </span>
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
                            <div id="tree-error" class="alert alert-danger" style="display: none"></div>
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
                                    window.setTimeout(function(){  // Simulate a slow Ajax request
                                        if(node.children.length == 0) {
                                            $("#tree-error").html("DataVault could not access the location you specified. Please check that you have provided the correct hostname, port number and have copied the authentication key to the location. It can take several minutes before the connection between Datavault and the location is set, so please reload the page and try again. If the problem persists and you need assistance please contact the DataVault support team.");
                                            $("#tree-error").show();
                                        }
                                    }, 3000);
                                },
                                selectMode: 1,
                                activate: function(event, data) {
                                    var node = data.tree.getActiveNode();
                
                                    if (node) {
                                        $(".file-path").val(node.key);
                                    } else {
                                        $(".file-path").val("");
                                    }
                                },
                                init: function(event, data) {
                                    // The GET request always returns the top level "SFTP filesystem" node if there's a filestore defined, even when it is bogus.
                                    // If there's no filestore defined, it returns an empty result. Therefore, if there's 1 or fewer items in the tree, we know
                                    // that there's likely some problem with the filestore.
                                    if ( data.tree.count() <= 0 ) {
                                        $("#tree-error").html("No File Location available, please click on 'File Locations' in the top menu to add them.");
                                        $("#tree-error").show();
                                        $("#tree").hide();
                                    } else {
                                        $("#tree-error").hide();
                                        $("#tree").show();
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
            </div>
        </div>
    </div>
</div>

<script>
    $(document).ready(function () {

        $('button[type="submit"]').on("click", function() {
            $('#submitAction').val($(this).attr('value'));
        });

        $('#retrieve-deposit').validate({
            ignore: ".ignore",
            rules: {
                hasExternalRecipients: {
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
            },
            errorPlacement: function(error, element) 
            {
                if ( element.is(":radio") ) 
                {
                    error.appendTo( element.parents('.form-group') );
                }
                else 
                { // This is the default behavior 
                    error.insertAfter( element );
                }
            }
        });

        $('[data-toggle="tooltip"]').tooltip({
            'placement': 'top'
        });
    });
</script>

</@layout.vaultLayout>
