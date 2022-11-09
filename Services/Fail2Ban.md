# Fail2Ban

https://www.fail2ban.org

I use Fail2Ban to prevent brute-force attacks on the SSH service of my servers.  
It allows me to automatically ban IPs that show numerous and suspicious signs like too many password failures.  

## Installation

**Arch :** 

```
sudo pacman -S fail2ban
```
  
**Debian :**

```
sudo apt install fail2ban
```

## Configuration

https://wiki.archlinux.org/title/Fail2ban  
https://www.linode.com/docs/guides/how-to-use-fail2ban-for-ssh-brute-force-protection/  
https://www.linuxtricks.fr/wiki/fail2ban-bannir-automatiquement-les-intrus  

Fail2Ban works with different blocks ("**[block_name]**") defined in the configuration file.  
The **[DEFAULT]** block defines the default parameters and values to apply to every other blocks (unless they are explicitily defined in the said blocks).  
You can then define a block for each services you want to monitor via fail2ban. A lot of them are pre-defined for various services like sshd, httpd, nginx, etc...  
The **[recidive]** block defines the paramaters and values to apply to IPs/hosts that recidived failed attempts.  
   
Start and enable the service and edit the configuration file:  
  
```
sudo systemctl enable --now fail2ban
sudoedit /etc/fail2ban/jail.conf
```

- [DEFAULT] block:
> [...]  
> [DEFAULT]  
> ignoreip = 127.0.0.1/8 ::1 "your_IP_or_IP_range" #IP(s) to ignore. You should put your IP or IP range, to avoid blocking yourself. See: https://wiki.archlinux.org/title/Fail2ban  
> bantime = 15m #How much the ban last.  
> findtime = 15m #Amount of time between failed attempts.      
> maxretry = 3 #Number of failed attempts before banning.  
> banaction = iptables-multiport #Block every opened port to the banned IP. 
> enabled = false #Default status of every other blocks. Put it to false and explicitely enable the blocks you want to enable.   
> [...]  
  
- [sshd] block:
> [...]   
> [sshd]  
> port = X #Replace X by the SSH port you've set.  
> logpath = %(sshd_log)s  
> backend = %(sshd_backend)s  
> enabled = true  
> [...]  
  
- [recidive] block:
> [...]  
> [recidive]  
> logpath  = /var/log/fail2ban.log #Path to the fail2ban logs.    
> banaction = %(banaction_allports)s #Block banned IP from all ports.  
> bantime  = 1w #Ban time of recidivist IPs.  
> findtime = 1d #Amount of time between recidive failed attempts.  
> enabled = true  
> [...]  
    
```
sudo systemctl restart fail2ban #Restart the service to apply
```

## Manage fail2ban with fail2ban-client

You can check the help with:  

```
fail2ban-client
```
  
You can check active jails with:  
  
```
sudo fail2ban-client status
```
  
You can display information about a specific jail with:  
*Example with the sshd jail*  
  
```
sudo fail2ban-client status sshd
```
  
You can check the compact list of banned IPs with:  
  
```
sudo fail2ban-client banned
```
  
You can manually unban an IP for a specific jail with:  
*Example with the sshd jail and the 192.168.1.250 IP*  
  
```
sudo fail2ban-client set sshd unbanip 192.168.1.250
```

## Tips and tricks

Here's a script I wrote to get the number of currently banned IPs.  
It's useful to monitor it via a monitoring server like Zabbix, for instance.  
  
```
#!/bin/bash

jails_list=$(fail2ban-client status | grep -w "Jail list:" | cut -f2 | sed s/,//g)

for i in ${jails_list} ; do ban_number=$(( ban_number + $(fail2ban-client status "${i}" | grep -w "Currently banned:" | cut -f2) )) ; done

echo "${ban_number}"
```
