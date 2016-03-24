<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="feedback">
<@layout.vaultLayout>
<#import "/spring.ftl" as spring />

<div class="container">

    <ol class="breadcrumb">
        <li class="active"><b>Help</b></li>
    </ol>

    <div class="bs-callout">
        <h4>Welcome to the five step guide to using the DataVault:</h4>
        <ol>
            <li><a href="#understand">Understand the concepts</a></li>
            <li><a href="#describe">Describe your data</a></li>
            <li><a href="#create">Create a vault</a></li>
            <li><a href="#deposit">Deposit data</a></li>
            <li><a href="#restore">Restore data</a></li>
        </ol>
        <p>If you need additional help, please use the <a href="${springMacroRequestContext.getContextPath()}/feedback/">feedback</a> form.</p>
    </div>

    <h4><a name="understand">Understand the concepts</a></h4>
    <p>
        The DataVault has been created to allow you to transfer your data into long term storage.  This allows you to free up
        your working data store once you have finished with your existing data.  You may wish to do this for a number of reasons:
    </p>
    <ul>
        <li>You have data the needs to be protected from accidental change or deletion</li>
        <li>Your funder requires you to keep data safe for a set time period</li>
        <li>You are not able to share your data openly, but want to keep a copy that can be requested</a>
    </ul>
    <div class="bs-callout bs-callout-danger">
        <h4>DataVault vs. Backup</h4>
        <p>
            Please note that the DataVault is not a system for routine backups.  Your data should normally be stored in a system where
            backups are routinely taken.  Your University centrally-provided storage includes backups.
        </p>
        <p>
            The DataVault is for single copies of data which need to be retained for a long period of time.
        </p>
    </div>
    <p>
        There are a number of concepts that you need to understand to use the DataVault:
    </p>
    <ul>
        <li>
            <span class="glyphicon glyphicon-folder-close"></span> <strong>Vault:</strong> A vault is an area into which you can deposit data.
            Each vault is associated with a particular dataset or project, so you should create a new vault for each dataset.  It is likely
            that you might create a new vault for each large project you undertake, so that you keep related datasets in a single vault.
        </li>
        <li>
            <span class="glyphicon glyphicon-save"></span> <strong>Deposit:</strong> Once you have created a vault, you can deposit data
            into the vault.  Each time you deposit data ito the vault, you give abrief description of what is in that particular deposit.
            A vault can contain more than one deposit.
        </li>
        <li>
            <span class="glyphicon glyphicon-open"></span> <strong>Restore:</strong> If you need to retrieve data from a vault, then you are
            able to request that a deposit is restored.  Sometimes this may take a few hours or longer, depending upon the location of the
            data, which may have been transfered into offline storage.
        </li>
    </ul>


    <h4><a name="describe">Describe your data</a></h4>
    <p>Before you put data into the DataVault, you must first describe it.  You should do this by visiting <a href="${link}">${system}</a></p>

    <h4><a name="create">Create a vault</a></h4>
    <p>Here</p>

    <h4><a name="deposit">Deposit data</a></h4>
    <p>Here</p>

    <h4><a name="restore">Restore data</a></h4>
    <p>Here</p>

</div>

</@layout.vaultLayout>