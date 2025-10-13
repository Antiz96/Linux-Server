# Ansible

## Installation

- Arch:

```bash
pacman -S ansible
```

- Debian:

```bash
apt install ansible
```

## Configure the ansible user

I use the [ansible user](https://github.com/Antiz96/Linux-Server/blob/main/VMs/Arch-Linux_Server_Template.md#create-and-configure-the-ansible-user) I created during the template installation.

```bash
rm /home/ansible/.ssh/authorized_keys # Delete the authorized key file which is only needed for ansible client
vim /home/ansible/.ssh/config # Create the ssh config according to my ssh configuration
```

> Host \*.domain  
> > User ansible  
> > Port X # Replace X by the port you SSH is listening on  
> > IdentityFile ~/.ssh/id_ed25519_ansible

```bash
vim /home/ansible/.ssh/id_ed25519_ansible
```

> Copy the ansible SSH private key here

## Create the working directories

I create one directory for playbooks and one dedicated for inventory files:

```bash
mkdir -p /opt/ansible/{playbooks,inventories}
chown -R ansible: /opt/ansible
```

## Configure ansible

```bash
vim /etc/ansible/ansible.cfg
```

> [defaults]  
> force_color = True # Force colored output (even in non TTY env)  
> host_key_checking = False # Disable ssh key checking when connection to a new host  
> interpreter_python = "/usr/bin/python3"  
> retry_files_enabled = True # Enable retry files in case of a failure during a playbook execution

## Playbooks

My ansible playbooks are available [here](https://github.com/Antiz96/Linux-Server/tree/main/Ansible/playbooks).
