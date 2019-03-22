

describe('Vault page', function(){
    beforeEach(function() {
        cy.visit('/')

        // runs once before all tests in the block
        cy.get('input[name="username"]')
            .type('user1')
            .should('have.value', 'user1')

        cy.get('input[name="password"]')
            .type('password1')
            .should('have.value', 'password1')

        cy.get('input.btn')
            .click()

        cy.url()
            .should('include', '/vaults')
    })

    it('Check required fields', function() {

        cy.get('button.btn-primary')
            .click()

        cy.get('#datasetID-error')
            .contains('This field is required.')
            .and('be.visible')


        cy.get('#vaultName-error')
            .contains('This field is required.')
            .and('be.visible')


        cy.get('#description-error')
            .contains('This field is required.')
            .and('be.visible')

        cy.get('#policyID-error')
            .contains('This field is required.')
            .and('be.visible')

        cy.get('#groupID-error')
            .contains('This field is required.')
            .and('be.visible')

        cy.get('#reviewDate-error')
            .contains('This field is required.')
            .and('be.visible')
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
})