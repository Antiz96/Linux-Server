# Backup Podman Containers Data

Backup all podman containers' data (mounted via volumes).

## Variables

The following variables are defined in `defaults/main.yml`:

- source_dir: `/data/podman/` (directory containing the podman containers' data to backup).
- backup_base_dir: `/backup/podman/` (base directory to store backups).
- backup_retention: `3` (numbers of backups to keep).
