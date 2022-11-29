sbt:
	export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5015" SBT_OPTS="-Xmx4G -Xms1G" && sbt

deepclean:
	rm -rf target/
	rm -rf frontend/target/
	rm -rf backend/target/
	rm -rf shared/.js/target
	rm -rf shared/.jvm/target
	rm -rf apps/demo1/backend/target/
	rm -rf apps/demo1/frontend/target/
	rm -rf project/target/
	rm -rf project/project

demo1/build-docker:
	sbt "demo1_backend / Docker / publishLocal"

demo1/push-docker:
	sbt "demo1_backend / Docker / publish"

demo1/run-docker:
	cd docker/dev && make demo1/run-docker

demo1/clean-js:
	rm apps/demo1/frontend/target/scala-2.13/scalajs-bundler/main/*.js*

demo1/bash:
	cd docker/dev && docker compose exec app-server /bin/ash

keycloak/up:
	cd docker/dev && make keycloak/up

orientdb/up:
	cd docker/dev && make orientdb/up

dev-stack/setup:
	cd docker/dev && make orientdb/setup

dev-stack/up:
	cd docker/dev && make stack/up

# Configure the production server stack
prod-stack/setup:
	cd docker/prod && make orientdb/setup

# Starts the whole server stack up
server/up:
	cd docker/prod && make server/up

smo-skull/import:
	cd docker/prod && make smo-skull/import

SERVER1_USER:=naskomit
SERVER1_URL:=reform.sysmoltd.com

server1/ssh:
	ssh $(SERVER1_USER)@$(SERVER1_URL)

server1/ssh+ports:
	ssh $(SERVER1_USER)@$(SERVER1_URL) -L 2480:localhost:2480 -L 9005:localhost:9005


