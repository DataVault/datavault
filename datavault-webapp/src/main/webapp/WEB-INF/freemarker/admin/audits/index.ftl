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

        <button id="audit" class="btn btn-primary"><span class="glyphicon glyphicon-eye-open" aria-hidden="true"></span> Run Audit</button>

        <#if audits?has_content>
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                    <tr class="tr">
                        <th>Audit ID</th>
                        <th>Date Started</th>
                        <th>Status</th>
                        <th>Size</th>
                    </tr>
                    </thead>

                    <tbody>
                    <#list audits as audit>
                        <tr class="tr">
                            <td>
                                <#if audit.auditChunks?has_content>
                                <a class="btn btn-link" data-toggle="collapse" href="#collapse${audit.getId()?html}"
                                   aria-expanded="false" aria-controls="collapse${audit.getId()?html}">
                                    <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
                                </a>
                                </#if>
                                ${audit.getId()?html}
                            </td>
                            <td>${audit.getCreationTime()?datetime}</td>
                            <td>${audit.getStatus()}</td>
                            <td>${audit.auditChunks?size}</td>
                        </tr>
                        <#if audit.auditChunks?has_content>
                        <tr>
                            <td colspan="4">
                                <div class="collapse table-responsive" id="collapse${audit.getId()?html}">
                                    <table class="table">
                                        <thead>
                                        <tr class="tr">
                                            <th>Chunk ID</th>
                                            <th>Date</th>
                                            <th>Status</th>
                                            <th>Message</th>
                                            <th>Deposit</th>
                                            <th>ArchiveId</th>
                                        </tr>
                                        </thead>

                                        <tbody>
                                        <#list audit.auditChunks as chunk >
                                        <tr class="tr">
                                            <td>
                                            <#if chunk.depositChunk??>
                                                ${chunk.depositChunk.getID()?html}
                                            </#if>
                                            </td>
                                            <td>
                                            <#if chunk.creationTime??>
                                                ${chunk.creationTime?datetime} -
                                            </#if>
                                            <#if chunk.completedTime??>
                                                <br/>${chunk.completedTime?datetime}
                                            </#if>
                                            </td>
                                            <td>
                                            <#if chunk.status??>
                                                ${chunk.status}
                                            </#if>
                                            </td>
                                            <td>
                                            <#if chunk.getNote()??>
                                                ${chunk.getNote()?html}
                                            </#if>
                                            </td>
                                            <td>
                                            <#if chunk.deposit??>
                                                ${chunk.deposit.getID()?html}
                                            </#if>
                                            </td>
                                            <td>
                                            <#if chunk.archiveId??>
                                                ${chunk.archiveId?html}
                                            </#if>
                                            </td>
                                        </tr>
                                        </#list>
                                        </tbody>
                                    </table>
                                </div>
                            </td>
                        </tr>
                        </#if>
                    </#list>
                    </tbody>
                </table>
            </div>
        </div>
    <#else>
        <div class="row">No Data Available</div>
    </#if>
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
