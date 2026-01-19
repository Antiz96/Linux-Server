# Arcane Agent

<https://getarcane.app/>

**This is the procedure to install and connect an agent to an already existing Arcane server**  
**To install an Arcane server, see** <https://github.com/Antiz96/Linux-Server/blob/main/Services/Arcane-Server.md>

## Install Docker on my Server (if not done already)

<https://getarcane.app/docs/features/environments>

## Installing Arcane agent on Docker

<https://docs.portainer.io/v/ce-2.11/start/install/agent/docker/linux>

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

> Add the URL of your Arcane server instance (e.g. https://[hostname]:3552)

Set secure permissions:

```bash
sudo chmod 600 /opt/arcane/env/* && sudo chmod 750 /opt/arcane/env
```

### Pull and run the container

```bash
sudo docker run -d --name arcane-agent --restart unless-stopped -e AGENT_MODE=true -e AGENT_TOKEN=$(sudo cat /opt/arcane/env/api.key) -e MANAGER_API_URL=$(sudo cat /opt/arcane/env/manager_api_url) -p 3553:3553 -v /var/run/docker.sock:/var/run/docker.sock -v /opt/arcane/data:/data ghcr.io/getarcaneapp/arcane-headless:latest
```

## Update / Upgrade procedure

```bash
sudo docker pull ghcr.io/getarcaneapp/arcane-headless:latest
sudo docker stop arcane-agent
sudo docker rm arcane-agent
sudo docker run -d --name arcane-agent --restart unless-stopped -e AGENT_MODE=true -e AGENT_TOKEN=$(sudo cat /opt/arcane/env/api.key) -e MANAGER_API_URL=$(sudo cat /opt/arcane/env/manager_api_url) -p 3553:3553 -v /var/run/docker.sock:/var/run/docker.sock -v /opt/arcane/data:/data ghcr.io/getarcaneapp/arcane-headless:latest
```

You can then optionally clean old dangling docker images (to clean up locally stored Docker images and regain some disk space):

```bash
sudo docker image prune
```
