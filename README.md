# Bedrock Triage Agent

A lightweight **Java 17 / Spring Boot 3** application that classifies a free-text issue into a **Category**, **Severity**, and **Summary** using **Amazon Bedrock** (Anthropic Claude 3 Haiku). 
It ships with a minimal web UI and a simple JSON API.

---
## Quickstart (no AWS required)

This app ships with a **mock** profile so you can run it locally without AWS.

### Run (Windows PowerShell)
```powershell
mvn -q spring-boot:run -Dspring-boot.run.profiles=mock
---
## ✨ Features

- 🧠 AI triage via **Amazon Bedrock** (Claude 3 Haiku)
- 🌐 Web UI at `/` and JSON API at `/api/triage`
- 🔀 Profiles:
  - `bedrock` – real Bedrock call (default for demo)
  - `mock` – offline deterministic stub (no AWS needed)
- 🩺 Health endpoint via Spring Actuator

---

## 🧱 Architecture

- **Spring Boot 3** (Web, Actuator, Thymeleaf)
- **AWS SDK v2**: `bedrockruntime`, `sso`, `ssooidc`, `apache-client`
- **Key classes**
  - `controller/ApiController` – REST endpoints + routes for UI
  - `service/BedrockTriageService` – Bedrock `invokeModel`
  - `service/MockTriageService` – returns stable, canned results (for `mock` profile)
  - `config/BedrockConfig` – builds `BedrockRuntimeClient` (uses profile/region)

The web UI is a single page (`templates/index.html`). Submitting text calls `/api/triage` and renders the structured result plus a “Raw JSON” toggle.

---

## 📦 Prerequisites

- **JDK 17+**
- **Maven 3.9+**
- **AWS CLI v2**
- Access to **Amazon Bedrock** in **us-east-1** (or adjust the region)
- Model: `anthropic.claude-3-haiku-20240307-v1:0`
- An AWS **SSO** profile (default name is `bedrock-sso`)

### Sample `~/.aws/config` (SSO)
```ini
[profile bedrock-sso]
sso_session = my-sso
sso_account_id = 123456789012
sso_role_name = AdministratorAccess
region = us-east-1
output = json

[sso-session my-sso]
sso_start_url = https://d-xxxxxxxx.awsapps.com/start
sso_registration_scopes = sso:account:access
sso_region = us-east-1

## Architecture
```mermaid
flowchart TD
  U[User] --> UI[/Web UI (Thymeleaf)/]
  UI -->|POST /api/triage| C[ApiController]
  C --> S[TriageService]
  S -->|profile=bedrock| B[BedrockRuntimeClient (AWS SDK v2)]
  B -->|invokeModel| M[Claude 3 Haiku via Bedrock]
  S -->|profile=mock| MK[MockTriageService]
  S --> R([Result: Category, Severity, Summary])
  R --> UI
  UI -.-> A[Spring Actuator /actuator/health]:::dim  %% dotted line to show health check

  subgraph Spring Boot App
    C
    S
    MK
    B
  end

  classDef dim fill:#eee,stroke:#888,color:#333
  ```
