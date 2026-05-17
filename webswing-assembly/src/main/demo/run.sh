#!/bin/sh

set -eu

APP="WebSwing"
WAR_PREFIX="webswing-server"
CONFIG_DIR="."
BASE="$(cd "$(dirname "$0")" && pwd -P)"
JAVA_HOME="/usr/lib/jvm/default-runtime"
LOGGING="-Djava.util.logging.config.file=${HOME}/.manticore/logging.properties"
MIN_JAVA_VERSION=17

# SSL certificates to import into the JVM truststore on startup.
# Format: alias|host|port (one per line)
# Add new certificates by appending lines.
SSL_CERTS=""
SSL_KEYSTORE="${JAVA_HOME}/lib/security/cacerts"
SSL_KEYSTORE_PASS="changeit"

# Server JVM flags
JAVA_OPTS="\
 -server -Xmx2G -Xms256m -Xss228k \
 -Dcom.sun.jndi.ldap.object.disableEndpointIdentification=true \
 -Dwebswing.websocketMessageSizeLimit=1024000 \
 -Djava.security.egd=file:/dev/urandom \
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

WEBSWING_OPTS="-h 0.0.0.0 -j $CONFIG_DIR/jetty.properties -c $CONFIG_DIR/webswing.config -pf $CONFIG_DIR/webswing.properties"
NICE_LEVEL=10
LOG_RETENTION_DAYS=10

PID_FILE="${BASE}/logs/${APP}.pid"
TAIL_PID_FILE="${BASE}/logs/${APP}-tail.pid"
LOG_FILE="${BASE}/logs/${APP}.log"

USAGE="$(basename "$0") -- WebSwing Server

Usage:
    $(basename "$0") { start | stop | restart | status | list }
    $(basename "$0") -v VERSION { start | restart }
    $(basename "$0") -h | --help

Options:
    -v VERSION    Use a specific WAR version (e.g. -v 26.0)
                  Default: auto-detect latest version

Commands:
    start         Start the server
    stop          Stop the server
    restart       Stop + Start
    status        Check if the server is running
    list          List all available WAR versions"

# ── WAR Version Detection ───────────────────────────────────────────────

# Compare two version strings: returns 0 if $1 >= $2, 1 otherwise
# Handles: 20.2.5 vs 26.0, 26.0 vs 26.0.1, etc.
version_ge() {
    v1="$1"
    v2="$2"

    # Compare segment by segment
    while true; do
        # Extract first segment
        seg1="${v1%%.*}"
        seg2="${v2%%.*}"

        # Default empty to 0
        seg1="${seg1:-0}"
        seg2="${seg2:-0}"

        if [ "${seg1}" -gt "${seg2}" ] 2>/dev/null; then
            return 0
        elif [ "${seg1}" -lt "${seg2}" ] 2>/dev/null; then
            return 1
        fi

        # Move to next segment
        case "${v1}" in
            *.*) v1="${v1#*.}" ;;
            *)   v1="" ;;
        esac
        case "${v2}" in
            *.*) v2="${v2#*.}" ;;
            *)   v2="" ;;
        esac

        # Both exhausted — equal
        if [ -z "${v1}" ] && [ -z "${v2}" ]; then
            return 0
        fi
    done
}

# Find all WAR files and extract versions
list_wars() {
    found=0
    for war in "${BASE}/${WAR_PREFIX}"-*.war; do
        [ -f "${war}" ] || continue
        filename=$(basename "${war}")
        ver="${filename#"${WAR_PREFIX}"-}"
        ver="${ver%.war}"
        echo "${ver}"
        found=1
    done

    # Also check for unversioned WAR
    if [ -f "${BASE}/${WAR_PREFIX}.war" ]; then
        echo "(unversioned)"
        found=1
    fi

    if [ "${found}" -eq 0 ]; then
        return 1
    fi
    return 0
}

# Find the latest versioned WAR file
find_latest_war() {
    latest_ver=""
    latest_war=""

    for war in "${BASE}/${WAR_PREFIX}"-*.war; do
        [ -f "${war}" ] || continue
        filename=$(basename "${war}")
        ver="${filename#"${WAR_PREFIX}"-}"
        ver="${ver%.war}"

        # Skip non-numeric versions (e.g. SNAPSHOT)
        case "${ver}" in
            *[!0-9.]*) continue ;;
        esac

        if [ -z "${latest_ver}" ] || version_ge "${ver}" "${latest_ver}"; then
            latest_ver="${ver}"
            latest_war="${war}"
        fi
    done

    # Fallback to unversioned WAR
    if [ -z "${latest_war}" ] && [ -f "${BASE}/${WAR_PREFIX}.war" ]; then
        latest_war="${BASE}/${WAR_PREFIX}.war"
        latest_ver="(unversioned)"
    fi

    if [ -z "${latest_war}" ]; then
        echo "ERROR: No WAR files found matching ${WAR_PREFIX}-*.war in ${BASE}" >&2
        echo "" >&2
        echo "  Expected files like:" >&2
        echo "    ${WAR_PREFIX}-26.0.war" >&2
        echo "    ${WAR_PREFIX}-20.2.5.war" >&2
        echo "    ${WAR_PREFIX}.war" >&2
        return 1
    fi

    WAR_FILE="${latest_war}"
    WAR_VERSION="${latest_ver}"
    return 0
}

# Resolve a specific version to a WAR file
find_war_by_version() {
    target_ver="$1"
    target_war="${BASE}/${WAR_PREFIX}-${target_ver}.war"

    if [ -f "${target_war}" ]; then
        WAR_FILE="${target_war}"
        WAR_VERSION="${target_ver}"
        return 0
    fi

    echo "ERROR: WAR file not found: ${target_war}" >&2
    echo "" >&2
    echo "  Available versions:" >&2
    list_wars | sed 's/^/    /' >&2
    return 1
}

# ── Dependency Checks ────────────────────────────────────────────────────

detect_distro() {
    if [ -f /etc/os-release ]; then
        # shellcheck disable=SC1091
        . /etc/os-release
        echo "${ID:-unknown}"
    elif [ -f /etc/redhat-release ]; then
        echo "rhel"
    elif [ -f /etc/debian_version ]; then
        echo "debian"
    else
        echo "unknown"
    fi
}

check_java() {
    java_bin="${JAVA_HOME}/bin/java"

    if [ ! -x "${java_bin}" ]; then
        if command -v java > /dev/null 2>&1; then
            java_bin="java"
            echo "WARN: JAVA_HOME (${JAVA_HOME}) not found, using system java."
        else
            distro=$(detect_distro)
            echo "ERROR: Java not found."
            echo ""
            echo "  JAVA_HOME is set to: ${JAVA_HOME}"
            echo "  Neither ${java_bin} nor 'java' on PATH exist."
            echo ""
            echo "  Install a JDK 21+ (recommended) for your distribution:"
            echo ""
            case "${distro}" in
                arch|manjaro)
                    echo "    sudo pacman -S jdk21-openjdk" ;;
                ubuntu|debian|linuxmint|pop)
                    echo "    sudo apt update && sudo apt install openjdk-21-jdk" ;;
                fedora)
                    echo "    sudo dnf install java-21-openjdk-devel" ;;
                rhel|centos|rocky|alma|ol)
                    echo "    sudo yum install java-21-openjdk-devel" ;;
                opensuse*|sles)
                    echo "    sudo zypper install java-21-openjdk-devel" ;;
                alpine)
                    echo "    apk add openjdk21" ;;
                *)
                    echo "    Visit https://adoptium.net/ to download Eclipse Temurin JDK 21+" ;;
            esac
            echo ""
            exit 1
        fi
    fi

    java_version_output=$("${java_bin}" -version 2>&1 | head -1)
    java_version=$(echo "${java_version_output}" | sed -n 's/.*version "\([0-9]*\).*/\1/p')

    if [ -z "${java_version}" ]; then
        echo "WARN: Could not determine Java version from: ${java_version_output}"
    elif [ "${java_version}" -lt "${MIN_JAVA_VERSION}" ] 2>/dev/null; then
        echo "ERROR: Java ${java_version} detected, but JDK ${MIN_JAVA_VERSION}+ is required."
        echo "  Detected: ${java_version_output}"
        exit 1
    else
        echo "Java ${java_version} detected."
    fi
}

check_xvfb() {
    # Webswing 26.4+ runs truly headless on JDK 21+ via java.desktop patches
    # (modpatch-java_desktop). No X server, no Xvfb, no DISPLAY required.
    # This function is kept as a no-op for backwards compatibility with any
    # downstream tooling that calls it directly.
    return 0
}

import_ssl_certs() {
    if [ -z "${SSL_CERTS}" ]; then
        return 0
    fi

    if ! command -v openssl > /dev/null 2>&1; then
        echo "WARN: openssl not found, skipping SSL certificate import."
        return 0
    fi

    keytool_bin="${JAVA_HOME}/bin/keytool"
    if [ ! -x "${keytool_bin}" ]; then
        echo "WARN: keytool not found at ${keytool_bin}, skipping SSL certificate import."
        return 0
    fi

    tmp_cert_dir=$(mktemp -d)
    trap_cleanup() { rm -rf "${tmp_cert_dir}"; }
    trap trap_cleanup EXIT

    echo "${SSL_CERTS}" | while IFS='|' read -r alias host port; do
        # Skip empty lines
        [ -z "${alias}" ] && continue

        echo "  Importing SSL cert: ${alias} (${host}:${port})"

        cert_bundle="${tmp_cert_dir}/${alias}-bundle.pem"

        # Download the full certificate chain
        if ! openssl s_client -connect "${host}:${port}" -showcerts </dev/null 2>/dev/null \
            | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > "${cert_bundle}" \
            || [ ! -s "${cert_bundle}" ]; then
            echo "    WARN: Failed to retrieve certificate from ${host}:${port}, skipping."
            continue
        fi

        # Split the chain into individual certs and import each one
        cert_index=0
        current_cert=""
        while IFS= read -r line; do
            case "${line}" in
                "-----BEGIN CERTIFICATE-----")
                    current_cert="${line}"
                    ;;
                "-----END CERTIFICATE-----")
                    current_cert="${current_cert}
${line}"
                    cert_file="${tmp_cert_dir}/${alias}-${cert_index}.pem"
                    echo "${current_cert}" > "${cert_file}"

                    cert_alias="${alias}-${cert_index}"

                    # Remove old entry (ignore errors if it doesn't exist)
                    "${keytool_bin}" -delete \
                        -keystore "${SSL_KEYSTORE}" \
                        -storepass "${SSL_KEYSTORE_PASS}" \
                        -alias "${cert_alias}" 2>/dev/null || true

                    # Import the certificate
                    if "${keytool_bin}" -import -noprompt -trustcacerts \
                        -alias "${cert_alias}" \
                        -file "${cert_file}" \
                        -keystore "${SSL_KEYSTORE}" \
                        -storepass "${SSL_KEYSTORE_PASS}" 2>/dev/null; then
                        echo "    Imported: ${cert_alias}"
                    else
                        echo "    WARN: Failed to import ${cert_alias}"
                    fi

                    cert_index=$((cert_index + 1))
                    current_cert=""
                    ;;
                *)
                    if [ -n "${current_cert}" ]; then
                        current_cert="${current_cert}
${line}"
                    fi
                    ;;
            esac
        done < "${cert_bundle}"

        echo "    Imported ${cert_index} certificate(s) for ${alias}."
    done

    rm -rf "${tmp_cert_dir}"
}

# ── Helpers ──────────────────────────────────────────────────────────────

log_msg() {
    echo "$(date '+%Y-%m-%d %X'): $1" >> "${LOG_FILE}"
}

is_running() {
    [ -f "${PID_FILE}" ] && kill -0 "$(cat "${PID_FILE}")" 2>/dev/null
}

ensure_xvfb() {
    # Webswing 26.4+ does not require X11 or Xvfb. Unset DISPLAY explicitly to
    # prevent any inherited value from accidentally triggering X11 code paths
    # in third-party libraries the child Swing app might pull in.
    unset DISPLAY
    unset XAUTHORITY
}

rotate_logs() {
    find "${LOG_FILE}"* -type f -mtime "+${LOG_RETENTION_DAYS}" -delete 2>/dev/null || true
    if [ -f "${LOG_FILE}" ]; then
        cp "${LOG_FILE}" "${LOG_FILE}_$(date '+%Y%m%d%H%M%S')"
        : > "${LOG_FILE}"
    fi
}

kill_tail() {
    if [ -f "${TAIL_PID_FILE}" ]; then
        kill "$(cat "${TAIL_PID_FILE}")" 2>/dev/null || true
        rm -f "${TAIL_PID_FILE}"
    fi
    pkill -f "tail -f ${LOG_FILE}" 2>/dev/null || true
}

# ── Commands ─────────────────────────────────────────────────────────────

cmd_list() {
    echo ""
    echo "==== Available ${APP} versions in ${BASE}"
    echo ""
    if ! list_wars; then
        echo "  (none found)"
    fi

    # Show which is latest
    if find_latest_war 2>/dev/null; then
        echo ""
        echo "  Latest: ${WAR_VERSION} -> $(basename "${WAR_FILE}")"
    fi
    echo ""
}

cmd_status() {
    echo ""
    echo "==== Status of ${APP}"

    if is_running; then
        pid=$(cat "${PID_FILE}")
        echo ""
        echo "${APP} is running with PID [${pid}]"
        ps -fp "${pid}" 2>/dev/null || true
    elif [ -f "${PID_FILE}" ]; then
        echo ""
        echo "${APP} is NOT running, but stale PID file found"
        rm -f "${PID_FILE}"
    else
        echo ""
        echo "${APP} is NOT running"
    fi
}

cmd_start() {
    if is_running; then
        echo "${APP} already running with PID [$(cat "${PID_FILE}")]"
        return 0
    fi

    check_java
    import_ssl_certs

    # Resolve WAR file
    if [ -n "${REQUESTED_VERSION}" ]; then
        find_war_by_version "${REQUESTED_VERSION}" || exit 1
    else
        find_latest_war || exit 1
    fi

    echo "==== Starting ${APP} v${WAR_VERSION}"
    echo "  WAR: $(basename "${WAR_FILE}")"
    mkdir -p "${BASE}/logs"

    rotate_logs
    ensure_xvfb

    cmd="${JAVA_HOME}/bin/java ${JAVA_OPTS} ${LOGGING} -jar ${WAR_FILE} ${WEBSWING_OPTS}"

    if nohup nice -n"${NICE_LEVEL}" sh -c \
        "${cmd}; sleep 1; echo \"\$(date '+%Y-%m-%d %X'): ${APP} STOPPED\" >> '${LOG_FILE}'; rm -f '${PID_FILE}'" \
        >> "${LOG_FILE}" 2>&1 &
    then
        sleep 1
        pgrep -P $! > "${PID_FILE}" 2>/dev/null || echo $! > "${PID_FILE}"
        echo "Started with PID [$(cat "${PID_FILE}")]"
        log_msg "${APP} v${WAR_VERSION} STARTED ($(basename "${WAR_FILE}"))"

        kill_tail
        tail -f "${LOG_FILE}" &
        echo $! > "${TAIL_PID_FILE}"
    else
        echo "Error starting ${APP}"
        rm -f "${PID_FILE}"
        return 1
    fi
}

cmd_stop() {
    echo "==== Stopping ${APP}"

    if [ ! -f "${PID_FILE}" ]; then
        echo "No PID file found -- ${APP} already stopped?"
        return 0
    fi

    pid=$(cat "${PID_FILE}")

    if kill "${pid}" 2>/dev/null; then
        echo "Sent SIGTERM to PID [${pid}]"
        count=0
        while kill -0 "${pid}" 2>/dev/null && [ "${count}" -lt 30 ]; do
            sleep 1
            count=$((count + 1))
        done
        if kill -0 "${pid}" 2>/dev/null; then
            echo "Process did not stop gracefully -- sending SIGKILL"
            kill -9 "${pid}" 2>/dev/null || true
        fi
        log_msg "${APP} STOPPED"
    else
        echo "Process [${pid}] not running"
    fi

    rm -f "${PID_FILE}"
    kill_tail
}

cmd_restart() {
    cmd_stop
    echo "Sleeping..."
    sleep 3
    cmd_start
}

# ── Main ─────────────────────────────────────────────────────────────────

REQUESTED_VERSION=""
action=""

while [ $# -gt 0 ]; do
    case "$1" in
        -h|--help)
            echo "${USAGE}"
            exit 0
            ;;
        -v|--version)
            if [ $# -lt 2 ]; then
                echo "ERROR: -v requires a version argument"
                exit 1
            fi
            REQUESTED_VERSION="$2"
            shift
            ;;
        start|stop|restart|status|list)
            action="$1"
            ;;
        *)
            echo "Unknown argument: $1"
            echo "${USAGE}"
            exit 1
            ;;
    esac
    shift
done

if [ -z "${action}" ]; then
    echo "No action specified."
    echo "${USAGE}"
    exit 1
fi

cd "${BASE}"

case "${action}" in
    list)    cmd_list    ;;
    status)  cmd_status  ;;
    start)   cmd_start   ;;
    stop)    cmd_stop    ;;
    restart) cmd_restart ;;
esac