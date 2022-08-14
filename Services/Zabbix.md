# Zabbix

https://www.zabbix.com/

## Install Docker on my Server (if not done already)

https://github.com/Antiz96/Server-Configuration/blob/main/Services/Docker.md

## Zabbix database 

Zabbix needs a database to store its data on.  
Zabbix is compatible with both MySQL and PostgreSQL.  
  
I personally use PostgreSQL inside a docker container.
You can read my install procedure of PostgreSQL on docker here : https://github.com/Antiz96/Server-Configuration/blob/main/Services/PostgreSQL.md

## Installing Zabbix-Server on Docker

There are 2 different docker images for Zabbix-Server, using either MySQL or PostgreSQL.  
Use the one according to your database :  
https://hub.docker.com/r/zabbix/zabbix-server-mysql  
https://hub.docker.com/r/zabbix/zabbix-server-pgsql  
  
Each docker images have several tags, all pointing to a different version and/or a different base OS (Alpine, Ubuntu or Oracle-Linux).  
You can see all available tags in the links above.  
  
I'm personally using the "latest" tag that points to the latest stable Zabbix-Server version based on Alpine.  

### Create the env files

*You'll need to specify the hostname of your database, its user and its password in the docker run command. Needless to say that those information are sensitive... The recommended way to treat sensitive information in a docker run command are [Docker Secrets](https://docs.docker.com/engine/swarm/secrets/) but, unfortunately, Docker Secrets can only be used with [Docker Swarm](https://www.sumologic.com/glossary/docker-swarm/) (which I don't use myself).*
*So, in order to avoid typing those information as plain text in the docker run command, I'm going to create my own (kind of) secret files inside the 'env' directory that contains those information. For security reasons, those files will only be viewable and editable by the root account, obviously.*

```
sudo mkdir -p /opt/zabbix/env
```

### Editing the env files

```
sudo vim /opt/zabbix/env/db_host
```
> hostname_of_the_database_server

```
sudo vim /opt/zabbix/env/db_user
```
> username

```
sudo vim /opt/zabbix/env/db_password
```
> password

```
sudo vim /opt/zabbix/env/db_name
```
> database_name

```
sudo chmod 600 /opt/zabbix/env/* && sudo chmod 750 /opt/zabbix/env
```

### Pull and run the container 

```
sudo docker run --name zabbix-server -p 10051:10051 --hostname=$(hostname) -e DB_SERVER_HOST=$(sudo cat /opt/zabbix/env/db_host) -e POSTGRES_USER=$(sudo cat /opt/zabbix/env/db_user) -e POSTGRES_PASSWORD=$(sudo cat /opt/zabbix/env/db_password) -e POSTGRES_DB=$(sudo cat /opt/zabbix/env/db_name) --restart=unless-stopped -d zabbix/zabbix-server-pgsql:latest 
```

## Installing Zabbix Web Frontend/Interface

Installing Zabbix-Server is not enough on its own.  
You also need to install the Zabbix Web interface to be able to access and configure the Zabbix Server through it.  
  
There are several docker images for Zabbix Web Interface, using either Apache or Nginx and MySQL or PostgreSQL.  
Use the one according to your database, Nginx or Apache is just a matter of preferences :   
https://hub.docker.com/r/zabbix/zabbix-web-apache-pgsql  
https://hub.docker.com/r/zabbix/zabbix-web-nginx-pgsql  
https://hub.docker.com/r/zabbix/zabbix-web-apache-mysql  
https://hub.docker.com/r/zabbix/zabbix-web-nginx-mysql  
  
Each docker images have several tags, all pointing to a different version and/or a different base OS (Alpine, Ubuntu or Oracle-Linux).  
You should use the exact same tag you used for the Zabbix-Server container.  
  
I'm personally using the NGINX/PGSQL docker image with "latest" tag that points to the latest stable Zabbix-Web-Interface version based on Alpine.  

### Pull and run the container

Change the "PHP_TZ" env variable's value according to your location/environment.

```
sudo docker run --name zabbix-web -p 8080:8080 -e DB_SERVER_HOST=$(sudo cat /opt/zabbix/env/db_host) -e POSTGRES_USER=$(sudo cat /opt/zabbix/env/db_user) -e POSTGRES_PASSWORD=$(sudo cat /opt/zabbix/env/db_password) -e POSTGRES_DB=$(sudo cat /opt/zabbix/env/dn_name) -e ZBX_SERVER_HOST=$(hostname) -e PHP_TZ="Europe/Paris" --restart=unless-stopped -d zabbix/zabbix-web-nginx-pgsql:latest
```

### Access

You can now access Zabbix on this URL :  
`http://[HOSTNAME]:8080/`

Default credentials : Admin:zabbix

## Update/Upgrade and reinstall procedure

Since we use Docker, the update and upgrade procedure is actually the same as it does not rely directly on our server.  

### Pull the docker images 

*(... to check if there's available updates)*  

```
sudo docker pull zabbix/zabbix-server-pgsql:latest
sudo docker pull zabbix/zabbix-web-nginx-pgsql:latest 
```

### Apply the update

#### Zabbix-Server

```
sudo docker stop zabbix-server
sudo docker rm zabbix-server
sudo docker run --name zabbix-server -p 10051:10051 --hostname=$(hostname) -e DB_SERVER_HOST=$(sudo cat /opt/zabbix/env/db_host) -e POSTGRES_USER=$(sudo cat /opt/zabbix/env/db_user) -e POSTGRES_PASSWORD=$(sudo cat /opt/zabbix/env/db_password) -e POSTGRES_DB=$(sudo cat /opt/zabbix/env/db_name) --restart=unless-stopped -d zabbix/zabbix-server-pgsql:latest
```

#### Zabbix-Web-Interface

```
sudo docker stop zabbix-web
sudo docker rm zabbix-web
sudo docker run --name zabbix-web -p 8080:8080 -e DB_SERVER_HOST=$(sudo cat /opt/zabbix/env/db_host) -e POSTGRES_USER=$(sudo cat /opt/zabbix/env/db_user) -e POSTGRES_PASSWORD=$(sudo cat /opt/zabbix/env/db_password) -e POSTGRES_DB=$(sudo cat /opt/zabbix/env/db_name) -e ZBX_SERVER_HOST=$(hostname) -e PHP_TZ="Europe/Paris" --restart=unless-stopped -d zabbix/zabbix-web-nginx-pgsql:latest
```

### After an update 

After an update, you can clean old dangling docker images (to regain spaces and clean up your local stored Docker images) :  

```
sudo docker image prune
```

Alternatively, you can clean all unused Docker component (stopped containers, network not use by any containers, dangling images and build cache) :  
**If you choose to do that, make sure all your containers are running ! Otherwise, they will be deleted**

```
sudo docker system prune
```
