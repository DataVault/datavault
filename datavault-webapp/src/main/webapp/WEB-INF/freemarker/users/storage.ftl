<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="user">
<#import "/spring.ftl" as spring />
<@layout.vaultLayout>

<div class="container">

    <div class="jumbotron">
        <p>Welcome to the Data Vault!</p>
        <p>As a new user we need to ask you a few questions about where you want to archive data from.</p>
    </div>

    <h2>Locally attached storage</h2>

    <form id="localForm" action="${springMacroRequestContext.getContextPath()}/filestores/local" method="post">
        <fieldset class="form-group">
            <label for="dirname">Please enter the local directory you would like to browse:</label>
            <input type="text" class="form-control" name="dirname" id="dirname">
        </fieldset>

        <button type="submit" value="submit" class="btn btn-primary">Add</button>

        <input type="hidden" id="submitAction" name="action" value="submit"/>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    </form>

    <br/>

    <h2>SFTP storage</h2>

    <form id="sftpForm" action="${springMacroRequestContext.getContextPath()}/filestores/keys" method="post">
        <fieldset class="form-group">
            <label>Please click here to generate a private/public keypair:</label>
        </fieldset>

        <button type="submit" value="submit" class="btn btn-primary">Add</button>

        <input type="hidden" id="submitAction" name="action" value="submit"/>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    </form>

    <br/>

    <h2>Dropbox</h2>

    <p>etc etc</p>

    <a href="${springMacroRequestContext.getContextPath()}" class="btn btn-primary" role="button">Continue</a>



    <script type="text/javascript">
        var localFrm = $('#localForm');
        localFrm.submit(function (ev) {
            $.ajax({
                type: localFrm.attr('method'),
                url: localFrm.attr('action'),
                data: localFrm.serialize(),
                success: function (data) {
                    alert('filestore added');
                }
            });

            ev.preventDefault();
        });
    </script>

    <script type="text/javascript">
        var sftpFrm = $('#sftpForm');
        sftpFrm.submit(function (ev) {
            $.ajax({
                type: sftpFrm.attr('method'),
                url: sftpFrm.attr('action'),
                data: sftpFrm.serialize(),
                success: function (data) {
                    alert('Public key is ' + data);
                }


            });

            ev.preventDefault();
        });


    </script>




</div>

</@layout.vaultLayout>