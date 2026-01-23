# Shell Command

Run arbitrary shell command(s).

Support for Debian, Arch Linux and Alpine Linux.

## Variables

The following variables can be set at the playbook level or as `--extra-vars` *(at least one of the `cmd_xxx` variables should be defined and not empty)*:

- user: user to run command(s) as (optional, falls back to default Ansible / SSH user if not set).
- cmd_debian: command(s) to run on Debian hosts.
- cmd_arch: command(s) to run on Arch Linux hosts.
- cmd_alpine: command(s) to run on Alpine Linux hosts.
