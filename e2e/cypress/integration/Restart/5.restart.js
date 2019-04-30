describe('Restart a Deposit', function() {

    var username = 'admin1'
    var password = 'password1'
    var csrf_token
    var csrf_header

    beforeEach(function () {
        cy.visit('/')

        // runs once before all tests in the block
        cy.get('input[name="username"]')
            .type(username)
            .should('have.value', username)

        cy.get('input[name="password"]')
            .type(password)
            .should('have.value', password)

        cy.get('input.btn')
            .click()

        cy.url()
            .should('include', '/vaults')
    })

    it('Create Vault', function () {
        // Select first Dataset
        cy.get('button[title="Please choose a Dataset"]')
            .click()
        cy.get('div.bootstrap-select.dataset-select.open > div > ul > li[data-original-index="1"] > a')
            .contains('Sample dataset 1')
            .click()
        cy.get('button[data-id="datasetID"]')
            .contains('Sample dataset 1')

        // Type Vault name
        cy.get('#vaultName')
            .type('Vault Test')
            .should('have.value', 'Vault Test')

        // Enter Description
        cy.get('#description')
            .type('This is a vault created for the Cypress test.')
            .should('have.value', 'This is a vault created for the Cypress test.')

        // Select Retention Policy
        cy.get('button[title="Please choose a retention policy"]')
            .click()
        cy.get('div.bootstrap-select.retentionPolicy-select.open > div > ul > li[data-original-index="1"] > a')
            .contains('AHRC Data Management Plan')
            .click()
        cy.get('button[data-id="policyID"]')
            .should('have.attr', 'title', 'AHRC Data Management Plan')
            .contains('AHRC Data Management Plan')

        // Open date picker for grant date
        cy.get('#grantEndDate').click()

        cy.get('#ui-datepicker-div select.ui-datepicker-year')
            .select('2025')
        cy.get('#ui-datepicker-div select.ui-datepicker-month')
            .select('Dec')
        cy.get('#ui-datepicker-div')
            .find('a.ui-state-default')
            .contains('15')
            .click()

        // Select School
        cy.get('button[title="Please choose a School"]')
            .click()
        cy.get('div.bootstrap-select.group-select.open > div > ul > li[data-original-index="1"] > a')
            .contains('Business School')
            .click()
        cy.get('button[data-id="groupID"]')
            .should('have.attr', 'title', 'Business School')
            .contains('Business School')

        // Open date picker for review date
        cy.get('#reviewDate').click()

        cy.get('#ui-datepicker-div select.ui-datepicker-year')
            .select('2024')
        cy.get('#ui-datepicker-div select.ui-datepicker-month')
            .select('Jun')
        cy.get('#ui-datepicker-div')
            .find('a.ui-state-default')
            .contains('26')
            .click()

        // Press Create Button
        cy.get('button.btn-primary')
            .click()

        // Check we are on the Vault page: We don't want the regexp to match /vaults/create
        cy.url().should('match',/.*\/vaults\/([0-9a-z]+\-){4}[0-9a-z]+\//)
        cy.get('h2').contains('Summary of Vault Metadata')

        // Return on the home page and check the new vault appear
        cy.get('nav.navbar ul.nav.navbar-nav')
            .find('a')
            .contains('DATAVAULT')
            .click()

        cy.get('h3').contains('Current Vaults')

        cy.get('table.table-bordered')
            .find('a')
            .contains('Vault Test')
    })

    function getCSRF_Token(){
        csrf_token = Cypress.$("meta[name=_csrf]").attr("content");
        csrf_header = Cypress.$("meta[name=_csrf_header]").attr("content");
    }

    it('Create Local Filestore', function() {
        cy.get('.navbar-right > li > a:contains("File Locations")')
            .click()

        getCSRF_Token()

        cy.log("header: "+csrf_header+", token: "+csrf_token);

        cy.server({
            method: "POST",
            onRequest:   function(route,  proxy) {
                proxy.xhr.setRequestHeader(csrf_header,  csrf_token);
            }
        })

        cy.request({
            method: 'POST',
            url: '/filestores/local',
            form: true,
            body: {
                path: '/Users'
            },
            headers:{
                "X-CSRF-TOKEN": csrf_token
            }
        })
    })

    it('Add Fake Archive Store to Cause Deposit Failure', function() {
        // Go to admin page
        cy.get('.nav.navbar-nav > li > a:contains("Administration")').click()

        // Go to Admin Archives Store Page
        cy.get('a[href="/datavault-webapp/admin/archivestores"]').click()

        // Click Add Archives Store Button
        cy.get('.btn-default:contains("Add Archivestore")').click()

        // Put Fake info
        cy.get('input[name="label"]')
            .type('Fake Archive')
            .should('have.value', 'Fake Archive')
        cy.get('.btn-primary[type="submit"]').click()


    })

    it('Create Deposit', function() {
        // Go to the Home page
        cy.get('.nav.navbar-nav > li > a:contains("DATAVAULT")').click()

        // Click on Deposit button
        cy.get('a.btn.btn-primary:contains("Deposit")').last().click()

        // Click on Datastorage button
        cy.get('button.btn.btn-default:contains("Data Storage")').click()

        // Open Filesystem
        cy.get('.fancytree-node .fancytree-expander').last().click()

        // Select the 'dir' directory
        cy.get('.fancytree-node .fancytree-title:contains("dir")').last().click()

        // Click 'Add' button
        cy.get('#add-from-storage-btn').click()

        // Enter deposit name
        cy.get('input[name="name"]')
            .type('Deposit Test')
            .should('have.value', 'Deposit Test')

        // Enter deposit description
        cy.get('input[name="description"]')
            .type('Deposit created for Cypress Tests')
            .should('have.value', 'Deposit created for Cypress Tests')

        cy.get('input[name="hasPersonalData"][value="No"]').click()

        cy.get('.btn-primary[value="submit"]').click()

        // Check it failed
        cy.get('#job-error').should('contain', 'Deposit failed: could not access archive filesystem')
    })

    it('Remove Fake Archive Store', function() {
        // Go to admin page
        cy.get('.nav.navbar-nav > li > a:contains("Administration")').click()

        // Go to Admin Archives Store Page
        cy.get('a[href="/datavault-webapp/admin/archivestores"]').click()

        var rows = cy.get('#fileStoresLocal tr')
        rows.each(function (element) {
            cy.get('#fileStoresLocal tr').then( function($row) {
                if ($row.find('td:contains("Fake")').length) {
                    cy.get('#fileStoresLocal tr .btn-danger:contains("Remove")').first().click()
                    cy.get('#confirm-removal #remove').click()
                    cy.get('#confirm-removal', {timeout: 10000}).should('not.visible');
                } else {
                    return false;
                }
            })
        })
    })

    it('Restart Deposit', function() {
        // Go to admin page
        cy.get('.nav.navbar-nav > li > a:contains("Administration")').click()

        // Go to Admin Archives Store Page
        cy.get('a[href="/datavault-webapp/admin/deposits"] > i.fa-download').click({ force: true })

        cy.get('.restart-deposit-btn').last().click()
    })
})