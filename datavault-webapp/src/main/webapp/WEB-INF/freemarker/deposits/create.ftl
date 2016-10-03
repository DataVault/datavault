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
        
<style>
    /* Uploader: Drag & Drop */
    .flow-error {display:none; font-size:14px; font-style:italic;}
    .flow-drop {padding:30px; font-size:13px; text-align:center; color:#666; font-weight:bold;background-color:#eee; border:2px dashed #aaa; border-radius:10px; z-index:9999;}
    .flow-dragover {padding:30px; color:#555; background-color:#ddd; border:1px solid #999;}

    /* Uploader: Progress bar */
    .flow-progress {margin:30px 0 30px 0; width:100%; display:none;}
    .progress-container {height:7px; background:#9CBD94; position:relative; }
    .progress-bar {position:absolute; top:0; left:0; bottom:0; background:#45913A; width:0;}
    .progress-text {font-size:11px; line-height:9px; padding-left:10px;}
    .progress-pause {padding:0 0 0 7px;}
    .progress-resume-link {display:none;}
    .is-paused .progress-resume-link {display:inline;}
    .is-paused .progress-pause-link {display:none;}
    .is-complete .progress-pause {display:none;}

    /* Uploader: List of items being uploaded */
    .flow-list {overflow:auto; margin-right:-20px; display:none;}
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
</style>

        <div class="form-group">
            <label class="control-label">File upload:</label>
            <span class="text-muted"><span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip" title="Select a single file, or a directory to add to the Vault."></span></span>

            <div class="flow-error">
              Your browser, unfortunately, is not supported by Flow.js. The library requires support for <a href="http://www.w3.org/TR/FileAPI/">the HTML5 File API</a> along with <a href="http://www.w3.org/TR/FileAPI/#normalization-of-params">file slicing</a>.
            </div>

            <div class="flow-drop" ondragenter="jQuery(this).addClass('flow-dragover');" ondragend="jQuery(this).removeClass('flow-dragover');" ondrop="jQuery(this).removeClass('flow-dragover');">
              Drag and drop to upload files or <a class="flow-browse"><u>browse</u></a>
            </div>

            <div class="flow-progress">
              <table>
                <tr>
                  <td width="100%"><div class="progress-container"><div class="progress-bar"></div></div></td>
                  <td class="progress-text" nowrap="nowrap"></td>
                </tr>
              </table>
            </div>

            <ul class="flow-list"></ul>
        </div>

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

    var r = new Flow({
        target:'${springMacroRequestContext.getContextPath()}/fileupload',
        query:{upload_token:'my_token'},
        headers:{'${_csrf.headerName}': '${_csrf.token}'},
        chunkSize:10*1024*1024,
        testChunks: false
    });
    
    r.assignDrop($('.flow-drop')[0]);
    r.assignBrowse($('.flow-browse')[0]);

    // Handle file add event
    r.on('fileAdded', function(file){
      // Show progress bar
      $('.flow-progress, .flow-list').show();
      // Add the file to the list
      $('.flow-list').append(
        '<li class="flow-file flow-file-'+file.uniqueIdentifier+'">' +
        'Uploading <span class="flow-file-name"></span> ' +
        '<span class="flow-file-size"></span> ' +
        '<span class="flow-file-progress"></span> '
      );
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
    r.on('complete', function(){
      // Hide pause/resume when the upload has completed
      $('.flow-progress .progress-resume-link, .flow-progress .progress-pause-link').hide();
    });
    r.on('fileSuccess', function(file,message){
      var $self = $('.flow-file-'+file.uniqueIdentifier);
      // Reflect that the file upload has completed
      $self.find('.flow-file-progress').text('(completed)');
      $self.find('.flow-file-pause, .flow-file-resume').remove();
      $self.find('.flow-file-download').attr('href', '/download/' + file.uniqueIdentifier).show();
    });
    r.on('fileError', function(file, message){
      // Reflect that the file upload has resulted in error
      $('.flow-file-'+file.uniqueIdentifier+' .flow-file-progress').html('(file could not be uploaded: '+message+')');
    });
    r.on('fileProgress', function(file){
      // Handle progress for both the file and the overall upload
      $('.flow-file-'+file.uniqueIdentifier+' .flow-file-progress')
        .html(Math.floor(file.progress()*100) + '% '
          + readablizeBytes(file.averageSpeed) + '/s '
          + secondsToStr(file.timeRemaining()) + ' remaining') ;
      $('.progress-bar').css({width:Math.floor(r.progress()*100) + '%'});
    });
    r.on('uploadStart', function(){
      // Show pause, hide resume
      $('.flow-progress .progress-resume-link').hide();
      $('.flow-progress .progress-pause-link').show();
    });
    r.on('catchAll', function() {
      console.log.apply(console, arguments);
    });
    window.r = {
      pause: function () {
        r.pause();
        // Show resume, hide pause
        $('.flow-file-resume').show();
        $('.flow-file-pause').hide();
        $('.flow-progress .progress-resume-link').show();
        $('.flow-progress .progress-pause-link').hide();
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