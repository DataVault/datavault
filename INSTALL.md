@@ -0,0 +1,48 @@
# Data Vault installation guide (Debian/Ubuntu)

## Prerequisites

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

# Set a root password when prompted by the installer.
```

### Configure MySQL database
```
# Start an MySQL shell session
mysql -i -u root -p

# Create a new database user and database
CREATE USER 'datavault'@'localhost' IDENTIFIED BY 'datavault';
GRANT ALL PRIVILEGES ON *.* TO 'datavault'@'localhost';
CREATE DATABASE datavault;
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
sudo rabbitmqctl add_user datavault datavault
sudo rabbitmqctl set_user_tags datavault administrator
sudo rabbitmqctl set_permissions -p / datavault ".*" ".*" ".*"
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
