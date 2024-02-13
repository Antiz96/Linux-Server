# The Lounge

<https://thelounge.chat/>  
<https://github.com/thelounge/thelounge-docker>

## Install Docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Install The Lounge on Docker

### Create a directory for persistent data

```bash
sudo mkdir /opt/thelounge
```

### Download and launch the docker container

```bash
sudo docker run -d --restart=unless-stopped -p 9000:9000 -v /opt/thelounge:/var/opt/thelounge --name thelounge ghcr.io/thelounge/thelounge:latest
```

## Access

You can connect to the the lounge web client using the following URL:  
`http://[HOSTNAME]:9000`

## Update/Upgrade Procedure

```bash
sudo docker pull ghcr.io/thelounge/thelounge:latest
sudo docker stop thelounge
sudo docker rm thelounge
sudo docker run -d --restart=unless-stopped -p 9000:9000 -v /opt/thelounge:/var/opt/thelounge --name thelounge ghcr.io/thelounge/thelounge:latest
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
