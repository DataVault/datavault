# DOCKER

Datavault is using Docker for several purpose:
* Run Integration tests on Travis
* Get a development environment easy to set-up
* Run Datavault on Amazon ECS

## Development

For development you can either use the main `docker-compose.yml` file which will keep everything contain and avoid mixed
up between your local environment and the datavault environment.

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
rebuild your project.

    sudo docker-compose up -d --build maven-build

and only rebuild the containers affected with the code change

    docker-compose up -d --build web

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

Integration tests require the database, rabbitMQ and Hashicorp Vault and a worker to run.

At the moment the test simply send request to RabbitMQ, observe the worker and verify that the tasks have been properly
executed. 

## Run Datavault on Amazon ECS

TO BE COMPLETED