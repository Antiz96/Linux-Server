# Portainer Agent

<https://www.portainer.io/>

**This is the procedure to install and connect an agent to an already existing Portainer server**  
**To install a Portainer server, see** <https://github.com/Antiz96/Linux-Server/blob/main/Services/Portainer-Server.md>

## Install Docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Installing Portainer agent on Docker

<https://docs.portainer.io/v/ce-2.11/start/install/agent/docker/linux>

### Pull and run the container

```bash
sudo docker run --privileged -d -p 9001:9001 --name portainer-agent --hostname portainer-agent --restart=unless-stopped -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/docker/volumes:/var/lib/docker/volumes portainer/agent:latest
```

### Add the agent to the Portainer Server

Add the following URL to the environment tab in your portainer server's web interface (you don't have to specify `tcp://`):  
`[HOSTNAME]:9001`

## Update/Upgrade procedure

```bash
sudo docker pull portainer/agent:latest
sudo docker stop portainer-agent
sudo docker rm portainer-agent
sudo docker run --privileged -d -p 9001:9001 --name portainer-agent --hostname portainer-agent --restart=unless-stopped -v /var/run/docker.sock:/var/run/docker.sock -v /var/lib/docker/volumes:/var/lib/docker/volumes portainer/agent:latest
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
