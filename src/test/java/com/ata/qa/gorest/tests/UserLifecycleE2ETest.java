package com.ata.qa.gorest.tests;

import com.ata.qa.gorest.base.BaseTest;
import com.ata.qa.gorest.data.TestDataFactory;
import com.ata.qa.gorest.model.User;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.ata.qa.gorest.support.UserAssertions.assertStatus;
import static com.ata.qa.gorest.support.UserAssertions.assertUserMatches;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("e2e")
@DisplayName("End-to-end /users lifecycle")
class UserLifecycleE2ETest extends BaseTest {

    @Test
    @Tag("regression")
    @DisplayName("Create -> read -> update -> delete -> verify the user is gone")
    void fullUserLifecycle() {
        requireAuth();

        // 1. CREATE
        User toCreate = TestDataFactory.randomActiveUser();
        Response createResponse = userService.createUser(toCreate);
        assertStatus(createResponse, 201);
        int id = createResponse.jsonPath().getInt("id");
        assertUserMatches(createResponse, toCreate);

        // 2. READ back the created user
        Response readResponse = userService.getUser(id);
        assertStatus(readResponse, 200);
        assertUserMatches(readResponse, toCreate);

        // 3. UPDATE a field
        User update = User.builder().status("inactive").build();
        Response updateResponse = userService.updateUser(id, update);
        assertStatus(updateResponse, 200);
        assertThat(updateResponse.jsonPath().getString("status")).isEqualTo("inactive");

        // 4. DELETE
        Response deleteResponse = userService.deleteUser(id);
        assertStatus(deleteResponse, 204);

        // 5. VERIFY the resource no longer exists
        Response afterDelete = userService.getUser(id);
        assertStatus(afterDelete, 404);
    }
}
