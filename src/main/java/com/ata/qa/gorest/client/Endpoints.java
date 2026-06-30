package com.ata.qa.gorest.client;

/**
 * Single source of truth for API paths. {@code {id}} is a REST Assured path-parameter
 * placeholder that is substituted positionally at call time.
 */
public final class Endpoints {

    public static final String USERS = "/users";
    public static final String USER_BY_ID = "/users/{id}";

    private Endpoints() {
    }
}
