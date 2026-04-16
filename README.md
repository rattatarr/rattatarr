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

---

## AI movie assistant (Ollama)
Each profile can chat with a local AI that knows their ratings and watch history.
No data leaves your machine — it runs entirely on your own hardware via [Ollama](https://ollama.com).

### Using the prompt
Honestly you don't really need to set that up if you don't want, or it is too complicated.
Use the functionality to export the prompt and feed it into ChatGPT/Claude/Gemini or whatever you enjoy, and it is free.
The purpose of the application is to organize the data. 
I will not implement other agents, feel free to open PRs for that if you really care about it.

### Recommended model (AI generated, not fact checked)

**`gemma3:4b`** — best balance of quality and resource use for recommendation tasks on home hardware:

| Model | RAM needed | Why |
|---|---|---|
| `gemma3:4b` | ~4 GB | Best quality-to-resource ratio, strong reasoning, recommended default |
| `llama3.2:3b` | ~2.5 GB | Lighter option if RAM is tight |
| `mistral:7b` | ~6 GB | Higher quality if you have the headroom |

### Setup with Docker Compose (same stack)

Add the Ollama service to your existing `docker-compose.yml`. You can check out [Ollama-Docker](https://github.com/mythrantic/ollama-docker)


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
    networks:
      - rattatarr-net

  ollama:
    image: ollama/ollama:latest
    container_name: ollama
    restart: unless-stopped
    volumes:
      - ./ollama:/root/.ollama
    networks:
      - rattatarr-net
    # If you have an NVIDIA GPU, uncomment these:
    # deploy:
    #   resources:
    #     reservations:
    #       devices:
    #         - driver: nvidia
    #           count: all
    #           capabilities: [gpu]

networks:
  rattatarr-net:
    driver: bridge
```


```bash
docker exec ollama ollama pull gemma3:4b
```

### Configure in Rattatarr

Open the app settings and fill in:

| Setting | Value |
|---|---|
| **Ollama base URL** | `http://ollama:11434` (Docker) or `http://localhost:11434` (local) |
| **Ollama API key** | Leave blank unless you secured Ollama with a token |
| **Ollama model** | `gemma3:4b` |

### Using the assistant

The AI assistant is per-profile and uses each user's ratings and watch history as context. To start:

1. Select a profile
2. Navigate to the assistant tab
3. Pick a suggested question or type your own
4. The conversation is remembered across sessions — use the clear button to start fresh

### Standalone Ollama (no Docker Compose integration)

If Ollama is already running on your host or another machine:

```bash
# Pull model
ollama pull gemma3:4b

# Verify it's running
curl http://localhost:11434/api/tags
```

Then set the base URL in Rattatarr settings to `http://<ollama-host>:11434`.
