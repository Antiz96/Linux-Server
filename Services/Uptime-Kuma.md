# Uptime Kuma

<https://uptime.kuma.pet/>

## Install Docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Installing Uptime Kuma on Docker

### Create a directory for persistent data

```bash
sudo mkdir /opt/uptime-kuma
```

### Download and launch the docker container

```bash
sudo docker run -d --restart=unless-stopped -p 3001:3001 -v /opt/uptime-kuma:/app/data --name uptime-kuma louislam/uptime-kuma:latest
```

## Access

You can connect to the uptime kuma web interface using the following URL:  
`http://[HOSTNAME]:3001`

## Update/Upgrade Procedure

```bash
sudo docker pull louislam/uptime-kuma:latest
sudo docker stop uptime-kuma
sudo docker rm uptime-kuma
sudo docker run -d --restart=unless-stopped -p 3001:3001 -v /opt/uptime-kuma:/app/data --name uptime-kuma louislam/uptime-kuma:latest
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
