# Backup Qemu VMs

Backup all qemu virtual machines.

## Variables

The following variable is defined in `defaults/main.yml`:

- backup_retention: `3` (numbers of backups to keep).

The following variables are defined in `vars/main.yml`:

- source_dir: `/data/qemu/vms/` (directory containing the qemu vms to backup).
- backup_base_dir: `/backup/qemu/` (base directory to store backups).
