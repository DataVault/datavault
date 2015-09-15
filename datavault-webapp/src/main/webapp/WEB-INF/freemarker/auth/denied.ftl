<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />

    <div class="row">
        <div class="col-sm-3 col-sm-offset-5 col-md-3 col-md-offset-5">

            <div class="alert alert-danger" role="alert">
                <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
                <span class="sr-only">Error:</span>
                Access denied.
            </div>

        </div>
    </div>

</@layout.vaultLayout>