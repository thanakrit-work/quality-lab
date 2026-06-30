package com.ata.qa.gorest.tests;

import com.ata.qa.gorest.base.BaseTest;
import com.ata.qa.gorest.data.TestDataFactory;
import com.ata.qa.gorest.model.User;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.ata.qa.gorest.support.UserAssertions.assertStatus;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("delete")
@DisplayName("DELETE /users/{id} - delete user")
class DeleteUserTests extends BaseTest {

    private static final int NON_EXISTENT_ID = 999_999_999;

    @Test
    @Tag("smoke")
    @DisplayName("Delete an existing user returns 204 and the user is then gone")
    void deleteExistingUser_shouldReturn204AndUserGone() {
        requireAuth();
        User created = TestDataFactory.randomActiveUser();
        Response createResponse = userService.createUser(created);
        assertStatus(createResponse, 201);
        int id = createResponse.jsonPath().getInt("id");

        Response deleteResponse = userService.deleteUser(id);
        assertStatus(deleteResponse, 204);

        // Verify the resource is actually gone.
        Response getResponse = userService.getUser(id);
        assertStatus(getResponse, 404);
    }

    @Test
    @Tag("negative")
    @DisplayName("Delete a non-existent user returns 404")
    void deleteNonExistentUser_shouldReturn404() {
        requireAuth();

        Response response = userService.deleteUser(NON_EXISTENT_ID);

        assertStatus(response, 404);
    }

    @Test
    @Tag("negative")
    @Tag("security")
    @DisplayName("Delete without a bearer token returns 401")
    void deleteWithoutToken_shouldReturn401() {
        // Pick any real id from the public list as the delete target.
        Response list = userService.listUsers();
        assertStatus(list, 200);
        User[] users = list.as(User[].class);
        assertThat(users).as("seeded users").isNotEmpty();
        int targetId = users[0].getId();

        Response response = userService.deleteUserWithoutAuth(targetId);

        assertStatus(response, 401);
        assertThat(response.jsonPath().getString("message"))
                .as("401 message")
                .containsIgnoringCase("authentication");
    }
}
