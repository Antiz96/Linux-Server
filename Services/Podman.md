# Podman

<https://podman.io/>

## Install Podman on Arch

```bash
sudo pacman -S podman
loginctl enable-linger $USER
```

## Install Podman on Alpine

```bash
sudo apk add podman
```

## Install Podman on Debian

```bash
sudo apt install podman
loginctl enable-linger $USER
```

## Setup rootless / unprivileged mode on Alpine

The prerequisites to use rootless podman with an unprivileged user should already be done by default on Arch Linux and Debian.  
To be able to use rootless podman with your current unprivileged user in Alpine, do the following:

```bash
sudo rc-update add cgroups
sudo rc-service cgroups start
sudo modprobe tun
echo "tun" | sudo tee -a /etc/modules
echo "$(whoami):100000:65536" | sudo tee -a /etc/subuid
echo "$(whoami):100000:65536" | sudo tee -a /etc/subgid
```

Note that containers started in rootless mode are only accessible / manageable by the user that created them.

## Usage

Generally speaking, just replace `docker` by `podman` in commands using your current user (no need for `sudo` or root privileges).

`podman` also have some extra capabilities / features (such as specific labels, `auto-update`, systemd units supports for containers, etc...).  
`podman` can also expose an API via a socket (like `Docker`) if needed (not enabled by default).

**Important note:** Contrary to Docker, Podman does not automatically open ports exposed to containers on the firewall. You need to open exposed ports yourself.

Refer to the documentation.

## Tips and tricks

### Change (rootless) podman datadir

Defaults in `~/.local/share/containers/storage`.

```bash
mkdir -p ~/.config/containers
vim ~/.config/containers/storage.conf
```

```text
[storage]
driver="overlay" # Adapt if needed
rootless_storage_path="/path/to/datadir" # Should be writeable by the user
```

### Containers running as systemd services and podman auto-update

To enable the auto-update support for a container, add `--label io.containers.autoupdate=registry` to its `podman run` command.

Once the container is running, generate a quadlet / systemd service file for it via `podlet` (`sudo pacman -S podlet`) and start it:

```bash
mkdir -p ~/.config/containers/systemd
podlet generate container <container_name> > ~/.config/containers/systemd/<container_name>.container
echo -e "\n[Install]\nWantedBy=default.target" >> ~/.config/containers/systemd/<container_name>.container # Required for the container to auto start at boot
systemctl --user daemon-reload
systemctl --user start <container_name>.service
```

You can now verify if a new image is available for every containers with the `io.containers.autoudpate` label and started with systemd via:

```bash
podman auto-update --dry-run
```

You can also format the output to only show specific columns (e.g. for scripts):

```bash
podman auto-update --dry-run --format "{{.Image}} {{.Updated}}"
```

You can apply update via:

```bash
podman auto-update
```
