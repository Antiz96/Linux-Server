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
mkdir -p /data/podman/volumes/filebrowser/{storage,cache}
chmod 700 /data/podman/volumes/filebrowser/{storage,cache}
touch /data/podman/volumes/filebrowser/config.yaml
chmod 600 /data/podman/volumes/filebrowser/config.yaml
vim /data/podman/volumes/filebrowser/config.yaml
```

```text
server:
  port: 8080 # Port for the server to listen on (inside the container)
  cacheDir: /home/filebrowser/data/cache # Path to cache dir (inside the container)
  sources:
    - path: /home/filebrowser/data/storage # Path to data dir (inside the container)
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

```bash
chown -R 166536:166536 /data/podman/volumes/filebrowser # Should match the value in /etc/subuid | /etc/subgid for your user. This is required since filebrowser v1.3.0 if running as an unprivileged user
```

### Pull and run the container

Update UID:GID for `--user` if needed.

```bash
podman run -v /data/podman/volumes/filebrowser:/home/filebrowser/data --user 1001:1001 -p 8080:8080 --name filebrowser -d --label io.containers.autoupdate=registry --restart="unless-stopped" docker.io/gtstef/filebrowser
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
