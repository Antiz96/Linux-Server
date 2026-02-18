# Update Antiz Website

Update <https://antiz.fr> website against latest git state / commit (<https://github.com/Antiz96/antiz.fr>).

## Variables

The following variables are set in `vars/main.yml`:

- deploy_hosts: Dictionary containing the host(s) on which to deploy depending on the targeted environments.

The following variable should be set at the inventory level, the playbook level or as `--extra-vars`:

- env: Determines on which host(s) to deploy, depending on the targeted environment (either `dev` or `prod`).
- branch: Git branch to update the `antiz.fr` website from (either `dev` or `main`, already set at the inventory level on my side).
