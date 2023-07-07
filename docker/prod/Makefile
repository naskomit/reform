#UID=$(shell id -u)
#GID=$(shell id -g)
UID = 2001
GID = 2001
DATA_DIR:= "../../data/"

down:
	docker compose down

orientdb/setup:
	DATA_DIR=$(DATA_DIR) docker compose build --build-arg HOST_UID=$(UID) --build-arg HOST_GID=$(GID) orientdb
	mkdir -p $(DATA_DIR)/orientdb/databases/
	mkdir -p $(DATA_DIR)/orientdb/backup/
	mkdir -p $(DATA_DIR)/orientdb/config/
	cp -r ../build/orientdb/config $(DATA_DIR)/orientdb/
	sudo chown -R $(UID):$(GID) $(DATA_DIR)

orientdb/bash:
	docker compose exec orientdb /bin/bash

demo1/bash:
	docker compose exec app-server /bin/ash

# The smo-skull-app probably comes from the name sysmo.smoskull.SmoSkullApp
smo-skull/import:
	docker compose exec app-server /opt/docker/bin/smo-skull-app import /data/import/skull_inventory/Metadata_v4_202306_.csv

server/up:
	DATA_DIR=$(DATA_DIR) docker compose up -d

server/down:
	DATA_DIR=$(DATA_DIR) docker compose down
