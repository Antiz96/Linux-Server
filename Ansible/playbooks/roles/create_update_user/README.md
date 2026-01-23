# Create / Update User

Create or update a user.

Support for Debian, Arch Linux and Alpine Linux.

## Variables

The following variable should be set at the playbook level or as `--extra-vars`:

- username: username of the user to create or update.

The following are optionals but can be set at the playbook level or as `--extra-vars`:

- user_passwd: password to set or update for the user.
- user_shell: shell to assign or update for the user.
- user_pubkey: SSH public key(s) to set or update for the user.
- user_group_debian: group(s) (comma separated) to set or update for the user for Debian hosts.
- user_group_arch: group(s) (comma separated) to set or update for the user for Arch Linux hosts.
- user_group_alpine: group(s) (comma separated) to set or update for the user for Alpine Linux hosts.
