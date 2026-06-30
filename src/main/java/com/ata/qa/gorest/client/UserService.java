package com.ata.qa.gorest.client;

import com.ata.qa.gorest.model.User;
import io.restassured.response.Response;

import java.util.Map;

/**
 * Service layer for the {@code /users} resource. Tests talk to this class in domain terms
 * ("create a user", "get a user") instead of repeating HTTP verbs, paths, and serialization.
 *
 * <p>Each method returns the raw {@link Response} so individual tests stay in full control of
 * what they assert (status, headers, body, schema). Variants ending in {@code WithoutAuth} skip
 * the bearer token and exist specifically to drive the negative authentication scenarios. The
 * {@code Map}-based overloads allow sending deliberately malformed/partial payloads that a typed
 * {@link User} could not express.</p>
 */
public class UserService {

    private final RestClient client;

    public UserService(RestClient client) {
        this.client = client;
    }

    // ---------- Read (public, no auth required) ----------

    public Response listUsers() {
        return client.anonymous().get(Endpoints.USERS);
    }

    public Response listUsers(int page) {
        return client.anonymous().queryParam("page", page).get(Endpoints.USERS);
    }

    public Response getUser(Object id) {
        return client.anonymous().get(Endpoints.USER_BY_ID, id);
    }

    // ---------- Create ----------

    public Response createUser(User user) {
        return client.authenticated().body(user).post(Endpoints.USERS);
    }

    /** Send an arbitrary body (used for missing/invalid-field negative tests). */
    public Response createUser(Map<String, ?> body) {
        return client.authenticated().body(body).post(Endpoints.USERS);
    }

    public Response createUserWithoutAuth(User user) {
        return client.anonymous().body(user).post(Endpoints.USERS);
    }

    // ---------- Update ----------

    public Response updateUser(Object id, User user) {
        return client.authenticated().body(user).put(Endpoints.USER_BY_ID, id);
    }

    public Response updateUser(Object id, Map<String, ?> body) {
        return client.authenticated().body(body).put(Endpoints.USER_BY_ID, id);
    }

    public Response updateUserWithoutAuth(Object id, User user) {
        return client.anonymous().body(user).put(Endpoints.USER_BY_ID, id);
    }

    // ---------- Delete ----------

    public Response deleteUser(Object id) {
        return client.authenticated().delete(Endpoints.USER_BY_ID, id);
    }

    public Response deleteUserWithoutAuth(Object id) {
        return client.anonymous().delete(Endpoints.USER_BY_ID, id);
    }
}
