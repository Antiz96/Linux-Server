# Postfix

<https://www.postfix.org>

Postfix is a Mail Server that I personally use as a SMTP relay in combination with a gmail address to be able to send mails from my Linux servers (mostly in scripts/scheduled jobs to send success and/or failure notifications).

## SMTP Relay

### Install the package and start the service

- Arch:

```bash
sudo pacman -S postfix
sudo systemctl enable --now postfix.service
```

- Debian:

```bash
sudo apt install postfix
sudo systemctl enable --now postfix.service
```

### Create a dedicated application password on GMail

For security reasons, you have to create a dedicated application password to connect your gmail address to the SMTP relay. Note that using 2FA on your google account is required to be able to create applications password.

Go into your [account's security settings](https://myaccount.google.com/security) and open the 2FA submenu.  
With 2FA enabled, go into the "Application passwords" menu at the bottom of the page.  
From there, create an application password and save it for later.

### Configure the SMTP relay on postfix

Create the general configuration for the SMTP relay:

```bash
sudo -e /etc/postfix/main.cf
```

```text
[...]
# SMTP relay GMail
relayhost = [smtp.gmail.com]:587
smtp_tls_security_level = secure
smtp_sasl_auth_enable = yes
smtp_sasl_password_maps = lmdb:/etc/postfix/.sasl_passwd
smtp_sasl_security_options = noanonymous
smtp_tls_CAfile = /etc/ssl/certs/ca-certificates.crt
```

Create the sasl_passwd file containing login information (with secured permissions) and generate the db file for it via `postmap`:

```bash
sudo -e /etc/postfix/.sasl_passwd
```

```text
[smtp.gmail.com]:587 EMAIL_ADDRESS@gmail.com:APPLICATION_PASSWORD_CREATED_EARLIER
```

```bash
sudo chmod 400 /etc/postfix/.sasl_passwd
sudo postmap /etc/postfix/.sasl_passwd
```

### Restart service to apply changes and send a test email

```bash
sudo systemctl restart postfix.service
echo -e "Subject:Test\n\nHello,\n\nThis is a test" | sendmail yourmailexample@mail.com, yourmailexample2@mail.com
```
