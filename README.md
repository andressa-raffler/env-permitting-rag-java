# env-permitting-rag-java

**Environmental Permitting — Retrieval-Augmented Generation (RAG) PoC**  
Java 21 · Spring Boot · Spring AI · Postgres + pgvector · Maven

This PoC answers questions about environmental permitting by retrieving the most relevant snippets from your documents (RAG) and generating answers **with citations**.

---

## 1) Overview

- **Goal:** Fast, local, explainable Q&A over 30–100 PDFs.
- **Core:** Spring Boot API (`/api/ask`) + Spring AI (chat & embeddings) + Postgres/pgvector (vector search).
- **Output:** JSON with `answer`, `citations[]`, `usage.latency_ms`.

> Sprint plan & acceptance criteria live in **[ENV_PERMITTING_RAG_PoC_PLAN.md](ENV_PERMITTING_RAG_PoC_PLAN.md)**.

---

## 2) Prerequisites

- Java **21**
- Docker & Docker Compose
- Maven Wrapper (`./mvnw`)
- An LLM API key (e.g., `OPENAI_API_KEY`)

---

## 3) Run locally (quick start)

1) **Start Postgres + pgvector**
```bash
docker compose up -d
```

2) **Index your PDFs** (put files under `data/docs/`; this folder is git-ignored)
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=ingest
```

3) **Start the API**
```bash
./mvnw spring-boot:run
```

4) **Ask a question**
```bash
curl -H "Content-Type: application/json"      -H "X-API-Key: dev-key"      -d '{"question":"What documents are required for a gas station operating permit in SP?"}'      http://localhost:8080/api/ask
```

---

## 4) Configuration (minimal)

Create `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/licgpt
    username: licgpt
    password: licgpt
  liquibase:
    change-log: classpath:/db/changelog/db.changelog-master.yaml

  ai:
    # OpenAI example (swap for Azure/Bedrock/Anthropic if needed)
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          # e.g. gpt-4o-mini (adjust to your account)
          model: gpt-4o-mini
          temperature: 0.2
      embedding:
        options:
          # e.g. text-embedding-3-small (1536 dims)
          model: text-embedding-3-small

poc:
  retriever:
    topK: 6
  chunk:
    sizeTokens: 900
    overlapTokens: 150
  api-key: dev-key   # header: X-API-Key
```

> If you use a corporate proxy/Artifactory and the Spring AI BOM `1.0.1` isn’t synced yet, switch to `1.0.0` in `pom.xml` and run `./mvnw -U clean package`.

---

## 5) API reference

### `POST /api/ask`
**Request**
```json
{
  "question": "What are the required documents for LO?",
  "uf": "SP"        // optional filter if your metadata includes UF/state
}
```

**Response**
```json
{
  "answer": "…",
  "citations": [
    {"title": "Norma LO - Postos", "section": "Art. 12", "uri": "file://data/docs/norma.pdf#p=12"}
  ],
  "usage": {"latency_ms": 1342}
}
```

---

## 6) Project layout (intended)

```
env-permitting-rag-java/
  ├─ src/main/java/...                 # controllers, retrieval, ingestion
  ├─ src/main/resources/
  │   └─ db/changelog/                 # Liquibase changelogs
  ├─ data/
  │   └─ docs/                         # your PDFs (ignored by git)
  ├─ docker-compose.yml                # Postgres + pgvector
  ├─ ENV_PERMITTING_RAG_PoC_PLAN.md    # sprint plan
  └─ pom.xml
```

---

## 7) Troubleshooting

- **Spring AI artifacts not found:** force refresh `./mvnw -U clean package`. If behind a proxy, your repo may not have synced the Spring AI BOM; try `1.0.0`.
- **DB errors on start:** ensure `docker compose up -d` is healthy; check `pg_isready` logs; re-run migrations.
- **High latency:** drop `topK`, shorten chunks, or pick a lighter chat model.

---

## 8) License

Apache 2.0