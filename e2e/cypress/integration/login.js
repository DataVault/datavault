describe('Login page', function(){
    it('Error message', function() {
        cy.visit('/')

        cy.url()
            .should('include', '/auth/login')

        cy.get('input[name="username"]')
            .type('fake-user')
            .should('have.value', 'fake-user')

        cy.get('input[name="password"]')
            .type('fake-password')
            .should('have.value', 'fake-password')

        cy.get('input.btn')
            .click()

        cy.url()
            .should('include', '/auth/login?error=true')

        cy.get('div.alert-danger')
            .contains('Invalid username or password!')
    })

    it('Successful login', function() {
        cy.visit('/')

        cy.url()
            .should('include', '/auth/login')

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
})
