# Report Generation State Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make AI interview report generation observable, retryable, and idempotent by introducing the `EVALUATING` session state.

**Architecture:** `AiInterviewSessionService.finish()` owns the state transition from `RUNNING` to `EVALUATING` before calling Spark, then to `FINISHED` after inserting the report. If Spark or report parsing fails through the existing `callAi` error path, the session returns to `RUNNING` so the user can retry. Existing reports are returned without another AI call.

**Tech Stack:** Spring Boot 3, MyBatis XML mapper, JUnit 5, Mockito, AssertJ, MySQL varchar session status.

## Global Constraints

- Do not commit local `.env` secrets or real API credentials.
- Do not touch security/rate-limit modules in this slice.
- Do not require a schema migration because `interview_session.status` is already `varchar(20)`.
- Preserve existing API response shape through `Result`.
- Keep `finish()` idempotent when a report already exists.

---

### Task 1: Add State-Transition Regression Tests

**Files:**
- Create: `springboot/src/test/java/com/example/service/AiInterviewSessionServiceFinishStateTest.java`

**Interfaces:**
- Consumes: `AiInterviewSessionService.finish(String sessionId)`
- Produces: executable tests for `updateToEvaluating`, `updateToRunningFromEvaluating`, existing-report idempotency, and duplicate generation prevention.

- [x] **Step 1: Write the failing test**

Add tests that verify:
- a `RUNNING` session calls `updateToEvaluating` before Spark and `updateToFinished` after report insert;
- Spark failure restores status through `updateToRunningFromEvaluating`;
- an existing report is returned without another Spark call and enriched with `questionReviews`;
- an `EVALUATING` session without a report returns `409` and does not call Spark.

- [x] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=AiInterviewSessionServiceFinishStateTest test`

Expected: FAIL because `AiInterviewSessionMapper` has no `updateToEvaluating` or `updateToRunningFromEvaluating` methods yet.

### Task 2: Implement Report Generation State Flow

**Files:**
- Modify: `springboot/src/main/java/com/example/mapper/AiInterviewSessionMapper.java`
- Modify: `springboot/src/main/resources/mapper/AiInterviewSessionMapper.xml`
- Modify: `springboot/src/main/java/com/example/service/AiInterviewSessionService.java`

**Interfaces:**
- Produces: `int updateToEvaluating(String id, Integer version)`
- Produces: `int updateToRunningFromEvaluating(String id)`
- Updates: `int updateToFinished(String id)` accepts `RUNNING` and `EVALUATING`

- [x] **Step 1: Add mapper methods**

Add `updateToEvaluating` with optimistic `version` checking, and `updateToRunningFromEvaluating` for retry recovery.

- [x] **Step 2: Update `finish()`**

Use `STATUS_EVALUATING`; return `enrichReport(existingReport)` when a report already exists; reject reportless `EVALUATING` with `409`; move `RUNNING` to `EVALUATING` before Spark; restore `RUNNING` when `RuntimeException` is raised during report generation.

- [x] **Step 3: Run focused tests**

Run: `mvn -q -Dtest=AiInterviewSessionServiceFinishStateTest test`

Expected: PASS.

### Task 3: Verify The Slice

**Files:**
- No production file changes beyond Task 2.

**Interfaces:**
- Consumes: Maven and Vite verification commands.
- Produces: green backend tests and frontend production build.

- [x] **Step 1: Run backend tests**

Run: `mvn -q test`

Expected: PASS. The failure-recovery test intentionally logs a mocked Spark timeout warning.

- [x] **Step 2: Run frontend build**

Run: `npm run build`

Expected: PASS. Existing Sass legacy API and chunk-size warnings may appear.

- [ ] **Step 3: Commit**

```bash
git add docs/superpowers/plans/2026-07-21-report-generation-state.md springboot/src/main/java/com/example/mapper/AiInterviewSessionMapper.java springboot/src/main/java/com/example/service/AiInterviewSessionService.java springboot/src/main/resources/mapper/AiInterviewSessionMapper.xml springboot/src/test/java/com/example/service/AiInterviewSessionServiceFinishStateTest.java
git commit -m "feat: stabilize interview report generation"
```
