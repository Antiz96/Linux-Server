# Glances

https://nicolargo.github.io/glances/

## Install Docker on my Server (if not done already)

https://github.com/Antiz96/Server-Configuration/blob/main/Services/Docker.md

## Installing Glances on Docker

https://github.com/nicolargo/glances#docker-the-fun-way

### Pull and run the container

```
sudo docker pull nicolargo/glances:latest-full 
sudo docker run -d --restart="unless-stopped" -p 61208-61209:61208-61209 -e GLANCES_OPT="-w" -v /var/run/docker.sock:/var/run/docker.sock:ro --pid host --name glances nicolargo/glances:latest-full
```

### Set a username and password for the web interface (optionnal but recommended)

I create env files to store the username and password so I won't need to pass them in plain text in the docker run command (which is a bit more secure). It also makes those parameters more flexible as they can be changed by editing the files without having to change the docker run command itself. Those files will obviously be only viewable and editable by the root account for security reasons.  

```
sudo mkdir -p /opt/glances/env
sudo docker exec -it glances bash
```

Inside the container :  

```
glances -s --username --password
```
> Define the Glances server username: #Type the username you want to use  
> Define the Glances server password (username): #Type the password you want to use   
> Password (confirm): #Confirm it  
> Do you want to save the password? [Yes/No]: **Yes**  
> Glances XML-RPC server is running on 0.0.0.0:61209  
> Announce the Glances server on the LAN (using 172.17.0.5 IP address)  
  
Then press `ctrl+\` to interupt the proccess and `exit` the container.    
Then, copy the password file locally in the env directory and create the "user" env file that will contains your username.    

*Replace "your_username" by your username :)*  
  
```
sudo docker cp glances:/root/.config/glances/your_username.pwd /opt/glances/env/password
sudo vim /opt/glances/env/user
```
> your_username 

Set secure permissions to the env files :  
  
```
sudo chmod 600 /opt/glances/env/*
```
  
Finally, stop and re-run the container by mapping the content of those files.  

```
sudo docker stop glances
sudo docker rm glances
sudo docker run -d --restart="unless-stopped" -p 61208-61209:61208-61209 -e GLANCES_OPT="-w -u $(sudo cat /opt/glances/env/user) --password" -v /opt/glances/env/password:/root/.config/glances/$(sudo cat /opt/glances/env/user).pwd -v /var/run/docker.sock:/var/run/docker.sock:ro --pid host --name glances nicolargo/glances:latest-full
```

### Access

You can now access it on this URL :  
`http://[HOSTNAME]:61208/` 

## Update/Upgrade and reinstall procedure

Since we use Docker, the update and upgrade procedure is actually the same as it does not rely directly on our server.  

### Pull the docker image 

*(... to check if there's available updates)*  

```
sudo docker pull nicolargo/glances:latest-full
```

### Apply the update

```
sudo docker stop glances
sudo docker rm glances
sudo docker run -d --restart="unless-stopped" -p 61208-61209:61208-61209 -e GLANCES_OPT="-w" -v /var/run/docker.sock:/var/run/docker.sock:ro --pid host --name glances nicolargo/glances:latest-full
```

Or, if you set a username and password (Replace **rcandau** by the username you've set earlier) :

```
sudo docker run -d --restart="unless-stopped" -p 61208-61209:61208-61209 -e GLANCES_OPT="-w -u $(sudo cat /opt/glances/env/user) --password" -v /opt/glances/env/password:/root/.config/glances/$(sudo cat /opt/glances/env/user).pwd -v /var/run/docker.sock:/var/run/docker.sock:ro --pid host --name glances nicolargo/glances:latest-full
```

### After an update 

After an update, you can clean old dangling docker images (to regain spaces and clean up your local stored Docker images) :  

```
sudo docker image prune
```

Alternatively, you can clean all unused Docker component (stopped containers, network not use by any containers, dangling images and build cache) :  
**If you choose to do that, make sure all your containers are running ! Otherwise, they will be deleted**

```
sudo docker system prune
```
