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

    <div id = "job-error" class="alert alert-danger" role="alert" style="display:none;">
        <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
        <span class="sr-only">Error</span>
        <span id="error-label">Error details</span>
    </div>

    <ul class="nav nav-tabs">
        <li class="active"><a data-toggle="tab" href="#deposit">Deposit</a></li>
        <li><a data-toggle="tab" href="#contents">Contents <span class="badge">${manifest?size}</span></a></li>
        <li><a data-toggle="tab" href="#events">Events <span class="badge">${events?size}</span></a></li>
        <li><a data-toggle="tab" href="#retrieves">Retrieves <span class="badge">${retrieves?size}</span></a></li>
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
                        <td>
                            <div id="deposit-status">
                                <#if deposit.status.name() == "COMPLETE">
                                    <div class="text-success">
                                        <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>
                                        Complete
                                    </div>

                                <#elseif deposit.status.name() == "FAILED">
                                    <div class="text-danger">
                                        <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
                                        Failed
                                    </div>

                                <#elseif deposit.status.name() == "NOT_STARTED">
                                    Not started

                                <#elseif deposit.status.name() == "IN_PROGRESS">
                                    <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span>
                                    In progress

                                <#else>
                                    ${deposit.status}
                                </#if>
                            </div>
                        </td>
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
                            <td>${event.eventClass?replace('org.datavaultplatform.common.event.', '')?html}</td>
                            <td>${event.message?html}</td>
                            <td>${event.timestamp?datetime}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="tab-pane" id="retrieves">
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                    <tr class="tr">
                        <th>Retrieve note</th>
                        <th>Status</th>
                        <th>Retrieve path</th>
                        <th>Timestamp</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#list retrieves as retrieve>
                        <tr class="tr">
                            <td>${retrieve.note?html}</td>
                            <td>${retrieve.status?html}</td>
                            <td>${retrieve.retrievePath?html}</td>
                            <td>${retrieve.timestamp?datetime}</td>
                        </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <#if deposit.status.name() == "COMPLETE">
        <a id="retrievebtn" class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}/retrieve">
            <span class="glyphicon glyphicon-open" aria-hidden="true"></span> Retrieve data
        </a>
    </#if>

</div>

<script>
    
    var depositStatus = "${deposit.status}";
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
            
            } else if (job.error == true) {
                <!-- a job error -->
                updateInterval = 5000;
                if ($('#progtrckr').is(":visible")) {
                    displayJob(job)
                    $("#progtrckr").fadeOut(1000, function() {
                        // Animation complete
                        location.reload(true)
                    });
                } else if (depositStatus != "COMPLETE" && depositStatus != "FAILED") {
                    // Refresh the deposit
                    location.reload(true)
                }
                $('#error-label').text(job.errorMessage)
                $('#job-error').show()
                
            } else if (job.state != job.states.length - 1) {
                <!-- an active job -->
                updateInterval = 500;
                $('#progtrckr').show()
                displayJob(job)
                
                if (depositStatus == "NOT_STARTED") {
                    $("#deposit-status").html("<span class=\"glyphicon glyphicon-refresh glyphicon-refresh-animate\"></span>&nbspIn progress")
                }
                
            } else {
                <!-- a complete job -->
                updateInterval = 5000;
                if ($('#progtrckr').is(":visible")) {
                    displayJob(job)
                    $("#progtrckr").fadeOut(1000, function() {
                        // Animation complete
                        location.reload(true)
                    });
                } else if (depositStatus != "COMPLETE") {
                    // Refresh the deposit
                    location.reload(true)
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

<style>
    .glyphicon-refresh-animate {
        -animation: spin 2.8s infinite linear;
        -webkit-animation: spinWebkit 2.8s infinite linear;
        -moz-animation: spinMoz 2.8s infinite linear;
    }

    @-webkit-keyframes spinWebkit {
        from { -webkit-transform: rotate(0deg);}
        to { -webkit-transform: rotate(360deg);}
    }
    @keyframes spinMoz {
        from { transform: scale(1) rotate(0deg);}
        to { transform: scale(1) rotate(360deg);}
    }
    @keyframes spin {
        from { transform: scale(1) rotate(0deg);}
        to { transform: scale(1) rotate(360deg);}
    }
</style>

</@layout.vaultLayout>
