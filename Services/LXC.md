# LXC

<https://linuxcontainers.org/>

## Install LXC on Arch

```bash
sudo pacman -S lxc lxcfs
```

## Install LXC on Alpine

```bash
sudo apk add lxc lxcfs lxc-download lxc-user-nic
sudo rc-update add cgroups
sudo rc-service cgroups start
```

## Install LXC on Debian

```bash
sudo apt install lxc lxcfs lxc-templates
```

## Setup rootless / unprivileged mode

To be able to use rootless / unprivileged LXC containers with your current unprivileged user, do the following:

```bash
mkdir -p ~/.config/lxc
cp /etc/lxc/default.conf ~/.config/lxc/default.conf
echo "lxc.idmap = u 0 100000 65536" >> ~/.config/lxc/default.conf
echo "lxc.idmap = g 0 100000 65536" >> ~/.config/lxc/default.conf
echo "$(whoami):100000:65536" | sudo tee -a /etc/subuid
echo "$(whoami):100000:65536" | sudo tee -a /etc/subgid
```

Note that containers started in rootless / unprivileged mode are only accessible / manageable by the user that created them.

## Create a network bridge

Create a network bridge on your network interface for the containers.  
Following information are targeted at Alpine and Debian (for Arch Linux, refer to your network manager documentation).

```bash
sudoedit /etc/network/interfaces
```

```text
# Assuming eth0 with address, netmask and gateway already set
auto lo
iface lo inet loopback

auto eth0
iface eth0 inet manual # Set it to manual and move parameters to the bridge interface

auto lxcbr0 # I advise to call it like that as its the name lxc is looking for by default
iface lxcbr0 inet static
    address 192.168.96.100 # Those were moved from the initial eth0 config above
    netmask 255.255.255.0 # This was moved from the initial eth0 config above
    gateway 192.168.96.254 # This was moved from the initial eth0 config above
    bridge-ports eth0
    bridge-stp off
    bridge-fd 0
```

```bash
sudoedit /etc/lxc/lxc-usernet
```

```text
# Replace <username> by your user
# Replace 15 by the number of network interfaces the given user is allowed to attach to the given bridge
<username> veth lxcbr0 15
```

```bash
sudo reboot
```

## Usage

### Create a container

```bash
lxc-create -n <container_name> -t download
```

And then select the distribution, release & architecture.

Alternatively, select parameters directly as arguments:

```bash
lxc-create -n <container_name> -t download -- --dist archlinux --release current --arch amd64
```

### List containers

```bash
lxc-ls -f
```

### Get info about a container

```bash
lxc-info <container_name>
```

### Start / Stop a container

```bash
lxc-start -n <container_name>
lxc-stop -n <container_name>
```

### Log into a container

Attach current session to the container (necessary to create first user):

```bash
lxc-attach -n <container_name>
```

Spawn a console into the container:

```bash
lxc-console -n <container_name>
```

To exit a container session, press `ctrl-a` then `q`.

### Delete a container

```bash
lxc-destroy <container_name>
```

## Tips and tricks

### Change lxc datadir

Defaults in `/var/lib/lxc`.

```bash
vim ~/.config/lxc/lxc.conf
```

```text
lxc.lxcpath = /path/to/datadir # Should be writeable by user
```

### Run unprivileged systemd-based distribution containers with Alpine / OpenRC

**NOTE:** The following trick allows to run unprivileged systemd-based distribution containers that still accept cgroups v1 (so basically distributions that still run systemd < v258).  
As far as I can tell, there's no way yet to run unprivileged system-based distribution containers that require cgroups v2 with OpenRC (so basically distributions that run systemd >= v258).

```bash
sudo mkdir -p /sys/fs/cgroup/systemd
sudo mount -t cgroup -o none,name=systemd systemd /sys/fs/cgroup/systemd
sudo chown 100000:100000 -R /sys/fs/cgroup/systemd/
```

You can put the above in an OpenRC init script or in your fstab to apply permanently.  
See the following links for more details:

- <https://wiki.gentoo.org/wiki/LXC#Systemd_containers_on_an_OpenRC_host>
- <https://j2h2.com/posts/alpine-linux-and-systemd-containers-round-2/>
- <https://github.com/debops/ansible-lxc/issues/15>
