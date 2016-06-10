<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="user">
<#import "/spring.ftl" as spring />
<@layout.vaultLayout>

<div class="container">

    <h1>Please configure your storage options below</h1>

    <div>
        <h2>Locally attached storage</h2>

        <form class="form" role="form" id="localForm" action="${springMacroRequestContext.getContextPath()}/filestores/local" method="post">
            <h4>Please enter the local directory you would like to browse:</h4>
            <div class="form-inline">
                <input type="text" class="form-control" name="dirname" id="dirname">
                <button type="submit" value="submit" class="btn btn-primary">Add</button>
            </div>
        </form>


        <div id="localFileStores">
            <h4>Existing filestores</h4>
            <#list filestores as filestore>
                <form class="form localFormDelete" role="form" id="localFormDelete" action="${springMacroRequestContext.getContextPath()}/filestores/${filestore.ID}" method="delete">
                    <div class="form-inline" id="localFileStore">
                        <input type="text" class="form-control" readonly="readonly" value="${filestore.properties['rootPath']}" >
                        <button type="submit" value="delete" class="btn btn-primary">Delete</button>
                    </div>
                </form>
            </#list>
        </div>

    </div>

    <div>
        <h2>SFTP storage</h2>

        <form class="form" role="form" id="sftpForm" action="${springMacroRequestContext.getContextPath()}/filestores/keys" method="post">
            <div class="form-inline">
                <label>Please click here to generate a private/public keypair:</label>
                <button type="submit" value="submit" class="btn btn-primary">Add</button>
            </div>
        </form>

    </div>

    <br/>

    <a href="${springMacroRequestContext.getContextPath()}" class="btn btn-primary" role="button">Continue</a>

    <script type="text/javascript">

        var localFrm = $('#localForm');
        localFrm.submit(function (ev) {
            $.ajax({
                type: localFrm.attr('method'),
                url: localFrm.attr('action'),
                data: localFrm.serialize(),
                success: function (data) {
                    $("#dirname").val("");
                    $("#localFileStores").append( "<form class='form localFormDelete' role='form' id='localFormDelete' action='${springMacroRequestContext.getContextPath()}/filestores/" + data.id  + "' method='delete'>" +
                                                    "<div class='form-inline' id='localFileStore'>" +
                                                    "<input type='text' class='form-control' readonly='readonly' value='" + data.properties.rootPath + "'>" +
                                                    "<button type='submit' value='delete' class='btn btn-primary'>Delete</button>" +
                                                    "</div>" +
                                                    "</form>" );
                }
            });

            ev.preventDefault();
        });


        var localFrmDel = $('.localFormDelete');
        localFrmDel.submit(function (ev) {
            $.ajax({
                type: localFrmDel.attr('method'),
                url: localFrmDel.attr('action'),
                data: localFrmDel.serialize(),
                success: function (data) {
                    alert('Key deleted ' + data);
                }
            });

            ev.preventDefault();
        });


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


        // Add Spring Security CSRF header to ajax requests
        $(function () {
            var token = $("meta[name='_csrf']").attr("content");
            var header = $("meta[name='_csrf_header']").attr("content");
            $(document).ajaxSend(function(e, xhr, options) {
                xhr.setRequestHeader(header, token);
            });
        });


    </script>




</div>

</@layout.vaultLayout>