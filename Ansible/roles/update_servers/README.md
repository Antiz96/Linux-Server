# Update Servers

Update my servers.  
Also prints orphan packages and pacnew files for Arch Linux & orphan packages and apk-new files for Alpine Linux.

Support for Debian, Arch Linux and Alpine Linux.

## Variables

The following variable is defined in `vars/main.yml`:

- lxc_user: `service` (name of the user running the LXC containers, for post-update restart of the containers).
