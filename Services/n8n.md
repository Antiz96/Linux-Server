# n8n

<https://n8n.io/>
<https://github.com/n8n-io/n8n>

## Install Docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Install n8n on Docker

### Create a directory for persistent data

```bash
sudo mkdir /opt/n8n
sudo chown 1000:1000 /opt/n8n # n8n needs this directory to be writable by the node user within the container which uses id 1000
```

### Download and launch the docker container

```bash
sudo docker run -d --restart=unless-stopped -p 5678:5678 -e GENERIC_TIMEZONE="Europe/Paris" -e TZ="Europe/Paris" -v /opt/n8n:/home/node/.n8n --name n8n docker.n8n.io/n8nio/n8n:latest
```

## Access

You can connect to the n8n webUI using the following URL:  
`http://[HOSTNAME]:5678`

## Update/Upgrade Procedure

```bash
sudo docker pull docker.n8n.io/n8nio/n8n:latest
sudo docker stop n8n
sudo docker rm n8n
sudo docker run -d --restart=unless-stopped -p 5678:5678 -e GENERIC_TIMEZONE="Europe/Paris" -e TZ="Europe/Paris" -v /opt/n8n:/home/node/.n8n --name n8n docker.n8n.io/n8nio/n8n:latest
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
