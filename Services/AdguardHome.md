# AdguardHome

<https://github.com/AdguardTeam/AdGuardHome>

## Installation

```bash
sudo pacman -S adguardhome
```

## Configuration

```bash
sudo firewall-cmd --add-port=3000/tcp --add-port=80/tcp --add-port=53/tcp --add-port=53/udp --permanent
sudo firewall-cmd --reload
sudo systemctl enable --now adguardhome
```

## Access

You can access AdguardHome first setup page on this URL:  
`http://[HOSTNAME]:3000/`

Once first setup is done, you can access the Web interface on this URL:  
`http://[HOSTNAME]:80/`

## Note

Configuration can be done via the WebUI or directly on the server by editing the `/var/lib/adguardhome/AdGuardHome.yaml` file (requires a restart of the service for changes to be applied).

- I had to set the `Rate limit` parameter to `0` (otherwise DNS requests were rate limited, most likely because of Zabbix).
- I had to disable the resolving of IPv6 addresses by checking the related parameter under the DNS settings in order to avoid unexpected NXDOMAIN responses (e.g. with `Homepage`).
- I had to disable `system-resolved` and switch back to a static entry in `/etc/resolv.conf` to avoid Adguard's service to fail due to conflicting binding between `systemd-resolved`'s stub listener and Adguard (which both want to bind port 53).
