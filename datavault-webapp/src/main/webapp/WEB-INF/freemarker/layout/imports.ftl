    <link rel="icon" type="image/ico" href="<@spring.url '/resources/favicon.ico?v=2'/>"/>

    <!-- CSS -->
    <link href="<@spring.url '/resources/bootstrap/css/bootstrap.min.css'/>" rel="stylesheet" type="text/css">
    <link href="<@spring.url '/resources/bootstrap-select/css/bootstrap-select.min.css'/>" rel="stylesheet" type="text/css">
    <link href="<@spring.url '/resources/font-awesome/css/font-awesome.min.css'/>" rel="stylesheet" type="text/css">
    <link href="<@spring.url '/resources/fancytree/skin-lion/ui.fancytree.min.css'/>" rel="stylesheet" type="text/css">
    <!--
    <link href="<@spring.url '/resources/theme/css/vault.css'/>" rel="stylesheet" type="text/css">
    -->

    <!-- jQuery js -->
    <script src="<@spring.url '/resources/jquery/js/jquery-1.11.3.min.js'/>"></script>

    <!-- jQuery UI js -->
    <script src="<@spring.url '/resources/jquery-ui/jquery-ui.min.js'/>"></script>
    <link href="<@spring.url '/resources/jquery-ui/jquery-ui.min.css'/>" rel="stylesheet" type="text/css">

    <!-- Bootstrap js -->
    <script src="<@spring.url '/resources/bootstrap/js/bootstrap.min.js'/>"></script>

    <!-- Bootstrap-select js -->
    <script src="<@spring.url '/resources/bootstrap-select/js/bootstrap-select.min.js'/>"></script>

    <!-- jQuery validate js -->
    <script src="<@spring.url '/resources/jquery-validate/js/jquery.validate.min.js'/>"></script>

    <!-- Fancytree js -->
    <script src="<@spring.url '/resources/fancytree/jquery.fancytree-all.min.js'/>"></script>

    <!-- Flow.js -->
    <script src="<@spring.url '/resources/flow/js/flow.min.js'/>"></script>

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
        <script src="<@spring.url '/resources/html5shiv/html5shiv.min.js'/>"></script>
        <script src="<@spring.url '/resources/respond/respond.min.js'/>"></script>
    <![endif]-->

    <!-- Spring Security CSRF (ajax) -->
    <meta name="_csrf" content="${(_csrf.token)!""}"/>
    <meta name="_csrf_header" content="${(_csrf.headerName)!""}"/>

    <!-- For IE compatibility -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    
    <!-- Jackie CSS, JS and IMG -->
    <link href="<@spring.url '/resources/theme/jackie/edgel.css'/>" rel="stylesheet" type="text/css">
    <link href="<@spring.url '/resources/theme/jackie/jackie.css'/>" rel="stylesheet" type="text/css">
    <!--
    <script src="<@spring.url '/resources/theme/jackie/edgel.js'/>"></script>
    -->
    <link href="<@spring.url '/resources/theme/jackie/jackie-quickfix.css'/>" rel="stylesheet" type="text/css">
    
    <!-- Styles to be overriden -->
    <link href="<@spring.url '/resources/theme/css/overrideStyles.css'/>" rel="stylesheet" type="text/css">

    <!-- Error Handler JS -->
    <script src="<@spring.url '/resources/theme/js/ErrorHandler.js'/>"></script>
    <script>
        ErrorHandler.initialiseSpringContextPath('${springMacroRequestContext.getContextPath()}');
    </script>
