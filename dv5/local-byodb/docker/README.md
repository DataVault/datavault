The file `rabbitmq_definitions.json` holds the configuration of a RabbitMQ broker - it is loaded on container startup.

USERS

1. Admin       - `rabbit`/`twin2port`
2. Non-Admin   - `datavault`/`datavault`

QUEUES

1. `datavault` - this is the shared worker task Q from Broker to Workers. 3 Workers read from this Q.
2. `datavault-event` - this is the Q for events from Workers to Broker. 1 Broker reads from this Q.
3. `restart-worker-1` - this is a Q for restarts - from Broker to Worker-1
4. `restart-worker-2` - this is a Q for restarts - from Broker to Worker-2
5. `restart-worker-3` - this is a Q for restarts - from Broker to Worker-3

EXCHANGES

1. `restart-worker` is a fan-out exchange. Messages sent to this exchanges will be delivered to Qs `restart-worker-1`,`restart-worker-2` and `restart-worker-3`

ROUTING

A message sent to the Default Exchange with routing key - `datavault` will cause a message to be sent to `datavault` Q

A message sent to the Default Exchange with routing key - `datavault-event` will cause a message to be sent to `datavault-event` Q

A message sent to the `restart-worker` Exchange will cause a message to be sent to each of `restart-worker-1`, `restart-worker-2` and `restart-worker-3`.