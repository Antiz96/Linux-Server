# Arcane Server

<https://getarcane.app/>

## Install Docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Installing Arcane server on Docker

<https://getarcane.app/docs/setup/installation>

### Create directories for env variables and persistent data

```bash
sudo mkdir -p /opt/arcane/{env,data}
```

### Generate secret keys and environment variables files

Generate secret keys files:

```bash
openssl rand -hex 32 | sudo tee /opt/arcane/env/encryption.key
openssl rand -hex 32 | sudo tee /opt/arcane/env/jwt_secret.key
```

Create the environment file containing the URL for your Arcane server:

```bash
sudoedit /opt/arcane/env/app_url
```

> Add the URL for your Arcane server instance (e.g. <https://[hostname]:3552>)

Set secure permissions:

```bash
sudo chmod 600 /opt/arcane/env/* && sudo chmod 750 /opt/arcane/env
```

### Download and launch the docker container

```bash
sudo docker run -d -p 3552:3552 --name arcane-server --restart=unless-stopped -v /var/run/docker.sock:/var/run/docker.sock -v /opt/arcane/data:/app/data -e APP_URL="$(sudo cat /opt/arcane/env/app_url)" -e PUID=$(id -u) -e PGID=$(id -g) -e ENCRYPTION_KEY=$(sudo cat /opt/arcane/env/encryption.key) -e JWT_SECRET=$(sudo cat /opt/arcane/env/jwt_secret.key) ghcr.io/getarcaneapp/arcane:latest
```

**Note:** If the host server is running Proxmox / PVE, be aware that (since PVE 9) extra hardening is applied at the AppArmor level, forbidding containers to listen / access to the host's UNIX sockets.  
This prevents the arcane-server container to interact with the host Docker daemon via the `/var/run/docker.sock` socket, causing any Docker-related operations from Arcane (interacting with containers, updating them, listing images, etc...) to fail.  
To workaround this, add the extra `--security-opt apparmor=unconfined` argument to the above `docker run` command, so that the container doesn't run with AppArmor profile loaded.  
See [this PR description](https://github.com/Antiz96/Linux-Server/pull/483) for more details.

## Access

You can connect to the arcane web interface via the URL you defined in the `/opt/arcane/env/app_url` file.

Default ID is arcane:arcane-admin.  
You'll be prompted to change the default password after the first connection.

## Update/Upgrade Procedure

```bash
sudo docker pull ghcr.io/getarcaneapp/arcane:latest
sudo docker stop arcane-server
sudo docker rm arcane-server
sudo docker run -d -p 3552:3552 --name arcane-server --restart=unless-stopped -v /var/run/docker.sock:/var/run/docker.sock -v /opt/arcane/data:/app/data -e APP_URL="$(sudo cat /opt/arcane/env/app_url)" -e PUID=$(id -u) -e PGID=$(id -g) -e ENCRYPTION_KEY=$(sudo cat /opt/arcane/env/encryption.key) -e JWT_SECRET=$(sudo cat /opt/arcane/env/jwt_secret.key) ghcr.io/getarcaneapp/arcane:latest
```

**Note:** If the host server is running Promox / PVE, keep in mind the related note in the ["Pull and run the container chapter"](#pull-and-run-the-container).

You can then optionally clean old dangling docker images (to clean up locally stored Docker images and regain some disk space):

```bash
sudo docker image prune
```
