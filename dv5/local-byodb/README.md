# To run DataVault on development machine with existing database

To run DataVault-5 locally against an existing, running database,
you’ll need 4 terminal windows. Assume you have `dv-5-webapp-broker-integration` branch checkout out. 
We’ll call the top level git directory `$DV5`

 *  byodb -> bring your own database

### Terminal 1 - starts LDAP/RABBIT/EMAIL  
```
$ cd $DV5/dv5/local-byodb/docker
$ ./docker_up.sh
```

### Terminal 2 - for Broker  (runs on port 8080 - debug port 5005)
You need to edit `$DV5/dv5/local-byodb/scripts/runLocalByodbBroker.sh`

For the db details, edit these 3 lines...

```
SPRING_DATASOURCE_USERNAME=<USERNAME> \
SPRING_DATASOURCE_PASSWORD=<PASSWORD> \
SPRING_DATASOURCE_URL=<JDBC URL> \
```

Once you’ve entered the db details…

```
$ cd $DV5/dv5/local-byodb/scripts
$ ./runLocalByodbBroker.sh
```

### Terminal 3 - for the Webapp (runs on port 8888 - debug port 5050)
```
$ cd $DV5/dv5/local-byodb/scripts
$ ./runLocalByodbWebapp.sh
```

### Terminal 4 - for the Worker (runs on port 9090 - debug port 5555)
```
$ cd $DV5/dv5/local-byodb/scripts
$ ./runLocalByodbWorker.sh
```
