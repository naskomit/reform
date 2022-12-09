# Reform Deployment

## First time setup

### Install docker

### Clone repository and initial setup

```shell
git clone git@github.com:naskomit/reform.git
cd reform
cd docker/prod
make orientdb/setup 
cd ../..
```

### Configure user for file access
```shell
sudo adduser -u 1001 dduser
sudo usermod -a -G dduser naskomit
sudo chown -R dduser:dduser data/
```


### Configure nginx

Create a new site-available reform.sysmoltd.com

Important: Make sure the option `ipv6only=on` in the line 
```
listen [::]:443 ssl ipv6only=on; # managed by Certbot
```

appears in ONLY one of the configuration files. Remove it from all the other ones. Otherwise the configuration is not going to work.

Create a symbolic link to site-enabled
```shell
sudo ln -s /etc/nginx/sites-available/reform.sysmoltd.com /etc/nginx/sites-enabled/reform.sysmoltd.com
```
Run opencert
```shell
sudo certbot --nginx -d reform.sysmoltd.com
```


## Update setup

