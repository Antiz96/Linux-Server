# BlackArch

Just a quick reminder on how I install BlackArch to practice security stuff.

## Install BlackArch Slim (XFCE)

<https://www.blackarch.org/downloads.html#install-iso>

### Partition scheme

> 1G EFI  
> 4G Swap  
> Left ext4 root

## Configuration

### Change user's shell to bash

```bash
sudo vim /etc/passwd #(change /bin/zsh to /bin/bash)
```

### Update the system and reboot

```bash
sudo pacman-key --refresh-keys
sudo pacman -Syu && paru -Syu
reboot
```

### Install dependencies and various stuff

```bash
sudo pacman -S bash-completion traceroute neofetch dmenu zathura zathura-pdf-poppler numlockx
```

#### Install virtualbox addons (if needed)

Insert the addon disk and execute the run file with sudo privileges

#### Install qemu-agent for Proxmox (if needed)

```bash
sudo pacman -S qemu-guest-agent
sudo systemctl enable --now qemu-guest-agent
```

#### Install Spice agent (if needed)

```bash
sudo pacman -S spice-vdagent
sudo systemctl start spice-vdagentd
```

### Start and enable the SSH Daemon

```bash
sudo systemctl enable --now sshd
```

### Reboot to apply changes

```bash
reboot
```

## Generate my ssh key

```bash
ssh-keygen
```

## Bash Theme

<https://github.com/speedenator/agnoster-bash>

```bash
cd /tmp
git clone https://github.com/powerline/fonts.git fonts
cd fonts
sh install.sh
cd $HOME
mkdir -p .bash/themes/agnoster-bash
git clone https://github.com/speedenator/agnoster-bash.git .bash/themes/agnoster-bash
```

## Install my personal config files

```bash
curl https://raw.githubusercontent.com/Antiz96/Linux-Server/main/Dotfiles/Bashrc/BlackArch -o ~/.bashrc
mkdir -p ~/.config/tmux/ && curl https://raw.githubusercontent.com/Antiz96/Linux-Desktop/main/Dotfiles/General/tmux.conf -o ~/.config/tmux/tmux.conf
mkdir -p ~/.config/zathura/ && curl https://raw.githubusercontent.com/Antiz96/Linux-Customisation/main/Dotfiles/General/zathurarc -o ~/.config/zathura/zathurarc && xdg-mime default org.pwmt.zathura.desktop application/pdf
```

## Create my export IP script

```bash
mkdir ~/Documents/scripts
vim ~/Documents/scripts/export_ip.sh
```

> [...]  
> #!/bin/bash  
> export ip="$1" && source ~/.bashrc && env | grep "$1"  && alias | grep "$1"  

```bash
chmod +x ~/Documents/scripts/export_ip.sh
```

## Put my openvpn files (tryhackme and hackthebox) in ~/Documents/Other

```bash
mkdir ~/Documents/Other
```

## Put my cheatsheet in /opt

```bash
sudo vim /opt/cheatsheet.txt
sudo chown antiz: /opt/cheatsheet.txt
```

## Create my wordlist directory

```bash
sudo mkdir /opt/wordlist
sudo cp /usr/share/dirbuster/directory-list-2.3-medium.txt /opt/wordlist/
sudo cp /usr/share/seclists/Passwords/Leaked-Databases/rockyou.txt.tar.gz /opt/wordlist/
sudo tar xvzf /opt/wordlist/rockyou.txt.tar.gz -C /opt/wordlist/ && sudo rm /opt/wordlist/rockyou.txt.tar.gz
sudo chown -R antiz: /opt/wordlist/
```

## Create my busybox directory

```bash
sudo mkdir /opt/busybox
sudo chown antiz: /opt/busybox
```

## Create my revshell directory

```bash
sudo mkdir /opt/revshell
sudo chown antiz: /opt/revshell
```

## Install various tools

```bash
sudo pacman -S sqlitebrowser && paru -S stegseek
```

## Firefox Bookmark

- <https://gtfobins.github.io/>
- <https://pentestmonkey.net/cheat-sheet/shells/reverse-shell-cheat-sheet>
- <https://github.com/swisskyrepo/PayloadsAllTheThings/blob/main/Methodology%20and%20Resources/Reverse%20Shell%20Cheatsheet.md>
- <https://busybox.net/downloads/binaries/1.31.0-i686-uclibc/>

## Settings

- Desktop Shortcut : Firefox, Terminal, File Manager, Trash
- Modify the logout widget in the panel settings --> Action Button - Shutdown, Reboot
- Startup App : Polkit, Numlockx (custom), Network, Settings Daemon, Spice Agent, Pulseaudio, Notification Daemon, xiccd, ssh keys agent, Folder update, Certificate Storage

## Static IP address, if needed

```bash
nmcli con show
sudo nmcli con modify 03994945-5119-3b3c-acbc-b599437851e8 ipv4.addresses 192.168.1.101/24
sudo nmcli con modify 03994945-5119-3b3c-acbc-b599437851e8 ipv4.gateway 192.168.1.254
sudo nmcli con modify 03994945-5119-3b3c-acbc-b599437851e8 ipv4.dns 192.168.1.1
sudo nmcli con modify 03994945-5119-3b3c-acbc-b599437851e8 ipv4.method manual
sudo nmcli con up 03994945-5119-3b3c-acbc-b599437851e8
```
