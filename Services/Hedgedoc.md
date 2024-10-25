# Hedgedoc

<https://hedgedoc.org/>

## Install docker on my server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Installing Hedgedoc on Docker

<https://github.com/hedgedoc/container>

*Note that I'm using `sqlite` as the database for my own simple need. For production usage, you should consider using another database engine such as postgreSQL or MariaDB.*

### Create a local folder for persistent database

```bash
sudo mkdir -p /opt/hedgedoc/{config,db}
sudo chown antiz: /opt/hedgedoc/{config,db} && chmod 700 /opt/hedgedoc/{config,db}
```

### Run the container

See the available parameters for the `docker run` command at <https://hub.docker.com/r/linuxserver/hedgedoc>.  
See the available environment variable you can pass with `-e` at <https://docs.hedgedoc.org/configuration/>.

Modify the parameters of the following command according to your needs/environment:

```bash
sudo docker run -d \
--name=hedgedoc \
-e PUID=$(id -u) \
-e PGID=$(id -g) \
-e TZ=Europe/Paris \
-e CMD_DB_URL=sqlite:///app/hedgedoc/db/database.sqlite \
-e CMD_DOMAIN=hedgedoc.home-infra.rc \
-e CMD_PROTOCOL_USESSL=true \
-e CMD_ALLOW_EMAIL_REGISTER=false \
-e CMD_ALLOW_ANONYMOUS=false \
-v /opt/hedgedoc/db:/app/hedgedoc/db \
-v /opt/hedgedoc/config:/config \
-p 3000:3000 \
--restart unless-stopped \
lscr.io/linuxserver/hedgedoc:latest
```

### Access

You can now access and configure Hedgedoc on this URL:  
<http://[HOSTNAME]:3000/>

## Update/Upgrade procedure

```bash
sudo docker lscr.io/linuxserver/hedgedoc:latest
sudo docker stop hedgedoc
sudo docker rm hedgedoc
sudo docker run -d \
--name=hedgedoc \
-e PUID=$(id -u) \
-e PGID=$(id -g) \
-e TZ=Europe/Paris \
-e CMD_DB_URL=sqlite:///app/hedgedoc/db/database.sqlite \
-e CMD_DOMAIN=hedgedoc.home-infra.rc \
-e CMD_PROTOCOL_USESSL=true \
-e CMD_ALLOW_EMAIL_REGISTER=false \
-e CMD_ALLOW_ANONYMOUS=false \
-v /opt/hedgedoc/db:/app/hedgedoc/db \
-v /opt/hedgedoc/config:/config \
-p 3000:3000 \
--restart unless-stopped \
lscr.io/linuxserver/hedgedoc:latest
```

### After any update

After an update, you can clean old dangling docker images (to regain spaces and clean up your local stored Docker images):

```bash
sudo docker image prune
```

Alternatively, you can clean all unused Docker component (stopped containers, network not use by any containers, dangling images and build cache):  
**If you choose to do that, make sure all your containers are running! Otherwise, they will be deleted.**

```bash
sudo docker system prune
```
