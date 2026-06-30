package com.ata.qa.gorest.data;

import com.ata.qa.gorest.model.User;
import net.datafaker.Faker;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Produces fresh, valid test data for every run.
 *
 * <p>The key design point is the e-mail address: GoRest enforces a unique e-mail per user, so a
 * hard-coded value would pass once and then fail with 422 forever. {@link #uniqueEmail()} combines
 * a timestamp and a random UUID fragment to stay unique across runs and across parallel threads,
 * which keeps the suite repeatable and self-contained.</p>
 */
public final class TestDataFactory {

    private static final Faker FAKER = new Faker();

    private TestDataFactory() {
    }

    /** A complete, valid, active user ready to be created. */
    public static User randomActiveUser() {
        return User.builder()
                .name(FAKER.name().fullName())
                .email(uniqueEmail())
                .gender(randomGender())
                .status("active")
                .build();
    }

    /** A complete, valid user with a caller-supplied status. */
    public static User randomUser(String status) {
        return User.builder()
                .name(FAKER.name().fullName())
                .email(uniqueEmail())
                .gender(randomGender())
                .status(status)
                .build();
    }

    /** Globally-unique, format-valid e-mail address. */
    public static String uniqueEmail() {
        String unique = System.currentTimeMillis() + "." + UUID.randomUUID().toString().substring(0, 8);
        return "qa.auto." + unique + "@example.test";
    }

    public static String randomGender() {
        return ThreadLocalRandom.current().nextBoolean() ? "male" : "female";
    }
}
