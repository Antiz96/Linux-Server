# Shell Command

Run arbitrary shell command(s).

Support for Debian, Arch Linux and Alpine Linux.

## Variables

The following variable is set in `defaults/main.yml`:

- sudo: `false` (controls whether to run command(s) as root or not, expects a boolean `true` or `false` value).

The following variables can be set at the playbook level or as `--extra-vars` *(at least one of them should not be undefined / empty)*:

- cmd_debian: command(s) to run on Debian hosts.
- cmd_arch: command(s) to run on Arch Linux hosts.
- cmd_alpine: command(s) to run on Alpine Linux hosts.
