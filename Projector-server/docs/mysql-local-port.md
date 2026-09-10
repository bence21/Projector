# MySQL local port override (Windows)

## When you need this

If MySQL80 starts then immediately stops, check the error log (e.g.
`C:\ProgramData\MySQL\MySQL Server 8.0\Data\*.err`). A common message is:

```
Can't start server: Bind on TCP/IP port: ... forbidden by its access permissions
```

Often port **3306** falls inside a Windows-excluded range (Hyper-V, WSL2, Docker).

## Fix (per developer machine)

### 1. Pick a free port

Outside excluded ranges on your PC. Example: **3452** (verify with
`netsh interface ipv4 show excludedportrange protocol=tcp`).

### 2. Change MySQL server port

Edit `C:\ProgramData\MySQL\MySQL Server 8.0\my.ini`:

- Under `[client]`: `port=3452`
- Under `[mysqld]`: `port=3452`

Restart the **MySQL80** service.

Or run the helper script as Administrator (from repo root):

```powershell
Copy-Item Projector-server/scripts/set-mysql-port.ps1.example Projector-server/scripts/set-mysql-port.local.ps1
# Edit set-mysql-port.local.ps1 if you need a different port
.\Projector-server\scripts\set-mysql-port.local.ps1
```

`set-mysql-port.local.ps1` is gitignored.

### 3. Point the app at that port

**Option A — local properties file (recommended)**

```bash
cp Projector-server/src/main/resources/application-local.properties.example \
   Projector-server/src/main/resources/application-local.properties
```

Uncomment and set:

```properties
spring.datasource.url=jdbc:mysql://localhost:3452/songbook
```

`application-local.properties` is gitignored; only your machine uses it.

**Option B — environment variable**

```powershell
$env:MYSQL_PORT = "3452"
```

Default in committed `application.properties` remains `3306` for everyone else.

### 4. Verify

```powershell
mysql -u songbook -p -h 127.0.0.1 -P 3452 songbook -e "SELECT 1"
```
