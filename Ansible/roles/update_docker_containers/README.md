# Update Docker Containers

Update all docker containers (via Arcane API).

## Variables

The following variables are vaulted and set in `vars/vault.yml`:

- api_url: the URL / API endpoint of my Arcane server.
- api_key: the API key / token.
- env_ids: dictionary containing all IDs of my Arcane environments.

The following variable is set in `defaults/main.yml`:

- dangling: `true` (controls whether to only remove dangling image or remove all unused images (not just dangling ones) after the update, expects a string (case sensitive) `true` or `false` value).
