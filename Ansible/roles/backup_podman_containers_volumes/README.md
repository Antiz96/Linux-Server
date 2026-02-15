# Backup Podman Containers Volumes

Backup all podman containers' volumes.

## Variables

The following variable is defined in `defaults/main.yml`:

- backup_retention: `3` (numbers of backups to keep).

The following variables are defined in `vars/main.yml`:

- source_dir: `/data/podman/volumes/` (directory containing the podman containers' volumes to backup).
- backup_base_dir: `/backup/podman/` (base directory to store backups).
