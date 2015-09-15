<#import "*/layout/loginlayout.ftl" as layout>

<@layout.loginLayout>
    <#import "/spring.ftl" as spring />

    <div class="jumbotron">
        <div class="text-center">
            <img width="300px" border="0" src="${springMacroRequestContext.getContextPath()}/resources/theme/images/logo-dvlarge.png" />
        </div>
    </div>

    <div class="container">
        <div class="row">
            <div class="col-sm-4 col-sm-offset-4 col-md-4 col-md-offset-4">
                <form class="form-signin" action="/datavault-webapp/auth/security_check" method="post">
                    <#if error?has_content>
                        <div class="alert alert-danger" role="alert">
                            <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
                            <span class="sr-only">Error:</span>
                            ${error}
                        </div>
                    </#if>
                    <div class="form-group">
                        <label for="username" class="sr-only">Username</label>
                        <input type="text" name="username" class="form-control" placeholder="Username" required autofocus>
                    </div>
                    <div class="form-group">
                        <label for="password" class="sr-only">Password</label>
                        <input type="password" name="password" class="form-control" placeholder="Password" required>
                    </div>
                    <div class="form-group">
                        <input class="btn btn-lg btn-primary btn-block" type="submit" value="Sign in" />
                    </div>
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                </form>
            </div>
        </div>
    </div>

</@layout.loginLayout>