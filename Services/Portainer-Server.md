# Portainer Server

<https://www.portainer.io/>

## Install Docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Installing Portainer server on Docker

### Create a directory for persistent data

```bash
sudo mkdir /opt/portainer
```

### Download and launch the docker container

```bash
sudo docker run -d -p 8000:8000 -p 9443:9443 --name portainer-server --restart=unless-stopped -v /var/run/docker.sock:/var/run/docker.sock -v /opt/portainer:/data portainer/portainer-ce:latest
```

## Access

You can connect to the portainer web interface using the following URL:  
`https://[HOSTNAME]:9443`

## Update/Upgrade Procedure

```bash
sudo docker pull portainer/portainer-ce:latest
sudo docker stop portainer
sudo docker rm portainer
sudo docker run -d -p 8000:8000 -p 9443:9443 --name portainer-server --restart=unless-stopped -v /var/run/docker.sock:/var/run/docker.sock -v /opt/portainer:/data portainer/portainer-ce:latest
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
