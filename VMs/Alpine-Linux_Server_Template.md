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

### Switch to https repositories

```bash
sed -i "s/http/https/g" /etc/apk/repositories
```

### Optional - Switch bootloader to Limine

Alpine uses `GRUB` (if UEFI, `Syslinux` otherwise) as the default bootloader.

I'm personally not a fan of `GRUB` so I'm switching to [Limine](https://pkgs.alpinelinux.org/packages?name=limine&branch=edge&repo=&arch=x86_64&origin=&flagged=&maintainer=) instead. See <https://wiki.archlinux.org/title/Limine> for more details.

Remount ESP on `/boot` (Limine expects the ESP to be mounted in the path the distribution puts the kernel and initramfs file to).

```bash
sed -i "s|boot/efi|boot|g" # Update fstab
umount /boot/efi # Umount ESP
rm -rf /boot/* # Clean up /boot
mount -a # Remount ESP on /boot
apk fix --reinstall linux-lts # Reinstall kernel package (to regenerate conf, vmlinuz file and initramfs)
```

Remove grub packages and leftover config file:

```bash
apk del grub grub-efi grub-bash-completion
rm -f /etc/default/grub
```

Install and configure limine:

```bash
apk add limine limine-efi-updater efibootmgr
mkdir -p /boot/EFI/limine
cp /usr/share/limine/BOOTX64.EFI /boot/EFI/limine/
vim /boot/limine.conf
```

```text
# Note: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx should be equal to the root partition UUID

timeout: 5

/Alpine Linux
    protocol: linux
    path: boot():/vmlinuz-lts
    cmdline: root=UUID=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx rw
    module_path: boot():/initramfs-lts
```

Add an entry for limine in NVRAM bootloader:

```bash
efibootmgr \
      --create \
      --disk /dev/sda \
      --part 1 \
      --label "Alpine Linux Limine Boot Loader" \
      --loader '\EFI\limine\BOOTX64.EFI' \
      --unicode
```

Setup automated Limine EFI updater:

```bash
vi /etc/limine/limine-efi-updater.conf
```

```text
[...]
efi_system_partition=/boot # Set path to ESP
[...]
destination_path=/EFI/limine # Set path to EFI executable (path created earlier after installing the limine package)
[...]
#disable_hook=1 # Comment this line for the hook to run automaticall on limine's update
```

Run the hook manually once:

```bash
apk fix limine-efi-updater
```

### Optional - Switch to the edge branch and enable testing repo

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
apk add vim vimdiff man-db sudo bash bash-completion openssh openssh-server-pam socat bind-tools wget traceroute rsync zip unzip diffutils plocate htop curl logrotate fail2ban fstrim chrony firewalld shadow py3-passlib fastfetch acl
```

### Configure various things

#### Modify hosts file

```bash
vim /etc/hosts
```

> [...]  
> 127.0.0.1       localhost  
> ::1             localhost  
> [...]

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

#### Configure apk cache

```bash
setup-apkcache # Setup cache, make it point to `/var/cache/apk`
apk cache -v sync # Initialize cache by removing old packages and download missing packages
vim /etc/local.d/cache.stop # Create a script to remove old packages and download missing packages from cache at shutdown/reboot
```

```text
#!/bin/sh

apk cache -v sync

return 0
```

```bash
chmod +x /etc/local.d/cache.stop # Make the script executable
rc-update add local default # Enable the local service
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
> UsePAM yes  
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

Alpine specific:

```bash
sed -i "s/10/3/g" /etc/fail2ban/jail.d/alpine-ssh.conf
sed -i "s/=\ ssh/=\ 'your_custom_ssh_port'/g" /etc/fail2ban/jail.d/alpine-ssh.conf
touch /var/log/fail2ban.log
rc-update add fail2ban
rc-service fail2ban start
```

#### Install qemu-guest-agent (for proxmox)

```bash
apk add qemu-guest-agent
rc-update add qemu-guest-agent
rc-service qemu-guest-agent start
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
> TLSPSKIdentity=XXXX  
> [...]  
> TLSPSKFile=/etc/zabbix/.psk  
> [...]  
> UserParameter=fail2ban_status_openrc,rc-service fail2ban status | awk '{print $3}'  
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
rc-update add zabbix-agentd
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

## Setup static IP Address

Done during `setup-alpine`.

## Reboot

```bash
reboot
```
