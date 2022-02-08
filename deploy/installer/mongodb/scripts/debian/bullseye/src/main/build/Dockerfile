########################################################################################################################
#                                                                                                                      #
#         !!!         THIS DOCKER IS FOR TESTING ONLY PURPOSES AND NOT FOR PRODUCTIVE DEPLOYMENT         !!!           #
#                                                                                                                      #
########################################################################################################################

FROM docker.io/${os.distribution}:${os.codename}

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
#
#RUN set -eux \
#    && wget -q https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public \
#    && gpg --no-default-keyring --keyring ./adoptopenjdk-keyring.gpg --import public \
#    && gpg --no-default-keyring --keyring ./adoptopenjdk-keyring.gpg --export --output adoptopenjdk-archive-keyring.gpg \
#    && mv adoptopenjdk-archive-keyring.gpg /usr/share/keyrings \
#    && rm adoptopenjdk-keyring.gpg public \
#    && echo "deb [signed-by=/usr/share/keyrings/adoptopenjdk-archive-keyring.gpg] https://adoptopenjdk.jfrog.io/adoptopenjdk/deb `lsb_release -cs` main" | tee /etc/apt/sources.list.d/adoptopenjdk.list \
#    && apt-get update -y \
#    && apt-get install -y --no-install-recommends \
#        adoptopenjdk-8-hotspot \
#    && update-java-alternatives -s adoptopenjdk-8-hotspot-amd64 \
#    && apt-get clean \
#    && rm -r /var/lib/apt/lists/*
#
#RUN set -eux \
#    && apt-get update -y \
#    && apt-get install -y --no-install-recommends \
#	    maven=3.6.3-* \
#    && apt-get clean \
#    && rm -r /var/lib/apt/lists/*

########################################################################################################################

#ENV EDU_HOME /opt/edu-sharing
#
#WORKDIR $EDU_HOME

########################################################################################################################

ENV MONGO_MAJOR 5.0

RUN set -eux \
    && wget -qO - https://www.mongodb.org/static/pgp/server-5.0.asc | apt-key add - \
    && curl -sSL https://www.mongodb.org/static/pgp/server-5.0.asc  -o mongoserver.asc \
    && gpg --no-default-keyring --keyring ./mongo_key_temp.gpg --import ./mongoserver.asc \
    && gpg --no-default-keyring --keyring ./mongo_key_temp.gpg --export > ./mongoserver_key.gpg \
    && mv mongoserver_key.gpg /etc/apt/trusted.gpg.d/ \
    && echo "deb http://repo.mongodb.org/apt/debian buster/mongodb-org/$MONGO_MAJOR main" | tee /etc/apt/sources.list.d/mongodb-org-5.0.list \
    && apt-get update -y \
    && apt-get install -y --no-install-recommends \
        mongodb-org

COPY assets/dbInit.js .
COPY assets/config/mongod.conf /etc/mongod.conf

RUN mkdir -p /data/db /data/configdb \
	&& chown -R mongodb:mongodb /data/db /data/configdb

#RUN set -eux \
#    && wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | apt-key add - \
#    && echo "deb https://artifacts.elastic.co/packages/${elasticsearch.version.major}.x/apt stable main" | tee /etc/apt/sources.list.d/elastic-${elasticsearch.version.major}.x.list \
#    && apt-get update -y \
#    && apt-get install -y --no-install-recommends \
#        elasticsearch=${elasticsearch.version}* \
#    && mkdir /var/run/elasticsearch \
#    && chown elasticsearch:elasticsearch -R /etc/default/elasticsearch \
#    && chown elasticsearch:elasticsearch -R /etc/elasticsearch \
#    && chown elasticsearch:elasticsearch -R /usr/share/elasticsearch \
#    && chown elasticsearch:elasticsearch -R /var/lib/elasticsearch \
#    && chown elasticsearch:elasticsearch -R /var/log/elasticsearch \
#    && chown elasticsearch:elasticsearch -R /var/run/elasticsearch \
#    && apt-get clean \
#    && rm -r /var/lib/apt/lists/*
#
#COPY --chown=elasticsearch:elasticsearch assets/config/elasticsearch.yml /etc/elasticsearch/elasticsearch.yml

########################################################################################################################

#COPY install.sh .env.base ./

COPY assets/entrypoint.sh ./

#COPY assets/settings.xml /root/.m2/

RUN set -eux \
	&& find . -type f -name '*.sh' -exec chmod +x {} \;

########################################################################################################################

EXPOSE 27017

#ENTRYPOINT ["/bin/bash"]
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
