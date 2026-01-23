# Delete User

Delete a user.

## Variables

The following variable is set in `defaults/main.yml`:

- remove_homedir: `false` (controls whether the user's home directory should be removed or not, expects a boolean `true` or `false` value).

The following variable should be set at the playbook level or as `--extra-vars`:

- username: username of the user to delete (example: `myuser`).
