<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="home">
<#import "/spring.ftl" as spring />
<#assign sec=JspTaglibs["http://www.springframework.org/security/tags"] />

<@layout.vaultLayout>

<div class="container">
    <div class="row">
        <div class="col-md-12">
            <div class="panel panel-uoe-low">

                <div class="associated-image">
                    <figure class="uoe-panel-image uoe-panel-image"></figure>
                </div>

                <div class="panel-body">
                    <h2>
                        Your vault is now pending acceptance.
                    </h2>

                    <br/>

                    <div class="container">
                        <div class="form-card">
                            <p>
                                Thank you for submitting your request for a vault to be created. The Research Data Support team will check this request,
                                and may contact you by email through Unidesk if we have questions about your vault. You will receive an email once we have
                                approved your vault, which will then be ready for you to go ahead and deposit your data.
                            </p>
                            <p>
                                You can view and edit your pending vault(s) at any time from the
                                <a href="${springMacroRequestContext.getContextPath()}/vaults/">
                                    homepage.
                                </a>
                            </p>
                            <p>
                                If you wish to create another new vault, please click on the button below.
                            </p>
                            <div class="row">
                                <a href="${springMacroRequestContext.getContextPath()}/vaults/buildsteps" class="btn btn-lg btn-primary">
                                    <span class="glyphicon glyphicon-folder-close"></span>
                                    Create another vault
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    </@layout.vaultLayout>
