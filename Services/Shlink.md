# Shlink

<https://shlink.io/>

Shlink is a self-hosted URL shortener service.

## Install Docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Installing Shlink on Docker

<https://shlink.io/documentation/install-docker-image/>  

### Pull and run the containers

#### Shlink Server

I'm personally using the embedded SQLite database but Shlink also supports MySQL, MariaDB, PostgreSQL or Microsoft SQL Server databases, see <https://shlink.io/documentation/install-docker-image/#use-an-external-db>.  
For a full list of supported environment variables for the docker image, see <https://shlink.io/documentation/install-docker-image/#use-an-external-db>.

```bash
sudo docker run -d --restart="unless-stopped" \
    --name shlink \
    -p 8080:8080 \
    -e DEFAULT_DOMAIN=s.antiz.fr \
    -e IS_HTTPS_ENABLED=true \
    -e DISABLE_TRACKING=true \
    shlinkio/shlink:stable
```

#### Shlink WebUI

Shlink optionally has a WebUI allowing to create, delete, manage short links (as well as the server itself) via a WebUI.

```bash
sudo docker run -d --restart="unless-stopped" \
    --name shlink-web \
    -p 8081:8080 \
    shlinkio/shlink-web-client:stable
```

#### Access Shlink WebUI

You can now access shlink WebUI at `http://[HOSTNAME]:8080`

## Update/Upgrade procedure

### Update Shlink Server

```bash
sudo docker pull shlinkio/shlink:stable
sudo docker stop shlink
sudo docker rm shlink
sudo docker run -d --restart="unless-stopped" \
    --name shlink \
    -p 8080:8080 \
    -e DEFAULT_DOMAIN=s.antiz.fr \
    -e IS_HTTPS_ENABLED=true \
    -e DISABLE_TRACKING=true \
    shlinkio/shlink:stable
```

### Update Shlink WebUI

```bash
sudo docker pull shlinkio/shlink-web-client:stable
sudo docker stop shlink-web
sudo docker rm shlink-web
sudo docker run -d --restart="unless-stopped" \
    --name shlink-web \
    -p 8081:8080 \
    shlinkio/shlink-web-client:stable
```

### After an update

After an update, you can clean old dangling docker images (to regain spaces and clean up your local stored Docker images):

```bash
sudo docker image prune
```

Alternatively, you can clean all unused Docker component (stopped containers, network not use by any containers, dangling images and build cache):  
**If you choose to do that, make sure all your containers are running! Otherwise, they will be deleted.**

```bash
sudo docker system prune
```
