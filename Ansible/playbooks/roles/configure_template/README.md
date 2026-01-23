# Configure Template

Configure new servers cloned from my Proxmox templates:  
Set hostname, `/etc/hosts` entries, IP address and Zabbix configuration, then update and reboot the server.

Support for Debian, Arch Linux and Alpine Linux.

## Variables

The following variable is set in `vars/main.yml`:

- domain: `.rc` (default domain for my servers).

The following variables should be set at the playbook level or as `--extra-vars`:

- ip: IPv4 address to set (example: `192.168.1.100`).
- hostname: hostname to set, **not including domain** (see the `domain` variable in `defaults/main.yml`, the full FQDN is `{{ hostname }}{{ domain }}`) (example: `myserver`).
- root_passwd: password to set for the root user (example: `strong_password`).
