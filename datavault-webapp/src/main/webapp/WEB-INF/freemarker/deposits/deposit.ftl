<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
<div class="container">
<div class="row">
<div class="col-sm-12 storage">
    <form>
        <div class="table-responsive">
            <table class="table">
                <thead>
                    <tr class="tr">
                        <th>ID</th>
                        <th>Note</th>
                        <th>Size (bytes)</th>
                        <th>Timestamp</th>
                    </tr>
                <thead>
                <tbody>
                    <tr class="tr">
                        <td><a href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}">${deposit.getID()}</a></td>
                        <td>${deposit.note?html}</td>
                        <td>${deposit.size}</td>
                        <td>${deposit.creationTime?datetime}</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <h3>Contents</h3>

        <div class="table-responsive">
            <table class="table table-bordered table-striped">
                <thead>
                    <tr class="tr">
                        <th>File</th>
                        <th>Fixity</th>
                    </tr>
                </thead>
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

        <h3>Events</h3>

        <div class="table-responsive">
            <table class="table table-bordered table-striped">
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

        <a class="btn btn-primary" href="${springMacroRequestContext.getContextPath()}/vaults/${vault.getID()}/deposits/${deposit.getID()}/withdraw">Withdraw data</a>

    </form>

</div>
</div>
</div>

</@layout.vaultLayout>
