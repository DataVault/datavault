<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="user">
<#import "/spring.ftl" as spring />
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li class="active"><b>Generate SSH Keys</b></li>
    </ol>

    <label class="control-label">User ID:</label> ${user.ID}

    <p>Congratulations! A public/private key pair have been generated on your behalf. Here is the public key, please put
        it on the appropriate server to allow Datavault to connect to that server using the matching private key.</p>

    <label class="control-label">Public Key:</label>
    <div>${publicKey}</div>

    <div>
        <a href="${springMacroRequestContext.getContextPath()}/">
            <button type="button" class="btn btn-primary">Return</button>
        </a>
    </div>


</div>

</@layout.vaultLayout>