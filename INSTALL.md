@@ -0,0 +1,48 @@
# Data Vault Installation instructions (Debian/Ubuntu)

## Pre-requisites

### Java (1.7)
```
sudo apt-get install openjdk-7-jdk
```

### Maven
```
sudo apt-get install maven
```

### Git
```
sudo apt-get install git
```

### MySQL
```
# Install package
sudo apt-get install mysql-server

# Set root password
...

# Configure database
...
```

### RabbitMQ
```
# Add the RabbitMQ repository and signing key to the package manager
# See: https://www.rabbitmq.com/install-debian.html
echo 'deb http://www.rabbitmq.com/debian/ testing main' | sudo tee /etc/apt/sources.list.d/rabbitmq.list
wget -O- https://www.rabbitmq.com/rabbitmq-release-signing-key.asc | sudo apt-key add -
sudo apt-get update

# Install packages
sudo apt-get install rabbitmq-server

# Configure RabbitMQ users
...
```

### Apache & Tomcat
```
# Install packages
sudo apt-get install tomcat7-admin

# Start tomcat
sudo /etc/init.d/tomcat7 start
```

## Data Vault

### Download and installation
```
...
```

### Setting up the Data Vault home directory
```
...
```

### Configuration
```
...
```

### Properties

| Name | Description |
| ------------- | ------------- |
| Abc  | 123  |
| Def  | 456  |
