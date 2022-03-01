sbt:
	export JAVA_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5015" SBT_OPTS="-XX:+CMSClassUnloadingEnabled -Xmx4G -Xms1G" && sbt