########################################################################################################################
#                                                                                                                      #
#         !!!         THIS DOCKER IS FOR TESTING ONLY PURPOSES AND NOT FOR PRODUCTIVE DEPLOYMENT         !!!           #
#                                                                                                                      #
########################################################################################################################

FROM dockerio.mirror.docker.edu-sharing.com/${os.distribution}:${os.codename}

RUN set -eux \
    && ln -sf /usr/share/zoneinfo/Europe/Berlin /etc/localtime

RUN set -eux \
    && apt-get update -y \
    && apt-get install -y --no-install-recommends \
        locales \
    && sed -i 's/^\(\\s+\)/# \1/' /etc/locale.gen \
    && sed -i 's/^# *\(de_DE.UTF-8\)/\1/' /etc/locale.gen \
    && locale-gen \
    && update-locale LANG=de_DE.utf8 \
    && apt-get clean \
    && rm -r /var/lib/apt/lists/*

RUN set -eux \
    && apt-get update -y \
    && apt-get install -y --no-install-recommends \
        apt-transport-https \
        apt-utils \
        ca-certificates \
        curl \
        dirmngr \
        dnsutils \
        gnupg \
        dirmngr \
        jq \
        numactl \
        lsb-release \
        lsof \
        nano \
        net-tools \
        procps \
        ruby-hocon \
        systemctl \
        unzip \
        vim \
        wait-for-it \
        wget \
        xmlstarlet \
    && apt-get clean \
    && rm -r /var/lib/apt/lists/*

########################################################################################################################

ENV MONGO_VERSION ${mongodb.version.major}.${mongodb.version.minor}

RUN set -eux \
    && wget -qO - https://www.mongodb.org/static/pgp/server-$MONGO_VERSION.asc | apt-key add - \
    && curl -sSL https://www.mongodb.org/static/pgp/server-$MONGO_VERSION.asc  -o mongoserver.asc \
    && gpg --no-default-keyring --keyring ./mongo_key_temp.gpg --import ./mongoserver.asc \
    && gpg --no-default-keyring --keyring ./mongo_key_temp.gpg --export > ./mongoserver_key.gpg \
    && mv mongoserver_key.gpg /etc/apt/trusted.gpg.d/ \
    && echo "deb https://artifacts.edu-sharing.com/repository/mongodb buster/mongodb-org/$MONGO_VERSION main" | tee /etc/apt/sources.list.d/mongodb.list \
    && apt-get update -y \
    && apt-get install -y --no-install-recommends \
        mongodb-org=$MONGO_VERSION.${mongodb.version.patch}* \
    && apt-get clean \
    && rm -r /var/lib/apt/lists/*

COPY assets/config/mongod.conf /etc/mongod.conf

RUN mkdir -p /data/db /data/configdb \
	&& chown -R mongodb:mongodb /data/db /data/configdb

########################################################################################################################

COPY assets/entrypoint.sh ./

RUN set -eux \
	&& find . -type f -name '*.sh' -exec chmod +x {} \;

########################################################################################################################

EXPOSE 27017

ENTRYPOINT ["./entrypoint.sh"]
#CMD ["mongod"]
CMD ["/bin/bash"]
########################################################################################################################

LABEL git.branch=${git.branch}
LABEL git.closest.tag.name=${git.closest.tag.fixed}
LABEL git.commit.id=${git.commit.id}
LABEL git.dirty=${git.dirty}
LABEL mvn.project.artifactId=${project.artifactId}
LABEL mvn.project.groupId=${project.groupId}
LABEL mvn.project.version=${project.version}
