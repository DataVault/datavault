DataVault - A long term archive for Research Data
=================================================

What
----
A Jisc-funded project to create an archive service for Research Data.  Funded under the 'Research at Risk' Data Spring programme.

Who
---
Originally developed by:

 * Tom Higgins - University of Manchester
 * Mary McDerby - University of Manchester
 * Robin Taylor - University of Edinburgh
 * Claire Knowles - University of Edinburgh
 * Stuart Lewis - University of Edinburgh

Further Information
-------------------

Project Blog: http://libraryblogs.is.ed.ac.uk/jiscdatavault/


Installation
------------

 *  Clone from Github: https://github.com/DataVault/datavault.git
 *  Install RabbitMQ: https://www.rabbitmq.com
 *  Install MySQL: https://www.mysql.com/
 *  Setup database and username to match those in build.properties
 *  Go into the data-vault home directory and mvn package
 *  Start up RabbitMQ - should get a healthy startup message
 *  RabbitMQ Browser admin tool instructions: https://www.rabbitmq.com/management.html
 *  Create a RabbitMQ user in the RabbitMQ admin tool with the username and password as defined in build.properties
 *  Start the worker by cd'ing to the worker target directory and running..
    java -cp datavault-worker-1.0-SNAPSHOT-jar-with-dependencies.jar org.datavault.worker.Main
 *  Deploy the datavault-webapp and default-broker to a webserver
 *  Start webserver