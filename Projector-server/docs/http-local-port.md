# HTTP local port override (Windows)

## When you need this

If Spring Boot fails with:

```
Web server failed to start. Port 8081 was already in use.
```

but `netstat` / `Get-NetTCPConnection` show nothing listening, the port is often
inside a Windows TCP exclusion range (Hyper-V, WSL2, Docker).

Check:

```powershell
netsh interface ipv4 show excludedportrange protocol=tcp
```

## Fix (per developer machine)

### 1. Pick a free port

Outside excluded ranges on your PC. Example: **8400**.

### 2. Point the app at that port

**Option A — local properties file (recommended)**

```bash
cp Projector-server/src/main/resources/application-local.properties.example \
   Projector-server/src/main/resources/application-local.properties
```

Uncomment and set:

```properties
server.port=8400
app.base-url=http://localhost:${server.port}
```

`application-local.properties` is gitignored; only your machine uses it.
`app.base-url` overrides `baseUrl` from `app.properties` at runtime.

**Option B — environment variable**

```powershell
$env:PORT = "8400"
```

For localhost `baseUrl` in `app.properties`, the `PORT` env var is applied
automatically to links/emails. Prefer Option A if you need a non-localhost base URL.

Default in committed `application.properties` / `app.properties` remains **8081**
for everyone else.

### 3. Point client apps at the same port

The server local override does **not** change desktop / Songbook by itself.
Committed defaults stay **8081**; use gitignored local files on your machine.

**Desktop**

```bash
cp projector-desktop/src/main/resources/credentials-local.properties.example \
   projector-desktop/src/main/resources/credentials-local.properties
```

Uncomment and set:

```properties
port=8400
```

Or set `$env:PORT = "8400"` (used if the local file has no `port` / `domain`).
Rebuild / restart desktop. Committed `Credentials.java` stays on `localhost:8081`.

**Songbook** — `Songbook/local.properties` (gitignored; already used for SDK path):

```properties
# Emulator → host machine
test.api.base.url=http://10.0.2.2:8400
# Physical device → your PC LAN IP
test.api.second.base.url=http://192.168.1.134:8400
```

Rebuild Songbook after changing these so `BuildConfig.API_BASE_URL` picks them up.

### 4. Verify

Restart the server and open http://localhost:8400 (or your chosen port).
Confirm desktop / Songbook call the same host:port.
