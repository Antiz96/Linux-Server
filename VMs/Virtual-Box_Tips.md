# Virtual Box Tips

## Video memory

You can actually go up to 256MB video memory from the sum up of your VM compared to only 128MB in the actual settings of the VM.

## Copy-Paste/Drag-n-drop/Shared-Foler/Autoresize-Screen

### Install the VBox utilities

Requires **kernel headers**

- Arch:

```bash
sudo pacman -S virtualbox-guest-utils
```

- Debian/Ubuntu:

```bash  
sudo apt install virtualbox-guest-utils
```

- Fedora/RHEL:

```bash
sudo dnf install virtualbox-guest-utils
```

Then, enable "Bidirectionnal copy-paste/drag-n-drop" in the peripherals settings of Virtual Box.

### Autolaunch at boot for standalone Window Manager

```bash
vim ~/.icewm/startup
```

> [...]  
> #VirtualBox utils (Copy-Paste/Drag-n-Drop/Shared-Folder/Autoresize-Screen)  
> exec VBoxClient-all

### Reboot to apply

```bash
reboot
```
