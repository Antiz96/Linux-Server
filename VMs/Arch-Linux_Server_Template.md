# Arch Linux Server Template

Just a quick reminder on how I install a minimal Arch Server environment to work with.  
It aims to be turned as a Template.

## Base Install

I basically follow my [Arch-Linux base installation guide](https://github.com/Antiz96/Linux-Desktop/blob/main/Arch-Linux/Base_installation.md) with the following exceptions:

- I use a different partition scheme for professional context (see [Partition scheme](https://github.com/Antiz96/Linux-Server/blob/main/VMs/Arch-Linux_Server_Template.md#partition-scheme)).
- I use the `linux-lts` kernel (instead of the `linux` one).
- I use `systemd-networkd` rather than `network-manager` (see [Setup Networking](#setup-networking).
- I use a different list of "useful packages to install", more suited for servers (see [Install useful packages](#install-useful-packages)).
- I do not create a regular user for my personal use during the install. Indeed, this will be handled by an ansible playbook. I do create an "ansible" user for that purpose afterward instead (see [Create and configure the ansible user](#create-and-configure-the-ansible-user)).

**Remember to set a password for the root account during the installation process, otherwise you won't be able to log in to the server after reboot!**

## Partition scheme

Replaces: <https://github.com/Antiz96/Linux-Desktop/blob/main/Arch-Linux/Base_installation.md#preparing-the-disk>

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

## Setup Networking

Replaces: <https://github.com/Antiz96/Linux-Desktop/blob/main/Arch-Linux/Base_installation.md#install-and-enable-network-manager>

```bash
systemctl enable systemd-networkd systemd-resolved
ln -sf /run/systemd/resolve/stub-resolv.conf /etc/resolv.conf
vim /etc/systemd/network/enp1s0.network # Adapt the name of the file according to your network card name
```

> #Adapt parameters as needed  
> [Match]  
> Name=enp1s0  
>  
> [Network]  
> Address=192.168.96.100/24  
> Gateway=192.168.96.254  
> DNS=192.168.96.1  
> IPv6AcceptRA=no

## Optional - Install and enable AppArmor

AppArmor is a kernel security module that restricts individual programs' capabilities.

Unlike Debian, Arch Linux does not install or enable AppArmor by default, and it does not maintain an extensive set of extra profiles.  
Nevertheless, the default profiles provided with AppArmor cover a few programs I commonly use, and some applications I install on my servers (e.g. Docker) include their own profiles. Therefore, enabling AppArmor still provides *some* additional layer of security, which is why I usually install and enable it.

```bash
pacman -S apparmor # Install AppArmor
vim /boot/loader/entries/arch.conf # Add AppArmor to the lsm parameter of the kernel cmdline in systemd-boot config
```

> [...]  
> options root=UUID=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx rw **lsm=landlock,lockdown,yama,integrity,apparmor,bpf**  
> [...]

```bash
systemctl enable apparmor.service # Start AppArmor service automatically on boot
reboot
aa-enabled # Verify that AppArmor is running
aa-status # Check the list of profile and their status
```

## Install useful packages

Replaces: <https://github.com/Antiz96/Linux-Desktop/blob/main/Arch-Linux/Base_installation.md#log-in-with-the-regular-user-previously-created-and-install-additional-useful-packages>

```bash
pacman -S man bash-completion openssh socat dnsutils wget traceroute rsync diffutils plocate htop logrotate pacman-contrib fail2ban python-passlib fastfetch
pacman -S --asdeps fakeroot # required for `checkupdates`
```

## Configure various things

### Enable services/timers

```bash
systemctl enable --now sshd logrotate.timer
```

### Secure SSH connection

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

### Configure Fail2Ban

Procedure: <https://github.com/Antiz96/Linux-Server/blob/main/Services/Fail2Ban.md>

### Install qemu-guest-agent (for proxmox)

```bash
pacman -S qemu-guest-agent
systemctl enable --now qemu-guest-agent
```

### Install and configure Zabbix Agent

```bash
firewall-cmd --add-port=10050/tcp --permanent
firewall-cmd --reload
pacman -S zabbix-agent
chage -E -1 zabbix # Remove eventual auto expiration on the zabbix account to allow it to run sudo (for User Parameters that may require privilege elevation)
vim /etc/zabbix/zabbix_agentd.conf
```

> [...]  
> Server=hostname_of_zabbix_server  
> [...]  
> ServerActive=hostname_of_zabbix_server  
> [...]  
> Hostname=template  
> [...]  
> TLSPSKIdentity=XXXX  
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

### Configure the inactivity timeout

```bash
vim /etc/bash.bashrc #Set the inactivity timeout to 15 min
```

> [...]  
> #Set inactivity timeout  
> TMOUT=900  
> readonly TMOUT  
> export TMOUT

## Create and configure the ansible user

Replaces: <https://github.com/Antiz96/Linux-Desktop/blob/main/Arch-Linux/Base_installation.md#user-configuration>

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
