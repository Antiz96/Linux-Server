# Red Hat Server Template

Just a quick reminder on how I install a minimal Redhat Server environment to work with.  
It aims to be turned as a template.

## Base Install

I basically follow each installation steps normally with the following exceptions:

- I use a different partition scheme for professional context (see [Partition scheme](https://github.com/Antiz96/Linux-Server/blob/main/VMs/RHEL_Server_Template.md#partition-scheme))
- I don't check anything during the **Software selection** step so I get a minimal installation. I install useful packages after the installation instead (see [Install useful packages](https://github.com/Antiz96/Linux-Server/blob/main/VMs/RHEL_Server_Template.md#install-useful-packages))
- I don't create any user during the installation process. Indeed, this will be handled by an ansible playbook. I do create an "ansible" user for that purpose afterward instead (see [Create and configure the ansible user](https://github.com/Antiz96/Linux-Server/blob/main/VMs/RHEL_Server_Template.md#create-and-configure-the-ansible-user)).

**Remember to set a password for the root account during the installation process, otherwise you won't be able to log in to the server after reboot!**

### Partition scheme

- Professional context:

> EFI partition mounted on /boot --> 1G - ESP  
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

```bash
dnf update && dnf install sudo vim man bash-completion openssh-server bind-utils traceroute rsync zip unzip diffutils firewalld mlocate curl openssl telnet chrony wget fail2ban epel-release && dnf install htop logrotate python3-passlib
```

### Configure various things

#### Set Selinux to "permissive"

- For the current session:

```bash
setenforce 0
```

- Permanently:

```bash
vim /etc/selinux/config
```

> [...]  
> SELINUX=permissive  
> [...]

#### Enable services

```bash
systemctl enable --now sshd logrotate.timer fstrim.timer
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
systemctl restart sshd #Restart the SSH daemon to apply changes (if it fails, you probably have to configure firewalld to accept the port you set first)
```

#### Configure Fail2Ban

Procedure: <https://github.com/Antiz96/Linux-Server/blob/main/Services/Fail2Ban.md>

#### Configure the firewall

```bash
systemctl enable --now firewalld #Autostart the firewall at boot.
firewall-cmd --remove-service="ssh" --permanent #Remove the opened ssh port by default as my PC doesn't run a ssh server.
firewall-cmd --remove-service="dhcpv6-client" --permanent #Remove the opened DHCPV6-client port by default as I don't use it.
firewall-cmd --add-port=X/tcp --permanent #Open the port we've set for SSH (replace "X" by the port)
firewall-cmd --reload #Apply changes
```

#### Install qemu-guest-agent (for proxmox)

```bash
dnf install qemu-guest-agent
systemctl enable --now qemu-guest-agent
```

#### Install and configure Zabbix Agent

```bash
firewall-cmd --add-port=10050/tcp --permanent
firewall-cmd --reload
dnf install zabbix-agent
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
> [...]  
> TLSConnect=psk  
> [...]  
> TLSAccept=psk

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
vim /etc/bash.bashrc #Set the inactivity timeout to 15 min
```

> [...]  
> #Set inactivity timeout  
> TMOUT=900  
> readonly TMOUT  
> export TMOUT

### Create and configure the ansible user

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

## Reboot

```bash
reboot
```
