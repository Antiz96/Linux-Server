# HomeHub

<https://github.com/surajverma/homehub>

## Install Podman on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Podman.md>

## Installing HomeHub on Docker / Podman

<https://github.com/surajverma/homehub#getting-started-is-easy>

### Create the data and uploads directories

You might want to create additional directories dedicated to other supported features (e.g. Media downloader, PDF compressor, etc...), but I personally don't use those.

```bash
mkdir -p /data/podman/volumes/homehub/{data,uploads}
```

### Generate secret key

```bash
openssl rand -base64 32 > /data/podman/volumes/homehub/.secret_key
chmod 400 /data/podman/volumes/homehub/.secret_key
```

### Create config

An example configuration file is available here at <https://github.com/surajverma/homehub/blob/main/config-example.yml>.  
Take this as an example and edit as needed.

```bash
vim /data/podman/volumes/homehub/config.yml
```

### Pull and run the container

```bash
podman run -d \
  --name homehub \
  -p 5000:5000 \
  -e FLASK_ENV=production \
  -e SECRET_KEY=$(cat /data/podman/volumes/homehub/.secret_key) \
  -v /data/podman/volumes/homehub/data:/app/data \
  -v /data/podman/volumes/homehub/uploads:/app/uploads \
  -v /data/podman/volumes/homehub/config.yml:/app/config.yml:ro \
  ghcr.io/surajverma/homehub:latest
```

### Access

You can now access and configure it on this URL:  
`http://[HOSTNAME]:5000/`

## Update/Upgrade procedure

I'm relying on `podman auto-update`.

Optionally clean old dangling images:

```bash
podman image prune
```
