package com.ata.qa.gorest.tests;

import com.ata.qa.gorest.base.BaseTest;
import com.ata.qa.gorest.data.TestDataFactory;
import com.ata.qa.gorest.model.User;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.ata.qa.gorest.support.UserAssertions.assertHasFieldError;
import static com.ata.qa.gorest.support.UserAssertions.assertStatus;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("update")
@DisplayName("PUT /users/{id} - update user")
class UpdateUserTests extends BaseTest {

    private static final int NON_EXISTENT_ID = 999_999_999;

    @Test
    @Tag("smoke")
    @DisplayName("Update an existing user returns 200 with the new values")
    void updateExistingUser_shouldReturn200() {
        requireAuth();
        User created = TestDataFactory.randomActiveUser();
        Response createResponse = userService.createUser(created);
        assertStatus(createResponse, 201);
        int id = createResponse.jsonPath().getInt("id");
        trackForCleanup(id);

        String newName = TestDataFactory.randomActiveUser().getName();
        User update = User.builder().name(newName).status("inactive").build();

        Response response = userService.updateUser(id, update);

        assertStatus(response, 200);
        assertThat(response.jsonPath().getString("name")).as("updated name").isEqualTo(newName);
        assertThat(response.jsonPath().getString("status")).as("updated status").isEqualTo("inactive");
    }

    @Test
    @Tag("negative")
    @DisplayName("Update a non-existent user returns 404")
    void updateNonExistentUser_shouldReturn404() {
        requireAuth();
        User update = TestDataFactory.randomActiveUser();

        Response response = userService.updateUser(NON_EXISTENT_ID, update);

        assertStatus(response, 404);
    }

    @Test
    @Tag("negative")
    @DisplayName("Update with a malformed e-mail returns 422")
    void updateUserWithInvalidEmail_shouldReturn422() {
        requireAuth();
        User created = TestDataFactory.randomActiveUser();
        Response createResponse = userService.createUser(created);
        assertStatus(createResponse, 201);
        int id = createResponse.jsonPath().getInt("id");
        trackForCleanup(id);

        User update = User.builder().email("not-a-valid-email").build();

        Response response = userService.updateUser(id, update);

        assertStatus(response, 422);
        assertHasFieldError(response, "email", "invalid");
    }

    @Test
    @Tag("negative")
    @Tag("security")
    @DisplayName("Update without a bearer token returns 401")
    void updateWithoutToken_shouldReturn401() {
        // Pick any real id from the public list as the update target.
        Response list = userService.listUsers();
        assertStatus(list, 200);
        User[] users = list.as(User[].class);
        assertThat(users).as("seeded users").isNotEmpty();
        int targetId = users[0].getId();

        User update = User.builder().name("Should Not Apply").build();

        Response response = userService.updateUserWithoutAuth(targetId, update);

        assertStatus(response, 401);
        assertThat(response.jsonPath().getString("message"))
                .as("401 message")
                .containsIgnoringCase("authentication");
    }
}
