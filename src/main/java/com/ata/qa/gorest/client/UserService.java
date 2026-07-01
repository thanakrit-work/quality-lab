package com.ata.qa.gorest.client;

import com.ata.qa.gorest.model.User;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Service layer for the {@code /users} resource. Tests talk to this class in domain terms
 * ("create a user", "get a user") instead of repeating HTTP verbs, paths, and serialization.
 *
 * <p>GoRest is a shared public sandbox behind Cloudflare, so it occasionally answers with a
 * transient gateway/tunnel error (502/503/504 or Cloudflare's 52x/530) that has nothing to do
 * with the request. Every call is therefore wrapped in {@link #withRetry}, which retries only
 * those transient upstream statuses with a short backoff and lets every real API response
 * (2xx/4xx) fall straight through to the assertions.</p>
 */
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final int MAX_ATTEMPTS = 4;
    private static final long BACKOFF_MS = 2000L;

    private final RestClient client;

    public UserService(RestClient client) {
        this.client = client;
    }

    // ---------- Read (public, no auth required) ----------

    public Response listUsers() {
        return withRetry(() -> client.anonymous().get(Endpoints.USERS));
    }

    public Response listUsers(int page) {
        return withRetry(() -> client.anonymous().queryParam("page", page).get(Endpoints.USERS));
    }

    public Response getUser(Object id) {
        return withRetry(() -> client.anonymous().get(Endpoints.USER_BY_ID, id));
    }

    // ---------- Create ----------

    public Response createUser(User user) {
        return withRetry(() -> client.authenticated().body(user).post(Endpoints.USERS));
    }

    /** Send an arbitrary body (used for missing/invalid-field negative tests). */
    public Response createUser(Map<String, ?> body) {
        return withRetry(() -> client.authenticated().body(body).post(Endpoints.USERS));
    }

    public Response createUserWithoutAuth(User user) {
        return withRetry(() -> client.anonymous().body(user).post(Endpoints.USERS));
    }

    // ---------- Update ----------

    public Response updateUser(Object id, User user) {
        return withRetry(() -> client.authenticated().body(user).put(Endpoints.USER_BY_ID, id));
    }

    public Response updateUser(Object id, Map<String, ?> body) {
        return withRetry(() -> client.authenticated().body(body).put(Endpoints.USER_BY_ID, id));
    }

    public Response updateUserWithoutAuth(Object id, User user) {
        return withRetry(() -> client.anonymous().body(user).put(Endpoints.USER_BY_ID, id));
    }

    // ---------- Delete ----------

    public Response deleteUser(Object id) {
        return withRetry(() -> client.authenticated().delete(Endpoints.USER_BY_ID, id));
    }

    public Response deleteUserWithoutAuth(Object id) {
        return withRetry(() -> client.anonymous().delete(Endpoints.USER_BY_ID, id));
    }

    // ---------- Retry plumbing ----------

    /**
     * Executes a request and retries only on transient upstream errors (gateway/tunnel), never on
     * a real API status such as 2xx/4xx. This keeps the suite from failing on a brief Cloudflare
     * hiccup in the shared sandbox, while real responses pass straight through to the assertions.
     */
    private Response withRetry(Supplier<Response> call) {
        Response response = call.get();
        for (int attempt = 1;
             attempt < MAX_ATTEMPTS && isTransientUpstreamError(response.statusCode());
             attempt++) {
            long wait = BACKOFF_MS * attempt;
            log.warn("Transient status {} from GoRest - retry {}/{} after {} ms",
                    response.statusCode(), attempt, MAX_ATTEMPTS - 1, wait);
            sleep(wait);
            response = call.get();
        }
        return response;
    }

    /** Standard gateway errors plus Cloudflare's 520-530 family (error 1033 surfaces as 530). */
    private boolean isTransientUpstreamError(int statusCode) {
        return statusCode == 502 || statusCode == 503 || statusCode == 504
                || (statusCode >= 520 && statusCode <= 530);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
