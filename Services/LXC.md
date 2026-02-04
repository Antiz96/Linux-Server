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

### Alpine / Debian

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

### Arch with NetworkManager

```bash
sudo nmcli connection add type bridge ifname lxcbr0 con-name lxcbr0 # Create bridge interface
sudo nmcli connection modify lxcbr0 ipv4.method manual ipv4.addresses 192.168.96.100/24 ipv4.gateway 192.168.96.254 ipv4.dns 192.168.96.1 ipv6.method disabled # Move network conf to bridge interface
sudo nmcli connection modify Wired\ connection\ 1 connection.master lxcbr0 connection.slave-type bridge # Attach physical NIC to the bridge
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

### Unprivileged containers and AppArmor

It seems that AppArmor doesn't play really nice with unprivileged containers and is very restrictive, eventually restricting expected actions within unprivileged containers by default, such as starting systemd services and other things (Debian even have [a dedicated page for related issues](https://wiki.debian.org/LXC/SystemdMountsAndAppArmor)).

Despite experimenting and trying potential workarounds I've found here and there, I wasn't able to configure AppArmor to play nice with unprivileged containers (including allowing mounting, nesting, etc...). Debian considers that disabling AppArmor is an acceptable approach for unprivileged containers (see [here](https://wiki.debian.org/LXC/SystemdMountsAndAppArmor#Permissive_AppArmor_profile)), which can be done by adding the following to your containers configuration:

```text
lxc.apparmor.profile = unconfined
```

### Autostart containers at boot

Add the following line to the containers' config you want to autostart at boot:

```text
lxc.start.auto = 1
```

Then start / enable the required service:

```bash
sudo systemctl enable --now lxc-auto.service
```

For unprivileged containers running as non-root user, create and enable the following user service instead:

```bash
mkdir -p ~/.config/systemd/user
vim ~/.config/systemd/user/lxc-autostart.service
```

```text
[Unit]
Description="lxc-autostart for lxc user"

[Service]
Type=oneshot
ExecStart=/usr/bin/lxc-autostart
ExecStop=/usr/bin/lxc-autostart -s
RemainAfterExit=1

[Install]
WantedBy=default.target
```

```bash
sudo systemctl daemon-reload
systemctl --user enable --now lxc-autostart.service
sudo loginctl enable-linger $USER
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
