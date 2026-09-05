# ─── Stage 1: Build the Vue SPA ───────────────────────────────────────────────
FROM node:24-alpine AS client-build

WORKDIR /app/client

# Enable corepack; pnpm version is pinned via package.json "packageManager"
RUN corepack enable

# Install dependencies (leverage layer cache)
# pnpm-workspace.yaml carries the allowBuilds policy; without it pnpm 11 fails with
# ERR_PNPM_IGNORED_BUILDS on esbuild / vue-demi.
COPY client/package.json client/pnpm-lock.yaml client/pnpm-workspace.yaml ./
RUN corepack prepare --activate && pnpm install --frozen-lockfile

# Copy source and build
COPY client/ ./
RUN pnpm build


# ─── Stage 2: Build the Spring Boot JAR ───────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS server-build

WORKDIR /app/server

# Copy Maven wrapper and pom first (layer cache for dependencies)
COPY server/.mvn/ .mvn/
COPY server/mvnw server/pom.xml ./
RUN ./mvnw dependency:go-offline -q

# Copy source and build (skip tests — run them in CI, not here)
COPY server/src/ src/
RUN ./mvnw package -q -DskipTests


# ─── Stage 3: Final image ─────────────────────────────────────────────────────
FROM caddy:2-alpine

# Install JRE (headless) to run the Spring Boot jar
RUN apk add --no-cache openjdk21-jre-headless

# Data directory for SQLite database (mount a volume here)
RUN mkdir -p /data
WORKDIR /app

# Copy built artefacts
COPY --from=server-build /app/server/target/rattatarr-*.jar app.jar
COPY --from=client-build /app/client/dist/ /srv/

# Caddyfile
COPY Caddyfile /etc/caddy/Caddyfile

# Entrypoint script
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Single port exposed — Caddy handles everything
EXPOSE 80

ENTRYPOINT ["/entrypoint.sh"]
