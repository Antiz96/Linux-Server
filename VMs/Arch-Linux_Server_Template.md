# Arch-Linux Server Template

Just a quick reminder on how I install a minimal Arch Server environment to work with.  
It aims to be turned as a Template.  

## Base Install

I basically follow my [Arch-Linux base installation guide](https://github.com/Antiz96/Linux-Configuration/blob/main/Arch-Linux/Base_installation.md) with the following exceptions:  
  
- I use a different partition scheme for professional context (see [Partition scheme](https://github.com/Antiz96/Server-Configuration/blob/main/VMs/Arch-Linux_Server_Template.md#partition-scheme))
- I add some packages that are suited for servers to the list of "useful packages to install" (see [Install useful packages](https://github.com/Antiz96/Server-Configuration/blob/main/VMs/Arch-Linux_Server_Template.md#install-useful-packages))
- I do not create a regular user for my personal use during the install. Indeed, this will be handled by an ansible playbook. I do create an "ansible" user for that purpose afterward instead (see [Create and configure the ansible user](https://github.com/Antiz96/Server-Configuration/blob/main/VMs/Arch-Linux_Server_Template.md#create-and-configure-the-ansible-user)).  
**Remember to set a password for the root account during the installation process, otherwise you won't be able to log in to the server after reboot !**

## Partition scheme

Replaces: https://github.com/Antiz96/Linux-Configuration/blob/main/Arch-Linux/Base_installation.md#preparing-the-disk

- Personal context:  
  
> EFI partition mounted on /boot/EFI --> 550M - ESP  
> Swap partition --> 4G - SWAP  
> Root partition mounted on / --> Left free space - EXT4    
  
- Professional context:  
  
> EFI partition mounted on /boot --> 550M - FAT32    
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

Replaces: https://github.com/Antiz96/Linux-Configuration/blob/main/Arch-Linux/Base_installation.md#log-in-with-the-regular-user-previously-created-and-install-additional-useful-packages

```
pacman -S base-devel linux-headers man bash-completion intel-ucode openssh inetutils dnsutils wget traceroute rsync zip unzip cronie diffutils mlocate htop parted postfix pacman-contribsudo grub-mkconfig -o /boot/grub/grub.cfg 
```

### Configure various things

#### Enable ssh and cronie   

```
systemctl enable --now sshd cronie
```

#### Secure SSH connexion

```
vi /etc/ssh/sshd_config
```

> [...]  
> Port **"X"** #Change the default SSH port (where "X" is the port you want to set)     
> [...]  
> PermitRootLogin no #Disable the SSH connexion for the root account  
> [...]    
> PasswordAuthentication no #Disable SSH connexions via password  
> AuthenticationMethods publickey #Authorize only SSH connexions via publickey  
> [...]  

```
firewall-cmd --add-port=X/tcp --permanent #Open the port we've set for SSH (replace "X" by the port)
firewall-cmd --reload #Apply changes
systemctl restart sshd #Restart the SSH daemon to apply changes
```

#### Install qemu-guest-agent (for proxmox)

```
pacman -S qemu-guest-agent
systemctl enable --now qemu-guest-agent
```

#### Install and configure Zabbix Agent

```
firewall-cmd --add-port=10050/tcp --permanent
firewall-cmd --reload
pacman -S zabbix-agent
vim /etc/zabbix/zabbix_agentd.conf
```

> [...]    
> Server=hostname_of_zabbix_server  
> [...]  
> ServerActive=hostname_of_zabbix_server  
> [...]  
> Hostname=template.rc 

```
systemctl enable --now zabbix-agent
``` 

#### Configure the inactivity timeout

```
sudo vim /etc/bash.bashrc #Set the inactivity timeout to 15 min
```

> [...]  
> #Set inactivity timeout  
> TMOUT=900  
> readonly TMOUT  
> export TMOUT  

### Create and configure the ansible user

Replaces: https://github.com/Antiz96/Linux-Configuration/blob/main/Arch-Linux/Base_installation.md#user-configuration

```
useradd -m -u 1000 ansible #Create the ansible user
vim /etc/sudoers.d/ansible #Make the ansible user a sudoer
```

> ansible ALL=(ALL) NOPASSWD: ALL  
  
```
mkdir -p /home/ansible/.ssh && chmod 700 /home/ansible/.ssh && chown ansible: /home/ansible/.ssh
touch /home/ansible/.ssh/authorized_keys && chmod 600 /home/ansible/.ssh/authorized_keys && chown ansible: /home/ansible/.ssh/authorized_keys #Create the authorized_keys file for the user ansible
vim /home/ansible/.ssh/authorized_keys #Insert the ansible master server's SSH public key in it (ansible@ansible-server)
```

> Copy the ansible master server's SSH public key here (ansible@ansible-server)  

### Setup static IP Address

```
nmcli con show 
nmcli con modify 03994945-5119-3b3c-acbc-b599437851e8 ipv4.addresses 192.168.1.100/24
nmcli con modify 03994945-5119-3b3c-acbc-b599437851e8 ipv4.gateway 192.168.1.254
nmcli con modify 03994945-5119-3b3c-acbc-b599437851e8 ipv4.dns 192.168.1.1
nmcli con modify 03994945-5119-3b3c-acbc-b599437851e8 ipv4.method manual
nmcli con up 03994945-5119-3b3c-acbc-b599437851e8 
```

## Reboot

```
reboot
```
