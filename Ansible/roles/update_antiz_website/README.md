# Update Antiz Website

Update <https://antiz.fr> website against latest git state / commit (<https://github.com/Antiz96/antiz.fr>).

## Variables

The following variables are set `vars/main.yml`:

- branch: Git branch to update the `antiz.fr` website from (either `dev` or `main`, dynamically set depending on the targeted environment).
- repo_dir: Path to the `antiz.fr` repository on the hosts (either `/opt/antiz.fr` or `/data/antiz.fr`, dynamically set depending on the targeted environment).

The following variable should be set at the inventory level, the playbook level or as `--extra-vars`:

- env: Environment on which to update the `antiz.fr` website (either `dev` or `prod`).
