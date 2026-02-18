# Update Podman Containers

Update all podman containers (via `podman auto-update`).

## Variables

The following variable is set in `defaults/main.yml`:

- dangling: `true` (controls whether to only remove dangling image or remove all unused images (not just dangling ones) after the update, expects a boolean `true` or `false` value).
