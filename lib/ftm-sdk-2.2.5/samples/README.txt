
Instructions to build the sample files:
---------------------------------------
1. To build all the sample programs, run the following command where JDK 1.7 (or later) is installed:

    cd samples/java
    javac -cp ../../libs/javax.json-1.0.4.jar:../../libs/log4j-1.2.17.jar:../../libs/slf4j-log4j12-1.7.7.jar:../../libs/slf4j-api-1.7.7.jar:../../libs/low-level-api-core-1.14.9.jar:../../libs/ftm-api-2.2.1.jar:. oracle/cloudstorage/ftm/samples/*.java

2. Make a copy of oscs-demo-account.properties as 'my-account.properties' and update this file with all cloud account information.

3. Modify the sample programs as required e.g. for object names, container names etc.

4. Run the sample program as follows:

java -cp ../../libs/javax.json-1.0.4.jar:../../libs/log4j-1.2.17.jar:../../libs/slf4j-log4j12-1.7.7.jar:../../libs/slf4j-api-1.7.7.jar:../../libs/low-level-api-core-1.14.9.jar:../../libs/ftm-api-2.2.1.jar:. oracle.cloudstorage.ftm.samples.UploadFileDemo
