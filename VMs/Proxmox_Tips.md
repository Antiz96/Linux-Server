# Proxmox Tips

## VMs settings/tweaks I use while creating a VM (just a reminder for me)

### System

- Graphic Card --> "Spice" for desktop VMs or "Default" for server VMs
- BIOS --> OVMF (UEFI) **If the VMs boots with an error saying "access denied" and/or into an EFI Shell, recreate it with the "Pre-Enroll keys" option unchecked**
- Agent Qemu --> Check

### Disk

- Check the "Discard" and the "SSD emulation" options (for thin provisioning and trim)

### CPU

- Type --> host
- Enable NUMA (Advanced option) --> checked

### Memory

- Ballooning Device (Advanced option) --> uncheck

### For Desktop VMs only --> Go to hardware tab

- Display --> 128 MiB
- Add --> Audio card --> AC97 --> Spice

## Install the Qemu guest agent (for a better VM management by Proxmox)

- Arch-Linux:

```bash
sudo pacman -S qemu-guest-agent
```

- Debian/Ubuntu:

```bash
sudo apt install qemu-guest-agent
```

- Fedora/RHEL:

```bash
sudo dnf install qemu-guest-agent
```

- Gentoo:

```bash
sudo emerge --ask qemu-guest-agent
```

Then, start and enable it:

```bash
sudo systemctl enable --now qemu-guest-agent
```

## Copy-Paste/Drag-n-drop for Desktop VMs

### Install the Spice agent package

- Arch-Linux:

```bash
sudo pacman -S spice-vdagent
```

- Debian/Ubuntu:

```bash
sudo apt install spice-vdagent
```

- Fedora/RHEL:

```bash
sudo dnf install spice-vdagent
```

- Gentoo:

```bash
sudo emerge --ask spice-vdagent
```

You then may need to start it manually (or reboot) for it to work depending on your system

```bash
sudo systemctl start spice-vdagentd
```

### Autolaunch at boot for IceWM (and standalone Window Manager in general)

```bash
vim ~/.icewm/startup
```

> [...]  
> #Spice Agent  
> /usr/bin/spice-vdagent &  
> /usr/bin/spice-vdagentd &
