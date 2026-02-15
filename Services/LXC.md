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
```

The following should already be done on Arch Linux and Debian, but is needed on Alpine:

```bash
echo "$(whoami):100000:65536" | sudo tee -a /etc/subuid
echo "$(whoami):100000:65536" | sudo tee -a /etc/subgid
```

Note that containers started in rootless / unprivileged mode are only accessible / manageable by the user that created them.

## Create a network bridge

Create a network bridge on the host network interface for the containers to get their own bridged network interface.  
Containers will automatically get an IP from DHCP at first boot, but can be configured to get their own static IP (either from the LXC config, or from the installed network manager inside the container).

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
# Replace 20 by the number of network interfaces that the given user should be allowed to attach to the given bridge
<username> veth lxcbr0 20
```

```bash
sudo reboot
```

### Arch Linux

#### With systemd-networkd

```bash
sudoedit /etc/systemd/network/lxcbr0.netdev # Create the bridge interface
```

```text
[NetDev]
Name=lxcbr0
Kind=bridge
```

```bash
sudoedit /etc/systemd/network/lxcbr0.network # Create the network config for the bridge interface
```

```text
[Match]
Name=lxcbr0

[Network]
Address=192.168.96.100/24
Gateway=192.168.96.254
DNS=192.168.96.1
IPv6AcceptRA=no
```

```bash
sudoedit /etc/systemd/network/eth0.network # Link physical nic to the bridge interface
```

```text
[Match]
Name=eth0

[Network]
Bridge=lxcbr0
```

```bash
sudoedit /etc/lxc/lxc-usernet
```

```text
# Replace <username> by your user
# Replace 20 by the number of network interfaces that the given user should be allowed to attach to the given bridge
<username> veth lxcbr0 20
```

```bash
sudo reboot
```

#### With NetworkManager

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
# Replace 20 by the number of network interfaces that the given user should be allowed to attach to the given bridge
<username> veth lxcbr0 20
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

Attach current session to the container (necessary at first boot to create root password):

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

### Snapshot / Clone / Backup

- Snapshot

```bash
lxc-snapshot -n <container_name> # Take a snapshot of a container
lxc-snapshot -n <container_name> -c file.txt # Take a snapshot of a container with comments from the content of file.txt
lxc-snapshot -n <container_name> -L # List snapshots of a container
lxc-snapshot -n <container_name> -L -C # List snapshots of a container including comments
lxc-snapshot -n <container_name> -r snap0 # Restore the "snap0" snapshot on a container
lxc-snapshot -n <container_name> -r snap0 -N <container_name2> # Restore the "snap0" snapshot to a separate new container
lxc-snapshot -n <container_name> -d snap0 # Delete the "snap0" snapshot of a container
```

- Clone

Note that, if running AppArmor, you might need to pass `lxc-copy` under complain mode for it to work properly and avoid permissions errors (`sudo aa-complain lxc-copy`).

```bash
lxc-copy -n <container_name> -N <container_name2> # Clone a container into a new one
```

- Backup

One can just rsync the container directory with proper options:

```bash
sudo rsync -a --numeric-ids \
  --exclude=/rootfs/proc/* \
  --exclude=/rootfs/sys/* \
  --exclude=/rootfs/dev/* \
  /lxc/datadir/mycontainer/ \
  /backup/lxc/mycontainer/
```

To restore, either stop or destroy the container, then:

```bash
sudo rsync -a --numeric-ids /backup/lxc/mycontainer/ /lxc/datadir/mycontainer/
```

## Tips and tricks

### Change lxc datadir

Defaults in `.local/share/lxc` (or `/var/lib/lxc` if rootfull).

```bash
vim ~/.config/lxc/lxc.conf # or /etc/lxc/lxc.conf if rootfull
```

```text
lxc.lxcpath = /path/to/datadir # Should be writeable by user
```

### Unprivileged containers and AppArmor

It seems that AppArmor doesn't play really nice with unprivileged LXC containers and is very restrictive for them, eventually preventing expected actions within unprivileged containers by default, such as starting systemd services and other things (Debian even have [a dedicated page for related issues](https://wiki.debian.org/LXC/SystemdMountsAndAppArmor)).

Despite experimenting and trying potential workarounds I've found here and there, I wasn't able to configure AppArmor to play nicely with unprivileged LXC containers (including allowing mounting, nesting, etc...). Debian considers that not loading the AppArmor profile within the container is an acceptable approach for unprivileged LXC containers (see [here](https://wiki.debian.org/LXC/SystemdMountsAndAppArmor#Permissive_AppArmor_profile)), which can be done by adding the following to the containers configuration:

```text
lxc.apparmor.profile = unconfined
```

### Limit containers resources

In containers' config:

```text
lxc.cgroup2.cpuset.cpus = 0-1 # Limit access to host's core 0 and 1
lxc.cgroup2.cpu.max = 200000 100000 # Limit CPU quota to the equivalent of two cores (400000 100000 for 4 cores)
lxc.cgroup2.memory.max = 2G # Limit RAM quote to 2G
lxc.cgroup2.memory.swap.max = 0 # Restrict usage of host's swap
```

For what it's worth, I'm personally only setting `lxc.cgroup2.cpu.max` and `lxc.cgroup2.memory.max`.

Note that (as opposed to a VM) this is just quota limit, not a definition of the "visible" resources within the container. So things like `htop`, `fastfetch` or `free` will still report the full number of CPU and RAM of the host, regardless of the above settings. To get the accurate number of CPU and RAM allowed in a container, run `nproc` and `cat /sys/fs/cgroup/memory.max` instead.

### Autostart containers at boot

Add the following line to the containers' config you want to autostart at boot:

```text
lxc.start.auto = 1
```

Then start / enable the required service (rootfull):

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
systemctl --user daemon-reload
systemctl --user enable --now lxc-autostart.service
loginctl enable-linger $USER
```

### Run unprivileged systemd-based distribution containers with Alpine / OpenRC

**NOTE:** The following trick allows to run unprivileged systemd-based distribution containers that still accept cgroups v1 (basically distributions that still run systemd < v258).  
As far as I can tell, there's no way yet to run unprivileged system-based distribution containers that require cgroups v2 with OpenRC (basically distributions that run systemd >= v258).

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
