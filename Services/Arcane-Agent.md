# Arcane Agent

<https://getarcane.app/>

**This is the procedure to install and connect an agent to an already existing Arcane server**  
**To install an Arcane server, see** <https://github.com/Antiz96/Linux-Server/blob/main/Services/Arcane-Server.md>

## Install Docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Installing Arcane agent on Docker

<https://getarcane.app/docs/features/environments>

### Create directories for env variables and persistent data

```bash
sudo mkdir -p /opt/arcane/{env,data}
```

### Create the new environment in the Arcane Server

From the Arcane server webUI, click "Add Environment" from the "Environment" menu.

Fill in the name and address of the host's API endpoint (e.g. `http://hostname:3553`) of the new environment.  
Note that those can be updated afterwards.

Then copy the generated API key for the next step.

### Create the environment variables files on the server

Add the API key generated in the previous step on the server:

```bash
sudoedit /opt/arcane/env/api.key
```

> Copy the generated API key

Create the environment file containing the URL of your Arcane server:

```bash
sudoedit /opt/arcane/env/manager_api_url
```

> Add the URL of your Arcane server instance (e.g. <https://[hostname]:3552>)

Set secure permissions:

```bash
sudo chmod 600 /opt/arcane/env/* && sudo chmod 750 /opt/arcane/env
```

### Pull and run the container

```bash
sudo docker run -d --name arcane-agent --restart unless-stopped -e AGENT_MODE=true -e AGENT_TOKEN=$(sudo cat /opt/arcane/env/api.key) -e MANAGER_API_URL=$(sudo cat /opt/arcane/env/manager_api_url) -p 3553:3553 -v /var/run/docker.sock:/var/run/docker.sock -v /opt/arcane/data:/data ghcr.io/getarcaneapp/arcane-headless:latest
```

**Note:** If the host server has AppArmor enabled (which is the case by default on Debian), the Docker daemon automatically loads a [`docker-default` AppArmor profile](https://docs.docker.com/engine/security/apparmor/) into containers. You can check if the `docker-default` AppArmor profile is enforced with `aa-status` and if it's loaded in a container with `docker inspect --format='{{.AppArmorProfile}}' "container_name"`.  

This AppArmor profile *may* prevent the arcane-agent container to interact with the host Docker daemon via the `/var/run/docker.sock` socket (for what it's worth, that was the case on Debian but not on Arch Linux on my side, despite AppArmor and the `docker-default` profile being loaded on both). This causes any Docker-related operations from Arcane (interacting with containers, updating them, listing images, etc...) to fail.  
To workaround this, you can add the extra `--security-opt apparmor=unconfined` argument to your `docker run` command, so that the container doesn't run with the `docker-default` AppArmor profile loaded. Alternatively, one can create a drop-in AppArmor profile to allow UNIX socket access from the container and load it via `--security-opt apparmor="profile_name"`.  

## Update / Upgrade procedure

```bash
sudo docker pull ghcr.io/getarcaneapp/arcane-headless:latest
sudo docker stop arcane-agent
sudo docker rm arcane-agent
sudo docker run -d --name arcane-agent --restart unless-stopped -e AGENT_MODE=true -e AGENT_TOKEN=$(sudo cat /opt/arcane/env/api.key) -e MANAGER_API_URL=$(sudo cat /opt/arcane/env/manager_api_url) -p 3553:3553 -v /var/run/docker.sock:/var/run/docker.sock -v /opt/arcane/data:/data ghcr.io/getarcaneapp/arcane-headless:latest
```

**Note:** Keep in mind the note about AppArmor in the ["Pull and run the container chapter"](#pull-and-run-the-container) if your host is running Debian and / or if you have AppArmor enabled.

You can then optionally clean old dangling docker images (to clean up locally stored Docker images and regain some disk space):

```bash
sudo docker image prune
```
