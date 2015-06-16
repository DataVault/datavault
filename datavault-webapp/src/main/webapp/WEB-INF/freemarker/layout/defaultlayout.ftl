<#macro vaultLayout>
<#import "/spring.ftl" as spring />
<html>
<head>
    <!-- Bootstrap -->
    <link href="<@spring.url '/resources/bootstrap/css/bootstrap.min.css'/>" rel="stylesheet" type="text/css">
    <link href="<@spring.url '/resources/theme/css/vault.css'/>" rel="stylesheet" type="text/css">

    <!-- Fancytree -->
    <link href="<@spring.url '/resources/fancytree/skin-lion/ui.fancytree.min.css'/>" rel="stylesheet" type="text/css">

    <!-- jQuery js -->
    <script src="<@spring.url '/resources/jquery/js/jquery-1.11.3.min.js'/>"></script>

    <!-- Bootstrap js -->
    <script src="<@spring.url '/resources/bootstrap/js/bootstrap.min.js'/>"></script>

    <!-- jQuery UI js -->
    <script src="<@spring.url '/resources/jquery-ui/jquery-ui.min.js'/>"></script>

    <!-- Fancytree js -->
    <script src="<@spring.url '/resources/fancytree/jquery.fancytree.min.js'/>"></script>

</head>
<body>

    <#include "header.ftl"/>
    <#nested/>
    <#include "footer.ftl"/>

</body>
</html>
</#macro>