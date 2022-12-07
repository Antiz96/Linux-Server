# SFTP
  
https://en.wikipedia.org/wiki/SSH_File_Transfer_Protocol  
  
SFTP is a file transfer protocol over ssh.  
This procedure aims to set up a SFTP server via the `sshd` service with a dedicated user that is only able to log in via the `sftp` protocol and is "jailed" inside the SFTP directory.  
  

## Installation

The only required service is `sshd`, install and enable it (if not done already).  

**Arch:** 

```
sudo pacman -S openssh
sudo systemctl enable --now sshd
```
  
**Debian:**

```
sudo apt install openssh-server
sudo systemctl enable --now sshd
```

## Configuration

https://citizix.com/how-to-set-up-an-sftp-server-on-debian-11-server/  
  
Create the directory you want the sftp instance to point to:  
```
sudo mkdir -p /path/to/the/sftp/directory #This whole path (the directory itself and its parent directories) has to be owned/writeable by the root user only (chown root: && chmod 755)
```
  
Create the dedicated user, associate a directory for that user inside the SFTP directory and make it not able to log in *(optionally you can create a dedicated group as well, so you can restrict the `sftp` instance to a group of users instead of a specific user)*:   
```
sudo useradd -d /path/to/the/sftp/directory/user_dir -s /usr/sbin/nologin username
```
  
Set a password to the user *(or set up a ssh-key for it if needed/preferred)*:  
```
sudo passwd username
```
  
Create the `sftp` rule in the `sshd` config file:  
```
sudoedit /etc/ssh/sshd_config
```
  
> Match User username #Alternatively, you can match a group of users instead with: `Match Group groupname`  
> > X11Forwarding no  
> > AllowTcpForwarding no  
> > ChrootDirectory /path/to/the/sftp/directory #Make sure to point to the SFTP directory, not the directory dedicated to the user.  
> > ForceCommand internal-sftp  
  
Restart the `sshd` service to apply changes:  
```
sudo systemctl restart sshd
```
