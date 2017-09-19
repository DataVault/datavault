<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />

<style>
    /* Uploader: Drag & Drop */
    .flow-error {display:none; font-size:14px; font-style:italic;}
    .flow-drop {padding:30px; font-size:13px; text-align:center; color:#666; font-weight:bold;background-color:#eee; border:2px dashed #aaa; border-radius:10px; z-index:9999;}
    .flow-dragover {padding:30px; color:#555; background-color:#ddd; border:1px solid #999;}

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
        
        <label class="control-label">Choose files to deposit:</label>
        <span class="text-muted"><span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" title="Select files or directories to add to the Vault."></span></span>

        <div class="form-group">

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

            <div id="uploadMaxSizeAlert" class="alert alert-warning" role="alert" style="display:none;">
              <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
              Files larger than 5GB cannot be uploaded directly from your computer.
            </div>

            <div class="flow-drop" ondragenter="jQuery(this).addClass('flow-dragover');" ondragend="jQuery(this).removeClass('flow-dragover');" ondrop="jQuery(this).removeClass('flow-dragover');">

                <div class="btn-toolbar">
                    <button type="button" class="btn btn-default" href="#" data-toggle="modal" data-target="#add-from-storage"><i class="fa fa-hdd-o" aria-hidden="true"></i> Data Storage</button>
                    <button type="button" class="btn btn-default flow-browse" data-toggle="tooltip" title="Maximum file size: 5GB"><i class="fa fa-laptop" aria-hidden="true"></i> My Computer</button>
                </div>

              <div class="progress" style="display:none; margin-top:15px;">
                <div id="upload-progress" class="progress-bar progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%">
                  <span id="progress-label" class="sr-only">0% Complete</span>
                </div>
              </div>

              <div id="upload-tree" class="fancytree tree-box text-left" style="display:none; margin-top:15px; max-height:25vh; overflow-y:scroll;"></div>
            </div>
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
                }
            });

            $("#upload-tree").fancytree({
                source: []
            });
        </script>

        <div class="btn-toolbar">
            <button id="deposit-submit-btn" type="submit" value="submit" class="btn btn-primary"><i class="fa fa-download fa-rotate-180" aria-hidden="true"></i> Deposit data</button>
            <button type="submit" value="cancel" class="btn btn-danger cancel">Cancel</button>
        </div>

        <input type="hidden" id="submitAction" name="action" value="submit"/>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

    </form>
</div>

<script>
    $(document).ready(function () {

        var updateProgress = function(percentComplete) {
          $('#upload-progress').css('width', percentComplete + '%').attr('aria-valuenow', percentComplete);
          $('#progress-label').text(percentComplete + '% Complete');
        }
       
        $('#add-from-storage-btn').on("click", function() {
            
            // Add the path to the hidden control
            var node = $("#tree").fancytree("getActiveNode");
            $('.file-path')
              .append($('<option>', { value : node.key })
              .text(node.key)
              .prop('selected', true));
            
            // Add the file to the list
            $("#upload-tree").show();
            
            var storageNode = $("#upload-tree").fancytree("getNodeByKey", "storage");
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
            
            var childNode = storageNode.addChildren({
              key: node.key,
              title: node.title,
              tooltip: "Path: " + node.key,
              folder: node.folder
            });

            $('#add-from-storage').modal('hide');
        });
  
        $('button[type="submit"]').on("click", function() {
            $('#submitAction').val($(this).attr('value'));
        });

        $('#create-deposit').validate({
            ignore: ".ignore",
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
            },
            submitHandler: function (form) {
                $('button[type="submit"]').prop('disabled', true);
                form.submit();
            }
        });

        $('[data-toggle="tooltip"]').tooltip({
            'placement': 'top'
        });

    var r = new Flow({
        target:'${springMacroRequestContext.getContextPath()}/fileupload',
        query:{fileUploadHandle:$('.file-upload-handle').val()},
        headers:{'${_csrf.headerName}': '${_csrf.token}'},
        chunkSize:10*1024*1024,
        testChunks: false,
        maxChunkRetries:1
    });
    
    // Keep track of uploaded directory names (top level only)
    var uploadDirs = {};

    r.assignDrop($('.flow-drop')[0]);
    r.assignBrowse($('.flow-browse')[0]);

    // Handle file add event
    r.on('fileAdded', function(file){
      
      // Prevent browser upload of large files (5GB)
      if (file.size > (5 * 1024 * 1024 * 1024)) {
        $('#uploadMaxSizeAlert').show();
        return false;
      }

      // Show progress bar
      $('.progress').show();

      // Prevent completion of deposit
      $('#deposit-submit-btn').prop('disabled', true);

      // Add the file to the list
      $("#upload-tree").show();
     
      var uploadsNode = $("#upload-tree").fancytree("getNodeByKey", "uploads");
      if (!uploadsNode) {
        var rootNode = $("#upload-tree").fancytree("getRootNode");
        uploadsNode = rootNode.addChildren({
            key: "uploads",
            title: "My Computer",
            tooltip: "Browser uploads.",
            folder: true,
            expanded: true
        });
      }
      
      if (file.relativePath == file.name) {
        var childNode = uploadsNode.addChildren({
          key: file.name,
          title: file.name,
          tooltip: file.name,
          folder: false
        });
      } else {
        var dirName = file.relativePath.split('/')[0];
        if (!(dirName in uploadDirs)) {
          uploadDirs[dirName] = true;
          var childNode = uploadsNode.addChildren({
            key: dirName,
            title: dirName,
            tooltip: dirName,
            folder: true
          });
        }
      }

      // extraClasses: 'progress-item'

      var $self = $('.flow-file-'+file.uniqueIdentifier);
      $self.find('.flow-file-name').text(file.name);
      $self.find('.flow-file-size').text(readablizeBytes(file.size));
      $self.find('.flow-file-download').attr('href', '/download/' + file.uniqueIdentifier).hide();
      $self.find('.flow-file-pause').on('click', function () {
        file.pause();
        $self.find('.flow-file-pause').hide();
        $self.find('.flow-file-resume').show();
      });
      $self.find('.flow-file-resume').on('click', function () {
        file.resume();
        $self.find('.flow-file-pause').show();
        $self.find('.flow-file-resume').hide();
      });
      $self.find('.flow-file-cancel').on('click', function () {
        file.cancel();
        $self.remove();
      });
    });
    r.on('filesSubmitted', function(file) {
      r.upload();
    });
    r.on('fileSuccess', function(file,message){
      // Reflect that the file upload has completed
      /*
      $("#upload-tree").fancytree("getTree").getNodeByKey(file.relativePath).extraClasses = '';
      $("#upload-tree").fancytree("getTree").getNodeByKey(file.relativePath).renderTitle();
      */
    });
    r.on('fileError', function(file, message){
      // Reflect that the file upload has resulted in error
      $('.flow-file-'+file.uniqueIdentifier+' .flow-file-progress').html('(file could not be uploaded: '+message+')');
    });
    r.on('fileProgress', function(file){
      if (r.progress() == 1.0) {
        
        // Hide progress bar
        $('.progress').hide();
        
        // Allow completion of deposit
        $('#deposit-submit-btn').prop('disabled', false);

      } else {
        updateProgress(Math.floor(r.progress()*100));
      }
    });
    r.on('catchAll', function() {
      // console.log.apply(console, arguments);
    });
    window.r = {
      pause: function () {
        r.pause();
      },
      cancel: function() {
        r.cancel();
        $('.flow-file').remove();
      },
      upload: function() {
        $('.flow-file-pause').show();
        $('.flow-file-resume').hide();
        r.resume();
      },
      flow: r
    };

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