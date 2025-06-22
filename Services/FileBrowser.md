# FileBrowser

<https://filebrowser.org/>

## Install Docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Installing FileBrowser on Docker

<https://github.com/filebrowser/filebrowser>

### Create the FileBrowser directory, database & configuration file (with the right permission)

```bash
sudo mkdir -p /data/FileBrowser/data && sudo chown antiz: /data/FileBrowser/data && chmod 700 /data/FileBrowser/data
sudo touch /data/FileBrowser/{filebrowser.db,settings.json} && sudo chown antiz: /data/FileBrowser/{filebrowser.db,settings.json} && chmod 600 /data/FileBrowser/{filebrowser.db,settings.json}
vim /data/FileBrowser/settings.json # https://github.com/filebrowser/filebrowser/blob/master/settings.json
```

```text
{
  "port": 80,
  "baseURL": "",
  "address": "",
  "log": "stdout",
  "database": "/database/filebrowser.db",
  "root": "/srv"
}
```

### Pull and run the container

```bash
sudo docker run -v /data/FileBrowser/data:/srv -v /data/FileBrowser:/database -v /data/FileBrowser:/config -u $(id -u):$(id -g) -p 8080:80 --name filebrowser -d --restart="unless-stopped" filebrowser/filebrowser
```

### Access

You can now access and configure it on this URL (admin:admin):  
`http://[HOSTNAME]:8080/`

## Configuration

Global Settings --> Dark Mode  
User Management --> Change default username and password

## Update/Upgrade and reinstall procedure

Since we use Docker, the update and upgrade procedure is actually the same as it does not rely directly on our server.  
Also, if you did a mapping between a volume stored on a local disk (like I did), all you need to do to reinstall your FileBrowser server is to re-download Docker (if you reinstalled your OS completely) and do the following steps.

### Pull the docker image

(... to check if there's an available update)

```bash
sudo docker pull filebrowser/filebrowser
```

### Apply the update

```bash
sudo docker stop filebrowser
sudo docker rm filebrowser
sudo docker run -v /data/FileBrowser/data:/srv -v /data/FileBrowser:/database -v /data/FileBrowser:/config -u $(id -u):$(id -g) -p 8080:80 --name filebrowser -d --restart="unless-stopped" filebrowser/filebrowser
```

### After an update

After an update, you can clean old dangling docker images (to regain spaces and clean up your local stored Docker images):

```bash
sudo docker image prune
```

Alternatively, you can clean all unused Docker component (stopped containers, network not use by any containers, dangling images and build cache) :  
**If you choose to do that, make sure all your containers are running ! Otherwise, they will be deleted.**

```bash
sudo docker system prune
```
