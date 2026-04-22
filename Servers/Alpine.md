# Alpine

<https://www.alpinelinux.org>  
<https://wiki.alpinelinux.org/wiki/Raspberry_Pi#Persistent_storage>  
<https://wiki.alpinelinux.org/wiki/Classic_install_or_sys_mode_on_Raspberry_Pi>

I run Alpine on my Raspberry Pi servers.

Alpine is meant to run from RAM when running on a Raspberry Pi (meaning that you have to run `lbu commit -d` each time you make a change to the system for said change to be persistent).  
I personally prefer running a regular / sys install on my Raspberry Pi, which can be done somewhat manually as described in this procedure.

## Prepare the SD card

Create the following partition on the SD Card via `fdisk`:

- Boot  --> /dev/mmcblk0p1, 1G, type c (FAT32 LBA)
- Root  --> /dev/mmcblk0p2, 30G, type 83 (linux)
- Data  --> /dev/mmcblk0p3, all free space remaining, type 83 (linux) # If needed

(Swap is done as a swapfile later)

Create the filesystems:

```bash
sudo mkfs.vfat /dev/mmcblk0p1
sudo mkfs.ext4 /dev/mmcblk0p2
sudo mkfs.ext4 /dev/mmcblk0p3
```

Download the RPI aarch64 image at <https://alpinelinux.org/downloads/> and extract it into the boot partition:

```bash
sudo mount /dev/mmcblk0p1 /mnt
sudo mv ~/Downloads/alpine-rpi-3.23.4-aarch64.tar.gz /mnt
cd /mnt
sudo tar -xzvf alpine-rpi-3.23.4-aarch64.tar.gz
sudo rm alpine-rpi-3.23.4-aarch64.tar.gz
```

Umount the boot partition, insert the SD card into the Raspberry Pi and start it:

```bash
cd
sudo umount /mnt
```

## Configure Alpine

Once booted in the Alpine live environment, use the `setup-alpine` script to configure your system and commit changes once done:

```bash
setup-alpine
lbu commit -d
```

## Setup disk install

Create a mountpoint for the root partition and move the configured system to it (you can ignore errors related to syslinux/extlinux during the execution of `setup-disk` script):

```bash
mkdir /stage
mount /dev/mmcblk0p2 /stage
export FORCE_BOOTFS=1
setup-disk -o /media/mmcblk0p1/MYHOSTNAME.apkovl.tar.gz /stage
```

Add the boot partition to the fstab:

```bash
apk add vim
blkid # To note /dev/mmcblk0p1 UUID
vim /stage/etc/fstab
```

> [...]  
> UUID=XXXX-XXXX    /boot    defaults    0 0

**Also delete the `cdrom` related line from the fstab** (as well as the `usbdisk` one if you do not intend to use usb drives on your Raspberry Pi).

Add the data partition to the fstab (if needed):

```bash
mkdir /stage/data
blkid # To note /dev/mmcblk0p3 UUID
vim /stage/etc/fstab
```

> [...]  
> UUID=XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX    /data    rw,relatime    0 1

Create swapfile and add it to the fstab:

```bash
fallocate -l 4G /stage/swapfile
chmod 600 /stage/swapfile
mkswap /stage/swapfile
swapon /stage/swapfile
vim /stage/etc/fstab
```

> [...]  
> /swapfile    none    swap    defaults    0 0

Add the root partition to the boot parameter:

```bash
mount -o remount,rw /media/mmcblk0p1
vim /media/mmcblk0p1/cmdline.txt
```

> [...] root=UUID=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx rw # Append at the end of the existing line, UUID is the one of the root partition

## Reboot and post-install configuration step

The disk installation / configuration is now over.  
You can umount the "stage" directory and `reboot` into the installed system:

```bash
umount -l /stage
reboot
```

Once the system is booted, activate swap at boot and remove the (now useless) `/media/mmcblk0p1/apks` reference in `/etc/apk/repositories` file to avoid related warning when using `apk`:

```bash
rc-update add swap boot
vi /etc/apk/repositories
```

> /media/mmcblk0p1/apks # Delete this line

You can now proceed with the [post-installation](https://github.com/Antiz96/Linux-Server/blob/main/VMs/Alpine-Linux_Server_Template.md):
