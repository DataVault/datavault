<#import "*/layout/defaultlayout.ftl" as layout>
<@layout.vaultLayout>

<div class="container">

    <div class="alert alert-danger" role="alert">
      <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
      <span class="sr-only">Error:</span>
      An error has occured!<br/><br/>
          <#if message?has_content>
              <span>${message}</span>
          </div>
      </#if>

    </div>

</div>

</@layout.vaultLayout>