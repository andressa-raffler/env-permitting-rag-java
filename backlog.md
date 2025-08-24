# Environmental Permitting — RAG PoC — Sprint Plan

## EPIC
Build a minimal Retrieval-Augmented Generation (RAG) assistant that answers environmental permitting questions using 30–100 PDFs, always citing sources.

---

### Milestone 0 — Repo & Infra (foundation)
- [X] Create repository (README with objective, scope, and PoC limits).
- [X] Define Maven project (parent, Java version, dependencies list) — no code yet.
- [X] Prepare Docker Compose with Postgres pgvector (service definition complete).
- [X] Prepare Liquibase changelog plan (extension `vector`, tables `document_source` and `document_chunk`, indexes).
- [X] Draft `application.yml` structure (profile `local`), without secrets.
  **DoD:** Local stack starts and migrations apply without errors; DB is reachable.

### Milestone 1 — AI configuration (models & secrets)

- [X] Choose provider and models (chat + embeddings) and embedding dimension (parametrized).
- [X] Define secret management approach (env vars, vault, etc.).
- [X] Plan a smoke test to verify embeddings and latency (no code in this document).
  **DoD:** Embedding vector dimension and latency validated in a local spike; API key does not leak to logs.

### Milestone 2 — Ingestion (PDF → chunks → embeddings → DB)

- [x] Define approach for PDF extraction (page-by-page for PoC; note future improvement for headings).
- [x] Define chunking strategy (size/overlap parameters).
- [x] Plan ingestion flow: read PDFs from `data/docs/`; insert `document_source`; generate embeddings; insert
  `document_chunk` with minimal metadata (`section`, optional `uf`).
- [ ] Define a simple reprocessing strategy (truncate or version).
  **DoD:** Ingesting 3–5 PDFs yields >50 chunks and all indexes are populated successfully.

### Milestone 3 — Retrieval (pgvector)
- [ ] Define DAO/Repository query using `ORDER BY embedding <-> :qvec LIMIT :k` with join on `document_source`.
- [ ] Add optional filter by `uf` (metadata-based).
- [ ] Plan an integration test using Testcontainers to validate similarity ordering.
  **DoD:** Service returns consistent **topK** results in <150ms (DB-only) for a small dataset.

### Milestone 4 — Endpoint `/api/ask` (PoC core)
- [ ] Draft system prompt guidelines: answer only from context; include citations; fallback when source is not found.
- [ ] Define context assembly from topK chunks (title, section, snippet).
- [ ] Specify request/response contract: `{question, uf?}` → `{answer, citations[], usage{latency_ms}}`.
- [ ] Minimal API security: `X-API-Key` header (configurable).
- [ ] Add a usage example reference in the README (no code here).
  **DoD:** Calling the endpoint returns an answer with at least one valid citation from ingested PDFs.

### Milestone 5 — Quick evaluation (offline & lightweight)
- [ ] Create `data/eval.csv` with ~20 real questions (plus expected sources when possible).
- [ ] Plan a parameterized test that loads CSV, calls the service, checks for expected citation presence, and produces a report.
  **DoD:** Evaluation report shows ≥70% pass on the first iteration (PoC target), listing failures for prioritization.

### Milestone 6 — Minimal observability
- [ ] Define structured logging fields per `/api/ask` request (total latency, topK, context size).
- [ ] Enable Actuator endpoints (`health`, `info`).
- [ ] Plan to collect token usage if supported; otherwise, keep a TODO.
  **DoD:** Logs include `latency_ms` and context size; `health` endpoint returns OK.

### Milestone 7 — Docs & demo
- [ ] Expand README with vision, requirements, run steps (compose → ingest → ask), limitations, costs, and next steps.
- [ ] Provide a Postman/HTTP collection reference with sample requests (no code here).
- [ ] Add a risk checklist with mitigations (PDF quality, chunk sizing, latency, accuracy).
  **DoD:** A teammate can follow the docs to run locally and reproduce a question with citation without assistance.
