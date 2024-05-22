# Gentoo Server Template

Just a quick reminder on how I install a minimal Gentoo Server environment to work with.  
It aims to be turned as a Template.

## Base Install

I basically follow my [Gentoo base installation guide](https://github.com/Antiz96/Linux-Configuration/blob/main/Gentoo/Base_installation.md) with the following exceptions:

- I use a different partition scheme for professional context (see [Partition scheme](https://github.com/Antiz96/Linux-Server/blob/main/VMs/Gentoo_Server_Template.md#partition-scheme))
- I add some packages that are suited for servers to the list of "useful packages to install" (see [Install useful packages](https://github.com/Antiz96/Linux-Server/blob/main/VMs/Gentoo_Server_Template.md#install-useful-packages))
- I do not create a regular user for my personal use during the install. Indeed, this will be handled by an ansible playbook. I do create an "ansible" user for that purpose afterward instead (see [Create and configure the ansible user](https://github.com/Antiz96/Linux-Server/blob/main/VMs/Gentoo_Server_Template.md#create-and-configure-the-ansible-user)).

**Remember to set a password for the root account during the installation process, otherwise you won't be able to log in to the server after reboot!**

## Partition scheme

Replaces the fdisk part in: <https://github.com/Antiz96/Linux-Configuration/blob/main/Gentoo/Base_installation.md#partitioning-the-disk-create-filesystem-and-mount-the-root-and-boot-partitions>

- Professional context:

> EFI partition mounted on /boot --> 1G - FAT32  
> Swap partition --> 4G - SWAP  
> Root partition --> Left free space - XFS - LVM  
> > / --> 3G  
> > /home --> 2G  
> > /tmp --> 2G  
> > /opt --> 2G  
> > /usr --> 4G  
> > /var --> 1G  
> > /var/log --> 4G

### Install useful packages

Replaces: <https://github.com/Antiz96/Linux-Configuration/blob/main/Gentoo/Base_installation.md#install-additional-useful-packages>

```bash
emerge -a bash-completion openssh ssh netkit-telnetd bind-tools wget traceroute rsync zip unzip cronie diffutils mlocate htop logrotate fail2ban
```

### Configure various things

#### Enable services

```bash
systemctl enable --now sshd cronie logrotate.timer fstrim.timer
```

#### Secure SSH connection

```bash
vi /etc/ssh/sshd_config
```

> [...]  
> Port **"X"** #Change the default SSH port (where "X" is the port you want to set)  
> [...]  
> PermitRootLogin no #Disable the SSH connection for the root account  
> [...]  
> PasswordAuthentication no #Disable SSH connexions via password  
> AuthenticationMethods publickey #Authorize only SSH connexions via publickey  
> [...]

```bash
firewall-cmd --add-port=X/tcp --permanent #Open the port we've set for SSH (replace "X" by the port)
firewall-cmd --reload #Apply changes
systemctl restart sshd #Restart the SSH daemon to apply changes
```

#### Configure Fail2Ban

Procedure: <https://github.com/Antiz96/Linux-Server/blob/main/Services/Fail2Ban.md>

#### Install qemu-guest-agent (for proxmox)

```bash
emerge -a qemu-guest-agent
systemctl enable --now qemu-guest-agent
```

#### Install and configure Zabbix Agent

```bash
firewall-cmd --add-port=10050/tcp --permanent
firewall-cmd --reload
emerge -a net-analyzer/zabbix
vim /etc/zabbix/zabbix_agentd.conf
```

> [...]  
> Server=hostname_of_zabbix_server  
> [...]  
> ServerActive=hostname_of_zabbix_server  
> [...]  
> Hostname=template.rc  
> [...]  
> TLSPSKIdentity=  
> [...]  
> TLSPSKFile=/etc/zabbix/.psk  
> [...]  
> UserParameter=fail2ban_status,systemctl is-active fail2ban  
> UserParameter=fail2ban_num,sudo /etc/zabbix/scripts/fail2ban_num.sh

```bash
mkdir /etc/zabbix/scripts
vim /etc/zabbix/scripts/fail2ban_num.sh
```

```text
#!/bin/bash

jails_list=$(fail2ban-client status | grep -w "Jail list:" | cut -f2 | sed s/,//g)

for i in ${jails_list} ; do ban_number=$(( ban_number + $(fail2ban-client status "${i}" | grep -w "Currently banned:" | cut -f2) )) ; done

echo "${ban_number}"
```

```bash
chmod +x /etc/zabbix/scripts/fail2ban_num.sh
vim /etc/sudoers.d/zabbix
```

> zabbix ALL=(ALL) NOPASSWD:/etc/zabbix/scripts/fail2ban_num.sh

```bash
systemctl enable --now zabbix-agent
```

#### Configure the inactivity timeout

```bash
vim /etc/bash/bashrc #Set the inactivity timeout to 15 min
```

> [...]  
> #Set inactivity timeout  
> TMOUT=900  
> readonly TMOUT  
> export TMOUT

### Create and configure the ansible user

Replaces: <https://github.com/Antiz96/Linux-Configuration/blob/main/Gentoo/Base_installation.md#create-a-regular-user>

```bash
useradd -m -u 1000 ansible #Create the ansible user
vim /etc/sudoers.d/ansible #Make the ansible user a sudoer
```

> ansible ALL=(ALL) NOPASSWD: ALL

```bash
mkdir -p /home/ansible/.ssh && chmod 700 /home/ansible/.ssh && chown ansible: /home/ansible/.ssh
touch /home/ansible/.ssh/authorized_keys && chmod 600 /home/ansible/.ssh/authorized_keys && chown ansible: /home/ansible/.ssh/authorized_keys #Create the authorized_keys file for the user ansible
vim /home/ansible/.ssh/authorized_keys #Insert the ansible master server's SSH public key in it (ansible@ansible-server)
```

> Copy the ansible master server's SSH public key here (ansible@ansible-server)

### Setup static IP Address

```bash
nmtui
```

## Reboot

```bash
reboot
```
