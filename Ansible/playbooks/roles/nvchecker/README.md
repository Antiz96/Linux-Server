# NVChecker

Run NVChecker on my server and send a mail including the new upstream releases (used to keep track of my Arch Linux and Alpine Linux packages).

## Variables

The following variables are vaulted and set in `vars/vault.yml`:

- github_token: github token for nvchecker to avoid GitHub API rate limits.
- email_address: the email address to send the mail containing the `nvchecker` output to.

The following variable is set in `vars/main.yml`:

- keyfile_path: `/home/ansible/.config/nvchecker/keyfile.toml` (where to deploy the nvchecker keyfile)
- config_path: `/home/ansible/.config/nvchecker/nvchecker.toml` (where to deploy the nvchecker configuration file)
- user: `ansible` (user to set permission to for the nvchecker keyfile and configuration file)
