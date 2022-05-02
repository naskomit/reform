sbt:
	export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5015" SBT_OPTS="-Xmx4G -Xms1G" && sbt

covid/docker:
	sbt "server/Docker/publishLocal"

covid/run:
	cd docker && docker compose up app-server
#	docker run -p 127.0.0.1:9000:9000 --network='docker_net-1' server:0.1.0-SNAPSHOT
	#docker run -p 0.0.0.0:9000:9000 server:0.1.0-SNAPSHOT

covid/bash:
	cd docker && docker compose exec app-server /bin/bash

reform_back/amm:
	sbt "reform_back/test:runMain amm"


