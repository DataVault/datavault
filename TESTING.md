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

To run them against localhost, run:

    mvn verify -P functional-test

By default, the tests run against localhost:58080 (the URL specified in `docker-compose.yml`) and use Firefox, but you can run them against a remote server with the `test.url` property, e.g.:

    mvn verify -P functional-test -Dtest.url=http://webapp:8080

You can also run them with a different browser using the `test.selenium.browser` property (currently only `firefox` and `chrome` are supported), e.g.:

    mvn verify -P functional-test -Dtest.selenium.browser=chrome

The tests require you to have Selenium & your browser installed locally.
If you don't/can't have this, you can also use a remote Selenium server using the `test.selenium.driver` property.
Note that if you're using a remote Selenium server, `localhost` would resolve to the Selenium server, so you will also need to specify a different `test.url`.
For example, you can run a standalone Selenium server for both browsers with:

    docker run -d -p 4444:4444 --shm-size 2g selenium/standalone-firefox # (the --shm-size is a known workaround to avoid the browser crashing, you may encounter errors if you don't use it)
    docker run -d -p 4445:4444 --shm-size 2g selenium/standalone-chrome 

Then run the tests with:

    mvn verify -P functional-test -Dtest.selenium.driver.url=http://localhost:4444/wd/hub -Dtest.url=http://webapp:8080 -Dtest.selenium.browser=firefox
    mvn verify -P functional-test -Dtest.selenium.driver.url=http://localhost:4445/wd/hub -Dtest.url=http://webapp:8080 -Dtest.selenium.browser=chrome

Failed tests will take screenshots of the state at the point of failure.
These can be found in `datavault-webapp/target/surefire-reports/errorScreenshots/`.

## Test failures

Note that the functional tests may generate some errors, e.g. :

* tests that fail due to operations taking longer than normal
* tests that fail if the environment is not setup correctly
* tests that fail on specific browsers

Unfortunately, this requires a degree of interpretation as to whether a test failure is indicative of a problem ( and for that reason, we don't run them as part of the CI process).

