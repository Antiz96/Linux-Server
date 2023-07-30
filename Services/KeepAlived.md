# KeepAlived

<https://github.com/acassen/keepalived>

<https://www.redhat.com/sysadmin/keepalived-basics>  
<https://tobru.ch/keepalived-check-and-notify-scripts/>  
<https://access.redhat.com/documentation/en-us/red_hat_enterprise_linux/7/html/load_balancer_administration/s1-lvs-connect-vsa>

I use Keepalived to manage a VIP (Virtual IP Address) between both of my servers for the NGINX service.  
I configured it to check for the NGINX service state and to use a Master/Backup model for the VIP, but I'll show how to configure it as a active/passive model as well.

## Installation

- Debian:

```bash
sudo apt install keepalived
```

- Arch:

```bash
sudo pacman -S keepalived
```

## Open the necessary ports on the firewall

```bash
sudo firewall-cmd --add-rich-rule='rule protocol value="vrrp" accept' --permanent
sudo firewall-cmd --reload
```

## Configuration

### Configure the hosts file on all cluster nodes

I configure the hosts file of all nodes so they can still "talk" to each user in case there's a DNS issue (it's always DNS).

```bash
sudo vim /etc/hosts
```

> [...]  
> #Cluster  
> IP_OF_NODE1        Hostname.domain Hostname  
> IP_OF_NODE2        Hostname.domain Hostname  
> IP_OF_NODE3        Hostname.domain Hostname  
> IP_OF_VIP          Hostname.domain Hostname

### Create a directory to store the check and the notify script on all nodes

(respectively aimed to trigger the changing state and the changing state action(s))  
The following examples are for an nginx cluster.

```bash
sudo mkdir /opt/keepalived/
sudo vim /opt/keepalived/keepalived_check.sh
```

> [...]  
> #!/bin/bash  
>
> MASTER=$(ip a | grep -w "192.168.1.20") # The MASTER is the node that has the VIP assigned  
>
> if [ -n "$MASTER" ]; then # If the node is the MASTER, check that it is a "good" MASTER (in this case, that the nginx service is running) or put it in FAULT state  
> > pidof nginx || exit 1  
>
> else # If the node isn't the MASTER, check that it is a "good" BACKUP (in this case, that the nginx configurations syntax is correct) or put it in FAULT state  
> > nginx -t 2>&1 | grep -w "syntax is ok" || exit 1  
>
> fi

```bash
sudo vim /opt/keepalived/keepalived_notify.sh
```

> [...]  
> #!/bin/bash  
>
> TYPE=$1  
> NAME=$2  
> STATE=$3  
>
> echo "$STATE" > /opt/keepalived/state.txt # Redirect the current state of the node in a file (useful to monitor the state of a node by checking the content of that file)  
>
> case $STATE in  
> > "MASTER") # If the node has the MASTER state, do something (in this case, start nginx)  
> > > systemctl start nginx  
> > > exit 0  
> >
> > ;;  
> > "BACKUP") # If the node has the BACKUP state, do something (in this case, stop nginx)  
> > > systemctl stop nginx  
> > > exit 0  
> >
> > ;;  
> > "FAULT") # If the node has the FAULT state, do something (in this case, stop nginx and exit with an error code)  
> > > systemctl stop nginx  
> > > exit 1  
> >
> > ;;  
> > \*)  
> > > echo "Unknown state : $STATE" > /opt/keepalived/state.txt # If the node has an UNKNOWN state, output it to the state file and exit with an error code)  
> > > exit 1  
> >
> > ;;
>
> esac

```base
sudo chmod 774 /opt/keepalived/*.sh
```

### Create the state file

This is a basic txt file I create to collect the current state of my nodes (see the keepalived_notify.sh script above).  
That's really useful to monitor their current state for instance.

```bash
sudo touch /opt/keepalived/state.txt && sudo chmod 644 /opt/keepalived/state.txt
```

### Create the configuration file on all of my servers

- Server1 (The Master node):

```bash
sudo vim /etc/keepalived/keepalived.conf
```

> global_defs {  
> > enable_script_security  
> > script_user root  
>
> }
>
> vrrp_script check_script {  
> > script "/opt/keepalived/keepalived_check.sh"  
> > interval 2 #Check every 2 seconds  
> > fall 2 #Require 2 consecutive failures to enter FAULT state  
> > rise 2 #Require 2 consecutive successes to exit FAULT state  
> > #timeout 1 #Wait 1 second before assuming a failure  
> > #weight 10 #Reduce priority by 10 on complete fall  
>
> }  
>
> vrrp_instance VIP_HOSTNAME { # Adapt the HOSTNAME to your VIP's hostname  
> > state MASTER  
> > interface eth0 # Adapt to your network interface  
> > virtual_router_id 1 # This router id should be unique to each cluster and only be shared by nodes of the same cluster. Increment it if needed.  
> > priority 150  
> > advert_int 1  
> > authentication {  
> > > auth_type PASS  
> > > auth_pass 1234 # Use a secure password here  
> >
> > }  
> >
> > virtual_ipaddress {  
> > > 192.168.1.20/24 # Adapt to your VIP ip  
> >
> > }  
> > notify "/opt/keepalived/keepalived_notify.sh"  
> > track_script {  
> > > check_script  
> >
> > }  
>
> }  

- Server2 (The Backup node):

```bash
sudo vim /etc/keepalived/keepalived.conf
```

> global_defs {  
> > enable_script_security  
> > script_user root  
>
> }
>
> vrrp_script check_script {  
> > script "/opt/keepalived/keepalived_check.sh"  
> > interval 2 #Check every 2 seconds  
> > fall 2 #Require 2 consecutive failures to enter FAULT state  
> > rise 2 #Require 2 consecutive successes to exit FAULT state  
> > #timeout 1 #Wait 1 second before assuming a failure  
> > #weight 10 #Reduce priority by 10 on complete fall  
>
> }  
>
> vrrp_instance VIP_HOSTNAME { # Adapt the HOSTNAME to your VIP's hostname  
> > state BACKUP  
> > interface eth0 # Adapt to your network interface  
> > virtual_router_id 1 # This router id should be unique to each cluster and only be shared by nodes of the same cluster. Increment it if needed.  
> > priority 100  
> > advert_int 1  
> > authentication {  
> > > auth_type PASS  
> > > auth_pass 1234 # Use a secure password here  
> >
> > }  
> >
> > virtual_ipaddress {  
> > > 192.168.1.20/24 # Adapt to your VIP ip  
> >
> > }  
> > notify "/opt/keepalived/keepalived_notify.sh"  
> > track_script {  
> > > check_script  
> >
> > }  
>
> }

## Start/Enable the keepalived service

```bash
sudo systemctl enable --now keepalived
```

## Active/Passive mode

If you want to use an Active/Passive mode rather then a Master/Backup mode, modify state as "BACKUP" and add the "nopreempt" option for **both** nodes in the config file, like so:

- Server1 (The First node):

```bash
sudo vim /etc/keepalived/keepalived.conf
```

> global_defs {  
> > enable_script_security  
> > script_user root  
>
> }
>
> vrrp_script check_script {  
> > script "/opt/keepalived/keepalived_check.sh"  
> > interval 2 #Check every 2 seconds  
> > fall 2 #Require 2 consecutive failures to enter FAULT state  
> > rise 2 #Require 2 consecutive successes to exit FAULT state  
> > #timeout 1 #Wait 1 second before assuming a failure  
> > #weight 10 #Reduce priority by 10 on complete fall  
>
> }  
>
> vrrp_instance VIP_HOSTNAME { # Adapt the HOSTNAME to your VIP's hostname  
> > state BACKUP  
> > interface eth0 # Adapt to your network interface  
> > virtual_router_id 1 # This router id should be unique to each cluster and only be shared by nodes of the same cluster. Increment it if needed.  
> > priority 150  
> > nopreempt  
> > advert_int 1  
> > authentication {  
> > > auth_type PASS  
> > > auth_pass 1234 # Use a secure password here  
> >
> > }  
> >  
> > virtual_ipaddress {  
> > > 192.168.1.20/24 # Adapt to your VIP ip  
> >
> > }  
> > notify "/opt/keepalived/keepalived_notify.sh"  
> > track_script {  
> > > check_script  
> >
> > }  
>
> }  

- Server2 (The second node):

```bash
sudo vim /etc/keepalived/keepalived.conf
```

> global_defs {  
> > enable_script_security  
> > script_user root  
>
> }
>
> vrrp_script check_script {  
> > script "/opt/keepalived/keepalived_check.sh"  
> > interval 2 #Check every 2 seconds  
> > fall 2 #Require 2 consecutive failures to enter FAULT state  
> > rise 2 #Require 2 consecutive successes to exit FAULT state  
> > #timeout 1 #Wait 1 second before assuming a failure  
> > #weight 10 #Reduce priority by 10 on complete fall  
>
> }  
>
> vrrp_instance VIP_HOSTNAME { # Adapt the HOSTNAME to your VIP's hostname  
> > state BACKUP  
> > interface eth0 # Adapt to your network interface  
> > virtual_router_id 1 # This router id should be unique to each cluster and only be shared by nodes of the same cluster. Increment it if needed.  
> > priority 100  
> > nopreemt  
> > advert_int 1  
> > authentication {  
> > > auth_type PASS  
> > > auth_pass 1234 # Use a secure password here  
> >
> > }  
> >
> > virtual_ipaddress {  
> > > 192.168.1.20/24 # Adapt to your VIP ip  
> >
> > }  
> > notify "/opt/keepalived/keepalived_notify.sh"  
> > track_script {  
> > > check_script  
> >
> > }  
>
> }
