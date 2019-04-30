describe('Retrieve Page', function(){
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

    it('Do Retrieve', function() {
        // Go to the Home page
        cy.get('.nav.navbar-nav > li > a:contains("DATAVAULT")').click()

        // Go to the Vault Test page
        cy.get('a:contains("Vault Test")').last().click()

        // Go to the Deposit Test page
        cy.get('a:contains("Deposit Test")').last().click()

        // Click on Retrieve button
        cy.get('#retrievebtn').click()

        cy.get('input[name="hasExternalRecipients"][value="false"]').click()

        // Open Filesystem
        cy.get('.fancytree-node .fancytree-expander').last().click()

        // Select the 'dir' directory
        cy.get('.fancytree-node .fancytree-title:contains("dir")').first().click()

        cy.get('.btn-primary[value="submit"]').click()
    })

})