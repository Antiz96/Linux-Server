# Kea

<https://github.com/isc-projects/kea>

## Installation

- Arch:

```bash
sudo pacman -S kea
```

- Debian:

```bash
sudo apt install kea
```

## Configuration

<https://kea.readthedocs.io/en/kea-2.4.0/>  
<https://www.techtutorials.tv/sections/linux/how-to-install-and-configure-kea-dhcp-server/>

I only configure the `dhcp4` server as I don't use `ipv6`.

```bash
sudo mv /etc/kea/kea-dhcp4.conf /etc/kea/kea-dhcp4.conf-bck
sudoedit /etc/kea/kea-dhcp4.conf
```

```text
{
"Dhcp4": {
    "interfaces-config": {
        "interfaces": ["ens18"], # Adapt to your network interface
        "dhcp-socket-type": "raw" # You can switch to "udp" if you prefer, but you'll have to open port 67/udp on the firewall
    },

    "lease-database": {
        "type": "memfile",
        "persist": true,
        "name": "/var/lib/kea/kea-leases-dhcp4.csv", # You can modify the path and filename if you want
        "lfc-interval": 3600
    },

    "renew-timer": 15840,
    "rebind-timer": 27720,
    "valid-lifetime": 31680,

    "option-data": [
        {
            "name": "domain-name-servers",
            "data": "192.168.96.1, 192.168.96.2" # Adapt to your desired DNS servers
        },

        {
            "name": "domain-search",
            "data": "lan"
        }
    ],

    "subnet4": [
        {
            "id": 1, # id has to be unique per subnet
            "subnet": "192.168.96.0/24", # Adapt to your network
            "pools": [ { "pool": "192.168.96.201 - 192.168.96.250" } ], # Adapt to your desired IP range
            "option-data": [
                {
                    "name": "routers",
                    "data": "192.168.96.254" # Adapt to your router's IP
                }
            ]
        }
    ]
}
}
```

**Before starting the service as described below, don't forget to turn off your current DHCP service on your router or wherever else it may be.**

```bash
sudo systemctl enable --now kea-dhcp4
```

Alternatively, the `keactrl` command can be used instead:  
<https://kea.readthedocs.io/en/kea-2.2.0/arm/keactrl.html>
