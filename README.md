# hello-war (Jakarta Servlet WAR for Tomcat)

A tiny, production-style **Jakarta Servlet** web app (no Spring) used to practice **Maven → Artifactory → Tomcat** CI/CD.

- **Language:** Java 17
- **Framework:** None — plain **Jakarta Servlet API (6.x)**
- **Packaging:** `war`
- **Server target:** Tomcat **10.1+** (uses `jakarta.*` packages, not `javax.*`)
- **Coordinates (GAV):** `com.devopsbyte.app:hello-war:1.0.0-SNAPSHOT`

---

## What it does

Simple endpoints for Phase‑1 CI/CD:
- `GET /hello` — Returns a greeting. Query param `name` is optional. Uses env var **`APP_GREETING`** as an optional prefix.
- `GET /health` — Lightweight health check for CD and load balancers. Returns HTTP `200` with JSON: `{"status":"UP", ...}`

Example responses:
- `GET /hello` → `Hello, world!`
- `GET /hello?name=Phoenix` → `Hello, Phoenix!`
- with `APP_GREETING=Namaste` → `Namaste, Phoenix!`
- `GET /health` → `{"status":"UP","app":"hello-war","timestamp":"2025-11-10T21:42:00Z"}`

---

## Project structure

```
src/
  main/
    java/
      com/devopsbyte/app/
        GreetingUtil.java
        web/HelloServlet.java
        web/HealthServlet.java
    webapp/
      index.jsp
  test/
    java/
      com/devopsbyte/app/GreetingUtilTest.java
pom.xml
```

---

## Prerequisites

- **JDK 17** (Temurin/Adoptium recommended)
- **Maven 3.8+**
- **Apache Tomcat 10.1+** (for local run)

> Tomcat 9 (javax) will NOT work; use Tomcat 10.1+ (jakarta).

---

## Build & test locally

```bash
# 1) Clean & run tests
mvn -B -ntp clean test

# 2) Build the WAR
mvn -B -ntp package
# => target/hello-war-1.0.0-SNAPSHOT.war

# 3) (Optional) View coverage report locally
# open target/site/jacoco/index.html
```

---

## Run on local Tomcat

1. **Stop Tomcat** if running.
2. Copy the WAR to Tomcat’s `webapps/`:
   ```bash
   cp target/hello-war-1.0.0-SNAPSHOT.war /path/to/tomcat/webapps/
   ```
3. **(Optional)** set a custom greeting:
   - Linux (setenv.sh):
     ```bash
     # in $CATALINA_BASE/bin/setenv.sh
     export APP_GREETING="Namaste"
     ```
   - Or systemd unit:
     ```ini
     Environment=APP_GREETING=Namaste
     ```
4. **Start Tomcat** and wait for the app context to deploy.

### URLs

Tomcat uses the WAR filename as context by default:
```
http://localhost:8080/hello-war-1.0.0-SNAPSHOT/
http://localhost:8080/hello-war-1.0.0-SNAPSHOT/hello
http://localhost:8080/hello-war-1.0.0-SNAPSHOT/health
```

- Want root context? Rename the WAR to `ROOT.war` before copying:
  ```bash
  cp target/hello-war-1.0.0-SNAPSHOT.war /path/to/tomcat/webapps/ROOT.war
  # then:
  # http://localhost:8080/
  # http://localhost:8080/hello
  # http://localhost:8080/health
  ```

### Quick curl checks

```bash
curl -s http://localhost:8080/hello-war-1.0.0-SNAPSHOT/hello
curl -s "http://localhost:8080/hello-war-1.0.0-SNAPSHOT/hello?name=Phoenix"
curl -s http://localhost:8080/hello-war-1.0.0-SNAPSHOT/health
```

---

## Maven coordinates / Artifactory path

- **Group:** `com.devopsbyte.app`
- **Artifact:** `hello-war`
- **Version:** `1.0.0-SNAPSHOT`

After `mvn deploy`, expect in Artifactory:
```
maven-releases-local/
  com/devopsbyte/app/hello-war/1.0.0-SNAPSHOT/
    hello-war-1.0.0-SNAPSHOT.war
    hello-war-1.0.0-SNAPSHOT.pom
```

---

## Common tasks

```bash
# Run tests
mvn -B -ntp test

# Package only
mvn -B -ntp clean package

# Deploy to the repo configured in <distributionManagement>
mvn -B -ntp -DskipTests deploy -s ~/.m2/settings.xml
```

---

## Configuration

- **APP_GREETING** (env var): optional greeting prefix for `/hello`.

---

## Why this project?

Minimal moving parts to keep focus on **CI/CD**:
- Build, test, and **publish** a WAR (Maven → Artifactory).
- Deploy to Tomcat (manual in Phase‑1; automated later).
- Health endpoint `/health` ready for CD checks and load balancers.
- Cleanly extensible for future phases (DB, JAX‑RS/REST, Spring if needed).

---

## Future‑proofing suggestions (already compatible)

- **/health** endpoint added for rollout checks (✓ now).
- Keep **`APP_GREETING`** for externalized config example.
- If you enable external DB, you can later add `/health/db` to verify connectivity (optional).
- Consider adding `/info` to expose version/build info (Phase‑2), without changing your pipeline flow.


---

## Optional external DB logging (Phase‑2 ready)

The app can optionally log `/hello` requests to an external PostgreSQL database
*without* changing any CI/CD or deployment flow.

- If **no DB env vars are set**, the app behaves exactly as a pure in‑memory app.
- If the following env vars are present for the Tomcat process, logging is enabled:

  - `DB_URL` — e.g. `jdbc:postgresql://host:5432/hellowar`
  - `DB_USER` — DB username
  - `DB_PASSWORD` — DB password
  - `APP_ENV` — optional label (e.g. `LOCAL`, `CI`, `DEV`, `STAGE`, `PROD`) stored with each log row

Logging is best‑effort: failures are written to stderr and **never** break the `/hello` response.
