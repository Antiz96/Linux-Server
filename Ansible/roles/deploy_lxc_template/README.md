# Deploy LXC Template

Configure new servers cloned from my LXC template:

Set hostname, `/etc/hosts` entries, IP address and Zabbix configuration.  
Then update the system, delete `/etc/machine-id` and ssh host keys, and restart the container.

Support for Debian, Arch Linux and Alpine Linux.

## Variables

The following variable is set in `defaults/main.yml`:

- autostart: `true` (controls whether the deployed LXC container should start automatically at boot or not, expects a boolean `true` or `false` value).

The following variables are set in `vars/main.yml`:

- domain: `.rc` (default domain for my servers).
- lxc_data_dir: `/data/lxc` (path to LXC data directory where containers are stored).
- lxc_user: `service` (name of the user to deploy the LXC container with).
- core_server: `"{{ (lxc_template | regex_search('core0[12]')) ~ '.rc' }}"` (Core server to deploy the LXC container on (e.g. `core01.rc`), automatically/dynamically extracted from the `lxc_template` variable).

The following variables should be set at the inventory level, the playbook level or as `--extra-vars`:

- lxc_template: Name of the LXC template to deploy (example: `lxc-arch-template-core01`). **Note:** The same name + domain (e.g. `lxc-arch-template-core01.rc`) should be passed as `-l / --limit` when running a playbook calling this role.
- ip: IPv4 address to set (example: `192.168.1.100`).
- hostname: hostname to set, **not including domain** (see the `domain` variable in `defaults/main.yml`, the full FQDN is `{{ hostname }}{{ domain }}`) (example: `myserver`).
- root_passwd: password to set for the root user (example: `strong_password`).
