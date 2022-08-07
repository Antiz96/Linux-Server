# Zabbix

https://www.zabbix.com/

## Install Docker on my Server (if not done already)

https://github.com/Antiz96/Server-Configuration/blob/main/Services/Docker.md

## Installing a PostgreSQL database for Zabbix on Docker

Zabbix needs a database to store its data on.  
Zabbix is compatible with both MySQL and PostgreSQL.  
  
I personally use PostreSQL inside a docker container.
https://hub.docker.com/_/postgres/

### Creating the directories to store the PostgreSQL data and the PostgreSQL environment variables

*Unless you're okay using the default values, you'll need to specify the database name, database user and database user's password in the docker run command. Needless to say that those information are sensitive... The recommanded way to treat sensitive information in a docker run command are [Docker Secrets](https://docs.docker.com/engine/swarm/secrets/) but, unfortunately, Docker Secrets can only be used with [Docker Swarm](https://www.sumologic.com/glossary/docker-swarm/) (which I don't use myself).*   
*So, in order to avoid typing those information as plain text in the docker run command, I'm going to create my own (kind of) secret files inside the 'env' directory that contains those information. For security reasons, those files will only be viewable and editable by the root account, obviously.*  

```
sudo mkdir -p /opt/postgres/{data,env}
```

### Editing the env files

```
sudo vim /opt/postgres/env/user
```
> username  
    
```
sudo vim /opt/postgres/env/password
```
> password  
  
```
sudo vim /opt/postgres/env/database
```
> database_name  

```
sudo chmod 600 /opt/postgres/env/* && sudo chmod 750 /opt/postgres/env
```

### Pull and run the container

```
sudo docker run -d --name postgres -p 5432:5432 -e POSTGRES_USER=$(sudo cat /opt/postgres/env/user) -e POSTGRES_PASSWORD=$(sudo cat /opt/postgres/env/password) -e POSTGRES_DB=$(sudo cat /opt/postgres/env/database) -v /opt/postgres/data:/var/lib/postgresql/data --restart=unless-stopped postgres
```

### Set an automatic backup of the database (optional) 

```
sudo mkdir /opt/postgres/backup #Creating the directory to store backup in
sudo docker exec -t postgres pg_dumpall -c -U $(sudo cat /opt/postgres/env/user) -l $(sudo cat /opt/postgres/env/database) | sudo tee /opt/postgres/backup/dump_$(date +%d-%m-%Y).sql > /dev/null #Performing a dump manually
```

There's multiple ways to automate that backup process. You can simply put the above command in a script and/or a cronjob for instance (*be careful with the use of sudo in cronjob. Either use the root account crontab or set sudo on NOPASSWD for that script **with secure permissions**, otherwise the cronjob won't be able to execute as it will wait for you to type your password*).  
  
Personally, I use an Ansible Playbook that does the dump and delete every dump older than 7 days.  
This Ansible Playbook is launched automatically each day by my Jenkins instance so it performs one dump a day and keep 7 days of dump.  
You can see that Ansible Playbook [here](https://github.com/Antiz96/Server-Configuration/blob/main/Ansible-Playbooks/server/roles/dump_zabbix_db/tasks/main.yml)  
  
To restore a dump, you can use the following command :

```
cat "path_to_the_dump" | sudo docker exec -i postgres psql -U "username" -l "database"
```

## Installing Zabbix-Server on Docker

There are 2 different docker images for Zabbix-Server, using either MySQL or PostgreSQL.  
Use the one according to your database :  
https://hub.docker.com/r/zabbix/zabbix-server-mysql  
https://hub.docker.com/r/zabbix/zabbix-server-pgsql  
  
Each docker images have several tags, all pointing to a different version and/or a different base OS (Alpine, Ubuntu or Oracle-Linux).  
You can see all available tags in the links above.  
  
I'm personally using the "latest" tag that points to the latest stable Zabbix-Server version based on Alpine.  

### Pull and run the container 

```
sudo docker run --name zabbix-server -p 10051:10051 -e DB_SERVER_HOST=$(hostname) -e POSTGRES_USER=$(sudo cat /opt/postgres/env/user) -e POSTGRES_PASSWORD=$(sudo cat /opt/postgres/env/password) -e POSTGRES_DB=$(sudo cat /opt/postgres/env/database) --restart=unless-stopped -d zabbix/zabbix-server-pgsql:latest 
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
sudo docker run --name zabbix-web -p 8080:8080 -e DB_SERVER_HOST=$(hostname) -e POSTGRES_USER=$(sudo cat /opt/postgres/env/user) -e POSTGRES_PASSWORD=$(sudo cat /opt/postgres/env/password) -e POSTGRES_DB=$(sudo cat /opt/postgres/env/database) -e ZBX_SERVER_HOST=$(hostname) -e PHP_TZ="Europe/Paris" --restart=unless-stopped -d zabbix/zabbix-web-nginx-pgsql:latest
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
sudo docker pull postgres
sudo docker pull zabbix/zabbix-server-pgsql:latest
sudo docker pull zabbix/zabbix-web-nginx-pgsql:latest 
```

### Apply the update

#### Postgres

```
sudo docker stop postgres
sudo docker rm postgres
sudo docker run -d --name postgres -p 5432:5432 -e POSTGRES_USER=$(sudo cat /opt/postgres/env/user) -e POSTGRES_PASSWORD=$(sudo cat /opt/postgres/env/password) -e POSTGRES_DB=$(sudo cat /opt/postgres/env/database) -v /opt/postgres/data:/var/lib/postgresql/data --restart=unless-stopped postgres
```

#### Zabbix-Server

```
sudo docker stop zabbix-server
sudo docker rm zabbix-server
sudo docker run --name zabbix-server -p 10051:10051 -e DB_SERVER_HOST=$(hostname) -e POSTGRES_USER=$(sudo cat /opt/postgres/env/user) -e POSTGRES_PASSWORD=$(sudo cat /opt/postgres/env/passwor
d) -e POSTGRES_DB=$(sudo cat /opt/postgres/env/database) --restart=unless-stopped -d zabbix/zabbix-server-pgsql:latest
```

#### Zabbix-Web-Interface

```
sudo docker stop zabbix-web
sudo docker rm zabbix-web
sudo docker run --name zabbix-web -p 8080:8080 -e DB_SERVER_HOST=$(hostname) -e POSTGRES_USER=$(sudo cat /opt/postgres/env/user) -e POSTGRES_PASSWORD=$(sudo cat /opt/postgres/env/password) -e
 POSTGRES_DB=$(sudo cat /opt/postgres/env/database) -e ZBX_SERVER_HOST=$(hostname) -e PHP_TZ="Europe/Paris" --restart=unless-stopped -d zabbix/zabbix-web-nginx-pgsql:latest
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
