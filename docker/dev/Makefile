UID=$(shell id -u)
GID =$(shell id -g)
DATA_DIR:= "../../data/"

down:
	docker compose down

orientdb/setup:
	docker compose build --build-arg HOST_UID=$(UID) --build-arg HOST_GID=$(GID) orientdb
	mkdir -p $(DATA_DIR)/orientdb/databases/
	mkdir -p $(DATA_DIR)/orientdb/backup/
	mkdir -p $(DATA_DIR)/orientdb/config/
	cp -r ../build/orientdb/config $(DATA_DIR)/orientdb/

orientdb/up:
	docker compose up orientdb

orientdb/shell:
	docker compose exec orientdb /bin/bash

nginx/up:
	docker compose up nginx

nginx/shell:
	docker compose exec nginx /bin/ash

nginx/check:
	docker compose exec nginx nginx -t

#nginx/cert:
#	openssl req -x509 -out localhost.crt -keyout localhost.key \
#	  -newkey rsa:2048 -nodes -sha256 \
#	  -subj '/CN=localhost' -extensions EXT -config <( \
#	   printf "[dn]\nCN=localhost\n[req]\ndistinguished_name = dn\n[EXT]\nsubjectAltName=DNS:localhost\nkeyUsage=digitalSignature\nextendedKeyUsage=serverAuth")


postgres/up:
	docker compose up postgres

postgres/db/create:
#	$(eval $@PGSQL:='CREATE DATABASE keycloak_db;')
#	docker compose exec -u postgres postgres psql -U smoadmin -c '\x' -c $($@PGSQL)
	$(eval $@PGSQL:='CREATE USER keycloak_user;')
	docker compose exec -u postgres postgres psql -U smoadmin -c '\x' -c $($@PGSQL)
	$(eval $@PGSQL:="alter user keycloak_user with encrypted password 'keypass';")
	docker compose exec -u postgres postgres psql -U smoadmin -c '\x' -c $($@PGSQL)
	$(eval $@PGSQL:='grant all privileges on database keycloak_db to keycloak_user ;')
	docker compose exec -u postgres postgres psql -U smoadmin -c '\x' -c $($@PGSQL)

postgres/shell:
	docker compose exec postgres /bin/ash


keycloak/up:
	docker compose up postgres keycloak

keycloak/shell:
	docker compose exec keycloak /bin/bash

keycloak/down:
	docker compose stop keycloak

rabbitmq/bash:
	docker compose exec rabbitmq /bin/bash

stack/up:
	docker compose up orientdb rabbitmq

demo1/run-docker:
	docker compose up app-server


demo1/shell:
	docker compose exec app-server /bin/ash

