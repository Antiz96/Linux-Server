# Gitea

<https://github.com/go-gitea/gitea>

## Installation

```bash
sudo pacman -S gitea
```

## Configuration

<https://docs.gitea.com/administration/customizing-gitea>  
<https://docs.gitea.com/administration/config-cheat-sheet>

Since this Gitea instance is only for my personal usage, I'm using SQLite as a built-in database for an easy and straightforward setup and maintenance.  
If you intend to use Gitea at a production grade with a high number of connections and users, you should consider using PostgreSQL or MariaDB instead:  
<https://docs.gitea.com/installation/database-prep>
<https://wiki.archlinux.org/title/Gitea#Configuration>
<https://github.com/Antiz96/Linux-Server/blob/main/Services/PostgreSQL.md>

```bash
sudo pacman -S sqlite
```

Optionally, you can modify the default `app.ini` configuration file to customize Gitea's settings. See the two links at the top of this chapter for more information.  
*If you're fine using the default settings, you can skip that part. The required settings to run Gitea can be customized graphically from the WebUI when accessing it the first time.

```bash
sudo -e /etc/gitea/app.ini
```

Open Gitea's port on the firewall and enable/start the service.

```bash
sudo firewall-cmd --add-port=3000/tcp --permanent
sudo firewall-cmd --reload
sudo systemctl enable --now gitea
```

Unlock gitea user (for SSH access):

```bash
sudo chage -E -1 gitea
```

## Access

You can access Gitea on this URL:
`http://[HOSTNAME]:3000/`

When running Gitea for the first time, it should redirect to `http://[HOSTNAME]:3000/install` for a first setup.
