### DataVault RabbitMQ scripts

## General Notes
These scripts are added to aid development - they are not required as
part of the release of DataVault to a managed environment like 'production'.

When the scripts interrogate the state of RabbitMQ - they data may be out of date by a few seconds.

## General Requirements
* You must have python3 on your path (rabbitmqadmin is a python script)
* When you run the *\*.sh* commands, RABBIT_PASS must be defined the environment for the RabbitMQ password.

## DataVault Queues

* **datavault** - messages for the worker to process
* **datavault-event** - messages about what the worker has done - for the broker
* **restart-worker-1** - restart messages for tasks that are in progress on worker 1
* **restart-worker-2** - restart messages for tasks that are in progress on worker 2
* **restart-worker-3** - restart messages for tasks that are in progress on worker 3

## RabbitMQ Scripts

*All RabbitMQ Scripts currently assume that...*
* Host is **localhost**
* Port is **5672**
* VirtualHost is **/**
* Username is **rabbit**
* Password passed in via **RABBIT_PASS** environmental variable 

List of Scripts...

* **createQueues.sh** - creates the 2 RabbitMQ queues
* **listQueues.sh** - lists all the RabbitMQ queues
* **deleteQueues.sh** - deletes the 2 RabbitMQ queues
* **purgeEventQueue.sh** - removes all messages from RabbitMQ queue - **datavault-event**
* **purgeWorkerQueue.sh** - removes all messages from RabbitMQ queue - **datavault**
* **sendWorkerShutdownMessage.sh** - sends a hi-priority shutdown message to RabbitMQ queue - **davault**
* **setup.sh** - used by other scripts to setup/check credentials etc

## rabbitmqadmin 

See https://www.rabbitmq.com/management-cli.html on how to get this python script.

