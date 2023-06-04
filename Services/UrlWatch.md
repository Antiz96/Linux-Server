# urlwatch
  
https://thp.io/2008/urlwatch/  
  
`urlwatch` is a tool that helps you watch changes in webpages and get notified (via e-mail, in your terminal or through various third party services) of any changes.  
I use it to monitor upstream releases of the various packages I maintain on the Arch repositories and the AUR.  

## Installation
  
```
sudo pacman -S urlwatch
```
  
## Configuration

### URLs configuration

The URL configuration serves to declare the URLs to watch and the different settings/filters to apply.  
To create or edit your URL configuration file, run:  
  
```
urlwatch --edit
```
  
See the [documentation](https://urlwatch.readthedocs.io/en/latest/introduction.html#jobs-and-filters) for more information.  
My personal configuration is available [here](https://github.com/Antiz96/Linux-Server/blob/main/Dotfiles/Services/UrlWatch-conf.yaml).  
 
Once configured (and after any future modifications in the configuration), run `urlwatch` once to initialize it.

### Reporter configuration

The reporter configuration serves to declare additional ways to report changes in URLs (in addition of showing them in the console), such as mail or various third party services.  
To create or edit your reporter configuration file, run:  
  
```
urlwatch --edit-config
```
  
I personally configured `urlwatch` to send changes via mail (through GMAIL SMTP), like so:  
  
Configure your GMail account to allow for "less secure" (password-based) apps to login
- Go to https://myaccount.google.com/
- Click on "Security", then "Applications passwords"
- Scroll all the way down to "Allow less secure apps" and enable it, then generate a password for `urlwatch`
  
Then, configure `urlwatch` that way:  
```
urlwatch --edit-config
```
> report/email/enabled: true  
> report/email/from: your.username@gmail.com (edit accordingly)  
> report/email/method: smtp  
> report/email/smtp/host: smtp.gmail.com  
> report/email/smtp/port: 587  
> report/email/smtp/starttls: true  
> report/email/smtp/insecure_password: the password you created in the previous step  
> report/email/to: The e-mail address you want to send reports to  
  
## Run periodically

Set `urlwatch` to run periodically via a cronjob, a systemd timer or any other way to get automatically notified in case of changes on the monitored URLs.
