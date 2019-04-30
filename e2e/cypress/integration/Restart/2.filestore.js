describe('Filestore Locations Page', function(){
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
})