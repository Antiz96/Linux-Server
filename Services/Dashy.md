# Dashy

<https://github.com/lissy93/dashy>

## Install docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Create a dedicated dashy directory

```bash
sudo mkdir /opt/dashy
```

## Pull and run the container

```bash
sudo docker run -d -p 8080:80 -v /opt/dashy/conf.yml:/app/public/conf.yml --name dashy --restart=unless-stopped lissy93/dashy:latest
```

## Update procedure

```bash
sudo docker pull lissy93/dashy:latest
sudo docker stop dashy
sudo docker rm dashy
sudo docker run -d -p 8080:80 -v /opt/dashy/conf.yml:/app/public/conf.yml --name dashy --restart=unless-stopped lissy93/dashy:latest
```

You can then optionally clean old dangling docker images (to clean up locally stored Docker images and regain some disk space):

```bash
sudo docker image prune
```

## Tips and tricks

### Re-read configuration file

If you modified your configuration file or if dashy did not load it correctly for some reason, you can make dashy re-read it without having to restart the container.

```bash
sudo docker exec -it dashy yarn build
```
