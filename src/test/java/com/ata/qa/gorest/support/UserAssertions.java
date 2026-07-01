package com.ata.qa.gorest.support;

import com.ata.qa.gorest.model.ApiFieldError;
import com.ata.qa.gorest.model.User;
import io.restassured.response.Response;

import java.util.Arrays;
import java.util.List;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Domain-specific assertion helpers. They centralise duplicated checks and, crucially, attach the
 * full response body to failure messages so a red test is diagnosable without a re-run.
 */
public final class UserAssertions {

    private UserAssertions() {
    }

    /** Asserts the HTTP status code, printing the response body on mismatch. */
    public static void assertStatus(Response response, int expected) {
        assertThat(response.statusCode())
                .as("Expected HTTP %d but got %d. Response body: %s",
                        expected, response.statusCode(), response.asString())
                .isEqualTo(expected);
    }

    /** Validates the response body against the single-user JSON schema (contract check). */
    public static void assertMatchesUserSchema(Response response) {
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemas/user-schema.json"));
    }

    /** Validates the response body against the user-list JSON schema. */
    public static void assertMatchesUsersListSchema(Response response) {
        response.then().assertThat().body(matchesJsonSchemaInClasspath("schemas/users-list-schema.json"));
    }

    /** Asserts the response user has the same business fields (name/email/gender/status) as expected. */
    public static void assertUserMatches(Response response, User expected) {
        User actual = response.as(User.class);
        assertThat(actual.getName()).as("name").isEqualTo(expected.getName());
        assertThat(actual.getEmail()).as("email").isEqualTo(expected.getEmail());
        assertThat(actual.getGender()).as("gender").isEqualTo(expected.getGender());
        assertThat(actual.getStatus()).as("status").isEqualTo(expected.getStatus());
    }

    /** Asserts the 422 body contains an error on {@code field} whose message contains {@code messageContains}. */
    public static void assertHasFieldError(Response response, String field, String messageContains) {
        List<ApiFieldError> errors = parseErrors(response);
        assertThat(errors)
                .as("Expected a validation error on field '%s' containing '%s'. Actual errors: %s",
                        field, messageContains, errors)
                .anySatisfy(error -> {
                    assertThat(error.getField()).isEqualTo(field);
                    assertThat(error.getMessage()).containsIgnoringCase(messageContains);
                });
    }

    /** Asserts the 422 body contains at least an error on the given field. */
    public static void assertHasFieldError(Response response, String field) {
        List<ApiFieldError> errors = parseErrors(response);
        assertThat(errors)
                .as("Expected a validation error on field '%s'. Actual errors: %s", field, errors)
                .extracting(ApiFieldError::getField)
                .contains(field);
    }

    private static List<ApiFieldError> parseErrors(Response response) {
        return Arrays.asList(response.as(ApiFieldError[].class));
    }
}
