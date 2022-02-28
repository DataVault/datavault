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
                            Create new vault
                            <small>
                            <span class="glyphicon glyphicon-info-sign" aria-hidden="true" data-toggle="tooltip"
                                  title="You can make multiple deposits into a vault. Each deposit may contain multiple files and folders. You no longer need to create a Pure record before creating your Vault.">
                            </span>
                            </small>
                        </h2>

                        <br/>

                        <!-- MultiStep Form -->
                        <div class="container">
                            <#if errors?has_content>
                            <div id="create-error" class="alert alert-danger" role="alert">
                                Your request contains the following errors<br/>
                                <#list errors as error>
                                    ${error}<br/>
                                </#list>
                            </div>
                            </#if>
                            <div class="row justify-content-center mt-0">
                                <div class="col-10 text-center p-0 mt-3 mb-2">
                                    <div class="card px-0 pt-4 pb-0 mt-3 mb-3">
                                        <div class="row">
                                            <div class="col-md-12 mx-0">
                                                <form id="vault-creation-form" class="form" role="form" action="${springMacroRequestContext.getContextPath()}/vaults/stepCreate" method="post" novalidate="novalidate" _lpchecked="1">
                                                    <#include "progressFieldset.ftl"/>
                                                    <!-- fieldsets -->
                                                    <#include "affirmationFieldset.ftl"/>
                                                    <#include "billingFieldset.ftl"/>
                                                    <#include "infoFieldset.ftl"/>
                                                    <#include "usersFieldset.ftl"/>
                                                    <#include "summaryFieldset.ftl"/>
                                                    <input type="hidden" id="submitAction" name="action" value="submit" />
                                                    <@spring.bind "vault.pendingID" />
                                                    <input type="hidden" id="pendingID" name="${spring.status.expression}" value="${spring.status.value!""}" />
                                                    <@spring.bind "vault.loggedInAs" />
                                                    <input type="hidden" id="loggedInAs" name="${spring.status.expression}" value="${spring.status.value!""}" />
                                                    <@spring.bind "vault.confirmed" />
                                                    <input type="hidden" id="confirmed" name="${spring.status.expression}" value="${spring.status.value!""}" />
                                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                                                </form>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
<#--                        </div>-->
                    </div>
                </div>
            </div>
        </div>
    </div>
    
   <!-- Custom javascript -->
   <!-- import date-validation-utils.j first -->
   <script src="<@spring.url '/resources/application/js/date-validation-utils.js'/>"></script>
   <script src="<@spring.url '/resources/application/js/new-create-prototype.js'/>"></script>
    
</@layout.vaultLayout>