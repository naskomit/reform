sbt:
	export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5015" SBT_OPTS="-Xmx4G -Xms1G" && sbt

covid/docker:
	sbt "covidhub_backend/Docker/publishLocal"

covid/run:
	cd docker && docker compose up app-server
#	docker run -p 127.0.0.1:9000:9000 --network='docker_net-1' server:0.1.0-SNAPSHOT
	#docker run -p 0.0.0.0:9000:9000 server:0.1.0-SNAPSHOT

covid/bash:
	cd docker && docker compose exec app-server /bin/bash

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

demo1/make-docker:
	sbt "demo1_backend / Docker / publishLocal"

demo1/run-docker:
	cd docker && docker compose up app-server

