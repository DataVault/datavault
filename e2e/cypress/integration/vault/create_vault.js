describe('Vault page', function(){
    before(function() {
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
    })

    it('Check required fields', function() {
        cy.url()
            .should('include', '/vaults')

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
})