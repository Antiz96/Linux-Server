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

Start and enable the service and edit the configuration file:

```
sudo systemctl enable --now fail2ban
sudoedit /etc/fail2ban/jail.conf
```

- [DEFAULT] section:
> [...]  
> ignoreip = 127.0.0.1/8 ::1 "your_IP_or_IP_range" #IP(s) to ignore. You should put your IP or IP range, to avoid blocking yourself.  
> [...]  
> bantime = 15m #How much the ban last.  
> [...]  
> findtime = 15m #Amount of time between failed attempts.      
> [...]  
> maxretry = 3 #Number of failed attempts before banning.  
> [...]  
> banaction = iptables-multiport #Block every opened port to the banned IP.  
> [...]  
  
- [sshd] section:
> [...]   
> port = X #Replace X by the SSH port you've set.  
> logpath = %(sshd_log)s  
> backend = %(sshd_backend)s  
> enabled = true  
> [...]  
  
- [recidive] section:
> [...]  
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
