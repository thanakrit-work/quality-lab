# GoRest API tests

Automated tests for the GoRest `/users` API, written in Java with REST Assured and JUnit 5. They cover the full create / read / update / delete flow plus the error and edge cases a basic happy-path suite tends to skip: bad input, missing authentication, duplicate data, and records that don't exist. 19 tests in all.

The brief asked for something "reliable, maintainable, and well-structured," so I kept the framework code as its own small layer instead of pushing everything into the test classes. More on that below.

## Running it

You'll need JDK 17+ and Maven 3.8+ (check with `java -version` and `mvn -version`).

Any write operation needs a GoRest token. Get a free one:

1. Sign in at https://gorest.co.in/ and copy your access token.
2. Put it in a `.env` file at the project root:

```
cp .env.example .env
# then edit .env and set:
# GOREST_API_TOKEN=your_token_here
```

Then run:

```
mvn clean test
```

The token can also come from an environment variable (`export GOREST_API_TOKEN=...`) or the command line (`mvn clean test -DGOREST_API_TOKEN=...`), which is what you'd use in CI. Either way it stays out of the repo — `.env` is git-ignored.

Run it without a token and the read-only tests still run; anything that needs auth is skipped rather than failed, with a message saying why. Add a token for the full run.

A few other commands I use:

```
mvn clean test -Dtest=CreateUserTests     # a single class
mvn clean test -Dgroups=smoke             # just the smoke tests
mvn clean test -Dgroups=negative          # just the negative cases
```

## How it's put together

The framework lives in `src/main/java` and the tests in `src/test/java`. The idea was to make the reusable pieces obvious and keep each test class about the scenario rather than the plumbing.

- **`client`** — `RestClient` builds the base request (URL, JSON headers, auth) and returns either an `anonymous()` or `authenticated()` spec, so a test reads as intent instead of header-wiring. `UserService` sits on top and exposes the actual operations: `createUser`, `getUser`, `updateUser`, and so on. Adding another endpoint later is just another method here.
- **`model`** — `User` and `ApiFieldError`, both plain POJOs. `User` drops null fields when it serialises, so the same object works as a full create body or a partial update body. `ApiFieldError` maps GoRest's 422 error array so the validation checks stay readable.
- **`config`** — one place that resolves the base URL and the token, looking at a system property first, then an environment variable, then `.env`. That ordering is what lets the same suite run locally and in CI without touching the code.
- **`data`** — `TestDataFactory` builds a fresh user for each run. The email is the part that matters: GoRest rejects a duplicate email with a 422, so a hardcoded one would pass once and fail every time after. I generate it from a timestamp plus a chunk of a UUID so runs never clash.
- **`support` / `base`** — the assertion helpers and the shared test lifecycle.

A few choices worth explaining:

**REST Assured + JUnit 5.** REST Assured is the usual pick for API testing in Java and reads almost like plain English. JUnit 5 was the preferred framework in the brief, and its assumptions are what let me skip the auth tests cleanly when no token is configured. Assertions use AssertJ, mostly because the failure messages are easier to read than Hamcrest's.

**What gets checked.** Every test asserts the status code. Past that I check whatever actually matters for the case: the returned data on creates and updates, the `field`/`message` on a 422, the message on a 401 or 404, and the pagination header on the list endpoint. The single-user and list responses also get validated against a JSON schema (under `src/test/resources/schemas`), which catches the kind of structural drift — a field changing type, an enum gaining a value — that a field-by-field check would walk straight past.

**Logging on failure only.** Logging every request just buries the signal, so the suite prints the full request and response only when something fails (`enableLoggingOfRequestAndResponseIfValidationFails`). Green runs stay quiet; a red one gives you everything you need. The assertion helpers also fold the response body into the failure message for the same reason.

**Resilience to a flaky sandbox.** GoRest sits behind Cloudflare and intermittently returns transient gateway/tunnel errors (502/503/504, and Cloudflare's 52x/530). The service layer retries only those transient statuses with a short backoff, so a blip in the shared sandbox doesn't fail the run, while real API responses (2xx/4xx) go straight through to the assertions.

**Cleanup.** Each test keeps track of the users it creates and deletes them when it's done, so the suite doesn't slowly clog the shared GoRest account and the tests don't lean on each other's data.

## What I tested, and why

The brief said the existing suite "validates basic flows but misses important validation and reliability checks," so I spent most of the effort on the failure cases rather than more variations of the happy path.

**Create (6)** — the valid case (201, data echoed back, schema-checked), then the ways it should fail: duplicate email, empty body, malformed email, and a write with no token. Duplicate-email and required-field rules are the sort of thing that breaks quietly in production, so they're worth nailing down.

**Read (5)** — listing users (200, non-empty, pagination header, schema), a specific page, fetching an existing user by id and confirming the data matches, and a 404 for an id that doesn't exist.

**Update (4)** — a valid update, a 404 for a user that isn't there, a 422 for a bad email (validation should apply to updates too, not only creates), and a 401 with no token.

**Delete (3)** — delete and then confirm it's actually gone with a follow-up GET, a 404 for a missing user, and a 401 with no token.

**End to end (1)** — a single test that walks the create → update → delete → confirm-gone chain, to check the operations work together and not just on their own. The update step doubles as proof that the created record exists, which is why there's no separate read-after-create step (see the note below).

Every test has a descriptive name and a `@DisplayName`, so the report reads in plain English.

## If I had more time

- Run it on every push with GitHub Actions, token stored as a secret.
- Add Allure for reports with history and request/response attachments.
- Extend the retry policy to rate-limit (429) responses too, then switch on parallel execution — the unique-data and cleanup design already allows for it.
- Cover a bit more ground: PATCH, pagination boundaries (last page, out-of-range page), and schemas for the error bodies, not just the success ones.
- Fold the validation tests into parameterised tests over a table of bad inputs, which would add coverage without much extra code.

## Notes and assumptions

- The brief lists PUT for updates, so that's what I used; GoRest also accepts PATCH.
- GETs are treated as public — only POST/PUT/DELETE need a token, as the brief states.
- A couple of the read and auth tests take an existing id from the first page of the list, which assumes the shared sandbox has data in it (it normally does). If it were ever empty, those would need to create a record first.
- GoRest is a shared public sandbox, so it occasionally rate-limits or has a brief outage. If a run flakes with a 429 or a connection error, wait a moment and run it again.
- GoRest's free tier can be slow to make a *just-created* user readable through `GET /users/{id}` — writes are immediately usable for update/delete, but the read-back can lag. The get-by-id and lifecycle tests are written not to depend on that: get-by-id reads a pre-existing user, and the lifecycle test confirms the record exists via the update response rather than a read.
