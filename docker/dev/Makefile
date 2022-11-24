UID=$(shell id -u)
GID =$(shell id -g)
DATA_DIR:= "../../data/"

down:
	docker compose down

orientdb/setup:
	DATA_DIR=$(DATA_DIR) docker compose build --build-arg HOST_UID=$(UID) --build-arg HOST_GID=$(GID) orientdb
	mkdir -p $(DATA_DIR)/orientdb/databases/
	mkdir -p $(DATA_DIR)/orientdb/backup/
	mkdir -p $(DATA_DIR)/orientdb/config/
	cp -r ../build/orientdb/config $(DATA_DIR)/orientdb/

orientdb/up:
	DATA_DIR=$(DATA_DIR) docker compose up orientdb

orientdb/bash:
	docker compose exec orientdb /bin/bash

keycloak/up:
	docker compose up keycloak

keycloak/down:
	docker compose stop keycloak

demo1/run-docker:
	DATA_DIR=$(DATA_DIR) docker compose up app-server


demo1/bash:
	docker compose exec app-server /bin/ash

