# Corosyn-qnet

I'm running this inside a Debian container on my Alpine's Raspberry PI server to act as a third / external node for my Proxmox cluster.  
See <https://pve.proxmox.com/wiki/Cluster_Manager#_corosync_external_vote_support>.

**Warning:** This solution works but is kinda hacky...

## Install Docker on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Docker.md>

## Set up ssh connection with the proxmox nodes

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

## Setup the Debian container

```bash
sudo docker run -dit --restart=unless-stopped --network host --hostname debian --name debian debian:bookworm-slim bash # Pull and run a Debian container
sudo docker exec -it debian bash # Enter the container
apt update && apt install corosync-qnetd # Install the corosync-qnetd package
exit # Exit the container
sudo docker commit debian qnetd:latest # Commit the container as a custom image
sudo docker rm -f debian # Delete the Debian container
sudo docker run -dit --restart=unless-stopped --network host --hostname qnetd --name qnetd qnetd corosync-qnetd -f # Run the custom container once to retrieve the corosync files from the host
sudo docker cp qnetd:/etc/corosync /etc/ # Copy the needed corosync files to host
sudo docker rm -f qnetd # Delete the container
sudo docker run -dit --restart=unless-stopped --network host -v /etc/corosync:/etc/corosync -v /tmp:/tmp --hostname qnetd --name qnetd qnetd corosync-qnetd -f # Run the custom container with the proper volume mapping
sudo docker image prune -a # Remove dangling image
```

## Setup cluster node

On the QDEVICE node:

```bash
sudo vim /usr/local/bin/corosync-qnetd-certutil
```

> #!/bin/bash  
> docker exec -i qnetd corosync-qnetd-certutil "$@"

```bash
sudo chmod +x /usr/local/bin/corosync-qnetd-certutil
```

On **every** Proxmox (PVE) nodes:

```bash
sudo apt install corosync-qdevice
```

From **one** of the Proxmox (PVE) nodes:

```bash
sudo pvecm qdevice setup <QDEVICE-IP> # **Warning:** This command expects **all** nodes to use the default 22 SSH port
```

You can check that all the steps have successfully completed with:

```bash
sudo pvecm status
```

## Update

```bash
sudo docker exec -it qnetd bash # Enter the container
apt update && apt full-upgrade && apt autoremove # Update the system
exit # Exit the container
```
