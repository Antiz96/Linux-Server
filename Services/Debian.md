# Debian

<https://www.debian.org/>

**If it's the very first install, remember to enable the intel virtualization technology (for Proxmox) and disable secure boot in the BIOS.**  
<https://rog.asus.com/us/support/FAQ/1043786>  
<https://techzillo.com/how-to-disable-or-enable-secure-boot-for-asus-motherboard/>

I do perform a **minimal installation**.  
I do not select anything during the installation process (no DE, no standard or additional packages, etc...)

## Partitioning

**System Disk:**  

- ESP   --> 1 GB
- Swap  --> 4 GB
- /     --> 25 GB (0% reserved block) - ext4
- /data --> Left free space (0% reserved block) - ext4

**Secondary Disk:**

- /storage --> All free space (0% reserved block) - ext4

## Install sudo and give sudo privileges to the regular user

As root (**only for this part**):

```bash
apt install sudo
usermod -aG sudo antiz
```

## Setup a static IP Address (if not done already during the installation process)

```bash
sudo vi /etc/network/interfaces
```

> [...]  
> iface enp0s3 inet static  
> > address 192.168.1.2/24  
> > gateway 192.168.1.254  
> > dns-nameservers 192.168.1.1

```bash
sudo systemctl restart networking
```

## Update the server and install useful packages

```bash
sudo apt update && sudo apt full-upgrade
sudo apt install vim man bash-completion openssh-server dnsutils traceroute rsync zip unzip diffutils firewalld plocate htop curl openssl telnet chrony wget logrotate fail2ban python3-passlib fastfetch
sudo systemctl enable --now logrotate.timer
```

## Secure SSH connexions

### Change the default SSH port

```bash
sudo vim /etc/ssh/sshd_config
```

> [...]  
> Port **"X"** *#Where "X" is the port you want to set*  
> [...]

### Disable ssh connection for the root account

```bash
sudo vim /etc/ssh/sshd_config
```

> [...]  
> PermitRootLogin no  
> [...]

### Creating a SSH key on the client, copying the public key to the server and restrict SSH connexions method to public key authentication

**On the client:**

```bash
ssh-keygen -t rsa -b 4096 #Choose a relevant name to remember on which server/service/entity you use this key. Also, set a strong passphrase for encryption.
ssh-copy-id -i ~/.ssh/"keyfile_name".pub "user"@"server" #Change "keyfile_name", "user" and "server" according to your environment
```

**On the Server:**

```bash
sudo vim /etc/ssh/sshd_config
```

> [...]  
> PasswordAuthentication no  
> AuthenticationMethods publickey

### Restart the SSH service to apply changes

**If you already have a firewall service running, be sure you opened the port you've set earlier for SSH before restarting the service, otherwise you won't be able to log back to your server.**  
At that point, I do not have a firewall service running, but I'll take care of that in the next step.

```bash
sudo systemctl restart sshd
```

Also, be aware that, from now, you'll need to specify the port and the private key to connect to ssh, like so : `ssh -p "port" -i "/path/to/privatekey" "user"@"server"`.  
However, you can create a config file in "~/.ssh" to avoid having to specify the port, the user and/or the ssh key each time:

```bash
vim ~/.ssh/config
```

> Host **"Host alias"**  
> > User **"Username"**  
> > Port **"SSH port"**  
> > IdentityFile **"Path to the keyfile"**  
> > Hostname **"Hostname of the server"**

## Configure and start the firewall

```bash
sudo systemctl enable --now firewalld
sudo firewall-cmd --add-port=X/tcp --permanent #Open the port we've set for SSH (replace "X" by the port)
sudo firewall-cmd --remove-service="ssh" --permanent #Close the default SSH port
sudo firewall-cmd --remove-service="dhcpv6-client" --permanent #Close the dhcpv6-client port as I don't use it
sudo firewall-cmd --reload
```

## Configure and start fail2ban

Procedure: <https://github.com/Antiz96/Linux-Server/blob/main/Services/Fail2Ban.md>

## Enable fstrim (for SSDs only - optional)

If you use SSDs, you can use `fstrim` to discard all unused blocks in the filesystem in order to improve performances.  
You can launch it manually by running `sudo fstrim -av`, but keep in mind that it is not recommended to launch it too frequently. It is commonly approved that running it once a week is a sufficient frequency for most desktop and server systems.

To launch `fstrim` automatically on a weekly basis, enable the associated systemd timer:

```bash
sudo systemctl enable --now fstrim.timer
```

## Enable Wake On Lan

<https://www.asus.com/support/FAQ/1045950/>  
<https://wiki.debian.org/WakeOnLan>

### Enable Wake On Lan in the BIOS

- pmx01:

DEL Key at startup to go to the BIOS  
Advanced --> APM Configuration --> Power On By PCI-E --> Enabled

- pmx02:

No related option in BIOS, it seems activated by default.

### Enable Wake On Lan in Debian

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

### Using Wake On Lan

<https://wiki.debian.org/WakeOnLan#Using_WOL>

#### Install wakeonlan on the client

```bash
paru -S wakeonlan || sudo apt install wakeonlan
```

#### Get the network card's mac address of the server

```bash
ip a
```

> [...]  
> 2: enp3s0: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast maaster vmbr0 state UP group default qlen 1000  
> link/ether **7c:10:c9:8c:88:9d** brd ff:ff:ff:ff:ff:ff  
> [...]

#### Power on the server remotely from the client

```bash
wakeonlan 7c:10:c9:8c:88:9d
```

## Download my .bashrc

```bash
curl https://raw.githubusercontent.com/Antiz96/Linux-Server/main/Dotfiles/Bashrc/Debian-Ubuntu-Server -o ~/.bashrc
source ~/.bashrc
```
