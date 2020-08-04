FROM maven:3.6.3-jdk-8-slim AS thirdparty
# We build the vendored dependencies in their own image so we only need to
# rebuild this image when those dependencies change, and so we don't need to
# keep their build dependencies.

RUN apt-get update && apt-get install -y wget \
    g++ \
    binutils \
    make \
    git \
    python

ADD ./3rdparty /opt/apalache/3rdparty
WORKDIR /opt/apalache/

# Workaround for Surefire not finding ForkedBooter
# (see https://stackoverflow.com/questions/53010200/maven-surefire-could-not-find-forkedbooter-class)
ENV _JAVA_OPTIONS="-Djdk.net.URLClassPath.disableClassPathURLCheck=true"

RUN 3rdparty/install-local.sh --nocache

FROM maven:3.6.3-jdk-8-slim

ADD . /opt/apalache/
WORKDIR /opt/apalache/

# Vendored binaries
COPY --from=thirdparty /opt/apalache/3rdparty/bin /opt/apalache/3rdparty/bin
# Vendored libraies
COPY --from=thirdparty /opt/apalache/3rdparty/lib /opt/apalache/3rdparty/lib
# The maven repository
COPY --from=thirdparty /root/.m2 /root/.m2

ENV LD_LIBRARY_PATH="/opt/apalache/3rdparty/lib/:${LD_LIBRARY_PATH}"

# clean the leftovers from the previous non-docker builds and build the package
RUN mvn clean package

# TLA parser requires all specification files to be in the same directory
# We assume the user bind-mounted the spec dir into /var/apalache
# In case the directory was not bind-mounted, we create one
RUN mkdir /var/apalache 2>/dev/null

# make apalache-mc available in PATH
ENV PATH="/usr/local/openjdk-8/bin/:/opt/apalache/bin:${PATH}"

# what to run
ENTRYPOINT ["/opt/apalache/bin/run-in-docker-container"]
