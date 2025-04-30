# Corosync-qnetd

I use this to setup a third / external corosync voter for my Proxmox cluster (which only has to 2 nodes) to ensure a quorum is reached.

<https://pve.proxmox.com/wiki/Cluster_Manager#_corosync_external_vote_support>

**Disclaimer:** The proper way to setup `corosync-qnetd` (as described in the above link) expects / assumes a Debian system.  
The solution described below shows how to run it inside a Debian container from another OS (from my Alpine Linux Raspberry PI server in that case). While this is handy in case you don't have (or don't want) a Debian system just for this, it's fair to say that this is a bit of an hacky-ish solution (as compared to running it directly on a Debian host).

## Install Docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Set up ssh connection with the Proxmox nodes

The QDevice external voter (in that case, my Raspberry PI) and the Proxmox nodes need to be able to connect through each other via ssh on the `root` account.

```bash
sudo -e /etc/ssh/sshd_config
```

> [...]  
> PermitRootLogin prohibit-password  
> [...]

```bash
sudo rc-service sshd restart
```

Proceed to SSH key exchange with every nodes (for the `root` user).  
Set `~/.ssh/config` if needed.

**Important:** Note that all nodes should also be able to login via ssh (passwordless) to themselves, so adapt your config if needed (see [this](https://forum.proxmox.com/threads/qdevice-is-not-voting.84976/#post-527855)).

## Open the required port on the firewall

```bash
sudo firewall-cmd --add-port=5403/tcp --permanent
sudo firewall-cmd --reload
```

## Setup the container

Run a Debian container, install the required `corosync-qnetd` package and start the daemon (to generate the related files):

```bash
sudo docker run -dit --restart=unless-stopped --network host --hostname debian --name debian debian:bookworm-slim bash
sudo docker exec -it debian bash -c "apt update && apt install corosync-qnetd && corosync-qnetd -fd" # Press `ctrl + c` once fully started
```

Copy the generated files to the host:

```bash
sudo docker cp debian:/etc/corosync /etc/
```

Create a custom image from the Debian container (in order to get an image which includes the `corosync-qnetd` package and the related files to run from the host):

```bash
sudo docker commit debian qnetd:latest
```

Delete the debian container and run the custom image with the proper volume mapping:

*Exposing `/tmp` is required for the `qdevice` setup.*

```bash
sudo docker rm -f debian
sudo docker run -dit --restart=unless-stopped --network host -v /etc/corosync:/etc/corosync -v /tmp:/tmp --hostname qnetd --name qnetd qnetd corosync-qnetd -fd
```

Remove dangling images (not strictly required, just basic cleanup):

```bash
sudo docker image prune -a
```

## Setup qdevice

- On the QDEVICE node:

Create a wrapper script around the `corosync-qnetd-certutils` command so it gets executed within the container (required for the `qdevice` setup).

```bash
sudo vim /usr/local/bin/corosync-qnetd-certutil
```

> #!/bin/bash  
> docker exec -i qnetd corosync-qnetd-certutil "$@"

```bash
sudo chmod +x /usr/local/bin/corosync-qnetd-certutil
```

- On **every** Proxmox (PVE) nodes:

```bash
sudo apt install corosync-qdevice
```

- From **one** of the Proxmox (PVE) nodes:

**Warning:** The setup command expects **all** nodes (including the QDEVICE) to use the default `22` SSH port.  
If you use a custom port, you have to switch back to the default `22` one for the setup to succeed (you can switch back to your custom port afterwards).

```bash
sudo pvecm qdevice setup <QDEVICE-IP>
```

You can check that all the steps have successfully completed with:

```bash
sudo pvecm status
```

## Update / Upgrade

Update packages from the container:

```bash
sudo docker exec -it qnetd bash -c "apt update && apt full-upgrade && apt autoremove"
```

To upgrade to a new Debian release (e.g. `trixie`), simply delete the container (`sudo docker rm -f qnetd`) and repeat all steps with the new tag for the Debian container created in the first step (e.g. `debian:trixie-slim`).
