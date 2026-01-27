# Watchtower

~~<https://github.com/containrrr/watchtower>~~ (historical repo)  
<https://github.com/nickfedor/watchtower>  (maintained fork)

I update all my Docker containers at once using "watchtower".  
Watchtower is a containerized (docker) service that will automatically check and apply updates of all your containers.  
It is highly customizable; you can configure it to check/apply updates every X time units or at a certain date/hour, you can ask it to only watch specific containers or only containers that has a certain label, sends you notification, etc...  
It automatically detect the source image and the run command and it will use them both correctly when applying update.  
See the documentation here: <https://watchtower.nickfedor.com/>

Personally, I'm not a huge fan of unattended update processes. I like to keep them interactive so I can see what's going on and directly act in case of issues.  
However, I like to simplify the process as much as possible! That's why I use watchtower with the "--run-once" mode, so it is up to me to manually run it when I want to.  
See <https://watchtower.nickfedor.com/v1.12.3/configuration/arguments/#run_once> for more details

## Update All Docker Containers at once

I basically just need to launch the following command each time I want to update my docker containers:  
*(containers has to be started in order to be updated)*

```bash
sudo docker run --rm -v /var/run/docker.sock:/var/run/docker.sock nickfedor/watchtower --porcelain v1 --run-once
```

I can then clean all dangling docker images:

```bash
sudo docker image prune
```

OR

Cleaning the dangling docker images can be automatically done by Watchtower itself with the `--cleanup` argument:

```bash
sudo docker run --rm -v /var/run/docker.sock:/var/run/docker.sock nickfedor/watchtower --procelain v1 --cleanup --run-once
```

**Note:** If the host server has AppArmor enabled (which is the case by default on Debian), the Docker daemon automatically loads a [`docker-default` AppArmor profile](https://docs.docker.com/engine/security/apparmor/) into containers. You can check if the `docker-default` AppArmor profile is enforced with `aa-status` and if it's loaded in a container with `docker inspect --format='{{.AppArmorProfile}}' "container_name"`.  

This AppArmor profile *may* prevent the watchtower container to interact with the host Docker daemon via the `/var/run/docker.sock` socket (for what it's worth, that was the case on Debian but not on Arch Linux on my side, despite AppArmor and the `docker-default` profile being loaded on both). This causes Watchtower to fail updating containers.  
To workaround this, you can add the extra `--security-opt apparmor=unconfined` argument to your `docker run` command, so that the container doesn't run with the `docker-default` AppArmor profile loaded. Alternatively, one can create a drop-in AppArmor profile to allow UNIX socket access from the container and load it via `--security-opt apparmor="profile_name"`.  
