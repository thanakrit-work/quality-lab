package com.ata.qa.gorest.client;

import com.ata.qa.gorest.config.Configuration;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Low-level HTTP concern: produces ready-to-use {@link RequestSpecification}s with the common
 * base URI, base path, and JSON content negotiation already applied.
 *
 * <p>Two flavours are exposed so callers express intent rather than wiring headers by hand:</p>
 * <ul>
 *   <li>{@link #anonymous()} - no {@code Authorization} header (reads, and negative auth tests)</li>
 *   <li>{@link #authenticated()} - adds {@code Authorization: Bearer &lt;token&gt;} (writes)</li>
 * </ul>
 */
public class RestClient {

    private final Configuration config;
    private final RequestSpecification baseSpec;

    public RestClient(Configuration config) {
        this.config = config;
        this.baseSpec = new RequestSpecBuilder()
                .setBaseUri(config.getBaseUri())
                .setBasePath(config.getBasePath())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    /** Request without authentication. */
    public RequestSpecification anonymous() {
        return given().spec(baseSpec);
    }

    /** Request carrying the bearer token for authenticated operations. */
    public RequestSpecification authenticated() {
        return given()
                .spec(baseSpec)
                .header("Authorization", "Bearer " + config.getToken());
    }
}
