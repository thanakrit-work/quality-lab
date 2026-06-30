package com.ata.qa.gorest.tests;

import com.ata.qa.gorest.base.BaseTest;
import com.ata.qa.gorest.data.TestDataFactory;
import com.ata.qa.gorest.model.User;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.ata.qa.gorest.support.UserAssertions.assertMatchesUsersListSchema;
import static com.ata.qa.gorest.support.UserAssertions.assertStatus;
import static com.ata.qa.gorest.support.UserAssertions.assertUserMatches;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("read")
@DisplayName("GET /users - read users")
class GetUserTests extends BaseTest {

    /** Large id assumed never to exist in the shared dataset. */
    private static final int NON_EXISTENT_ID = 999_999_999;

    @Test
    @Tag("smoke")
    @DisplayName("List users returns 200, a non-empty array and pagination metadata")
    void listUsers_shouldReturn200AndNonEmpty() {
        Response response = userService.listUsers();

        assertStatus(response, 200);
        User[] users = response.as(User[].class);
        assertThat(users).as("users on first page").isNotEmpty();
        assertThat(response.getHeader("X-Pagination-Page"))
                .as("pagination header should be present")
                .isNotNull();
    }

    @Test
    @DisplayName("List users response conforms to the user-list JSON schema")
    void listUsers_shouldMatchListSchema() {
        Response response = userService.listUsers();

        assertStatus(response, 200);
        assertMatchesUsersListSchema(response);
    }

    @Test
    @DisplayName("Requesting page 2 returns that page in the pagination metadata")
    void listUsers_secondPage_shouldReturnRequestedPage() {
        Response response = userService.listUsers(2);

        assertStatus(response, 200);
        assertThat(response.getHeader("X-Pagination-Page"))
                .as("requested page")
                .isEqualTo("2");
    }

    @Test
    @Tag("smoke")
    @DisplayName("Get an existing user returns 200 with the correct data")
    void getExistingUser_shouldReturn200AndCorrectData() {
        requireAuth();
        User created = TestDataFactory.randomActiveUser();
        Response createResponse = userService.createUser(created);
        assertStatus(createResponse, 201);
        int id = createResponse.jsonPath().getInt("id");
        trackForCleanup(id);

        Response response = userService.getUser(id);

        assertStatus(response, 200);
        assertThat(response.jsonPath().getInt("id")).isEqualTo(id);
        assertUserMatches(response, created);
    }

    @Test
    @Tag("negative")
    @DisplayName("Get a non-existent user returns 404")
    void getNonExistentUser_shouldReturn404() {
        Response response = userService.getUser(NON_EXISTENT_ID);

        assertStatus(response, 404);
        assertThat(response.jsonPath().getString("message"))
                .as("404 message")
                .containsIgnoringCase("not found");
    }
}
