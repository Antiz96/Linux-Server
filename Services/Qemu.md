# Qemu

<https://www.qemu.org/>

## Install qemu on Arch

```bash
sudo pacman -S qemu-base libvirt
```

To use the QXL video driver / SPICE for VMs with graphical environments:

```bash
sudo pacman -S qemu-hw-display-qxl qemu-audio-spice qemu-ui-spice-core qemu-ui-spice-app
```

## Create rootless / unprivileged VMs

I personally create and manage my VMs as an unprivileged user (for convenience and security reasons).

```bash
mkdir -p /data/qemu/{vms,iso} # Create a directory to store VMs and ISOs
mkdir /data/qemu/vms/arch-dev # Create a directory to store the VM files
qemu-img create -f qcow2 /data/qemu/vms/arch-dev/arch-dev.qcow2 30G # Create a 30G virtual disk for the VM
cp /usr/share/edk2/x64/OVMF_VARS.4m.fd /data/qemu/vms/arch-dev/OVMF_VARS.4m.fd # Copy pflash drive for UEFI support
```

Download the ISO (if not done already):

```bash
curl https://fastly.mirror.pkgbuild.com/iso/2026.02.01/archlinux-2026.02.01-x86_64.iso -o /data/qemu/iso/archlinux-2026.02.01-x86_64.iso
```

Create the config file for the VM:

```bash
vim /data/qemu/vms/arch-dev/arch-dev.xml
```

```xml
<domain type='kvm'>
  <!-- VM Name -->
  <name>arch-dev</name>

  <!-- Resource allocation -->
  <memory unit='MiB'>4096</memory>
  <vcpu>4</vcpu>

  <os>
    <type arch='x86_64'>hvm</type>

    <!-- UEFI -->
    <loader readonly='yes' type='pflash'>/usr/share/edk2/x64/OVMF_CODE.4m.fd</loader>
    <nvram>/data/qemu/vms/arch-dev/OVMF_VARS.4m.fd</nvram>

    <!-- Boot order -->
    <boot dev='cdrom'/>
    <boot dev='hd'/>
  </os>

  <!-- UEFI features -->
  <features>
    <acpi/>
    <apic/>
  </features>

  <devices>

    <!-- Main disk -->
    <disk type='file' device='disk'>
      <driver name='qemu' type='qcow2'/>
      <source file='/data/qemu/vms/arch-dev/arch-dev.qcow2'/>
      <target dev='vda' bus='virtio'/>
    </disk>

    <!-- ISO -->
    <disk type='file' device='cdrom'>
      <source file='/data/qemu/iso/archlinux-2026.02.01-x86_64.iso'/>
      <target dev='sda' bus='sata'/>
      <readonly/>
    </disk>

    <!-- Unprivileged NAT Network -->
    <interface type='user'/>

    <!-- QXL Graphics -->
    <video>
      <model type='qxl'/>
    </video>

    <!-- SPICE Server -->
    <graphics type='spice' port='5900' autoport='no' listen='0.0.0.0' passwd='strong_password'/>

  </devices>
</domain>
```

**Notes:**

- If you don't intend to connect remotely via the SPICE server (e.g. if you will connect via SSH instead), you should get rid of the `listen='0.0.0.0'` parameter. Also, the `port=XXXX` parameter can be dropped and the `autoport=no` parameter changed to `autoport=no` to allow SPICE to bind a free port dynamically (because ports have to be unique per VMs). You *could* also drop the `passwd`.
- Once the guest OS is installed, you can remove the cdrom device from the `<boot>` and `<devices>` sections, then run `virsh define /path/to/xml` to apply.

Register the XML file for the VM and start it:

```bash
virsh define /data/qemu/vms/arch-dev/arch-dev.xml
virsh start arch-dev
```

Connect remotely to the VM (requires `virt-viewer` installed):

- Via SSH

```bash
virt-viewer --connect qemu+ssh://user@host/session arch-dev
```

- Via the SPICE server:

```bash
remote-viewer spice://host:5900
```

**Troubleshooting:** I haven't succeeded getting DNS resolution to work with the `10.0.2.3` DNS resolver set by the default `user` NAT network type yet. While waiting to find a proper fix, I'm "forcing" the DNS server manually myself within the VM as a workaround.

- In the Arch ISO (to be able to install the system):

```bash
rm /etc/resolv.conf
echo "nameserver 9.9.9.9" > /etc/resolv.conf
```

- From the installed system:

```bash
sudo nmtui
```

## Basic usage

Register a VM in libvirt from a XML (or apply updates made to the XML):

```bash
virsh define /path/to/xml
```

Unregister / delete a VM from libvirt:

```bash
virsh undefine <vm_name>
```

List running VMs:

```bash
virsh list
```

List all VMs (including stopped ones):

```bash
virsh list --all
```

Check VM info:

```bash
virsh dominfo <vm_name>
```

Start a VM:

```bash
virsh start <vm_name>
```

Stop a VM:

```bash
virsh shutdown <vm_name>
```

Force stop a VM:

```bash
virsh destroy <vm_name>
```

Reboot a VM gracefully:

```bash
virsh reboot <vm_name>
```

Autostart a VM at boot (needs session lingering enabled for the user that runs the VM: `sudo loginctl enable-linger <username>`):

```bash
virsh autostart <vm_name>
```

Disable autostart for a VM:

```bash
virsh autostart --disable <vm_name>
```

Create a snapshot for a VM:

```bash
virsh snapshot-create-as <vm_name> <snapshot_name> "optional description"
```

List snapshots for a VM:

```bash
virsh snapshot-list <vm_name>
```

Revert to a snapshot:

```bash
virsh snapshot-revert <vm_name> <snapshot_name>
```

Delete a snapshot:

```bash
virsh snapshot-delete <vm_name> <snapshot_name>
```

See `virsh --help` for more.
