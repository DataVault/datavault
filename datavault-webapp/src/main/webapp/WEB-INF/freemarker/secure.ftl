<#assign sec=JspTaglibs["http://www.springframework.org/security/tags"] />
<@sec.authentication var="principal" property="principal" />
<html>
SECURE PAGE
<hr/>
<@sec.authorize access="isAuthenticated()">
  <ul>
    <li><a href="${springMacroRequestContext.getContextPath()}/auth/logout">Logout</a></li>
    <li>logged in as [<@sec.authentication property="principal.username" />]</li>
  </ul>
  Number of Authorities : [${principal.authorities?size}]
  <ol>
    <#list principal.authorities as auth>
      <li>${auth}</li>
    </#list>
  </ol>
</@sec.authorize>

<@sec.authorize access="! isAuthenticated()">
  <ul>
    <li>NOT logged in</li>
    <li><a href="${springMacroRequestContext.getContextPath()}/auth/login">LOGIN</a></li>
  </ul>
</@sec.authorize>
</html>
