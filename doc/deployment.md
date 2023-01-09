# Reform Deployment

## First time setup

### Install docker

https://docs.docker.com/engine/install/ubuntu/

```shell
sudo apt-get update

sudo apt-get install \
    ca-certificates \
    curl \
    gnupg \
    lsb-release

sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null


sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo docker run hello-world
```

### Add user to run the docker images

```shell
sudo useradd -u 2001 -m -g docker reform
```

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

