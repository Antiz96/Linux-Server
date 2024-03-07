# NVChecker

<https://github.com/lilydjwg/nvchecker>  

`nvchecker` (short for new version checker) is for checking if a new version of some software has been released.
I use it to monitor upstream releases of the various packages I maintain on the Arch/Alpine repositories and the AUR.

## Installation

```bash
sudo pacman -S nvchecker
sudo pacman -S --asdeps git python-packaging # Optional dependencies to support git repositories and pypi sources that I personally need.
```

## Configuration

<https://nvchecker.readthedocs.io/en/latest/>

### Configuration file

The configuration file serves to declare the software sources to watch and the different settings/filters to apply as well as some settings for nvchecker as whole.  
Create and edit your configuration file:

```bash
mkdir -p ~/.config/nvchecker/
vim ~/.config/nvchecker/nvchecker.toml
```

See the [documentation](https://nvchecker.readthedocs.io/en/latest/usage.html#configuration-files) for more information.  
My personal configuration is available [here](https://github.com/Antiz96/Linux-Server/blob/main/Dotfiles/Services/nvchecker.toml).

Once configured (and after any future modifications in the configuration), you can run `nvchecker` to test it.

## Notifications

The `nvchecker-notify` command sends a desktop notification on new releases, but I personally have nvchecker running on a remote server and I expect it to send mails instead. Unfortunately, nvchecker itself doesn't have a built in way to do that, unlike UrlWatch (as nvchecker is not really meant to be executed that way I guess).  

However, while not built in into `nvchecker`, mail notifications are still fairly easy to set up by using an existing email address configured in an smtp relay (I personally do this via postfix, see my [related procedure](https://github.com/Antiz96/Linux-Server/blob/main/Services/Postfix.md)).

Withi both the smtp relay and my nvchecker configuration properly configured on my server, I use the following script to send me mails on new upstream/software releases:

```bash
#!/bin/bash

if [ "${1}" == "--update-config" ]; then
        curl -s https://raw.githubusercontent.com/Antiz96/Linux-Server/main/Dotfiles/Services/nvchecker.toml -o /home/ansible/.config/nvchecker/nvchecker.toml && echo -e "\nConfiguration updated" || exit 1
fi

nvchecker_output="$(nvchecker 2>&1)"

if [ -n "${nvchecker_output}" ]; then
        echo -e "\n${nvchecker_output}\n"
        echo -e "Subject:NVChecker - New upstream releases\n\n${nvchecker_output}" | sendmail antiz@archlinux.org || exit 1
        nvtake --all
else
        echo -e "\nNo new upstream release\n"
fi
```

This script will launch `nvchecker` and verify if new releases are available. If so, it displays the new available releases as well as sending them in a mail to me and then it "ackownoledge" new release with the `nvtake` command so they are printed again in the next run.  
If there's no new new release, it just displays a related message.

Passing the `--update-config` argument to the script will download a fresh copy of my nvchecker configuration file from my related GitHub repo before proceeding with the rest of the script (I run this after I modified my configuration and pushed the changes to my GitHub repo).

## Run periodically

Set the above script to run periodically via a cronjob, a systemd timer or any other way to get automatically notified in case of changes on the monitored URLs.
