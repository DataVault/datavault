<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="admin">
<@layout.vaultLayout>

    <div class="modal fade" id="confirm-removal" tabindex="-1" role="dialog" aria-labelledby="confirmRemovalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                    <h4 class="modal-title" id="confirmRemovalLabel">Confirm removal of Deposit</h4>
                </div>
                <div class="modal-body">
                    <p>You have selected to delete the files in this deposit.<br> Do you have the authorisation of the Owner / Data Manager and Head of School or College, as per the review process? (In the case of an ‘orphan’ vault, permission of the Head of School and College is necessary).</p>

                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-danger btn-ok" id="remove">Yes</button>
                </div>
            </div>
        </div>
    </div>

    <div class="container">

        <ol class="breadcrumb">
            <li><a href="${springMacroRequestContext.getContextPath()}/admin/"><b>Administration</b></a></li>
            <li class="active"><b>Audits</b></li>
        </ol>
        <div class="row">
            <div class="cold-md-12">
                <button id="audit" class="btn btn-primary pull-right">
                    <span class="glyphicon glyphicon-eye-open" aria-hidden="true"></span> Run Audit
                </button>
            </div>
        </div>

        <#if deposits?has_content>

        <form action="" method="get" class="form-inline">
            <div class="form-group">
                Sort by:
                <label class="checkbox-inline">
                    <input class="form-check-input" type="radio" name="sort" id="date" value="date" <#if sort == "date">checked</#if>> Date
                </label>
                <label class="checkbox-inline">
                    <input class="form-check-input" type="radio" name="sort" id="chunkStatus" value="chunkStatus" <#if sort == "chunkStatus">checked</#if>> Chunk Status
                </label>
                <label class="checkbox-inline">
                    <input class="form-check-input" type="radio" name="sort" id="chunkNum" value="chunkNum" <#if sort == "chunkNum">checked</#if>> Chunk Number
                </label>
            </div>
            <button type="submit" class="btn btn-primary">Submit</button>
        </form>

        <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">
            <#list deposits as deposit>
            <div class="panel panel-default">
                <div class="panel-heading row" role="tab" id="heading-${deposit["deposit"].getID()?html}">
                    <div class="col-md-6">
                    <h4 class="panel-title">
                        <a role="button" data-toggle="collapse" data-parent="#accordion"
                           href="#collapse-${deposit["deposit"].getID()?html}"
                           aria-expanded="true" aria-controls="collapse${deposit["deposit"].getID()?html}">
                            ${deposit["deposit"].getName()?html} [${deposit["deposit"].getID()?html}]
                        </a>
                    </h4>
                    </div>
                    <div class="col-md-3">
                        ${deposit["deposit"].getCreationTime()?datetime}
                    </div>
                    <div class="col-md-3">
                        ${deposit["deposit"].getStatus()?html}
                    </div>
                </div>
                <div id="collapse-${deposit["deposit"].getID()?html}" class="panel-collapse collapse" role="tabpanel"
                     aria-labelledby="heading-${deposit["deposit"].getID()?html}">
                    <div class="panel-body">
                        <div class="table-responsive">
                            <table class="table table-striped">
                                <thead>
                                <tr class="tr">
                                    <th>Status</th>
                                    <th>Chunk</th>
                                    <th>Last Audit Start</th>
                                    <th>Last Audit End</th>
                                    <th>Last Audit Status</th>
                                    <th>Last Audit Message</th>
                                </tr>
                                </thead>

                                <tbody>
                                    <#list deposit["chunks_info"] as chunkInfo>
                                    <tr class="tr">
                                        <td>
                                            <#if chunkInfo["last_audit_chunk"]??>
                                            <span class="glyphicon glyphicon-ok" aria-hidden="true"></span>
                                            <#else>
                                            <span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
                                            </#if>
                                        </td>
                                        <td>
                                            ${chunkInfo["deposit_chunk"].getChunkNum()?html}
                                        </td>
                                        <#if chunkInfo["last_audit_chunk"]??>
                                            <td>
                                            <#if chunkInfo["last_audit_chunk"].getCreationTime()??>
                                                ${chunkInfo["last_audit_chunk"].getCreationTime()?datetime}
                                            </#if>
                                            </td>
                                            <td>
                                            <#if chunkInfo["last_audit_chunk"].getCompletedTime()??>
                                                ${chunkInfo["last_audit_chunk"].getCompletedTime()?datetime}
                                            </#if>
                                            </td>
                                            <td>
                                            <#if chunkInfo["last_audit_chunk"].getStatus()??>
                                                ${chunkInfo["last_audit_chunk"].getStatus()?html}
                                            </#if>
                                            </td>
                                            <td>
                                            <#if chunkInfo["last_audit_chunk"].getNote()??>
                                                ${chunkInfo["last_audit_chunk"].getNote()?html}
                                            </#if>
                                            </td>
                                        <#else>
                                            <td>-</td><td>-</td><td>-</td><td>-</td>
                                        </#if>
                                    </tr>
                                    </#list>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            </#list>
        </div>
        <#else>
            No Data Available
        </#if>
    </div>
    <script>
        $("button#audit").click(function(){
            $.ajax({
                url: '${springMacroRequestContext.getContextPath()}/admin/deposits/audit',
                type: 'GET',
                success: function (data) {
                    console.log("----success---");
                },
                error: function(xhr, ajaxOptions, thrownError) {
                    alert('Error: unable to delete Deposit');
                }
            });
        });

        //Add Spring Security CSRF header to ajax requests
        $(function () {
            var token = $("meta[name='_csrf']").attr("content");
            var header = $("meta[name='_csrf_header']").attr("content");
            $(document).ajaxSend(function(e, xhr, options) {
                xhr.setRequestHeader(header, token);
            });
        });
    </script>
</@layout.vaultLayout>
