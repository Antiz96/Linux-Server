# PostgreSQL

https://www.postgresql.org/

## Install Docker on my Server (if not done already)

https://github.com/Antiz96/Server-Configuration/blob/main/Services/Docker.md

## Installing a PostgreSQL database on Docker

https://hub.docker.com/_/postgres/

### Creating the directories to store the PostgreSQL data and the PostgreSQL environment variables

*Unless you're okay using the default values, you'll need to specify the database name, database user and database user's password in the docker run command. Needless to say that those information are sensitive... The recommended way to treat sensitive information in a docker run command are [Docker Secrets](https://docs.docker.com/engine/swarm/secrets/) but, unfortunately, Docker Secrets can only be used with [Docker Swarm](https://www.sumologic.com/glossary/docker-swarm/) (which I don't use myself).*   
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
sudo mkdir /opt/postgres/backup && sudo chmod 750 /opt/postgres/backup #Creating the directory to store backup in with secure permissions
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
## Update/Upgrade and reinstall procedure

Since we use Docker, the update and upgrade procedure is actually the same as it does not rely directly on our server.

### Pull the docker image

*(... to check if there's available updates)*

```
sudo docker pull postgres
```

### Apply the update

```
sudo docker stop postgres
sudo docker rm postgres
sudo docker run -d --name postgres -p 5432:5432 -e POSTGRES_USER=$(sudo cat /opt/postgres/env/user) -e POSTGRES_PASSWORD=$(sudo cat /opt/postgres/env/password) -e POSTGRES_DB=$(sudo cat /opt/postgres/env/database) -v /opt/postgres/data:/var/lib/postgresql/data --restart=unless-stopped postgres
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
