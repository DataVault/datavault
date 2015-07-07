<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">
<div class="row">
<div class="col-sm-12 storage">

    <ul class="nav nav-tabs">
        <li class="active"><a data-toggle="tab" href="#deposit">Deposit</a></li>
        <li><a data-toggle="tab" href="#contents">Contents</a></li>
        <li><a data-toggle="tab" href="#events">Events</a></li>
    </ul>

    <form>
        <div id="deposit-tab-content" class="tab-content">

            <div class="tab-pane active" id="deposit">
                <div class="table-responsive">
                    <table class="table">
                        <thead>
                            <tr class="tr">
                                <th>Note</th>
                                <th>Status</th>
                                <th>Size (bytes)</th>
                                <th>Timestamp</th>
                            </tr>
                        <thead>
                        <tbody>
                            <tr class="tr">
                                <td>${deposit.note?html}</td>
                                <td>${deposit.status}</td>
                                <td>${deposit.size}</td>
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
                                <th>Checksum</th>
                            </tr>
                        </thead>
                        <tbody>
                            <#list manifest as filefixity>
                                <tr class="tr">
                                    <td>${filefixity.file?html}</td>
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
                                <th>Event</th>
                                <th>Message</th>
                                <th>Timestamp</th>
                            </tr>
                        </thead>
                        <tbody>
                            <#list events as event>
                                <tr class="tr">
                                    <td>${event.eventClass?html}</td>
                                    <td>${event.message?html}</td>
                                    <td>${event.timestamp?datetime}</td>
                                </tr>
                            </#list>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}/restore">
            <span class="glyphicon glyphicon-open" aria-hidden="true"></span> Restore data
        </a>

    </form>

</div>
</div>
</div>

</@layout.vaultLayout>
