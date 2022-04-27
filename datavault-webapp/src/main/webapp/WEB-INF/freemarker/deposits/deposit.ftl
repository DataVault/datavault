<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<#assign sec=JspTaglibs["http://www.springframework.org/security/tags"] />
<@layout.vaultLayout>
<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>Home</b></a></li>
        <li><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/"><b>Vault:</b> ${vault.name?html}</a></li>
        <li class="active"><b>Deposit:</b> ${deposit.name?html}</li>
    </ol>

    <div class="panel panel-uoe-low">
        <div class="associated-image">
            <figure class="uoe-panel-image uoe-panel-image"></figure>
        </div>

        <div class="panel-body">

            <h2>View the Deposit: <small>${deposit.name?html}</small></h2>
            <br/>

            <ol id="progtrckr" class="progtrckr" data-progtrckr-steps="0" style="display:none;">
            </ol>

            <div id="progress-transfer" style="display:none;">
                <span id="progress-copied">Placeholder</span>
                <div class="progress">
                  <div id="progress" class="progress-bar progress-bar-striped active" 
                  role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" 
                  aria-valuetext="0% Complete">
                    <span id="progress-label" class="sr-only">0% Complete</span>
                  </div>
                </div>
            </div>
    
            <div id = "job-error" class="alert alert-danger" role="alert" style="display:none;">
                <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
                <span class="sr-only">Error</span>
                <span id="error-label">Error details</span>
            </div>

            <div id="job-info" class="alert alert-info" role="alert" style="display:none;">
                <button type="button" class="close" data-dismiss="alert">&times;</button>
                <span id="job-info-msq"></span>
            </div>

            <#assign canRetrieveDataExpression = "hasPermission('${vault.ID}', 'vault', 'VIEW_DEPOSITS_AND_RETRIEVES') or hasPermission('${vault.groupID}', 'GROUP', 'CAN_RETRIEVE_DATA')">
            <@sec.authorize access=canRetrieveDataExpression>
            <#if deposit.status.name() == "COMPLETE">
                <a id="retrievebtn" class="btn btn-primary pull-right" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}/retrieve" disabled='disabled'>
                    <i class="fa fa-upload fa-rotate-180" aria-hidden="true"></i> Retrieve data
                </a>
            </#if>
            </@sec.authorize>
            
            <table class="table table-sm">
                <tbody>
                    <tr>
                        <th scope="col">Deposit Name</th>
                        <td>${deposit.name?html}</td>
                    </tr>
                    <tr>
                        <th scope="col">Deposited By</th>
                        <td>${deposit.getUserID()}</td>
                    </tr>
                    <tr>
                        <th scope="col">Deposit Size</th>
                        <td>
                            <#if deposit.status.name() != "NOT_STARTED">
                            ${deposit.getSizeStr()}
                            </#if>
                        </td>
                    </tr>
                    <tr>
                        <th scope="col">Description</th>
                        <td>${deposit.getDescription()}</td>
                    </tr>
                    <tr>
                        <th scope="col">Time and Date of Deposit</th>
                        <td>${deposit.creationTime?datetime}</td>
                    </tr>
                    <tr>
                    <th scope="col">Contains Personal Data</th>
                        <td>
                        <#if deposit.hasPersonalData>Yes<#else>No</#if>
                        </td>
                    </tr>
                    <tr>
                        <th scope="col">Personal Data Statement</th>
                        <td>${deposit.personalDataStatement}</td>
                    </tr>
                    <tr>
                        <th scope="col">Status</th>
                        <td>
                            <div id="deposit-status" class="job-status">
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
                                Queued
                                <#elseif deposit.status.name() == "IN_PROGRESS">
                                <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span>
                                In progress
                                <#else>
                                ${deposit.status}
                                </#if>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
            
            <ul class="nav nav-tabs">
                <li class="active"><a data-toggle="tab" href="#events">Events <span class="badge">${events?size}</span></a></li>
                <li><a data-toggle="tab" href="#retrievals">Retrievals <span class="badge">${retrieves?size}</span></a></li>
            </ul>
            
            <div id="deposit-tab-content" class="tab-content">
    
                <div class="tab-pane active" id="events">
                    <div class="scrollable">
                        <div class="table-responsive">
                            <table class="table table-bordered">
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
                                        <td>${event.userID!""}</td>
                                        <td>${event.eventClass?replace('org.datavaultplatform.common.event.', '')?html}</td>
                                        <td>${event.message?html}</td>
                                        <td>${event.timestamp?datetime}</td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
    
                <div class="tab-pane" id="retrievals">
                    <div class="scrollable">
                        <div class="table-responsive">
                            <table class="table table-bordered">
                                <thead>
                                <tr class="tr">
                                    <th>Retrieved By</th>
                                    <th>For external user?</th>
                                    <th>Date and Time</th>
                                    <th>Status</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#list retrieves as retrieve>
                                    <tr class="tr">
                                        <td>${retrieve.user.getID()?html}</td>
                                        <td><#if retrieve.hasExternalRecipients>Yes<#else>No</#if></td>
                                        <td>${retrieve.timestamp?datetime}</td>
                                        <td>
                                            <div class="job-status">
                                                <#if retrieve.status.name() == "COMPLETE">
                                                <div class="text-success">
                                                    <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>
                                                    Complete
                                                </div>
                                                <#elseif retrieve.status.name() == "FAILED">
                                                <div class="text-danger">
                                                    <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
                                                    Failed
                                                </div>
                                                <#elseif retrieve.status.name() == "NOT_STARTED">
                                                Queued
                                                <#elseif retrieve.status.name() == "IN_PROGRESS">
                                                <span class="glyphicon glyphicon-refresh glyphicon-refresh-animate"></span>
                                                In progress
                                                <#else>
                                                ${retrieve.status}
                                                </#if>
                                            </div>
                                        </td>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>

</div>

<script>
    
    var depositStatus = "${deposit.status}"
    var updateInterval = 500;
    
    var depositInProgress = false
    if (depositStatus == "IN_PROGRESS") {
        depositInProgress = true
    }
    
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
            $('#progress').attr('aria-valuetext', percentComplete + '% Complete');
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
                } else if (depositStatus != "COMPLETE" && depositStatus != "FAILED" && depositStatus != "DELETED" && depositStatus != "") {
                    // Refresh the deposit
                    location.reload(true)
                }
                $('#error-label').text(job.errorMessage)
                $('#job-error').show()
                $('#retrievebtn').removeAttr('disabled');
                
            } else if (job.state != job.states.length - 1) {
                <!-- an active job -->
                updateInterval = 500;
                $('#progtrckr').show()
                $('#retrievebtn').attr('disabled', 'disabled');
                displayJob(job)
                
                if (depositStatus == "NOT_STARTED" && depositInProgress == false) {
                    $("#deposit-status").html("<span class=\"glyphicon glyphicon-refresh glyphicon-refresh-animate\"></span>&nbspIn progress")
                    $("#job-info-msq").html("<strong>Your deposit is in progress.</strong>  You will receive an email notification when the deposit is complete. You may close your browser, and the deposit will continue regardless.")
                    $("#job-info").show()
                    depositInProgress = true
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
                } else if (depositStatus != "COMPLETE" && depositStatus != "DELETED") {
                    // Refresh the deposit
                    location.reload(true)
                }
                $('#retrievebtn').removeAttr('disabled');
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
    /** Overrides color of progress bar */
    ol.progtrckr li.progtrckr-done {
      border-bottom: 4px solid #046B99;
    }
    
    ol.progtrckr li.progtrckr-done:before {
      background-color: #046B99;
    }
    
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
