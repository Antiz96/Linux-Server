# Proxmox

<https://www.proxmox.com>

I personally install ProxMox as a service over a regular install of Debian.

## Pre-Installation

### Open the port used by Proxmox (and its component) on the firewall

I only open the port for proxmox service's that I use.  
For a full list of port use by the different proxmox services, refer to this link: <https://pve.proxmox.com/wiki/Firewall>

```bash
sudo firewall-cmd --zone=public --add-port=8006/tcp --permanent #Web Interface port
sudo firewall-cmd --zone=public --add-port=3128/tcp --permanent #Spice proxy port
sudo firewall-cmd --reload
```

### Create the Proxmox main directory and the Proxmox data directory

(...to store the VMs, backups, etc...)

```bash
sudo mkdir -p /proxmox/vms
sudo mkdir -p /storage/proxmox
```

## Installation

### Install Proxmox on Debian

**Official Guide:**  
<https://pve.proxmox.com/wiki/Install_Proxmox_VE_on_Debian_12_Bookworm>

If you're facing a dependency problem during this installation (**dpkg deb error subprocess paste was killed by signal broken pipe**), then:

```bash
sudo dpkg -P qemu-system-data && sudo apt install -f
```

*User Management Part: Create regular user in Datacenter --> Permissions --> User (PVE Realm) and then, add PVEAdmin role in the main "Permission tab"*  
*Linux Bridge Part: In Proxmox --> System --> Network, edit the actual network card and delete IP/Netmask and Gateway. Once done, create a new "Linux Bridge" card, add the IP/Netmask and Gateway and add the actual network card as the bridge port.*

### Install additional useful packages

```bash
sudo apt install ksmtuned
```

*ksmtuned --> Allows the use of ballooning (<https://pve.proxmox.com/wiki/Dynamic_Memory_Management>).*

## Access

You can now access the Proxmox's web interface on the following URL:  
`https://[HOSTNAME]:8006/`

## Configuration

### Add storages for VMs, Backups and ISO via the Proxmox Web interface

Datacenter --> Storage

ADD - Type : directory | ID : Backup | Directory : /storage/proxmox/backup | Content : VZDump Backup File  
ADD - Type : directory | ID : ISO | Directory : /storage/proxmox/iso | Content : ISO Image  
ADD - Type : directory | ID : VMs | Directory : /proxmox/vms | Content : Disk Image  
ADD - Type : directory | ID : VMs_Data | Directory : /storage/proxmox/vms_data | Content : Disk Image  
EDIT local directory --> uncheck "enabled" checkbox

### Disable the root account on the Web Interface (for security reasons)

Log in to your regular PVEAdmin account

Datacenter --> Permissions --> User  
EDIT root account --> uncheck "enabled" checkbox  
(you can reactivate it the same way if you need it for some reasons)

### Modify proxmox repo from "enterprise" to "no-subscription"

*(...as I do not use a subscription for my personal needs, I do not have a subscription key. Therefore, I cannot authenticate to the enterprise repo and use it, leading to an error "401 unauthorized" when performing "apt update").*  

```bash
sudo vim /etc/apt/sources.list.d/pve-enterprise.list
```

> **#** deb <https://enterprise.proxmox.com/debian/pve bookworm pve-enterprise>

```bash
sudo vim /etc/apt/sources.list.d/pve-no-subscription.list
```

> deb <http://download.proxmox.com/debian bookworm pve-no-subscription>

### Get rid of the "No valid subscription key found" message when logging in to the web interface

```bash
sudo cp -p /usr/share/javascript/proxmox-widget-toolkit/proxmoxlib.js /usr/share/javascript/proxmox-widget-toolkit/proxmoxlib.js-bck
sudo vim /usr/share/javascript/proxmox-widget-toolkit/proxmoxlib.js
```

--> Change "Ext.Msg.show" to "void"

> [...]  
> **void**({  
> >  title: gettext('No valid subscription'),  
> > [...]

```bash
sudo systemctl restart pveproxy
```

## Update/Upgrade procedure

Proxmox and its components are no more than regular packages installed on the Debian server.  
So the update/upgrade procedure is basically just update/upgrade your system.

### Update Proxmox

```bash
sudo apt update && sudo apt full-upgrade
```

### Upgrade Proxmox

Proxmox upgrades usually follow Debian upgrade.  
You have to fully update the system, change Debian and Proxmox repos to the new Debian version (for instance, from "bullseye" to "bookworm") and perform an update + dist-upgrade.  
Usually, Proxmox write a wiki page/tutorial on how to upgrade from a major release to another, both for Debian and Proxmox at the same time.

For instance, upgrade from "bullseye" to "bookworm" and from Proxmox v7 to Proxmox v8:  
<https://pve.proxmox.com/wiki/Upgrade_from_7_to_8>

I personally prefer to reinstall Debian and Proxmox completely from scratch by following this procedure in case of major upgrade.
