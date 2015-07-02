<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">
<div class="row">
<div class="col-sm-12 storage">
    <form>
        <div class="table-responsive">
            <table class="table">
                <tr class="tr">
                    <th>ID</th>
                    <th>Note</th>
                    <th>Size (bytes)</th>
                    <th>Timestamp</th>
                </tr>
                <tbody>
                    <tr class="tr">
                        <td><a href="${springMacroRequestContext.getRequestUri()}">${deposit.getID()}</a></td>
                        <td>${deposit.note?html}</td>
                        <td>${deposit.size}</td>
                        <td>${deposit.creationTime?datetime}</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <h1>Contents</h1>

        <div class="table-responsive">
            <table class="table table-bordered table-striped">
                <tr class="tr">
                    <th>File</th>
                    <th>Fixity</th>
                </tr>

                <tbody>
                <#list manifest as filefixity>
                    <tr class="tr">
                        <td>${filefixity.file?html}</td>
                        <td style="font-family:monospace;">${filefixity.fixity} <span class="label label-primary">${filefixity.algorithm}</span></td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>

        <h1>Events</h1>

        <div class="table-responsive">
            <table class="table table-bordered table-striped">
                <tr class="tr">
                    <th>Event</th>
                    <th>Message</th>
                    <th>Timestamp</th>
                </tr>

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

    </form>

    <a href="${springMacroRequestContext.getRequestUri()}/withdraw">Withdraw data</a>
</div>
</div>
</div>

</@layout.vaultLayout>
