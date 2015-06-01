<html>
<head>
    <title>Vaults</title>
</head>
<body>
<h1>Vaults</h1>

<form>
    <table>

        <tr>
            <th>Name</th>
            <th>Timestamp</th>
        </tr>

    <#list vaults as vault>

        <tr>
            <td>${vault.getName()}</td>
            <td>${vault.getCreationTime()?datetime}</td>
        </tr>

    </#list>

    </table>
</form>

</body>
</html>