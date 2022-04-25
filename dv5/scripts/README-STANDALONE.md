# DataVault with Spring Boot

## Java
Use java 8

## Using Maven Wrapper

#### compile, test and install jars into local m2 repo
```
./mvnw clean install
```

#### Running the WebApp on http://localhost:8080
This script runs the DataVault Web App in 'standalone' mode for testing Spring and Spring Security Configuration
```
./runWebAppStandalone.sh
```

#### Attempt to access a protected resource


In your browser, navigate to http://localhost:8080/secure, it should redirect you to the 
datavault login page.

#### username : 'user'

You will login as the user 'user', password 'password'. To get the password look through the console output of the
running webapp for something like...

Once you are logged in, you should be back on http://localhost:8080/secure.

The screen should say something like...
* SECURE PAGE
* Logout (link)
* logged in as [user]

Number of Authorities [1]
1. ROLE_USER

If you click the logout link, you should end up at http://localhost:8080/auth/confirmation page. 
The confirmation page should say something like...
* To logout of the DataVault you must close your browser, in order to end the browser session, so that you will be logged out of EASE.

If you navigate now to http://localhost:8080/index - the page should say ...
* INDEX PAGE
* NOT logged in
* LOGIN (link)

If you login again, you should be taken back to the http://localhost:8080/ page which should say...
* INDEX PAGE
* Logout (link)
* logged in as [user]

Number of Authorities [1]
1. ROLE_USER

#### username : 'admin'
You can also login as 'admin'/'admin' - the 'admin' user has ROLE_USER and ROLE_ADMIN
