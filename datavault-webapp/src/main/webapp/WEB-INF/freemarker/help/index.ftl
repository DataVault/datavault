<#import "*/layout/defaultlayout.ftl" as layout>
<#-- Specify which navbar element should be flagged as active -->
<#global nav="help">
<@layout.vaultLayout>
<#import "/spring.ftl" as spring />

<div class="container">

    <ol class="breadcrumb">
        <li><a href="${springMacroRequestContext.getContextPath()}/"><b>Home</b></a></li>
        <li class="active"><b>Help</b></li>
    </ol>

    <div class="panel panel-uoe-low">
        <div class="associated-image">
            <figure class="uoe-panel-image uoe-panel-image"></figure>
        </div>

        <div class="panel-body">

            <h2>Help</h2>
            <br/>

            <div class="bs-callout">
                <h4>Welcome to the five step guide to using the DataVault:</h4>
                <ol>
                    <li><a href="#understand">Understand the concepts</a></li>
                    <li><a href="#describe">Describe your data</a></li>
                    <li><a href="#create">Create a vault</a></li>
                    <li><a href="#deposit">Deposit data</a></li>
                    <li><a href="#retrieve">Retrieve data</a></li>
                </ol>
                <p>If you need additional help, please use the <a href="${springMacroRequestContext.getContextPath()}/feedback/">feedback</a> form.</p>
            </div>

            <h4><a name="understand">1 - Understand the concepts</a></h4>
            <p>
                The DataVault has been created to allow you to transfer your data into long term storage.  This allows you to free up
                your working data store once you have finished with your existing data.  You may wish to do this for a number of reasons:
            </p>
            <ul>
                <li>You have data that needs to be protected from accidental change or deletion</li>
                <li>Your funder requires you to keep data safe for a set time period</li>
                <li>You are not able to share your data openly, but want to keep a copy that can be requested</a>
            </ul>
            <div class="bs-callout bs-callout-danger">
                <h4>DataVault vs. Backup</h4>
                <p>

                    Please note that the DataVault is not a system for routine backups. Your data should normally be stored in a service where
                    backups are routinely taken. If you require a backup service please speak to your IT Services.
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
                    <i class="fa fa-download fa-rotate-180" aria-hidden="true"></i> <strong>Deposit:</strong> Once you have created a vault, you can deposit data
                    into the vault.  Each time you deposit data into the vault, you give a brief description of what is in that particular deposit.
                    A vault can contain more than one deposit.
                </li>
                <li>
                    <i class="fa fa-upload fa-rotate-180" aria-hidden="true"></i> <strong>Retrieve:</strong> If you need to retrieve data from a vault, then you are
                    able to request that a deposit is restored.  Sometimes this may take a few hours or longer, depending upon the location of the
                    data, which may have been transferred into offline storage.
                </li>
            </ul>


            <h4><a name="describe">2 - Describe your data</a></h4>
            <p>
                Before you put data into the DataVault, you must first describe it.  You should do this by visiting <a href="${link}" target="_blank">${system}</a>.
                Record a new dataset entry in your ${system} account.  When you create a vault, you will be able to select this dataset from the list
                of datasets you have.
            </p>

            <h4><a name="create">3 - Create a vault</a></h4>
            <p>
                The first step of creating a new vault is to give it a name and a description.  The name should be short but meaningful.  A good example
                of a name might be "Project {your project name} - data and software".  This will help you, or others, know which vault to open
                in the future when looking for data related to a specific project.
            </p>
            <p>
                The description can be longer, and can be used to provide further information such as who else was involved with the creation of
                the data, dates relating to the data, or any other details that you or others might need in the future.
            </p>
            <p>
                Now that you have given your vault a name and a description, you need to select the dataset description that you previously entered
                into ${system} (this is done via the 'Relates to' dropdown menu).  By relating your data to this description in ${system}, it means that you won't need to re-enter any of the
                details about the data again!
            </p>
            <p>
                Next you need to select which retentionPolicy should be used to govern the data.  The choice of retentionPolicy will usually be related to your funder
                (if an external funder paid for your research project), or a default university retentionPolicy.  The retentionPolicy governs how long the data will
                be kept in the vault before it needs to be reviewed.  A typical retentionPolicy might keep the data for 5 or 10 years before requiring a review
                that will allow you to decide whether to keep the data for longer or to delete it.
            </p>
            <p>
                Finally, select which group you belong to.  Your group's Data Manager will also be able to access your data vaults in order to assist you with the management of them.
            </p>
            <div class="bs-callout bs-callout-info">
                <h4>Access to your vaults</h4>
                <p>
                    In order to assist you with your vaults, your group's Data Manager will also have access.  So too will the Data Vault support team.
                </p>
                <p>
                    When your vault is ready for review in a number of years time, your Data Manager will be involved in the review of the data
                    to help decide whether to keep it for longer, and if so, for how long.
                </p>
            </div>
            <p>
                Finally, click on the 'Create new Vault' button to create the empty vault.  The vault is now ready to accept deposits.
            </p>

            <h4><a name="deposit">4 - Deposit data</a></h4>
            <p>
                Now that you have created a vault, you can deposit files into the vault.  You can do this in more than one stage, by using multiple
                'deposits'.  You may wish to deposit in parts due to the size of the data, to split it into logical chunks that you might wish to
                restore separately, of because the data is ready to be deposited at different times.  If the deposit is unrelated to other deposits
                in the same vault, it should be put into a new separate vault.
            </p>
            <p>
                When you click on 'Deposit data' you will be requested to do two things:
            </p>
            <p>
                First you need to enter a 'Deposit Note'.  This is a brief description of the deposit, so that you can tell the difference between
                different deposits in the same vault.
            </p>
            <div class="bs-callout bs-callout-info">
                <h4>Naming deposits</h4>
                <p>
                    A useful deposit note might be 'Study 34, cohort 5 - collected on 24th September 2015', whereas a deposit note such as 'Another deposit' is not as useful.
                </p>
            </div>
            <p>
                Second, you will see a representation of your filestore.  Navigate through this until you find the directory / folder, or individual
                file that you wish to deposit.  Select the relevant directory / folder or file by clicking on it.
            </p>
            <p>
                Finally, click on the 'Deposit Data' button to initiate the deposit.  Depending on how busy the Data Vault system is, your deposit
                may start instantly, or it may be placed in a queue to process later.  You will be able to view the progress of the deposit from the
                deposit page.
            </p>

            <h4><a name="retrieve">5 - Retrieve data</a></h4>
            <p>
                To retrieve data, first select the Vault you wish to retrieve data from, and then select the relevant Deposit.  Click on 'Retrieve data'.
                If you wish to retrieve data from multiple Deposits, you will need to perform the following step for each Deposit.
            </p>
            <p>
                First you must enter a retrieval note. This is a brief description of why you are retrieving the data. A useful note should state
                whether the data is to be re-used so as to guide future retention decisions. This is stored in an audit log, so that you can see who
                retrieved data, and why they retrieved it.
            </p>
            <p>
                Second, select a directory / folder where you wish the data to be retrieved to.  You will probably want to create a new empty
                directory / folder for the data.
            </p>
            <p>
                Finally, click on the 'Retrieve data' button to initiate the restore.  Depending on how busy the Data Vault system is, your retrieval
                may start instantly, or it may be placed in a queue to process later.  You will be able to view the progress of the restore from the
                deposit page.
            </p>
        </div>
    </div>

</div>

</@layout.vaultLayout>