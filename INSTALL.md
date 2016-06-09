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
# Start MySQL shell session
mysql -i -u root -p

# Create a new database user and database
CREATE USER 'datavault'@'localhost' IDENTIFIED BY 'datavault';
GRANT ALL PRIVILEGES ON *.* TO 'datavault'@'localhost';
CREATE DATABASE datavault;
EXIT;
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

### Download and build the Data Vault code
```
git clone https://github.com/DataVault/datavault.git
cd datavault
export MAVEN_OPTS="-Xmx1024m"
mvn package
```

### Configure the Data Vault home directory
```
# Copy the generated datavault home directory
cd ~/datavault
sudo cp -R datavault-assembly/target/datavault-assembly-1.0-SNAPSHOT-assembly/datavault-home /opt/datavault

# change ownership of the home directory
sudo chown -R www-data /opt/datavault

# set read/write permissions on the logging directory
sudo chmod 666 /opt/datavault/logs

# Set the $DATAVAULT_HOME environment variable (for the current shell user)
# add the following line to your ~/.profile
# export DATAVAULT_HOME=/opt/datavault

# Set the $DATAVAULT_HOME environment variable (for tomcat)
# add the following line to /usr/share/tomcat7/bin/setenv.sh
# export DATAVAULT_HOME=/opt/datavault
```

### Configuration (datavault.properties)
```
# edit the $DATAVAULT_HOME/config/datavault.properties file
# each property is described in the table below
```

| Name | Description |
| ------------- | ------------- |
| Abc  | 123  |
| Def  | 456  |

### Deploy web applications
```
# deploy generated war files to the tomcat directory
cd ~/datavault

# Deploy the broker (REST API)
sudo cp datavault-broker/target/datavault-broker.war /var/lib/tomcat7/webapps

# Deploy the sample web application
sudo cp datavault-webapp/target/datavault-webapp.war /var/lib/tomcat7/webapps/ROOT.war
```

### Start the worker processes
```
cd ~/datavault/datavault-worker/target
nohup java -cp datavault-worker-1.0-SNAPSHOT.jar:./* org.datavaultplatform.worker.WorkerManager > /dev/null 2>&1 &
```

### Other considerations
* HTTPS (for both tomcat and RabbitMQ connections)
* Shibboleth authentication
* SFTP and storage configuration
