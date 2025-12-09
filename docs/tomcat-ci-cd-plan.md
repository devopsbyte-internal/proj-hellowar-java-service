
# Tomcat CI/CD Plan (Maven → WAR → EC2)

#	Step	Now (Phase-1: Feet-Wet)	Later (Phase-2/3/4: Full Industry Flow)

1	Environments & branching	
Single `main` → single **prod** EC2/Tomcat.	
>> Later — Add `dev/staging/prod`, protected branches, required reviews, tags for releases.

2	Maven project hygiene	
WAR packaging; clean GAV; unit tests; SonarCloud wired; no DB.	
>> Later — Add integration tests/testcontainers, per-env profiles, dependency mgmt (BOM), SemVer policy, license checks.

3	CI (build/test/quality)	
Trigger: `workflow_dispatch` only. Steps: JDK → `test` → Sonar (informational) → `package` (WAR) → upload artifacts.	
>> Later — Normal triggers (PR/push/tag); parallel stages; quality-gate enforcement; caching; SBOM/SAST; PR annotations.

— Split “build & deploy” later; for Phase‑1, keep CI and CD logically separate even if both are manual. CI should: setup JDK → `mvn -B test` → Sonar (informational) → `mvn -B clean package` → upload WAR as artifact.

4	Artifact repository	
Artifactory Cloud as canonical store; publish versioned WAR (+build metadata).	
>> Later — Add AWS CodeArtifact & S3; promotion flows (dev→prod repos); retention/immutability; provenance & build-info.

5	Infra & Tomcat baseline	
One free-tier EC2; Tomcat installed; run as `tomcat` user; systemd service; minimal SGs (HTTP/SSH).	
>> Later — IaC (Terraform), ALB+ASG, private subnets, SSM Session Manager (no public SSH), hardened AMI/patching; **externalized config** via env/JNDI/property files under `/opt/apps/<app>/config`; secrets via SSM/Secrets Manager.

6	Deployment strategy	
Pull model with `/opt/deploy/deploy.sh`: fetch from Artifactory → drop into `webapps/` → systemd restart. In-place deploy.	
>> Later — Versioned releases + symlink for rollback; blue/green or canary via ALB; also implement push model.

7	CD pipeline	
After CI success, manually dispatch CD: pick artifact version → remote exec (SSM/SSH) to run deploy script. Post-check: Tomcat up & port listening. Record success/fail.	
>> Later — Multi-env targets, approvals, pre-deploy health gates, `/health` endpoint, ALB target health checks, change tickets.

8	Rollback	
— (skip).	
>> Later — Automated rollback to N-1 (symlink flip) or redeploy last good build.

9	Observability	
— (skip; use EC2/Tomcat logs directly).	
>> Later — Central logs (CloudWatch/ELK/Loki), metrics/alerts (5xx, latency, JVM), dashboards, deployment markers.

10	Security & secrets	
Least-privilege CI token; EC2 IAM role (or scoped creds) to pull artifact; remove default Tomcat users; tight SGs.	
>> Later — Full secret mgmt (SSM/Secrets Manager), per-env roles/permissions, CIS hardening, key rotation, signed artifacts, repo protections.

11	Release & governance	
Simple: merge to `main` = deploy; lightweight CHANGELOG.	
>> Later — Tags `vX.Y.Z`, formal release notes, approvals, freeze windows, audit trail, DORA metrics.
