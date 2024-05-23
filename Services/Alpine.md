# Alpine

<https://www.alpinelinux.org>  
<https://wiki.alpinelinux.org/wiki/Raspberry_Pi#Persistent_storage>  
<https://wiki.alpinelinux.org/wiki/Classic_install_or_sys_mode_on_Raspberry_Pi>

Alpine is meant to run from RAM when running on a Raspberry Pi (meaning that you have to run `lbu commit -d` each time you make a change to the system, otherwise said changes are lost after rebooting).  
I personally want a regular/sys install on my Raspberry Pi, which can be done somewhat manually as described in this procedure.

## Prepare the SD card

Create the following partition on the SD Card via `fdisk`:

- Boot  --> /dev/mmcblk0p1, 550M, type c (FAT32 LBA)
- Root  --> /dev/mmcblk0p2, 11G, type 83 (linux)
- Swap  --> /dev/mmcblk0p3, 4G, type 82 (swap)

Create the filesystems:

```bash
mkfs.vfat /dev/mmcblk0p1
mkfs.ext4 /dev/mmcblk0p2
mkswap /dev/mmcblk0p3
```

Download the RPI aarch64 image at <https://alpinelinux.org/downloads/> and extract it into the boot partition:

```bash
mount /dev/mmcblk0p1 /mnt
mv alpine-rpi-3.20.0-aarch64.tar.gz /mnt
cd /mnt
tar -xzvf alpine-rpi-3.20.0-aarch64.tar.gz
rm alpine-rpi-3.20.0-aarch64.tar.gz
```

Un-mount the boot partition, insert the SD card into the Raspberry Pi and start it:

```bash
cd
umount /mnt
```

## Configure Alpine

Once booted on Alpine live environment, use the `setup-alpine` script to configure your system and commit changes once done:

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

Add the boot and swap partitions to the fstab:

```bash
apk add vim
vim /stage/etc/fstab
```

> [...]  
> /dev/mmcblk0p1 /media/mmcblk0p1 vfat defaults 0 0  
> /dev/mmcblk0p3 none swap defaults 0 0

**Also delete the `cdrom` related line from the fstab** (as well as the `usbdisk` one if you do not intend to use usb drives on your Raspberry Pi).

Add the root partition to the boot parameter:

```bash
mount -o remount,rw /media/mmcblk0p1
sed -i '$ s/$/ root=\/dev\/mmcblk0p2/' /media/mmcblk0p1/cmdline.txt
```

Update boot partition:

```bash
rm -f /media/mmcblk0p1/boot/*
cd /stage
rm boot/boot
mv boot/* /media/mmcblk0p1/boot/  
rm -Rf boot
mkdir media/mmcblk0p1
ln -s media/mmcblk0p1/boot boot # You can safely ignore errors here
```

The initial configuration is over! You can now `reboot`, activate swap at boot and proceed with the [post-installation](https://github.com/Antiz96/Linux-Server/blob/main/VMs/Alpine-Linux_Server_Template.md):

```bash
reboot
rc-update add swap boot
```
