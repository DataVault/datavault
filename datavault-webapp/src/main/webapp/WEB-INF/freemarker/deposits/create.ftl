<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />

<style>
    /* Uploader: border */
    .flow-drop {padding:30px; font-size:13px; text-align:center; color:#666; font-weight:bold;background-color:#eee; border:2px dashed #aaa; border-radius:10px; z-index:9999;}

    /* Uploader: List of items being uploaded */
    .uploader-item {width:148px; height:90px; background-color:#666; position:relative; border:2px solid black; float:left; margin:0 6px 6px 0;}
    .uploader-item-thumbnail {width:100%; height:100%; position:absolute; top:0; left:0;}
    .uploader-item img.uploader-item-thumbnail {opacity:0;}
    .uploader-item-creating-thumbnail {padding:0 5px; font-size:9px; color:white;}
    .uploader-item-title {position:absolute; font-size:9px; line-height:11px; padding:3px 50px 3px 5px; bottom:0; left:0; right:0; color:white; background-color:rgba(0,0,0,0.6); min-height:27px;}
    .uploader-item-status {position:absolute; bottom:3px; right:3px;}

    /* Uploader: Hover & Active status */
    .uploader-item:hover, .is-active .uploader-item {border-color:#4a873c; cursor:pointer; }
    .uploader-item:hover .uploader-item-title, .is-active .uploader-item .uploader-item-title {background-color:rgba(74,135,60,0.8);}

    /* Uploader: Error status */
    .is-error .uploader-item:hover, .is-active.is-error .uploader-item {border-color:#900;}
    .is-error .uploader-item:hover .uploader-item-title, .is-active.is-error .uploader-item .uploader-item-title {background-color:rgba(153,0,0,0.6);}
    .is-error .uploader-item-creating-thumbnail {display:none;}

    /* In progress item */
    .progress-item {opacity:0.5;}
</style>

<div class="modal fade" id="add-from-storage" tabindex="-1" role="dialog" aria-labelledby="addFromStorage" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true"><i class="fa fa-times" aria-hidden="true"></i></button>
                <h4 class="modal-title" id="addFromStorage">Add files from storage</h4>
            </div>
            <div class="modal-body">
                <form id="add-from-storage-form">
                    <div class="form-group">
                        <label class="control-label">Select file or folder:</label>
                        <div id="tree-error" class="alert alert-danger" style="display:none"></div>
                        <div id="tree" class="fancytree tree-box" style="max-height:50vh; overflow-y:scroll;"></div>
                        <label id="deposit-size" class="text-muted small">No files selected</label>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                <button type="button" id="add-from-storage-btn" form="add-from-storage" class="btn btn-primary btn-ok">Add</button>
            </div>
        </div>
    </div>
</div>

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>Home</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/"><b>Vault:</b> ${vault.name?html}</a></li>
        <li class="active">Create new deposit</li>
    </ol>
    
    <div class="panel panel-uoe-low">
        <div class="associated-image">
            <figure class="uoe-panel-image uoe-panel-image"></figure>
        </div>

        <div class="panel-body">

            <h2>Create a new Deposit</h2>
            <div class="alert alert-info" role="alert">
                <p>
                    You must set up the folder from which you want to deposit your files (using the <a href="${springMacroRequestContext.getContextPath()}/filestores">File Locations page</a>) BEFORE you can set the deposit name and description.
                </p>
            </div>

            <form id="create-deposit" class="form" role="form" action="" method="post">
                <div class="row">
                    <div class="col-sm-10">
    
                        <div class="form-group">
                            <label class="control-label">Deposit Name</label>
                            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                title="The deposit name should  help reviewers understand whether this subset of the data needs to be kept longer than other parts of the vault.&nbsp;Maximum 400 characters.">
                            </span>
                            <@spring.bind "deposit.name" />
                            <input type="text" class="form-control" name="${spring.status.expression}" value='${spring.status.value!""}'/>
                        </div>
                        
                        <div class="form-group">
                            <label class="control-label">Deposit Description</label>
                            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                                title="This description should contain information to assist the vault Owner and any other colleagues who will be part of the review process when the vault retention period expires, in deciding whether the data should be retained or deleted. Maximum 6,000 characters.">
                            </span>
                            <@spring.bind "deposit.description" />
                            <input type="text" class="form-control" name="${spring.status.expression}" value='${spring.status.value!""}'/>
                        </div>
                    </div>
                </div>
                
                <label>
                    <strong>Select the files to include in this deposit</strong>
                </label>
                <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                    title="If you don't see the filestore you wish to use here, please click on 'File Locations' to add it.">
                </span>
                <br/>
                <small>Click on the button marked 'Data Storage (SFTP)' to: connect to the location(s) you specified on the File Locations page, and browse to the files you want to upload.</small>
                <br/><br/>
                
                <@spring.bind "deposit.fileUploadHandle" />
                <input type="hidden"
                   class="form-control file-upload-handle"
                   name="${spring.status.expression}"
                   value="${spring.status.value!""}"/>
                   
                <@spring.bind "deposit.depositPaths" />
                <select
                       multiple="true"
                       class="file-path"
                       style="display:none;"
                       name="${spring.status.expression}"
                       value="${spring.status.value!""}">
                </select>
        
                <div class="flow-drop">
    
                    <div class="btn-toolbar">
                        <button type="button" class="btn btn-default" href="#" data-toggle="modal" data-target="#add-from-storage">
                            <i class="fa fa-hdd-o" aria-hidden="true"></i> Data Storage (SFTP)
                        </button>
                    </div>
    
                    <div class="progress" style="display:none; margin-top:15px;">
                        <div id="upload-progress" class="progress-bar progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%">
                            <span id="progress-label" class="sr-only">0% Complete</span>
                        </div>
                    </div>
    
                    <div id="upload-tree" class="fancytree tree-box text-left" style="display:none; margin-top:15px; max-height:25vh; overflow-y:scroll;"></div>
                </div>
                
                <script>
                    var filesizeSeq = 0;
                    
                    // Create the tree inside the "tree" element.
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
                                data: {
                                    mode: "children",
                                    parent: node.key
                                },
                                cache: false
                            };
                        },
                        loadChildren: function(event, data) {
                            var node = data.node;
                            if(node != null && !node.key.includes('/') && node.children.length == 0) {
                                $("#tree-error").html("DataVault could not access the location you specified. Please check that you have provided the correct hostname, port number and have copied the authentication key to the location. It can take several minutes before the connection between Datavault and the location is set, so please reload the page and try again. If the problem persists and you need assistance please contact the DataVault support team.");
                                $("#tree-error").show();
                            }else{
                                $("#tree-error").hide();
                            }
                        },
                        selectMode: 1,
                        activate: function(event, data) {
                            var node = data.tree.getActiveNode();
                            
                            filesizeSeq = filesizeSeq + 1;
                            var currentSeq = filesizeSeq;
                            
                            if (node) {
                                $("#deposit-size").text("Deposit size: calculating ...");
                                
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
                                $("#deposit-size").text("No files selected");
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
        
                    $("#upload-tree").fancytree({
                        source: []
                    });
                </script>
                    
                <div class="form-group">
                    <label for="has-personal-data" class="control-label">
                        Does this deposit contain personally identifying information?
                    </label>
                    <small>
                        (More about personal data
                        [<a href="https://www.ed.ac.uk/data-protection/data-protection-guidance/definitions/definitions-personal-data">
                            https://www.ed.ac.uk/data-protection/data-protection-guidance/definitions/definitions-personal-data
                        </a>])
                    </small>
                    <br/>
                    <div class="radio-has-personal-data radio-inline">
                        <@spring.bind "deposit.hasPersonalData" />
                        <input type="radio" name="${spring.status.expression}" value="yes"> Yes
                    </div>
                    <div class="radio-has-personal-data radio-inline">
                        <@spring.bind "deposit.hasPersonalData" />
                        <input type="radio" name="${spring.status.expression}" value="No"> No
                    </div>
                </div>
                
                <div id="content-has-personal-data" class="hidden">
                    <div class="alert alert-info" role="alert">
                        <p>
                        PERSONAL DATA STATEMENT: Please describe the nature of the personally identifying information and 
                        what steps you have taken to ensure compliance with data protection legislation . 
                        Good practice means anonymising your data, if you can do so without losing its usefulness. 
                        If on the other hand you need to retain some kind of subject identifier, the data should be pseudonymised, 
                        if it is practical. Whereas, if you unavoidably need to keep the identifying information of your subjects, 
                        this personal data may be deposited in the DataVault because it is encrypted. 
                        You must be able to provide justification for retaining the personal data.
                        </p>
                    </div>
                    
                    <div class="form-group">
                        <label for="personal-data-statement">Personal Data Statement</label>
                        <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" 
                            title="Maximum 6,000 characters."></span>
                        <@spring.bind "deposit.personalDataStatement" />
                        <textarea class="form-control" id="${spring.status.expression}" name="${spring.status.expression}" rows="3"></textarea>
                    </div>
                
                </div>
                
                <div class="alert alert-info" role="alert">
                    <p>Your deposit will be confirmed by email. Do not edit any files that have been included until the deposit is complete.</p>
                </div> 
                
                <div class="btn-toolbar pull-right">
                    <a class="btn btn-lg btn-link" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}">Cancel</a>
                    <button type="submit" value="submit" class="btn btn-lg btn-primary">
                        <i class="fa fa-download fa-rotate-180" aria-hidden="true"></i>
                        Deposit data
                    </button>
                </div>
        
                <input type="hidden" id="submitAction" name="action" value="submit"/>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        
            </form>
        </div>
    </div>
</div>

<script>
    $(function () {

        $('[data-toggle="popover"]').popover();
        
        var updateProgress = function(percentComplete) {
          $('#upload-progress').css('width', percentComplete + '%').attr('aria-valuenow', percentComplete);
          $('#progress-label').text(percentComplete + '% Complete');
        }
        
        $("input[name='hasPersonalData']").change( function() {
            if ($(this).val() === 'yes'){
                $('#content-has-personal-data').removeClass('hidden');
                $('#personalDataStatement').removeClass('ignore');
            }else{
                $('#content-has-personal-data').addClass('hidden');
                $('#personalDataStatement').addClass('ignore');
            }
        });
        
        $('#add-from-storage-btn').on("click", function() {
            var node = $("#tree").fancytree("getActiveNode");

            var storageNode = $("#upload-tree").fancytree("getNodeByKey", "storage");

            var paths = [];
            if (storageNode) {
                var pathNodes = storageNode.getChildren();

                var isFull = false;

                if (pathNodes != null) {
                    for (var i = 0; i < pathNodes.length; i++) {
                        var checkedNode = pathNodes[i];
                        paths.push(checkedNode.getKeyPath().replace("/storage/", "/"));
                    }
                }
            }

            paths.push(node.key);

            $.ajax({
                url: "${springMacroRequestContext.getContextPath()}/checkdepositsize",
                type: "GET",
                data: {filepath: paths},
                dataType: 'json',
                success: function (result) {
                    if (result.success == "false") {
                        isFull = true
                    }

                    if(!isFull) {
                        // Add the path to the hidden control
                        $('.file-path')
                            .append($('<option>', { value : node.key })
                                .text(node.key)
                                .prop('selected', true));

                        //Show tree
                        $("#upload-tree").show();

                        if (!storageNode) {
                            var rootNode = $("#upload-tree").fancytree("getRootNode");
                            storageNode = rootNode.addChildren({
                                key: "storage",
                                title: "Data Storage",
                                tooltip: "Data Storage",
                                folder: true,
                                expanded: true
                            });
                        }

                        // Add the path to the hidden control
                        storageNode.addChildren({
                            key: node.key,
                            title: node.title,
                            tooltip: "Path: " + node.key,
                            folder: node.folder
                        });
                    }
                    else{
                        alert("A deposit is limited to " + result.max + "!")
                    }
                }
            })
            .fail(function( msg ) {
                alert("Couldn't calculate size of the deposit!\nMake sure the path to the file location is correct and try again.");
            })
            .done(function( msg ) {
                $('#add-from-storage').modal('hide');
            });
        });
  
        $('button[type="submit"]').on("click", function() {
            $('#submitAction').val($(this).attr('value'));
        });

        $('#create-deposit').validate({
            ignore: ".ignore",
            rules: {
                'name': {
                    required: true
                },
                'depositPaths': {
                    required: true
                },
                'hasPersonalData': {
                    required: true
                },
                'personalDataStatement': {
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

    function readablizeBytes(bytes) {
        var s = ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB'];
        var e = Math.floor(Math.log(bytes) / Math.log(1024));
        return (bytes / Math.pow(1024, e)).toFixed(2) + " " + s[e];
    }
    function secondsToStr (temp) {
        function numberEnding (number) {
            return (number > 1) ? 's' : '';
        }
        var years = Math.floor(temp / 31536000);
        if (years) {
            return years + ' year' + numberEnding(years);
        }
        var days = Math.floor((temp %= 31536000) / 86400);
        if (days) {
            return days + ' day' + numberEnding(days);
        }
        var hours = Math.floor((temp %= 86400) / 3600);
        if (hours) {
            return hours + ' hour' + numberEnding(hours);
        }
        var minutes = Math.floor((temp %= 3600) / 60);
        if (minutes) {
            return minutes + ' minute' + numberEnding(minutes);
        }
        var seconds = temp % 60;
        return seconds + ' second' + numberEnding(seconds);
    }

</script>

</@layout.vaultLayout>
