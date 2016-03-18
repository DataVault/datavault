<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>My Vaults</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}"><b>Vault:</b> ${vault.name?html}</a></li>
        <li class="active"><b>Deposit:</b> ${deposit.note?html}</li>
    </ol>

    <ol id="progtrckr" class="progtrckr" data-progtrckr-steps="0" style="display:none;">
    </ol>

    <div id="progress-transfer" style="display:none;">
        <span id="progress-copied">Placeholder</span>
        <div class="progress">
          <div id="progress" class="progress-bar progress-bar progress-bar-striped active" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%">
            <span id="progress-label" class="sr-only">0% Complete</span>
          </div>
        </div>
    </div>

    <ul class="nav nav-tabs">
        <li class="active"><a data-toggle="tab" href="#deposit">Deposit</a></li>
        <li><a data-toggle="tab" href="#contents">Contents <span class="badge">${manifest?size}</span></a></li>
        <li><a data-toggle="tab" href="#events">Events <span class="badge">${events?size}</span></a></li>
        <li><a data-toggle="tab" href="#restores">Restores <span class="badge">${restores?size}</span></a></li>
    </ul>

    <div id="deposit-tab-content" class="tab-content">

        <div class="tab-pane active" id="deposit">
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                    <tr class="tr">
                        <th>Note</th>
                        <th>Status</th>
                        <th>Size</th>
                        <th>Timestamp</th>
                    </tr>
                    <thead>
                    <tbody>
                    <tr class="tr">
                        <td>${deposit.note?html}</td>
                        <td>${deposit.status}</td>
                        <td>${deposit.getSizeStr()}</td>
                        <td>${deposit.creationTime?datetime}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="tab-pane" id="contents">
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                    <tr class="tr">
                        <th>File</th>
                        <th>Type</th>
                        <th>Checksum</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list manifest as filefixity>
                        <tr class="tr">
                            <td>${filefixity.file?html}</td>
                            <td>${filefixity.fileType?html}</td>
                            <td style="font-family:monospace;">${filefixity.fixity}&nbsp<span class="label label-primary">${filefixity.algorithm}</span></td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="tab-pane" id="events">
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                    <tr class="tr">
                        <th>User</th>
                        <th>Event</th>
                        <th>Message</th>
                        <th>Timestamp</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list events as event>
                        <tr class="tr">
                            <td>${event.userID?html}</td>
                            <td>${event.eventClass?html}</td>
                            <td>${event.message?html}</td>
                            <td>${event.timestamp?datetime}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="tab-pane" id="restores">
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                    <tr class="tr">
                        <th>Restore note</th>
                        <th>Status</th>
                        <th>Restore path</th>
                        <th>Timestamp</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list restores as restore>
                        <tr class="tr">
                            <td>${restore.note?html}</td>
                            <td>${restore.status?html}</td>
                            <td>${restore.restorePath?html}</td>
                            <td>${restore.timestamp?datetime}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <#if deposit.status.name() == "COMPLETE">
        <a id="restorebtn" class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}/restore">
            <span class="glyphicon glyphicon-open" aria-hidden="true"></span> Restore data
        </a>
    </#if>

</div>

<script>

    var updateInterval = 500;

    function displayJob(job) {

        $('#progtrckr').empty()
        $('#progtrckr').attr("data-progtrckr-steps", job.states.length)
        
        var trackerHTML = ""
        for (var i = 0; i < job.states.length; i++) {
            var todoClass = "progtrckr-todo"
            if (i <= job.state) {
                todoClass = "progtrckr-done"
            }
            trackerHTML += "<li class=\"" + todoClass + "\">" + job.states[i] + "</li>"
        }
        $('#progtrckr')[0].innerHTML = trackerHTML

        if (job.progressMax == 0 || job.progress == job.progressMax) {
            $('#progress-transfer').hide();
        } else {
            $('#progress-transfer').show();
            
            var percentComplete = 0
            if (job.progressMax > 0 && job.progress > 0) {
                percentComplete = (job.progress / job.progressMax) * 100
            }
            
            $('#progress').css('width', percentComplete + '%').attr('aria-valuenow', percentComplete);
            $('#progress-label').text(percentComplete + '% Complete')
            $('#progress-copied').text(job.progressMessage)
        }
    }

    function updateProgress(jobs) {
        
        for (var i = 0; i < jobs.length; i++) {
            
            job = jobs[i];
            
            <!-- only consider the most recent job -->
            if (i < jobs.length - 1) {
                continue;
            }

            if (job.states.length == 0) {
                <!-- a pending job -->
                updateInterval = 500;
                $('#progtrckr').hide()
                
            } else if (job.state != job.states.length - 1) {
                <!-- an active job -->
                updateInterval = 500;
                $('#progtrckr').show()
                displayJob(job)
                
            } else {
                <!-- a complete job -->
                updateInterval = 5000;
                if ($('#progtrckr').is(":visible")) {
                    displayJob(job)
                    $("#progtrckr").fadeOut(1000, function() {
                        // Animation complete
                        location.reload(true);
                    });
                }
            }
        }
    }

    function load() {
        setTimeout(function () {
            $.ajax({
                url: "${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}/jobs",
                type: "GET",
                dataType: 'json',  
                success: function (result) {
                    updateProgress(result)

                    if (result.status != 'COMPLETE') {
                        load()
                    }
                }
            });
        }, updateInterval);
    }
    load();
</script>

</@layout.vaultLayout>
