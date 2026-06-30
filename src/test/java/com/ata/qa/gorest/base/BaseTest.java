package com.ata.qa.gorest.base;

import com.ata.qa.gorest.client.RestClient;
import com.ata.qa.gorest.client.UserService;
import com.ata.qa.gorest.config.Configuration;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Common parent for every test class. Responsibilities:
 *
 * <ul>
 *   <li>builds the shared {@link Configuration}, {@link RestClient}, and {@link UserService} once;</li>
 *   <li>enables REST Assured logging of the full request/response <em>only when an assertion fails</em>,
 *       which gives useful diagnostics without flooding the console on green runs;</li>
 *   <li>tracks any users created during a test and deletes them afterwards, so the suite is
 *       self-cleaning and leaves no residue on the shared GoRest account;</li>
 *   <li>exposes {@link #requireAuth()} so authenticated tests are skipped (not failed) when no token
 *       is configured.</li>
 * </ul>
 */
public abstract class BaseTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    protected static Configuration config;
    protected static RestClient restClient;
    protected static UserService userService;

    /** Ids of users created by the current test, removed automatically in teardown. */
    private final List<Integer> createdUserIds = new ArrayList<>();

    @BeforeAll
    static void globalSetup() {
        config = Configuration.getInstance();
        restClient = new RestClient(config);
        userService = new UserService(restClient);

        // Print the whole request/response if (and only if) a validation fails.
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterEach
    void cleanUpCreatedUsers() {
        for (Integer id : createdUserIds) {
            try {
                userService.deleteUser(id);
            } catch (Exception ex) {
                log.warn("Cleanup: could not delete user id={} ({})", id, ex.getMessage());
            }
        }
        createdUserIds.clear();
    }

    /** Register a user id so it is deleted when the test finishes. */
    protected void trackForCleanup(int id) {
        createdUserIds.add(id);
    }

    /**
     * Skips the calling test when no API token is configured. Authenticated operations cannot be
     * exercised without credentials, so skipping (rather than failing) keeps {@code mvn clean test}
     * green for a reviewer who has not yet supplied a token, while a token unlocks the full suite.
     */
    protected void requireAuth() {
        Assumptions.assumeTrue(
                config.hasToken(),
                "GOREST_API_TOKEN is not set - skipping authenticated test. See README 'Setup'.");
    }
}
