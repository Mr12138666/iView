# Per-Question Review Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add per-question interview evaluation so reports can show question-level scores, deductions, and improvement advice.

**Architecture:** Reuse the existing `interview_message.question_id` column and `interview_report.raw_content` JSON storage. The service will associate AI/user message pairs with selected question-bank questions, ask Spark for a structured report containing `questionReviews`, parse it conservatively, and expose it through the existing session detail API. The frontend history drawer will render the structured review when present and gracefully fall back to the existing text report.

**Tech Stack:** Spring Boot 3.3, MyBatis XML, FastJSON, Vue 3, Element Plus, SCSS.

## Global Constraints

- Do not work on security/rate limiting in this slice.
- Do not commit local `.env`, logs, `target`, `dist`, or `node_modules`.
- Use existing `Result` response shape and existing `/api/interview/session/{id}` detail endpoint.
- Keep compatibility with old reports whose `rawContent` has no `questionReviews`.
- Follow TDD for backend behavior changes.

---

### Task 1: Persist Question Context On Interview Messages

**Files:**
- Modify: `springboot/src/main/java/com/example/service/AiInterviewSessionService.java`
- Test: `springboot/src/test/java/com/example/service/AiInterviewSessionServiceQuestionContextTest.java`

**Interfaces:**
- Produces: `saveMessage(String sessionId, int order, String role, String content, String modality, Integer questionId)` stores `question_id`.
- Consumes: existing `AiInterviewMessageMapper.insert`.

- [ ] **Step 1: Write the failing test**

Create `AiInterviewSessionServiceQuestionContextTest` verifying `start()` stores the assistant first question with a selected question id, and `answer()` stores the user answer with the latest assistant question id.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=AiInterviewSessionServiceQuestionContextTest test`

Expected: FAIL because the current service saves messages without binding question ids.

- [ ] **Step 3: Write minimal implementation**

Track selected questions from the question bank in start prompts, bind the first selected `InterviewQuestion.id` to the first assistant message, and infer the current question id from the latest assistant message when saving a user answer.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=AiInterviewSessionServiceQuestionContextTest test`

Expected: PASS.

### Task 2: Parse And Expose Structured Question Reviews

**Files:**
- Modify: `springboot/src/main/java/com/example/entity/AiInterviewReport.java`
- Modify: `springboot/src/main/java/com/example/service/AiInterviewSessionService.java`
- Test: `springboot/src/test/java/com/example/service/AiInterviewSessionServiceReportParseTest.java`

**Interfaces:**
- Produces: `AiInterviewReport.questionReviews` as JSON string for API consumers.
- Consumes: Spark report JSON field `questionReviews`.

- [ ] **Step 1: Write the failing test**

Add a parse-focused test that feeds report JSON containing `questionReviews` and expects `AiInterviewReport.getQuestionReviews()` to contain the same array.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -q -Dtest=AiInterviewSessionServiceReportParseTest test`

Expected: FAIL because `questionReviews` is not represented on `AiInterviewReport`.

- [ ] **Step 3: Write minimal implementation**

Add a non-persistent `questionReviews` field to `AiInterviewReport`, parse it from `rawContent`, and enrich reports returned from `finish()` and `detail()`.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -q -Dtest=AiInterviewSessionServiceReportParseTest test`

Expected: PASS.

### Task 3: Strengthen Report Prompt For Question-Level Feedback

**Files:**
- Modify: `springboot/src/main/java/com/example/service/AiInterviewSessionService.java`
- Test: existing report parse tests and real API smoke test.

**Interfaces:**
- Produces: Spark report prompt requiring `questionReviews[]` with `questionId`, `questionTitle`, `score`, `deductionReason`, `coverage`, and `suggestion`.
- Consumes: existing conversation history and question-bank metadata.

- [ ] **Step 1: Update prompt only after Task 2 is green**

Extend `buildReportMessages` so report JSON includes question-level review fields.

- [ ] **Step 2: Run service tests**

Run: `mvn -q test`

Expected: PASS.

### Task 4: Render Per-Question Review In History Drawer

**Files:**
- Modify: `vue/src/views/front/InterviewHistory.vue`

**Interfaces:**
- Consumes: `detail.report.questionReviews`, a JSON string or parsed array.
- Produces: a readable "逐题复盘" section with score, deduction reason, coverage, and suggestion.

- [ ] **Step 1: Add parser/computed rendering**

Parse `questionReviews` defensively and render a compact section after the report summary.

- [ ] **Step 2: Run frontend build**

Run: `npm run build`

Expected: PASS.

### Task 5: Final Verification And Commit

**Files:**
- All files modified in Tasks 1-4.

**Interfaces:**
- Produces: committed implementation branch.

- [ ] **Step 1: Run full verification**

Run: `mvn -q test` in `springboot` and `npm run build` in `vue`.

- [ ] **Step 2: Run real API smoke test**

Use local backend and remote MySQL to verify `start -> answer -> finish -> detail` returns a report whose `questionReviews` is present when Spark returns structured JSON.

- [ ] **Step 3: Sensitive diff scan**

Search staged changes for the local Spark and MySQL secrets before committing.

- [ ] **Step 4: Commit**

Commit with: `feat: add per-question interview review`
