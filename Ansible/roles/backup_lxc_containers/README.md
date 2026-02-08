# Backup LXC Containers

Backup all lxc containers.

## Variables

The following variables are defined in `defaults/main.yml`:

- source_dir: `/data/lxc/` (directory containing the lxc containers to backup).
- backup_base_dir: `/backup/lxc/` (base directory to store backups).
- backup_retention: `3` (numbers of backups to keep).
