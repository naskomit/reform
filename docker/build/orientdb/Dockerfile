############################################################
# Dockerfile to run an OrientDB (Graph) Container
############################################################

FROM openjdk:8-jdk-slim
ARG HOST_UID
ARG HOST_GID
ARG HOST_GROUP=docker
ARG LOCAL_USER=docker
# Override the orientdb download location with e.g.:
#   docker build -t mine --build-arg ORIENTDB_DOWNLOAD_SERVER=https://repo1.maven.org/maven2/com/orientechnologies/ .
ARG ORIENTDB_DOWNLOAD_SERVER="https://repo1.maven.org/maven2/com/orientechnologies"
ARG ORIENTDB_DIST=orientdb-tp3

ENV host_uid=$HOST_UID
ENV host_gid=$HOST_GID
ENV host_group=$HOST_GROUP
ENV local_user $LOCAL_USER

MAINTAINER OrientDB LTD (info@orientdb.com)


ENV ORIENTDB_VERSION 3.2.5
ENV ORIENTDB_DOWNLOAD_MD5 b1fc46b7a1fbdae1cf94cc8ccabfbbb9
ENV ORIENTDB_DOWNLOAD_SHA1 7125b9cddbdfe2aaaa630b820e36880699402637

#ENV ORIENTDB_DOWNLOAD_URL ${ORIENTDB_DOWNLOAD_SERVER:-https://repo1.maven.org/maven2/com/orientechnologies}/orientdb-community/$ORIENTDB_VERSION/orientdb-community-$ORIENTDB_VERSION.tar.gz
ENV ORIENTDB_DOWNLOAD_URL $ORIENTDB_DOWNLOAD_SERVER/orientdb-tp3/$ORIENTDB_VERSION/$ORIENTDB_DIST-$ORIENTDB_VERSION.tar.gz
ENV ORIENTDB_ROOT_PASSWORD=smopass

USER root
RUN rm -f /var/log/lastlog; rm -rf /var/log/faillog
RUN useradd $local_user
COPY fix_user_ids.pl /usr/local/bin/fix_user_idsv2.pl
RUN  /usr/local/bin/fix_user_idsv2.pl $local_user $host_uid $host_gid $host_group

RUN apt update \
    && apt install -y curl wget \
    && rm -rf /var/lib/apt/lists/*

#download distribution tar, untar and delete databases
RUN mkdir /orientdb && \
  wget  $ORIENTDB_DOWNLOAD_URL \
  && echo "$ORIENTDB_DOWNLOAD_MD5 *$ORIENTDB_DIST-$ORIENTDB_VERSION.tar.gz" | md5sum -c - \
  && echo "$ORIENTDB_DOWNLOAD_SHA1 *$ORIENTDB_DIST-$ORIENTDB_VERSION.tar.gz" | sha1sum -c - \
  && tar -xvzf $ORIENTDB_DIST-$ORIENTDB_VERSION.tar.gz -C /orientdb --strip-components=1 \
  && rm $ORIENTDB_DIST-$ORIENTDB_VERSION.tar.gz \
  && rm -rf /orientdb/databases/*

RUN chown -R docker:docker /orientdb

#ENV PATH /orientdb/bin:$PATH

#VOLUME ["/orientdb/backup", "/orientdb/databases", "/orientdb/config"]

USER $LOCAL_USER
ENV PATH /orientdb/bin:$PATH

WORKDIR /orientdb

##OrientDb binary
#EXPOSE 2424
#
##OrientDb http
#EXPOSE 2480

# Default command start the server
#CMD ["server.sh"]