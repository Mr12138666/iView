# Management REST API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expose the question bank, question, scoring rule, and bank-question relation APIs using the REST-style paths described in the product API draft.

**Architecture:** Keep the legacy manager UI endpoints unchanged and add a focused REST alias controller that delegates to the existing management services. This avoids breaking `/questionBank/selectPage` callers while making `/api/question-bank/page`, `/api/question/{id}`, and related paths available for enterprise-style clients and documentation alignment.

**Tech Stack:** Spring Boot 3 MVC controllers, existing service layer, JUnit 5, Mockito, standalone MockMvc.

## Global Constraints

- Preserve existing manager frontend paths.
- Do not add database migrations for this slice.
- Do not implement new security modules here; existing service-level admin checks remain in place.
- Keep response shape as `Result` with string `code`.
- Do not commit local secrets or generated dependency/build folders.

---

### Task 1: Add REST Contract Tests

**Files:**
- Create: `springboot/src/test/java/com/example/controller/ManagementRestApiControllerTest.java`

**Interfaces:**
- Consumes: REST paths from `C:\Users\Sunrise_Jay\Documents\demo\docs\05-接口草案.md`
- Produces: controller tests for management REST aliases.

- [x] **Step 1: Write tests for question-bank endpoints**

Cover:
- `POST /api/question-bank`
- `PUT /api/question-bank/{id}`
- `GET /api/question-bank/{id}`
- `GET /api/question-bank/page`
- `DELETE /api/question-bank/{id}`

- [x] **Step 2: Write tests for question endpoints**

Cover:
- `POST /api/question`
- `PUT /api/question/{id}`
- `GET /api/question/{id}`
- `GET /api/question/page`
- `DELETE /api/question/{id}`

- [x] **Step 3: Write tests for scoring-rule and relation endpoints**

Cover:
- `POST /api/scoring-rule`
- `PUT /api/scoring-rule/{id}`
- `GET /api/scoring-rule/{id}`
- `GET /api/scoring-rule/page`
- `DELETE /api/scoring-rule/{id}`
- `POST /api/question-bank/{bankId}/questions`

- [x] **Step 4: Verify red**

Run: `mvn -q -Dtest=ManagementRestApiControllerTest test`

Expected: FAIL with `404` because the REST paths do not exist yet.

### Task 2: Implement REST Alias Controller

**Files:**
- Create: `springboot/src/main/java/com/example/controller/ManagementRestApiController.java`

**Interfaces:**
- Produces: REST management endpoints that delegate to `QuestionBankService`, `InterviewQuestionService`, `QuestionBankQuestionService`, and `ScoringRuleService`.

- [x] **Step 1: Add question-bank endpoints**

Delegate create, update, detail, page, and delete to `QuestionBankService`.

- [x] **Step 2: Add question endpoints**

Delegate create, update, detail, page, and delete to `InterviewQuestionService`.

- [x] **Step 3: Add scoring-rule endpoints**

Delegate create, update, detail, page, and delete to `ScoringRuleService`.

- [x] **Step 4: Add batch bank-question relation endpoint**

For each posted question id, create a `QuestionBankQuestion` relation with the path `bankId` and delegate to `QuestionBankQuestionService.add`.

- [x] **Step 5: Verify green**

Run: `mvn -q -Dtest=ManagementRestApiControllerTest test`

Expected: PASS.

### Task 3: Verify And Commit

**Files:**
- Create: `docs/superpowers/plans/2026-07-21-management-rest-api.md`

**Interfaces:**
- Consumes: Maven and Vite verification commands.
- Produces: committed source and plan changes.

- [x] **Step 1: Run backend tests**

Run: `mvn -q test`

Expected: PASS.

- [x] **Step 2: Run frontend build**

Run: `npm run build`

Expected: PASS. Existing Sass legacy API and chunk-size warnings may appear.

- [ ] **Step 3: Commit**

```bash
git add docs/superpowers/plans/2026-07-21-management-rest-api.md springboot/src/main/java/com/example/controller/ManagementRestApiController.java springboot/src/test/java/com/example/controller/ManagementRestApiControllerTest.java
git commit -m "feat: expose management rest api aliases"
```
