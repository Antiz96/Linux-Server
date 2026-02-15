# Backup LXC Containers

Backup all lxc containers.

## Variables

The following variable is defined in `defaults/main.yml`:

- backup_retention: `3` (numbers of backups to keep).

The following variables are defined in `vars/main.yml`:

- source_dir: `/data/lxc/` (directory containing the lxc containers to backup).
- backup_base_dir: `/backup/lxc/` (base directory to store backups).
