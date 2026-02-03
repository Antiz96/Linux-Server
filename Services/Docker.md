# Docker

<https://www.docker.com/>

## Install Docker on Arch

```bash
sudo pacman -S docker
sudo systemctl enable --now docker
```

## Install Docker on Alpine

```bash
sudo apk add docker
sudo rc-update add docker
sudo rc-service docker start
```

## Install Docker on Debian

### Make sure docker is not already installed via the regular Debian repos

```bash
sudo apt remove docker docker-engine docker.io
```

### Install docker dependencies

```bash
sudo apt install apt-transport-https ca-certificates curl gnupg lsb-release
```

### Add docker GPG Key and repo

```bash
curl -fsSL https://download.docker.com/linux/debian/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
sudo vim /etc/apt/sources.list.d/docker.sources
```

```text
Types: deb
URIs: https://download.docker.com/linux/debian/
Suites: trixie
Components: stable
Signed-By: /usr/share/keyrings/docker-archive-keyring.gpg
```

### Update repo list and install docker

```bash
sudo apt update && sudo apt install docker-ce docker-ce-cli containerd.io
```

### Start and enable the docker service

```bash
sudo systemctl enable --now docker
```

## Tips and tricks

### Change docker datadir path

Defaults in `/var/lib/docker`.

```bash
sudo mkdir -p /etc/docker
sudoedit /etc/docker/daemon.json
```

```text
{
  "data-root": "/path/to/datadir"
}
```

Then restart the `docker` service.
