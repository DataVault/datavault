<!DOCTYPE html><!--hello.ftl-->
<#import "/spring.ftl" as spring />
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Hello ${name}!</title>
  <link href="<@spring.url '/resources/test/css/hello.css'/>" rel="stylesheet" type="text/css">
</head>
<body>
<h2 class="hello-title">Hello ${name}!</h2>
<script src="<@spring.url '/resources/test/js/hello.js'/>"></script>
</body>
</html>