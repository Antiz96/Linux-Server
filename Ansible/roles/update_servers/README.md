# Update Servers

Update my servers.  
Also prints orphan packages and pacnew files for Arch Linux & orphan packages and apk-new files for Alpine Linux.

Support for Debian, Arch Linux and Alpine Linux.

## Variables

The following variable is set in `defaults/main.yml`:

- reboot_async: `false` (controls whether Ansible should use the "reboot" module or if it should program a reboot in 1 min "asynchronously", expects a boolean `true` or `false` value, defaults to `false`. Set this to `true` as an inventory variable for the host that runs the Ansible controller node).

The following variable is defined in `vars/main.yml`:

- lxc_user: `service` (name of the user running the LXC containers, for post-update restart of the containers).
