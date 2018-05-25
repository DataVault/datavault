There are three different types of tests for DataVault:

* Unit tests (the vast majority of the tests)
* Integration tests, 
* Functional tests

All three can be run via Maven, but in slightly different ways

# Running unit tests

Run:

    mvn test

# Running integration tests

Integration tests have to be run after you have deployed the application on your local machine (it's not possible at present to run them against a remote server).
To run them, first deploy the application (using `docker-compose.yml` or any other way), then run:

    mvn verify -P integration-test

# Running functional tests

Functional tests can be run against any deployed instance of the application, locally or otherwise.
They require you to first deploy and configure the application.
Note that they will make persistent changes, so if this is a problem you'll need to be prepared to delete data afterwards.
They should be written in such a way that they can cope with differences in the existing state, e.g. a test may do different things depending on whether the user had previously set up a vault or not, but should pass regardless.
To run them against localhost, run:

    mvn verify -P functional-test

The tests require you to have Selenium & Firefox installed locally.
If you don't/can't have this, you can also use a remote Selenium server using the `test.selenium.driver` property.
For example, you can run a standalone Selenium server with:

    docker run -d -p 4444:4444 selenium/standalone-firefox

Then run the tests with:

    mvn verify -P functional-test -Dtest.selenium.driver.url=http://localhost:4444/wd/hub

By default, the tests run against localhost:58080 (the URL specified in `docker-compose.yml`), but you can run them against a remote server with the `test.url` property, e.g.:

    mvn verify -P functional-test -Dtest.url=http://dlib-dumpling.edina.ac.uk

