<html>
<head>
    <title>Vaults</title>
</head>
<body>
<h1>Vaults</h1>

<form>
    <table>

        <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Description</th>
            <th>Size (bytes)</th>
            <th>Timestamp</th>
        </tr>

        <tr>
            <td><a href="${springMacroRequestContext.getRequestUri()}vaults/${vault.getID()}">${vault.getID()}</a></td>
            <td>${vault.getName()}</td>
            <td>${vault.getDescription()}</td>
            <td>${vault.getSize()}</td>
            <td>${vault.getCreationTime()?datetime}</td>
        </tr>

    </table>
</form>

<a href="${springMacroRequestContext.getRequestUri()}/vaults/create">Create a new Vault</a>


</body>
</html>