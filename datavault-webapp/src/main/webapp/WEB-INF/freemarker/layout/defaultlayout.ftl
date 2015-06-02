<#macro vaultLayout>
<#import "/spring.ftl" as spring />
<html>
<head>
    <!-- Bootstrap -->
    <link href="<@spring.url '/resources/bootstrap/css/bootstrap.min.css'/>" rel="stylesheet" type="text/css">
    <link href="<@spring.url '/resources/theme/css/vault.css'/>" rel="stylesheet type="text/css">
    <script src="<@spring.url '/resources/bootstrap/js/bootstrap.min.js'/>"></script>
</head>
<body>

    <#include "header.ftl"/>
    <#nested/>
    <#include "footer.ftl"/>

</body>
</html>
</#macro>