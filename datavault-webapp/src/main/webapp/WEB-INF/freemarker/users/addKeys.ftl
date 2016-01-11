<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="user">
<#import "/spring.ftl" as spring />
<@layout.vaultLayout>

<div class="container">

    <ol class="breadcrumb">
        <li class="active"><b>Generate SSH Keys</b></li>
    </ol>


    <form id="add-keys" class="form" role="form" action="" method="post">

        <div class="form-group">
            <label class="control-label">User ID:</label> ${user.ID}
        </div>

        <#if keysExist>
            <p>You have already generated keys, step away from the computer!</p>
        <#else>
            <p>Hit the button below to generate keys and thereby sign your life away!</p>
        </#if>


        <div class="form-group">
            <button type="submit" name="action" value="submit" class="btn btn-primary"><span class="glyphicon glyphicon-floppy-disk"></span> Generate</button>
            <button type="submit" name="action" value="cancel" class="btn btn-danger cancel">Cancel</button>
        </div>

        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

    </form>

</div>

</@layout.vaultLayout>