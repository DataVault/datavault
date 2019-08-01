# DOCKER

Datavault is using Docker for several purpose:
* Run Integration tests on Travis
* Get a development environment easy to set-up
* Run Datavault on Amazon ECS

## Development

For development you can either use the main `docker-compose.yml` file which will keep everything contained 
and avoid mixing configurations between your local environment and the datavault environment.

N.B.: You'll notice that some commands require `sudo`, this is only because the `mysql` container has some file in the
shared volume that can only be access by using `sudo`. This can normally be avoid by running the following command:

    sudo chown -R $USER:$USER ./docker

### Maven build container

We use a separate `maven-build` container that will run the maven command to build our app. This then can will be copy 
on other containers to get the different sub-project (i.e. worker, broker and webapp) running.

    sudo docker-compose build maven-build

This container can also be used to run unit test:

    docker-compose run maven-build mvn package
    
### External services

Datavault requires several services to be running:
* MySQL
* RabbitMQ
* Hashicorp Vault (only in dev and test for the moment)

They can all be started with a single command:

    docker-compose up -d rabbitmq mysql vault vault-administration

`vault-administration` will just configure our Hashicorp Vault server and stop it can be deleted afterward:

    docker-compose rm vault-administration

### Start Datavault

Now we can start Datavault services one by one or all together:

    docker-compose up -d --build web broker workers

### Redeploy

Once you are done developping awesome features for datavault, you don't need to restart every containers, you can just
rebuild the container you need.

    docker-compose up -d --build web

This should automatically trigger a maven build of the new code in the `maven-build` container and update the web container.

### Clean up

At some occasion you might want to clean up your docker environment and restart from clean containers. 
Docker Compose let you do that with the `down` command:

    docker-compose down --remove-orphans --rmi all --volumes

This will remove every images, volumes and containers created.

### Volumes

You might want to use volume to avoid rebuilding from scratch a container and keep the `m2` repository. 
For this purpose, there is also a `docker-compose-dev.yml` file using extra volumes on each container. 
In order to use it you need to add `-f docker-compose-dev.yml` on each command, for example:

    sudo docker-compose -f docker-compose-dev.yml up -d --build maven-build

Only use this file if you know what you are doing as it hasn't been optimised.

## Integration test on Travis

In the `.travis.yml`, we use the docker service in order to run our integration test.

Integration tests only require the database, rabbitMQ and Hashicorp Vault and a worker to run.

At the moment the test simply send request to RabbitMQ, observe the worker and verify that the tasks have been properly
executed. 

## Run Datavault on Amazon ECS

TO BE COMPLETED

## Setup SFTP container

We have a container to simulate the SFTP connection to Datastore, at the moment it only work with `user1`.

You can check the logs with the the following command
```bash
docker-compose logs -f sftp
```

In order to use it you'll need to create a new SFTP Location for `user1` with the following details:

| Hostname | Port | Path    |
|----------|------|---------|
| sftp     | 22   | /upload |

Then you'll have to copy the public key into `docker/config/.ssh/authorized_keys` as root (the file needs to be own by user 1001)

```bash
sudo echo %KEY >> docker/config/.ssh/authorized_keys
```

Once done, you should be able to create a new deposit from the file location created.

In case of issue:
* Make sure the `docker/.ssh` folder is own by 1001 and is only readable

```bash
sudo chown -R 1001:1001 docker/config/.ssh
sudo chmod -R 700 docker/config/.ssh
sudo chmod 600 docker/config/.ssh/authorized_keys
```