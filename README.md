<div align="center">

# WebSwing Lite 26.0

### Enterprise Java Swing Applications — Delivered Through Your Browser

[![Build](https://github.com/manticore-projects/webswing/actions/workflows/Gradle.yml/badge.svg)](https://github.com/manticore-projects/webswing/actions/workflows/Gradle.yml)
[![Version](https://img.shields.io/badge/Version-26.0-orange?style=for-the-badge)](https://github.com/manticore-projects/webswing/releases)
[![JDK 13+](https://img.shields.io/badge/JDK-13%2B%20%7C%2021%20%7C%2025%20%7C%2026-blue?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Node.js 24](https://img.shields.io/badge/Node.js-24%20LTS-339933?style=for-the-badge&logo=nodedotjs&logoColor=white)](https://nodejs.org/)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-red?style=for-the-badge)](https://www.gnu.org/licenses/agpl-3.0)
[![Build](https://img.shields.io/badge/Build-Gradle%20%7C%20Maven-02303A?style=for-the-badge&logo=gradle&logoColor=white)](https://gradle.org/)
[![GitHub](https://img.shields.io/github/stars/manticore-projects/webswing?style=for-the-badge&logo=github)](https://github.com/manticore-projects/webswing)

<br/>

*Run any Java Swing application inside a modern web browser — pure HTML5, zero plugins, zero client-side installation.*

<br/>

[Getting Started](#-getting-started) · [Build](#-build-instructions) · [What's New](#-whats-new) · [Architecture](#-architecture) 

---

</div>

## Overview

**WebSwing Lite** is a modernized, open-source edition of [WebSwing](https://www.webswing.org) — the web server that renders Java Swing applications in HTML5 Canvas and delivers them to any browser via WebSocket.

This edition is based on the **last open-source release (v20.2.5)** of WebSwing, updated and maintained by [Manticore Projects](https://manticore-projects.com) with a focus on **modern JDK compatibility**, **security**, and **build toolchain modernization**.

> **Looking for the full-featured commercial edition?**
> WebSwing Lite does not include advanced capabilities such as cluster session pooling, load balancing, recording/playback, advanced admin console, and commercial support.
> For production deployments at scale, we encourage you to explore the [**Commercial Edition at webswing.org**](https://www.webswing.org) →

---

## ✦ What's New

### JDK Compatibility

| JDK | Status | Notes |
|:---:|:------:|:------|
| 13–17 | 🔶 Should work | Untested — community feedback welcome |
| 21 (LTS) | ✅ Fully supported | Primary target |
| 23 | ✅ Supported | Short-term release |
| 25 (LTS) | ✅ Supported | Current LTS |
| 26 | ✅ Supported | Requires `--sun-misc-unsafe-memory-access=allow` |

All internal APIs adapted for the post-JDK-11 module system — no `--illegal-access=permit`, no `-noverify` required.

### Build & Runtime Modernization

- **Node.js 24 LTS** — migrated from Node 10; Webpack 5, TypeScript 5, Dart Sass
- **Gradle build system** — fast, incremental builds alongside the existing Maven build (Maven will be phased out in a future release)
- **All dependencies updated** — Jackson, Guava, Log4j2, SLF4J 2.0, Bouncy Castle, Apache Commons, Protocol Buffers, and more
- **Modernized start/stop script** — `bash` strict mode, graceful shutdown with timeout, Xvfb lifecycle management, clean log rotation

### Performance

- **SSE/AVX-optimized PNG encoding** via [fpng-java](https://manticore-projects.com/FPNG-Java/index.html) — hardware-accelerated image compression in the DirectDraw rendering pipeline
- **Browser-side font rendering** — text is rendered as font names + coordinates instead of server-side glyph bitmaps, reducing WebSocket bandwidth by up to 80%

### Security

- **Apache Shiro integration** — pluggable authentication and authorization supporting Active Directory / LDAP, JDBC realms, and custom providers
- **TLS/SSL** — full HTTPS and WSS support with configurable keystores and truststores

### Roadmap

- Migration to **Jetty 12** (EE8 environment for `javax.servlet` compatibility)
- Migration to **Apache HttpClient 5** replacing the legacy 4.x client
- `PlatformGraphicsInfo` module patch integrated into the build system (replacing the runtime `Unsafe` workaround)

---

## 🚀 Getting Started

### Prerequisites

| Component | Version |
|-----------|---------|
| JDK | 13 or later (21+ recommended; [Eclipse Temurin](https://adoptium.net/)) |
| Xvfb | Required on headless Linux servers |

### Quick Start

```bash
# Download the latest release
curl -LO https://github.com/manticore-projects/webswing/releases/latest/download/webswing-26.0-distribution.zip
unzip webswing-26.0-distribution.zip
cd webswing-26.0

# Start the server
./run.sh start

# Open in your browser
open http://localhost:8080
```

### Management

```bash
./run.sh start      # Start the server (background, with log tailing)
./run.sh stop       # Graceful shutdown (30s timeout, then SIGKILL)
./run.sh restart    # Stop + Start
./run.sh status     # Check if the server is running
```

---

## 🔨 Build Instructions

### Prerequisites

| Tool   | Version |
|--------|---------|
| JDK    | 21+     |
| Gradle | 8.12    |
| Git    | 2.x     |

> Node.js and npm are **automatically downloaded** during the build — no manual installation needed.

### Build with Gradle

```bash
# Clone the repository
git clone https://github.com/manticore-projects/webswing.git
cd webswing

# Full build
./gradlew clean build

# Build specific modules
./gradlew :webswing-directdraw:webswing-directdraw-javascript:build
./gradlew :webswing-server:webswing-server-frontend:build
```

### Deploy

```bash
# Extract to your deployment directory
unzip webswing-assembly/dist/webswing-26.0-distribution.zip -d /opt/webswing

# Configure your Swing application in webswing.config
vim /opt/webswing/webswing.config

# Start
cd /opt/webswing && ./run.sh start
```

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Browser                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  HTML5 Canvas  ◄──── WebSocket ────►  Event Capture   │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────┬───────────────────────────┘
                                  │
                    ┌─────────────▼──────────-───┐
                    │     WebSwing Server        │
                    │    (Jetty 9 / Servlet)     │
                    │                            │
                    │  ┌──────────────────────┐  │
                    │  │  Session Manager     │  │
                    │  │  Security (Shiro)    │  │
                    │  │  WebSocket Handler   │  │
                    │  └──────────┬───────────┘  │
                    └─────────────┼──────────────┘
                                  │ spawns
                    ┌─────────────▼─────────-────┐
                    │     Child JVM Process      │
                    │                            │
                    │  ┌──────────────────────┐  │
                    │  │  WebToolkit (AWT)    │  │
                    │  │  DirectDraw Pipeline │  │
                    │  │  Font Renderer       │  │
                    │  │  ──────────────────  │  │
                    │  │  Your Swing App      │  │
                    │  └──────────────────────┘  │
                    └────────────────────────────┘
```

**How it works:** The server intercepts Java2D `Graphics2D` paint operations in the child JVM, serializes them via Protocol Buffers, and streams them over WebSocket to the browser. The browser's JavaScript engine deserializes and replays the draw commands on an HTML5 Canvas. User input (mouse, keyboard) flows back over the same WebSocket.

---

## 📦 Dependency Overview

| Component | Version | Purpose |
|-----------|---------|---------|
| Jetty | 9.4.58 | Embedded HTTP/WebSocket server |
| Jackson | 2.19.0 | JSON serialization |
| Protocol Buffers | 3.25.5 | Binary wire format (DirectDraw) |
| Apache Shiro | 1.13.0 | Authentication & authorization |
| Guava | 33.5.0 | Core utilities |
| Log4j 2 | 2.25.3 | Logging framework |
| SLF4J | 2.0.17 | Logging facade |
| Bouncy Castle | 1.80 | Cryptography |
| Webpack | 5.x | JavaScript bundling |
| TypeScript | 5.x | Type-safe frontend code |


---

## Attribution

This project is based on [WebSwing](https://www.webswing.org) v20.2.5, the last open-source release under the AGPL v3 license. All credit for the original WebSwing architecture and implementation goes to the WebSwing team.

If you use WebSwing Lite to deliver your product, **please provide attribution and a link to [webswing.org](https://www.webswing.org).**

## License

This project is licensed under the [GNU Affero General Public License v3.0](https://www.gnu.org/licenses/agpl-3.0.en.html).

---

<div align="center">

**Maintained by [Manticore Projects](https://manticore-projects.com)**

*Building enterprise financial software for banks and insurances since 2014.*

</div>