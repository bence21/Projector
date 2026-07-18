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

### 3. Verify

Restart the server and open http://localhost:8400 (or your chosen port).
