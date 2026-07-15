# FileBrowser

<https://donetick.com/>

## Install Podman on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Podman.md>

## Installing Donetick on Docker / Podman

<https://github.com/donetick/donetick#using-docker>

### Create the data and config directories

```bash
mkdir -p /data/podman/volumes/donetick/{data,config}
```

### Create config

An example configuration file is available here at <https://github.com/donetick/donetick/blob/main/config/selfhosted.yaml>.  
Take this as an example and edit as needed.

To generate a secured JWT Secret, I use the output of `openssl rand -base64 32`.

```bash
vim /data/podman/volumes/donetick/config/selfhosted.yaml
```

### Pull and run the container

```bash
podman run -d \
  --name donetick \
  -v /data/podman/volumes/donetick/data:/donetick-data \
  -v /data/podman/volumes/donetick/config:/config \
  -p 2021:2021 \
  -e DT_ENV=selfhosted \
  -e DT_SQLITE_PATH=/donetick-data/donetick.db \
  -e TZ=Europe/Paris \  
  --health-cmd "wget --no-verbose --tries=1 --spider http://localhost:2021/api/v1/health || exit 1" \
  --health-start-period 1m \
  --health-timeout 5s \
  --health-interval 1m \
  --health-retries 3 \
  docker.io/donetick/donetick
```

### Access

You can now access and configure it on this URL:  
`http://[HOSTNAME]:2021/`

## Update/Upgrade and reinstall procedure

I'm relying on `podman auto-update`.

Optionally clean old dangling images:

```bash
podman image prune
```
