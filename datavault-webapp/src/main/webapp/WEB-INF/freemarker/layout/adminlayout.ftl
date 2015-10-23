<#macro vaultLayout>
<#import "/spring.ftl" as spring />
<html>
<head>

    <#include "imports.ftl"/>

</head>
<body>

    <#include "adminheader.ftl"/>
    <#nested/>
    <#include "footer.ftl"/>

</body>
</html>
</#macro>