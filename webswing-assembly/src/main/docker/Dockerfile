# ── Stage 1: Build ────────────────────────────────────────────────────
# Uses the Gradle wrapper from the repo to produce the distribution zip.
FROM eclipse-temurin:21-jdk-noble AS builder

RUN apt-get update && apt-get install -y --no-install-recommends \
        git \
        unzip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /build

# Cache Gradle wrapper + dependencies before copying full source
COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
RUN chmod +x gradlew && ./gradlew --version --no-daemon

# Copy build definitions next (changes less often than source)
COPY build.gradle settings.gradle gradle.properties ./
COPY buildSrc/ buildSrc/

# Copy full source and build the distribution
COPY . .
RUN ./gradlew :webswing-assembly:distZip --no-daemon -x test -x spotbugsMain -x checkstyleMain -x pmdMain

# Unpack the distribution
RUN mkdir -p /opt/webswing \
    && unzip webswing-assembly/build/dist/webswing-*.zip -d /tmp/dist \
    && mv /tmp/dist/webswing-*/* /opt/webswing/ \
    && chmod +x /opt/webswing/*.sh

# ── Stage 2: Runtime ─────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-noble

LABEL maintainer="Manticore Projects <info@manticore-projects.com>" \
      org.opencontainers.image.title="Webswing" \
      org.opencontainers.image.description="Java Swing applications in the browser" \
      org.opencontainers.image.source="https://github.com/manticore-projects/webswing"

# X11 / AWT dependencies for headless Swing rendering
# upgrade first to pick up security patches for base OS packages (wget, tar, systemd, etc.)
RUN apt-get update && apt-get upgrade -y && apt-get install -y --no-install-recommends \
        xvfb \
        libxext6 \
        libxi6 \
        libxtst6 \
        libxrender1 \
        libfontconfig1 \
        fonts-dejavu-core \
        curl \
    && rm -rf /var/lib/apt/lists/*

# Non-root user
RUN groupadd -r webswing && useradd -r -g webswing -m -s /bin/false webswing

# Copy distribution from builder
COPY --from=builder --chown=webswing:webswing /opt/webswing /opt/webswing

WORKDIR /opt/webswing

# Logging, data, and X11 directories (writable by webswing user)
RUN mkdir -p logs tmp /tmp/.X11-unix \
    && chown -R webswing:webswing logs tmp \
    && chmod 1777 /tmp/.X11-unix

ENV JAVA_HOME="/opt/java/openjdk" \
    DISPLAY=":99" \
    WEBSWING_HOME="/opt/webswing"

# JVM flags — mirrors run.sh but without daemonization
ENV JAVA_OPTS="\
 -server -Xmx2G -Xms256m -Xss228k \
 -Djava.security.egd=file:/dev/urandom \
 -Djava.io.tmpdir=/opt/webswing/tmp \
 --add-modules=java.desktop \
 --add-exports=java.desktop/sun.awt=ALL-UNNAMED \
 --add-exports=java.desktop/sun.awt.dnd=ALL-UNNAMED \
 --add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED \
 --add-exports=java.desktop/sun.awt.image=ALL-UNNAMED \
 --add-exports=java.desktop/sun.java2d=ALL-UNNAMED \
 --add-exports=java.desktop/sun.java2d.pipe=ALL-UNNAMED \
 --add-exports=java.desktop/sun.java2d.loops=ALL-UNNAMED \
 --add-exports=java.desktop/sun.font=ALL-UNNAMED \
 --add-exports=java.desktop/sun.print=ALL-UNNAMED \
 --add-exports=java.desktop/java.awt.peer=ALL-UNNAMED \
 --add-exports=java.desktop/java.awt.dnd=ALL-UNNAMED \
 --add-exports=java.base/sun.nio.cs=ALL-UNNAMED \
 --add-opens=java.desktop/sun.awt.image=ALL-UNNAMED \
"

ENV WEBSWING_OPTS="-h 0.0.0.0 -j jetty.properties -c webswing.config -pf webswing.properties"

EXPOSE 8080 8443

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD curl -sf http://localhost:8080/ || exit 1

USER webswing

# Start Xvfb in background, then run Webswing in foreground.
# WAR filename is resolved at runtime so versioned names work.
CMD sh -c '\
    Xvfb ${DISPLAY} -screen 0 1024x768x24 -nolisten tcp &\
    sleep 1 && \
    WAR=$(ls ${WEBSWING_HOME}/webswing-server*.war 2>/dev/null | head -1) && \
    exec java ${JAVA_OPTS} -jar ${WAR} ${WEBSWING_OPTS}'