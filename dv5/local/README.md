# To run DataVault on development machine with database as Docker container

To run DataVault-5 locally,
you’ll need 4 terminal windows. Assume you have `dv-5-webapp-broker-integration` branch checkout out. 
We’ll call the top level git directory `$DV5`

### Terminal 1 - starts LDAP/RABBIT/EMAIL  
```
$ cd $DV5/dv5/local/docker
$ ./docker_up.sh
```

### Terminal 2 - for Broker  (runs on port 8080 - debug port 5005)
```
$ cd $DV5/dv5/local/scripts
$ ./runLocalBroker.sh
```

### Terminal 3 - for the Webapp (runs on port 8888 - debug port 5050)
```
$ cd $DV5/dv5/local-byodb/scripts
$ ./runLocalWebapp.sh
```

### Terminal 4 - for the Worker (runs on port 9090 - debug port 5555)
```
$ cd $DV5/dv5/local/scripts
$ ./runLocalWorker.sh
```
