UID=$(shell id -u)
GID =$(shell id -g)

down:
	docker compose down

orientdb/setup:
	docker compose build --build-arg HOST_UID=$(UID) --build-arg HOST_GID=$(GID) orientdb
	mkdir -p ../data/orientdb/databases/
	mkdir -p ../data/orientdb/backup/
	mkdir -p ../data/orientdb/config/
	cp -r build/orientdb/config ../data/orientdb/

orientdb/up:
	docker compose up orientdb

orientdb/bash:
	docker compose exec orientdb /bin/bash

#cd docker &&
keycloak/up:
	docker compose up keycloak

keycloak/down:
	docker compose stop keycloak