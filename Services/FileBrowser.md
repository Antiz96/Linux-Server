# FileBrowser

~~<https://filebrowser.org/>~~

I recently switched to the FileBrowser Quantum fork, which bring a few extra features and a polished UI (among other things).

<https://filebrowserquantum.com/en/>

## Install Podman on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Podman.md>

## Installing FileBrowser on Docker / Podman

<https://filebrowserquantum.com/en/docs/getting-started/docker/>

### Create the data, cache, config directories & configuration file

```bash
mkdir -p /data/podman/volumes/filebrowser/{data,cache,config} && chmod 700 /data/podman/volumes/filebrowser/{data,cache,config}
touch /data/podman/volumes/filebrowser/config/config.yaml && chmod 600 /data/podman/volumes/filebrowser/config/config.yaml
vim /data/podman/volumes/filebrowser/config/config.yaml
```

```text
server:
  port: 8080 # Port for the server to listen on (inside the container)
  cacheDir: /home/filebrowser/cache # Path to cache dir (inside the container)
  sources:
    - path: /home/filebrowser/data # Path to data dir (inside the container)
      config:
        defaultEnabled: true

auth:
  adminUsername: Antiz # Name of the default admin password
  adminPassword: "changeit" # Password for the above admin user
  methods:
    password:
      enabled: true # Enable password authentication
      minLength: 8 # Num of minimum password length
      signup: false # Enable / Disable signup for users
```

### Pull and run the container

```bash
podman run -v /data/podman/volumes/filebrowser/config/config.yaml:/home/filebrowser/config.yaml -v /data/podman/volumes/filebrowser/data:/home/filebrowser/data -v /data/podman/volumes/filebrowser/cache:/home/filebrowser/cache -p 8080:8080 --name filebrowser -d --label io.containers.autoupdate=registry --restart="unless-stopped" docker.io/gtstef/filebrowser
```

### Access

You can now access and configure it on this URL:  
`http://[HOSTNAME]:8080/`

## Update/Upgrade and reinstall procedure

I'm relying on `podman auto-update`.

Optionally clean old dangling images:

```bash
podman image prune
```
