# Cypress Tests

## Run Cypress tests locally

### Install Cypress

You need to install [Cypress](https://docs.cypress.io/guides/getting-started/installing-cypress.html)
under the ./e2e.

```shell
cd ./e2e/
npm install cypress --save-dev
```

This should create a 'node_modules' directory with all libraries required for cypress.

### Run Test Runner

Once install you can start the Test Runner with the following command:

```shell
./node_modules/.bin/cypress open
```

You should find all information about how to use the Test Runner and write test [online](https://docs.cypress.io/guides/overview/why-cypress.html#In-a-nutshell)

## Run Cypress inside the docker container

We've added a Dockerfile which will run the test inside a container. This is what the Travis CI is using.

```shell
docker-compose -f docker-compose-test.yml up --build e2e
```

You'll find videos and screenshots of the test under `./docker/cypress/`
