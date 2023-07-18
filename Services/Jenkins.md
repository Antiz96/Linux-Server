# Jenkins

## Installation

**Arch :**  

```
pacman -S jenkins
```

**Debian :**  

```
apt install jenkins
```

## Configuration

```
firewall-cmd --add-port=8090/tcp --permanent #Opening the needed port to access the jenkins web interface
firewall-cmd --reload
systemctl enable --now jenkins #Start and enable the jenkins service
```

You can then connect to the Jenkins web interface via `http://HOSTNAME:8090` to configure it.  
From there, I install the suggested plugins but you can manually select the ones you want if needed.  
  
After I created my user from the initial setup on the WebUI, I change the jenkins security like so :  
- Administrate Jenkins --> Configure global security --> Matrix authorization policy based on project --> Add my user with the "Administrate" global permission.
  
This allows me to set permissions per user, either globally or directly on projects.

### Personal configuration

I personally use Jenkins to launch ansible playbooks via the "ansible" user:
  
```
vim /etc/sudoers.d/jenkins #Give the jenkins user the necessary permissions to run ansible-playbooks as the "ansible" user
```
> jenkins ALL=(ansible) NOPASSWD:/usr/bin/ansible-playbook
  
