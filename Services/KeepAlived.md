# KeepAlived

<https://github.com/acassen/keepalived>

<https://keepalived.org>  
<https://www.redhat.com/sysadmin/keepalived-basics>  
<https://tobru.ch/keepalived-check-and-notify-scripts/>  
<https://access.redhat.com/documentation/en-us/red_hat_enterprise_linux/7/html/load_balancer_administration/s1-lvs-connect-vsa>

I use Keepalived to manage a VIP (Virtual IP Address) between my clusters.  
The procedure below uses an NGINX cluster as an example based on an "MASTER/BACKUP" *(this is keepalived terminology)* cluster model, but it also shows how to use configure keepalive to use more of an "ACTIVE/PASSIVE" model (where no node is designated as the preferred "MASTER").

## Installation

- Debian:

```bash
sudo apt install keepalived
```

- Arch:

```bash
sudo pacman -S keepalived
```

- Alpine:

```bash
sudo apk add keepalived
```

## Open the necessary ports on the firewall

```bash
sudo firewall-cmd --add-rich-rule='rule protocol value="vrrp" accept' --permanent
sudo firewall-cmd --reload
```

## Configuration

### Configure the hosts file on all cluster nodes

I configure the hosts file of all nodes so they can still "talk" to each other in case there's a DNS issue (it's always DNS).

```bash
sudo vim /etc/hosts
```

> [...]  
> #Cluster  
> IP_OF_NODE1        Hostname.domain Hostname  
> IP_OF_NODE2        Hostname.domain Hostname  
> IP_OF_NODE3        Hostname.domain Hostname  
> IP_OF_VIP          Hostname.domain Hostname

### Create a directory to store the check and the notify scripts on all nodes

Respectively aimed to trigger the state changes and the related actions.  
The following examples are for made for an NGINX cluster.

```bash
sudo mkdir /opt/keepalived/
sudo vim /opt/keepalived/keepalived_check.sh
```

```bash
#!/bin/bash

MASTER=$(ip a | grep -w "192.168.96.20") # The MASTER node is the node that has the VIP assigned

if [ -n "${MASTER}" ]; then # If the node is the MASTER one, check that it is in a good state for a MASTER node (in this case, that the NGINX service is running) or exit in error otherwise
        pidof nginx || exit 1
else # If the node isn't the MASTER one, check that it is in a good state for a BACKUP node (in this case, that the NGINX configuration syntax is correct) or exit in error otherwise
        nginx -t 2>&1 | grep -w "syntax is ok" || exit 1
fi
```

```bash
sudo vim /opt/keepalived/keepalived_notify.sh
```

```bash
#!/bin/bash

TYPE="${1}"
NAME="${2}"
STATE="${3}"

echo "${STATE}" > /opt/keepalived/state.txt # Redirect the current state of the node in a file (useful to monitor the state of a node by checking the content of that file)

case "${STATE}" in
    "MASTER") # If the node has the MASTER state, do something (in this case, start nginx)
        systemctl start nginx
        exit 0
    ;;
    "BACKUP"|"STOP") # If the node has the BACKUP state or the STOP state (meaning that the keepalived service itself is stopped, likely intentionally), do something (in this case, stop nginx)
        systemctl stop nginx
        exit 0
    ;;
    "FAULT") # If the node has the FAULT state, do something (in this case, stop nginx and exit with an error code)
        systemctl stop nginx
        exit 1
    ;;
    *)
        echo "Unknown state: ${STATE}" > /opt/keepalived/state.txt # If the node has any other state, output it to the state file and exit with an error code)
        exit 1
    ;;
esac
```

```bash
sudo chmod 750 /opt/keepalived/*.sh
```

### Create a file to store the key for HMAC authentication

Use to authenticate nodes to the cluster

```bash
sudo vim /opt/keepalived/.vrrp.key # You can name this file however you want
```

Generate an hexadecimal prefixed secure key with `printf "hex:%s\n" "$(openssl rand -hex 32)"` and put it in the file.  
Note that the same key should be deployed on all nodes.

```bash
sudo chmod 400 /opt/keepalived/.vrrp.key
```

### Create the state file

This is a basic txt file I create to collect the current state of my nodes (see the keepalived_notify.sh script above).  
That's really useful to monitor the current state of the nodes.

```bash
sudo touch /opt/keepalived/state.txt && sudo chmod 644 /opt/keepalived/state.txt
```

### Create the configuration files

This is a configuration example based on the "MASTER/BACKUP" model.  
See the small paragraph at the end of this section for a configuration based on a more "ACTIVE/PASSIVE" model.

- On the primary (MASTER) node:

```bash
sudo vim /etc/keepalived/keepalived.conf
```

```text
global_defs {
    enable_script_security
    script_user root
}

vrrp_script check_script {
    script "/opt/keepalived/keepalived_check.sh"
    interval 2 #Check every 2 seconds
    fall 2 #Require 2 consecutive failures to enter FAULT state
    rise 2 #Require 2 consecutive successes to exit FAULT state
    #timeout 1 #Wait 1 second before assuming a failure
    #weight 10 #Reduce priority by 10 on complete fall
}

vrrp_instance VIP_HOSTNAME { # Adapt the HOSTNAME to your VIP's hostname
    state MASTER # Default state of the node
    interface eth0 # Adapt to your network interface
    virtual_router_id 1 # This router id should be unique to each cluster and only be shared by nodes of the same cluster. Increment it if needed.
    priority 150 # Slightly higher priority than the secondary node(s), ensuring that it becomes the MASTER node again once back online after a failover
    advert_int 1
    version 3 # Explicitly use VRRPv3

    auth_hmac {
        key 1 file:/opt/keepalived/.vrrp.key # Path to the keyfile created earlier
        active_key 1
        anti_replay time # The `anti_replay time` protection implies that all nodes have their clocks synchronized (e.g. via NTP). Otherwise use `anti_replay monotonic` instead (and get rid of the `time_window` line)
        time_window 5 # 5 seconds tolerance
    }

    unicast_src_ip 192.168.96.21 # IP of this node

    unicast_peer {
        192.168.96.22 # IPs of the other node(s)
    }

    check_unicast_src

    virtual_ipaddress {
        192.168.96.20/24 # IP of the VIP
    }

    notify "/opt/keepalived/keepalived_notify.sh"

    track_script {
        check_script
    }
}
```

- On the secondary (BACKUP) node(s):

```bash
sudo vim /etc/keepalived/keepalived.conf
```

```text
global_defs {
    enable_script_security
    script_user root
}

vrrp_script check_script {
    script "/opt/keepalived/keepalived_check.sh"
    interval 2 #Check every 2 seconds
    fall 2 #Require 2 consecutive failures to enter FAULT state
    rise 2 #Require 2 consecutive successes to exit FAULT state
    #timeout 1 #Wait 1 second before assuming a failure
    #weight 10 #Reduce priority by 10 on complete fall
}

vrrp_instance VIP_HOSTNAME { # Adapt the HOSTNAME to your VIP's hostname
    state BACKUP # Default state of the node
    interface eth0 # Adapt to your network interface
    virtual_router_id 1 # This router id should be unique to each cluster and only be shared by nodes of the same cluster. Increment it if needed.
    priority 100 # Slightly lower priority than the primary node, ensuring that it becomes a BACKUP node again once back online after a failover
    advert_int 1
    version 3 # Explicitly use VRRPv3

    auth_hmac {
        key 1 file:/opt/keepalived/.vrrp.key # Path to the keyfile created earlier
        active_key 1
        anti_replay time # The `anti_replay time` protection implies that all nodes have their clocks synchronized (e.g. via NTP). Otherwise use `anti_replay monotonic` instead (and get rid of the `time_window` line)
        time_window 5 # 5 seconds tolerance
    }

    unicast_src_ip 192.168.96.22 # IP of this node

    unicast_peer {
        192.168.96.21 # IP(s) of the other node(s)
    }

    check_unicast_src

    virtual_ipaddress {
        192.168.96.20/24 # IP of the VIP
    }

    notify "/opt/keepalived/keepalived_notify.sh"

    track_script {
        check_script
    }
}
```

To configure the cluster in an "ACTIVE/PASSIVE" model (where no node is designated as the preferred "MASTER") give all nodes the "BACKUP" state by default, assign them equal priorities, and add `nopreempt` to the `vrrp_instance` block. This prevents a recovered node from automatically taking over the "MASTER" role from the currently active node.

```text
[...]
vrrp_instance VIP_HOSTNAME { # Adapt the HOSTNAME to your VIP's hostname
    state BACKUP
    interface eth0
    virtual_router_id 1
    priority 100
    nopreempt
    advert_int 1
[...]
```

## Start/Enable the keepalived service

```bash
sudo systemctl enable --now keepalived
```
