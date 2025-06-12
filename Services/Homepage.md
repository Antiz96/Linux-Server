# Homepage

<https://github.com/gethomepage/homepage>

## Install docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Create dedicated directories

```bash
sudo mkdir -p /opt/homepage/{config,images}
```

## Pull and run the container

Modify the "HOMEPAGE_ALLOWED_HOSTS" environment variable to point to the server that will expose the Homepage service.

```bash
sudo docker run -d --name homepage \
  -e HOMEPAGE_ALLOWED_HOSTS=asprd01.rc:3000 \
  -e PUID=1000 \
  -e PGID=1000 \
  -p 3000:3000 \
  -v /opt/homepage/config:/app/config \
  -v /opt/homepage/images:/app/public/images \
  -v /var/run/docker.sock:/var/run/docker.sock:ro \
  --restart unless-stopped \
  ghcr.io/gethomepage/homepage:latest
```

## Post run config

Access the webpage to generate the config and then truncate the file I don't need:

```bash
sudo truncate -s 0 /opt/homepage/config/bookmarks.yaml
```

## Update procedure

```bash
sudo docker pull ghcr.io/gethomepage/homepage:latest
sudo docker stop homepage
sudo docker rm homepage
sudo docker run -d --name homepage \
  -e HOMEPAGE_ALLOWED_HOSTS=asprd01.rc:3000 \
  -e PUID=1000 \
  -e PGID=1000 \
  -p 3000:3000 \
  -v /opt/homepage/config:/app/config \
  -v /opt/homepage/images:/app/public/images \
  -v /var/run/docker.sock:/var/run/docker.sock:ro \
  --restart unless-stopped \
  ghcr.io/gethomepage/homepage:latest
```

## After an update

After an update, you can clean old dangling docker images (to regain spaces and clean up your local stored Docker images):

```bash
sudo docker image prune
```

Alternatively, you can clean all unused Docker component (stopped containers, network not use by any containers, dangling images and build cache):  
**If you choose to do that, make sure all your containers are running! Otherwise, they will be deleted.**  

```bash
sudo docker system prune
```
