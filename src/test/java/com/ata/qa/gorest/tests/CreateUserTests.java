package com.ata.qa.gorest.tests;

import com.ata.qa.gorest.base.BaseTest;
import com.ata.qa.gorest.data.TestDataFactory;
import com.ata.qa.gorest.model.User;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.ata.qa.gorest.support.UserAssertions.assertHasFieldError;
import static com.ata.qa.gorest.support.UserAssertions.assertMatchesUserSchema;
import static com.ata.qa.gorest.support.UserAssertions.assertStatus;
import static com.ata.qa.gorest.support.UserAssertions.assertUserMatches;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("create")
@DisplayName("POST /users - create user")
class CreateUserTests extends BaseTest {

    @Test
    @Tag("smoke")
    @DisplayName("Valid payload returns 201 and echoes the created user")
    void createUserWithValidData_shouldReturn201() {
        requireAuth();
        User newUser = TestDataFactory.randomActiveUser();

        Response response = userService.createUser(newUser);

        assertStatus(response, 201);
        int id = response.jsonPath().getInt("id");
        trackForCleanup(id);
        assertThat(id).as("server-assigned id").isPositive();
        assertUserMatches(response, newUser);
    }

    @Test
    @DisplayName("Created user response conforms to the user JSON schema")
    void createdUser_shouldMatchUserSchema() {
        requireAuth();
        User newUser = TestDataFactory.randomActiveUser();

        Response response = userService.createUser(newUser);

        assertStatus(response, 201);
        trackForCleanup(response.jsonPath().getInt("id"));
        assertMatchesUserSchema(response);
    }

    @Test
    @Tag("negative")
    @DisplayName("Duplicate e-mail is rejected with 422")
    void createUserWithDuplicateEmail_shouldReturn422() {
        requireAuth();

        // First creation succeeds and reserves the e-mail.
        User firstUser = TestDataFactory.randomActiveUser();
        Response firstResponse = userService.createUser(firstUser);
        assertStatus(firstResponse, 201);
        trackForCleanup(firstResponse.jsonPath().getInt("id"));

        // Second user re-uses the same e-mail.
        User duplicate = User.builder()
                .name(TestDataFactory.randomActiveUser().getName())
                .email(firstUser.getEmail())
                .gender("female")
                .status("active")
                .build();

        Response response = userService.createUser(duplicate);

        assertStatus(response, 422);
        assertHasFieldError(response, "email", "taken");
    }

    @Test
    @Tag("negative")
    @DisplayName("Empty payload is rejected with 422 listing the missing fields")
    void createUserWithMissingFields_shouldReturn422() {
        requireAuth();
        Map<String, Object> emptyBody = new HashMap<>();

        Response response = userService.createUser(emptyBody);

        assertStatus(response, 422);
        assertHasFieldError(response, "email");
        assertHasFieldError(response, "name");
    }

    @Test
    @Tag("negative")
    @DisplayName("Malformed e-mail is rejected with 422")
    void createUserWithInvalidEmail_shouldReturn422() {
        requireAuth();
        User invalid = TestDataFactory.randomActiveUser();
        invalid.setEmail("not-a-valid-email");

        Response response = userService.createUser(invalid);

        assertStatus(response, 422);
        assertHasFieldError(response, "email", "invalid");
    }

    @Test
    @Tag("negative")
    @Tag("security")
    @DisplayName("Create without a bearer token returns 401")
    void createUserWithoutToken_shouldReturn401() {
        User newUser = TestDataFactory.randomActiveUser();

        Response response = userService.createUserWithoutAuth(newUser);

        assertStatus(response, 401);
        assertThat(response.jsonPath().getString("message"))
                .as("401 message")
                .containsIgnoringCase("authentication");
    }
}
