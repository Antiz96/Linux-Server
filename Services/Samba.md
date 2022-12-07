# Samba
  
https://en.wikipedia.org/wiki/Samba_(software)  
  
Samba is a Windows like sharing meant to run on Linux.  
This procedure aims to set up a Samba share server with a dedicated user.  
  

## Installation

**Arch:** 

```
sudo pacman -S samba
sudo systemctl enable --now nmbd smbd
```
  
**Debian:**

```
sudo apt install samba
sudo systemctl enable --now nmbd smbd
```

## Configuration

https://vitux.com/samba-debian-11/  
  
Create the directory to share:  
```
sudo mkdir -p /path/to/the/samba/directory
```
 
Configure the shared directory in the configuration file:  
```
sudoedit /etc/samba/smb.conf
```
  
> [name_of_the_samba_share] #Call it whatever you want  
> comment = Some comments  
> path = /path/to/the/samba/directory  
> read-only = no  
> browsable = yes  
  
Create a password dedicated to the samba share for the user *(the user has to be an existing user on the system and therefore should exist in `/etc/passwd`. Create the user first if needed.)*:  
```
sudo smbpasswd -a username
```
  
Restart the service to apply changes:  
```
sudo systemctl restart smbd
```
  
You can then connect to your Samba instance *(requires the `smbclient` package):  
```
sudo smbclient //Server_Name/Samba_Share_Name -U username
```
