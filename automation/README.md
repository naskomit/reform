## Create virtual environment

```
make setup
activate-global-python-argcomplete --user
```

## Activate virtual environment
```
source ./envs/dev/bin/activate
```

### Cleanup virtual environments

```
make remove
```

### Forward port 443 to port 8443 which is exposed by Vagrant

https://superuser.com/questions/710253/allow-non-root-process-to-bind-to-port-80-and-443
https://coderwall.com/p/plejka/forward-port-80-to-port-3000

Local:
```
sudo iptables -t nat -I OUTPUT -p tcp -d 127.0.0.1 --dport 80 -j REDIRECT --to-ports 8480
sudo iptables -t nat -I OUTPUT -p tcp -d 127.0.0.1 --dport 443 -j REDIRECT --to-ports 8443
```
External:
```
sudo iptables -t nat -I PREROUTING -p tcp --dport 443 -j REDIRECT --to-ports 8443
```

# sudo iptables -t nat -I OUTPUT -p tcp -d 127.0.0.1 --dport 443 -j REDIRECT --to-ports 8443
# 