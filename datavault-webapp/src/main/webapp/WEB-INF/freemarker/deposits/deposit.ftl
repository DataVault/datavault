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
                        <td>${deposit.getID()}</td>
                        <td>${deposit.note}</td>
                        <td>${deposit.size}</td>
                        <td>${deposit.creationTime?datetime}</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <h1>Contents</h1>

        <div class="table-responsive">
            <table class="table">
                <tr class="tr">
                    <th>File</th>
                    <th>Fixity</th>
                </tr>

                <#list manifest as filefixity>
                    <tbody>
                    <tr class="tr">
                        <td>${filefixity.file}</td>
                        <td>${filefixity.fixity}</td>
                    </tr>
                    </tbody>
                </#list>
            </table>
        </div>

    </form>

</div>
</div>
</div>

</@layout.vaultLayout>
