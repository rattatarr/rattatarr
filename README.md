# Rattatarr

Rattatarr lets you track, rate, and analyze your media consumption locally.
Built for homelab setups, with a simple UI.

⚠️ Status: Actively under development. Expect breaking changes.

## What this does

- Syncs media from Jellyfin
- Automatically tracks activity from Jellyfin (planned)
- Lets local profiles rate items (0.5 to 10)
- Insights on your watch/rating activity
- New movie recommendations from multiple sources (planned)
- Stores data in SQLite
- Serves single web UI + API on one port

## Quick start (Docker Compose)

Create `docker-compose.yml`:

```yaml
services:
  rattatarr:
    image: ghcr.io/rattatarr/rattatarr:latest
    container_name: rattatarr
    restart: unless-stopped
    ports:
      - "80:80"
    volumes:
      - ./data:/data
    environment:
      # optional, can be set later in app
      RATTATARR_JELLYFIN_BASE_URL: "http://jellyfin:8096"
      # optional, can be set later in app
      RATTATARR_JELLYFIN_API_KEY: "YOUR_JELLYFIN_API_KEY"
      # depends on your setup, this might be required
      RATTATARR_CORS_ALLOWED_ORIGINS: "http://your-hostname"
```

Start:

```bash
docker compose up -d
```

Open:

- App: `http://<your-host>:80`
- API docs: `http://<your-host>:80/docs`

## Portainer stack

Use same Compose content above in Portainer:

1. Stacks -> Add stack
2. Paste Compose YAML
3. Set Jellyfin values
4. Deploy

## Environment variables

- `RATTATARR_JELLYFIN_BASE_URL` (required): Jellyfin URL, e.g. `http://192.168.1.10:8096`
- `RATTATARR_JELLYFIN_API_KEY` (required): Jellyfin API key
- `RATTATARR_CORS_ALLOWED_ORIGINS` (optional): CSV origins for direct API access

## Persistent data

- Mount `/data` to keep `rattatarr.db`
- Example host path: `./data:/data` or `/opt/rattatarr/data:/data`

## Notes for arr-stack homelab setups

- Put Rattatarr and Jellyfin on same Docker network
- Use service DNS for base URL (example: `http://jellyfin:8096`)
- Keep container restart policy as `unless-stopped`
