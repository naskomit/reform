sbt:
	export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5015" SBT_OPTS="-Xmx4G -Xms1G" && sbt

#reform_back/amm:
#	sbt "reform_back/test:runMain amm"

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

#docker/demo1:
#	docker run -p 9005:9000 demo1_backend:0.1.1

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

prod-stack/setup:
	cd docker/prod && make orientdb/setup

server/up:
	cd docker/prod && make server/up

smo-skull/import:
	cd docker/prod && make smo-skull/import

server1/ssh:
	ssh naskomit@reform.sysmoltd.com
