<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="none">
<@layout.vaultLayout>
    <#import "/spring.ftl" as spring />

    <div class="row">
        <div class="col-sm-3 col-sm-offset-5 col-md-3 col-md-offset-5">

            <div class="alert alert-info" role="alert">
                <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
                <span class="sr-only">Warning:</span>
                <strong>To logout of the DataVault you must close your browser, in order to end the browser session, so that you will be logged out of EASE.</strong>
            </div>

        </div>
    </div>

</@layout.vaultLayout>