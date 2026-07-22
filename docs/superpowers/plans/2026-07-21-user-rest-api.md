# User REST API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expose the user account APIs described in the product API draft under `/api/user/*` while preserving legacy `/login`, `/register`, and role-specific update endpoints.

**Architecture:** Add a focused REST controller that delegates to the existing three-role services. Login and registration remain service-driven, `current` reads the existing token utility, `logout` is stateless, and `profile` updates only the authenticated account id derived from the token.

**Tech Stack:** Spring Boot 3 MVC, existing JWT token utility, existing `AdminService` / `EmployService` / `UserService`, JUnit 5, Mockito, standalone MockMvc.

## Global Constraints

- Keep legacy account endpoints working.
- Do not introduce a new security or session store module in this slice.
- Whitelist only `/api/user/login` and `/api/user/register`.
- Never trust `id` or `role` from the profile request body.
- Keep response shape as `Result` with string `code`.

---

### Task 1: Add User REST Contract Tests

**Files:**
- Create: `springboot/src/test/java/com/example/controller/UserRestApiControllerTest.java`
- Create: `springboot/src/test/java/com/example/common/config/WebConfigPublicApiTest.java`

**Interfaces:**
- Consumes: `/api/user/register`, `/api/user/login`, `/api/user/current`, `/api/user/logout`, `/api/user/profile`
- Produces: executable tests for controller delegation, current-user lookup, stateless logout, current-id profile update, and login/register public whitelist.

- [x] **Step 1: Write controller tests**

Verify:
- `POST /api/user/login` delegates by role and returns the token account.
- `POST /api/user/register` delegates to existing register service.
- `GET /api/user/current` returns `TokenUtils.getCurrentUser()`.
- `POST /api/user/logout` returns success.
- `PUT /api/user/profile` ignores body `id` / `role` and updates the authenticated user id.

- [x] **Step 2: Write WebConfig whitelist test**

Verify `/api/user/login` and `/api/user/register` are excluded from the JWT interceptor.

- [x] **Step 3: Verify red**

Run: `mvn -q '-Dtest=UserRestApiControllerTest,WebConfigPublicApiTest' test`

Expected: FAIL because `UserRestApiController` does not exist yet.

### Task 2: Implement User REST Controller

**Files:**
- Create: `springboot/src/main/java/com/example/controller/UserRestApiController.java`
- Modify: `springboot/src/main/java/com/example/common/config/WebConfig.java`

**Interfaces:**
- Produces: REST account endpoints at `/api/user/*`.
- Updates: JWT public paths for `/api/user/login` and `/api/user/register`.

- [x] **Step 1: Add login and register**

Delegate role-based login and register to the existing services.

- [x] **Step 2: Add current and logout**

Return the token current user through `TokenUtils`; return stateless success for logout.

- [x] **Step 3: Add profile update**

Create role-specific update entities using the current token id and role, and copy only profile fields from the request body.

- [x] **Step 4: Verify focused tests**

Run: `mvn -q '-Dtest=UserRestApiControllerTest,WebConfigPublicApiTest' test`

Expected: PASS.

### Task 3: Verify And Commit

**Files:**
- Create: `docs/superpowers/plans/2026-07-21-user-rest-api.md`

**Interfaces:**
- Consumes: Maven and Vite verification commands.
- Produces: committed source, tests, and plan.

- [x] **Step 1: Run backend tests**

Run: `mvn -q test`

Expected: PASS.

- [x] **Step 2: Run frontend build**

Run: `npm run build`

Expected: PASS. Existing Sass legacy API and chunk-size warnings may appear.

- [ ] **Step 3: Commit**

```bash
git add docs/superpowers/plans/2026-07-21-user-rest-api.md springboot/src/main/java/com/example/controller/UserRestApiController.java springboot/src/main/java/com/example/common/config/WebConfig.java springboot/src/test/java/com/example/controller/UserRestApiControllerTest.java springboot/src/test/java/com/example/common/config/WebConfigPublicApiTest.java
git commit -m "feat: expose user rest api aliases"
```
