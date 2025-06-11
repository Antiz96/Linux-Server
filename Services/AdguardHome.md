# AdguardHome

<https://github.com/AdguardTeam/AdGuardHome>

## Installation

```bash
sudo pacman -S adguardhome
```

## Configuration

```bash
sudo firewall-cmd --add-port=3000/tcp --add-port=80/tcp --add-port=53/tcp --permanent
sudo firewall-cmd --reload
sudo systemctl enable --now adguardhome
```

## Access

You can access AdguardHome firt setup page on this URL:  
`http://[HOSTNAME]:3000/`

Once first setup is done, you can access the Web interface on this URL:  
`http://[HOSTNAME]:80/`
