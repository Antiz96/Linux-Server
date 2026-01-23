# Regenerate SSL Certificate

Regenerate / renew and deploy my home infrastructure's self-signed wildcard SSL certificate.

## Variables

The following variable is vaulted and set in `vars/vault.yml`:

- email_address: the email address to fill in the CSR information and to send a post-deployment confirmation mail to.

The following variables are set in `vars/main.yml`:

- domain_name: `home-infra.rc` (domain name / CN to fill in the CSR information).
- country: `FR` (country to fill in the CSR information).
- state: `Seine-Maritime` (state to fill in the CSR information).
- locality: `Rouen` (locality to fill in the CSR information).
- organisation: `Home-Infra` (organisation to fill in the CSR information).
- organisation_unit: `IT` (organisation unit to fill in the CSR information).

The following variable is set in `defaults/main.yml`:

- key: `false` (controls whether the private key should be copied to hosts, expects a boolean `true` or `false` value, defaults to `false`. Set this to `true` per hosts with inventory variables where needed).
