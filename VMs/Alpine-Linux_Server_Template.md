# Alpine Linux Server Template

Just a quick reminder on how I install a minimal Alpine Server environment to work with.  
It aims to be turned as a Template.

## Base Install

I basically follow installation steps normally (`setup-alpine`) with the following exceptions:

- I use a different partition scheme for professional context (see [Partition scheme](https://github.com/Antiz96/Linux-Server/blob/main/VMs/Arch-Linux_Server_Template.md#partition-scheme)).
- I do not create a regular user for my personal use during the install. Indeed, this will be handled by an ansible playbook. I do create an "ansible" user for that purpose afterward instead (see [Create and configure the ansible user](https://github.com/Antiz96/Linux-Server/blob/main/VMs/Alpine-Linux_Server_Template.md#create-and-configure-the-ansible-user)).

### Partition scheme

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

### Switch to the edge branch and active testing repo - Optional

I personally depends on a few packages that are still currently in the testing repositories.  
As such, I currently switch to the edge branch of the repositories (which basically turns Alpine into a rolling release) and I activate the testing repositories.

```bash
vi /etc/apk/repositories
```

> "repo_url"/pub/alpine/**edge**/main  
> "repo_url"/pub/alpine/**edge**/community  
> "repo_url"/pub/alpine/**edge/testing**

```bash
apk update && apk upgrade
```

### Install useful packages

```bash
apk add vim man-db sudo bash bash-completion openssh inetutils-telnet bind-tools wget traceroute rsync zip unzip diffutils mlocate htop curl logrotate fail2ban fstrim chrony firewalld shadow
```

### Configure various things

#### Set bash as the default shell

```bash
chsh -s /bin/bash
```

#### Configure sudo

```bash
visudo
```

> [...]  
> %wheel ALL=(ALL) ALL  
> [...]

#### Enable services

```bash
rc-update add sshd
rc-service sshd start
rc-update add chronyd
rc-service chronyd start
rc-update add firewalld
rc-service firewalld start
```

#### Auto clean packages cache on reboot

```bash
vim /etc/local.d/cache.stop
```

```bash
#!/bin/sh

apk cache -v sync

return 0
```

#### Secure SSH connection

```bash
vim /etc/ssh/sshd_config
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
firewall-cmd --remove-service="ssh" --permanent #Remove the opened ssh port by default as my PC doesn't run a ssh server.
firewall-cmd --remove-service="dhcpv6-client" --permanent #Remove the opened DHCPV6-client port by default as I don't use it.
firewall-cmd --add-port=X/tcp --permanent #Open the port we've set for SSH (replace "X" by the port)
firewall-cmd --reload #Apply changes
rc-service sshd restart
```

#### Configure Fail2Ban

Procedure: <https://github.com/Antiz96/Linux-Server/blob/main/Services/Fail2Ban.md>

#### Install qemu-guest-agent (for proxmox)

```bash
apk add qemu-guest-agent
rc-update add qemu-guest-agent
```

#### Install and configure Zabbix Agent

```bash
firewall-cmd --add-port=10050/tcp --permanent
firewall-cmd --reload
echo "readproc:x:30:zabbix" >> /etc/group
apk add zabbix-agent
vim /etc/zabbix/zabbix_agentd.conf
```

> [...]  
> Server=hostname_of_zabbix_server  
> [...]  
> ServerActive=hostname_of_zabbix_server  
> [...]  
> Hostname=template.rc  
> [...]  
> UserParameter=fail2ban_status,rc-service fail2ban status | awk '{print $3}'  
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
rc-update add zabbix-agentd
rc-service zabbix-agentd start
```

#### Configure the inactivity timeout

```bash
vim /etc/profile
```

```text
[...]
# Source global bash config, when interactive but not posix or sh mode
if test "$BASH" &&\
   test "$PS1" &&\
   test -z "$POSIXLY_CORRECT" &&\
   test "${0#-}" != sh &&\
   test -r /etc/bash.bashrc
then
        . /etc/bash.bashrc
fi
```

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

```bash
chsh ansible -s /bin/bash #Set the default ansible's shell to /bin/bash
```

For some reason, it seems like you cannot log to an account via SSH using a key authentication if the said account [never had a password set before](https://stackoverflow.com/questions/61833713/how-to-login-by-ssh-in-alpine-linux-without-passwords) on Alpine.  
The workaround is to set a password to the account, and then delete it:

```bash
passwd ansible
passwd -d ansible
```

## Setup static IP Address

Done during `setup-alpine`.

## Reboot

```bash
reboot
```
