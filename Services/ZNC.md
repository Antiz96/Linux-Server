# ZNC

<https://wiki.znc.in/ZNC>

## Install docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Generate the config file

```bash
sudo docker run -it -v /opt/znc/:/znc-data znc --makeconf
```

You can find details about the configuration [here](https://wiki.znc.in/Configuration).  
The configuration can be modified via the web interface later on.

## Run the container

```bash
sudo docker run -d -p 8081:8081 -v /opt/znc:/znc-data --name znc --restart=unless-stopped znc #Adapt the port to your configuration
```

You can then access the web interface at `http://server_ip:port` from which you can configure ZNC *(personal reminder: don't forget to enable and configure the SASL module)*.  
Once you've done configuring it, connect your IRC client to your ZNC instance.

## Update procedure

```bash
sudo docker pull znc
sudo docker stop znc
sudo docker rm znc
sudo docker run -d -p 8081:8081 -v /opt/znc:/znc-data --name znc --restart=unless-stopped znc
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
