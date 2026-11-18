# Semaphore

<https://semaphoreui.com/>

## Install Podman on my Server (if not done already)

<https://github.com/Antiz96/Linux-Server/blob/main/Services/Podman.md>

## Installing Semaphore on Docker / Podman

<https://semaphoreui.com/docs/admin-guide/installation/docker>  
<https://semaphoreui.com/install/docker/>

### Create the data, config, resources and env directories

- The data directory will be mounted to Semaphore's DB path in the container for data persistence.
- The env directory will be mounted to Semaphore's config path in the container for config persistence.
- The resources directory will be mounted in the Semaphore container as a way for me to store external resources (e.g. scripts) to the container so it can use them in tasks.  
- The env directory is where I store secret environment variable values for the container.

```bash
mkdir -p /data/podman/volumes/semaphore/{data,config,resources,env}
chmod 750 /data/podman/volumes/semaphore/env
```

As I'm running Semaphore in a podman rootless container, I need to chown the data and config directory to my user's subuid/subgid to allow the container to write in it:

```bash
chown 166536:166536 /data/podman/volumes/semaphore/{config,data}
```

### Create secrets

```bash
touch /data/podman/volumes/semaphore/env/{admin_{username,name,email,password},web_root,schedule_timezone,key_encryption,email_{sender,host,port,username,password}}
chmod 600 /data/podman/volumes/semaphore/env/*
```

Then add the corresponding values to those files (see <https://semaphoreui.com/install/docker/> for examples).  
Note that you may note need all of those, this values are linked to environment variables passed to the `podman run` command below (see the above link for the list).

### Pull and run the container

```bash
podman run -d \
  --name semaphore \
  -p 3000:3000 \
  -e SEMAPHORE_DB_DIALECT=sqlite \
  -e SEMAPHORE_ADMIN="$(cat /data/podman/volumes/semaphore/env/admin_username)" \
  -e SEMAPHORE_ADMIN_NAME="$(cat /data/podman/volumes/semaphore/env/admin_name)" \
  -e SEMAPHORE_ADMIN_EMAIL="$(cat /data/podman/volumes/semaphore/env/admin_email)" \
  -e SEMAPHORE_ADMIN_PASSWORD="$(cat /data/podman/volumes/semaphore/env/admin_password)" \
  -e SEMAPHORE_WEB_ROOT="$(cat /data/podman/volumes/semaphore/env/web_root)" \
  -e SEMAPHORE_SCHEDULE_TIMEZONE="$(cat /data/podman/volumes/semaphore/env/schedule_timezone)" \
  -e SEMAPHORE_ACCESS_KEY_ENCRYPTION="$(cat /data/podman/volumes/semaphore/env/key_encryption)" \
  -e SEMAPHORE_EMAIL_ALERT=True \
  -e SEMAPHORE_EMAIL_SECURE=True \
  -e SEMAPHORE_EMAIL_SENDER="$(cat /data/podman/volumes/semaphore/env/email_sender)" \
  -e SEMAPHORE_EMAIL_HOST="$(cat /data/podman/volumes/semaphore/env/email_host)" \
  -e SEMAPHORE_EMAIL_PORT="$(cat /data/podman/volumes/semaphore/env/email_port)" \
  -e SEMAPHORE_EMAIL_USERNAME="$(cat /data/podman/volumes/semaphore/env/email_username)" \
  -e SEMAPHORE_EMAIL_PASSWORD="$(cat /data/podman/volumes/semaphore/env/email_password)" \
  -e 'SEMAPHORE_ENV_VARS={"ANSIBLE_SSH_COMMON_ARGS":"-F /opt/resources/ssh_config"}' \
  -v /data/podman/volumes/semaphore/data:/var/lib/semaphore \
  -v /data/podman/volumes/semaphore/config:/etc/semaphore \
  -v /data/podman/volumes/semaphore/resources:/opt/resources:ro \
  docker.io/semaphoreui/semaphore:latest
```

### Access

You can now access and configure it on this URL:  
`http://[HOSTNAME]:3000/`

## Update/Upgrade procedure

I'm relying on `podman auto-update`.

Optionally clean old dangling images:

```bash
podman image prune
```
