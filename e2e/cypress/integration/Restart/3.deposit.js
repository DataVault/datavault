describe('Deposit Page', function(){
    var username = 'user1'
    var password = 'password1'
    var csrf_token
    var csrf_header

    beforeEach(function() {
        cy.visit('/')

        // runs once before all tests in the block
        cy.get('input[name="username"]')
            .type(username)
            .should('have.value', 'user1')

        cy.get('input[name="password"]')
            .type(password)
            .should('have.value', 'password1')

        cy.get('input.btn')
            .click()

        cy.url()
            .should('include', '/vaults')
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
    })
})