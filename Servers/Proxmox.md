# Proxmox

<https://www.proxmox.com/en/>

**If it's the very first install, remember to enable the intel virtualization technology and disable secure boot in the BIOS.**  
<https://rog.asus.com/us/support/FAQ/1043786>  
<https://techzillo.com/how-to-disable-or-enable-secure-boot-for-asus-motherboard/>

## Graphical installation

Follow the classic graphical installation process.  
For the first filesystem related step, select the disk dedicated to the OS and use `ext4` as the filesystem *(for `pmx02`, set hdsize to 75 GiB)*.

## Setup Proxmox community repositories and update the system

```bash
sed -i '1s/^/# /' /etc/apt/sources.list.d/ceph.list /etc/apt/sources.list.d/pve-enterprise.list
echo "deb http://download.proxmox.com/debian $(grep CODENAME /etc/os-release | cut -f2 -d'=') pve-no-subscription" > /etc/apt/sources.list.d/pve-no-subscription.list
apt update
```

## Create and configure my user

```bash
useradd -s /bin/bash -m -u 1001 antiz
passwd antiz
apt install sudo
usermod -aG sudo antiz
su - antiz
```

### Download my dotfiles

```bash
curl https://raw.githubusercontent.com/Antiz96/Linux-Server/main/Dotfiles/Bashrc/Debian-Ubuntu-Server -o ~/.bashrc
curl https://raw.githubusercontent.com/Antiz96/Linux-Server/main/Dotfiles/General/vimrc -o ~/.vimrc
mkdir -p ~/.vim/colors && curl https://raw.githubusercontent.com/vv9k/vim-github-dark/master/colors/ghdark.vim -o ~/.vim/colors/ghdark.vim
source ~/.bashrc
```

## Update the server and install useful packages

```bash
sudo apt update && sudo apt full-upgrade && sudo apt autoremove
sudo apt install vim man bash-completion openssh-server dnsutils traceroute rsync zip unzip diffutils firewalld plocate htop curl openssl telnet chrony wget logrotate fail2ban python3-passlib fastfetch
sudo systemctl enable --now ssh chrony firewalld logrotate.timer fstrim.timer
sudo reboot
```

## Setup additional DNS servers (if needed)

```bash
sudo vim /etc/network/interfaces
```

> [...]  
> auto enp3s0
> iface enp3s0 inet manual  
> > **dns-nameservers 192.168.1.1 192.168.1.2**  
> [...]

```bash
echo "nameserver 192.168.1.2" | sudo tee -a /etc/resolv.conf
sudo systemctl restart networking
```

## Setup secure SSH connection

### Change the default SSH port

```bash
sudo vim /etc/ssh/sshd_config
```

> [...]  
> Port **"X"** *# Where "X" is the port you want to set*  
> [...]

### Disable ssh connection for the root account

```bash
sudo vim /etc/ssh/sshd_config
```

> [...]  
> PermitRootLogin no  
> [...]

### Restrict SSH connection method to public key authentication

```bash
sudo vim /etc/ssh/sshd_config
```

> [...]  
> PasswordAuthentication no  
> AuthenticationMethods publickey

### Open the SSH port on the firewall

```bash
sudo firewall-cmd --add-port=X/tcp --permanent # Open the port we've set for SSH (replace "X" by the port)
sudo firewall-cmd --remove-service="ssh" --permanent # Close the default SSH port
sudo firewall-cmd --remove-service="dhcpv6-client" --permanent # Close the dhcpv6-client port as I don't use it
sudo firewall-cmd --reload
```

### Restart the SSH service to apply changes

```bash
sudo systemctl restart sshd
```

## Configure and start fail2ban

Procedure: <https://github.com/Antiz96/Linux-Server/blob/main/Services/Fail2Ban.md>

## Enable Wake On Lan

<https://www.asus.com/support/FAQ/1045950/>  
<https://wiki.debian.org/WakeOnLan>

### Enable Wake On Lan in the BIOS

- pmx01:

DEL Key at startup to go to the BIOS  
Advanced --> APM Configuration --> Power On By PCI-E --> Enabled

- pmx02:

No related option in BIOS, it seems activated by default.

### Enable Wake On Lan support in the OS

```bash
sudo apt install ethtool
sudo ethtool -s enp3s0 wol g # Adapt network card name if needed
sudo vim /etc/network/interfaces
```

> [...]  
> auto enp3s0
> iface enp3s0 inet manual  
> > **post-up ethtool -s enp3s0 wol g** # Adapt network card name if needed  
> > **post-down ethtool -s enp3s0 wol g** # Adapt network card name if needed  
>
> [...]

Verify with:

```bash
sudo ethtool enp3s0 # Adapt network card name if needed  

```

> [...]  
> Wake-on: **g** # "g" means it is enabled  
> [...]

## Configure the inactivity timeout

```bash
sudo vim /etc/bash.bashrc # Set the inactivity timeout to 15 min
```

> [...]  
> #Set inactivity timeout  
> TMOUT=900  
> readonly TMOUT  
> export TMOUT

## Create and configure the ansible user

```bash
sudo useradd -s /bin/bash -m -u 1000 ansible # Create the ansible user
sudo vim /etc/sudoers.d/ansible # Make the ansible user a sudoer
```

> ansible ALL=(ALL) NOPASSWD: ALL

```bash
sudo mkdir -p /home/ansible/.ssh && sudo chmod 700 /home/ansible/.ssh && sudo chown ansible: /home/ansible/.ssh
sudo touch /home/ansible/.ssh/authorized_keys && sudo chmod 600 /home/ansible/.ssh/authorized_keys && sudo chown ansible: /home/ansible/.ssh/authorized_keys
sudo vim /home/ansible/.ssh/authorized_keys
```

> Copy the ansible master server's SSH public key here (ansible@ansible-server)

## Install and configure Zabbix Agent

```bash
sudo firewall-cmd --add-port=10050/tcp --permanent
sudo firewall-cmd --reload
sudo apt install zabbix-agent
sudo vim /etc/zabbix/zabbix_agentd.conf
```

> [...]  
> Server=hostname_of_zabbix_server  
> [...]  
> ServerActive=hostname_of_zabbix_server  
> [...]  
> Hostname=pmx01.rc # Adapt the hostname if needed  
> [...]  
> TLSPSKIdentity=XXXX # Should be filled in according to the "Deploying a New Server" procedure  
> [...]  
> TLSPSKFile=/etc/zabbix/.psk  
> [...]  
> UserParameter=fail2ban_status,systemctl is-active fail2ban  
> UserParameter=fail2ban_num,sudo /etc/zabbix/scripts/fail2ban_num.sh  
> UserParameter=pve-cluster_status,systemctl is-active pve-cluster  
> UserParameter=pvedaemon_status,systemctl is-active pvedaemon  
> UserParameter=pveproxy_status,systemctl is-active pveproxy  
> [...]  
> TLSConnect=psk  
> [...]  
> TLSAccept=psk

```bash
sudo mkdir /etc/zabbix/scripts
sudo vim /etc/zabbix/scripts/fail2ban_num.sh
```

```bash
#!/bin/bash

jails_list=$(fail2ban-client status | grep -w "Jail list:" | cut -f2 | sed s/,//g)

for i in ${jails_list} ; do ban_number=$(( ban_number + $(fail2ban-client status "${i}" | grep -w "Currently banned:" | cut -f2) )) ; done

echo "${ban_number}"
```

```bash
sudo chmod +x /etc/zabbix/scripts/fail2ban_num.sh
sudo vim /etc/sudoers.d/zabbix
```

> zabbix ALL=(ALL) NOPASSWD:/etc/zabbix/scripts/fail2ban_num.sh

```bash
sudo systemctl enable --now zabbix-agent
```

## Configure Proxmox

### Open the port used by Proxmox (and its component) on the firewall

I only open the port for proxmox service's that I use.  
For a full list of port use by the different proxmox services, refer to this link: <https://pve.proxmox.com/wiki/Firewall>

```bash
sudo firewall-cmd --zone=public --add-port=8006/tcp --permanent # Web Interface port
sudo firewall-cmd --zone=public --add-port=3128/tcp --permanent # Spice proxy port
sudo firewall-cmd --reload
```

### Create the cluster

- From the **main** node WebUI (login with the system's `root` credentials):

"Datacenter" --> "Cluster" --> "Create Cluster" (give it a name, select the network interface, then click "Create").

- From the **secondary** node(s) WebUI:

"Datacenter" --> "Cluster" --> "Join Cluster" (copy / paster the join information from the main node, enter the root password of the main node, select the network interface, then click "Join Cluster").

#### Setup the ssh connection between the nodes

Allow ssh connection for the `root` user using key authentication (Proxmox uses ssh with the `root` user of each node for some specific components such as the vncproxy used to access the console of VMs from another node):

```bash
sudoedit /etc/ssh/sshd_config
```

> [...]  
> PermitRootLogin prohibit-password  
> [...]

```bash
sudo systemctl restart sshd
```

In case you don't use the default `22` port for ssh:

```bash
sudoedit /root/.ssh/config
```

> [...]  
> Host "IP_of_node_2"  
> > Port "Port_number"

Then copy the public key of each nodes to the `/root/.ssh/authorized_keys` files (a key pair is automatically created for the `root` during Proxmox's installation).  
Do this between every nodes so they can all connect to each other via `ssh` on the `root` account.

### Setup ZFS pool (for VMs disks)

We're going to create a ZFS pool for VMs disks (allowing to replicate them between the cluster nodes for *semi* HA).

- From **all** nodes:

```bash
sudo fdisk /dev/nvme0n1 # Create a partition on the dedicated disk with all the free space as Linux filesystem
```

- From the **main** node:

From the WebUI (<https://[HOSTNAME]:8006/>):

"*Node name*" --> "Disks" --> "ZFS" --> "Create: ZFS" (give it a name, check the "Add storage" checkbox, select the partition, leave the rest at default unless you wanna do some RAID, then click "Create").

Then (required for free space discard support):

"Datacenter" --> "Storage" --> "*ZFS Pool name*" --> "Edit" (check the "Thin provision" checkbox, then click "OK").

- From the **secondary** node(s):

From the WebUI (<https://[HOSTNAME]:8006/>):

"*Node name*" --> "Disks" --> "ZFS" --> "Create: ZFS" (put the **same** name as the ZFS pool created on the main node, **uncheck** the "Add storage" checkbox, select the partition, leave the rest at default unless you wanna do some RAID, then click "Create").

### Setup ZFS replication and HA (for VMs disks)

To setup ZFS replication for VMs, from the WebUI:

"*VM_Name*" --> "Replication" --> "Add" (select the target node for the replication, the schedule, then click "Create").

This allows to copy VMs Disks to other nodes, allowing to start a VM from the different nodes of the cluster.  
This can be used to setup a *semi* HA (High Availability) where a VM will automatically start from a different node if a node goes down (it's a *semi* HA because the replication is asynchronous, so you will still loose the data written since the last replication).

To setup HA, from the WebUI:

"Datacenter" --> "HA" --> "Add" (select the VM to add to HA, then click "Add").

### Limit ZFS Memory Usage (optional)

ZFS uses 50% of the host memory for the Adaptive Replacement Cache (ARC) by default (see [this link](https://pve.proxmox.com/pve-docs/pve-admin-guide.html#sysadmin_zfs_limit_memory_usage) for details.

If wanted / needed, you can limit ZFS memory usage. But be aware that allocating enough memory for the ARC is crucial for IO performance, so reduce it with caution.  
As a general rule of thumb, it is advised to allocate at least 2 GiB + 1 GiB per TiB of storage in the ZFS pool. For instance, a ZFS pool of 8 TiB storage should use 10 GiB (2 GiB base + 8 GiB as there is 8 TiB storage in the pool).

My ZFS pool is 1 TiB, so I allocate 3 GiB of memory for the ARC *(modify the first number (3) to the desired amount of allocated memory (in GiB)*:

```bash
echo "$[3 * 1024*1024*1024]" | sudo tee /sys/module/zfs/parameters/zfs_arc_max # Modify the value for the current boot
sudo vim /etc/modprobe.d/zfs.conf # Modify the value permanently (this file may not exists yet)
```

> [...]  
> options zfs zfs_arc_max=3221225472 # Result value of `echo "$[3 * 1024*1024*1024]"`

If you have ZFS as your root file system, you have to rebuild your initramfs and reboot as well, see [this link](https://pve.proxmox.com/pve-docs/pve-admin-guide.html#sysadmin_zfs_limit_memory_usage) for details.

### Create Backup / ISO storage

```bash
sudo fdisk /dev/nvme0n2 # Create a partition on the dedicated disk will all the free space as Linux filesystem
```

```bash
sudo mkfs.ext4 /dev/nvme0n2p1 # Format it as ext4 (or whatever other FS you prefer)
sudo mkdir -p /data/proxmox/{backup,iso} # Create mount point and data directories
sudo vim /etc/fstab # Add it to the fstab
```

> [...]  
> #Data  
> UUID=ff3414a7-c564-427c-868f-9a72edccd87d       /data      ext4    defaults        0 2

```bash
sudo mount -a # Mount it
sudo systemctl daemon-reload # Reload systemd
```

Then, from the WebUI (<https://[HOSTNAME]:8006/>):

"Datacenter" --> "Storage" --> "Add" --> "Directory" (give it a name / ID, enter the directory path, select the content and the node, then click "Add").

This should be done for both the `/data/proxmox/backup` directory (content: "VZDump backup file") and the `/data/proxmox/iso` directory (content: "ISO image") on all the nodes of the future cluster.

### Setup backup jobs

To create automatic backup jobs for VMs, from the WebUI:

"Datacenter" --> "Backup" --> "Add" (select the node, backup storage, schedule, VMs to backup, retention threshold from the retention tab, etc..., then click "Create").

### Create a dedicated admin user (and disable the default root pam user access)

From the WebUI (<https://[HOSTNAME]:8006/>):

"Datacenter" --> "Permissions" --> "Users" --> "Add" (fill in all information, Realm: "PVE authentication server", then click "Add").

Then:

"Datacenter" --> "Permissions" --> "Add" --> "User Permission" (Path: "/", User: "username@pve", Role: "PVEAdmin", then click "Add").

Logout from `root` and login with your user, then:

"Datacenter" --> "Permissions" --> "Users" --> "root" --> "Edit" (uncheck the "enabled" checkbox, then click "OK").
