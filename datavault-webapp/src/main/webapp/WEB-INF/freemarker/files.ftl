<html>
<head>
    <title>Files</title>
</head>
<body>
<h1>Select a file or directory for archiving</h1>

<form>
    <table>

    <#list files?keys as key>

        <tr>
            <td>${files[key]}</td>
            <td>${key}</td>
            <td><input type="radio" name="files" value="${files[key]}"></td>


        </tr>

    </#list>

    </table>
</form>

</body>
</html>