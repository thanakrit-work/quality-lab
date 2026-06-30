package com.ata.qa.gorest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * Represents a GoRest user. Used both as a request payload (POST/PUT) and as a
 * response model (GET).
 *
 * <ul>
 *   <li>{@code @JsonInclude(NON_NULL)} - null fields are omitted on serialization, so the
 *       same model can describe a full create payload or a partial update payload.</li>
 *   <li>{@code @JsonIgnoreProperties(ignoreUnknown = true)} - the suite does not break if the
 *       API adds new fields to its response.</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private Integer id;
    private String name;
    private String email;
    private String gender;   // "male" | "female"
    private String status;   // "active" | "inactive"

    public User() {
        // Required by Jackson for deserialization.
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Fluent builder so test data reads clearly at the call site. */
    public static final class Builder {
        private final User user = new User();

        public Builder id(Integer id) {
            user.id = id;
            return this;
        }

        public Builder name(String name) {
            user.name = name;
            return this;
        }

        public Builder email(String email) {
            user.email = email;
            return this;
        }

        public Builder gender(String gender) {
            user.gender = gender;
            return this;
        }

        public Builder status(String status) {
            user.status = status;
            return this;
        }

        public User build() {
            return user;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && Objects.equals(email, other.email)
                && Objects.equals(gender, other.gender)
                && Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, gender, status);
    }

    @Override
    public String toString() {
        return "User{id=" + id
                + ", name='" + name + '\''
                + ", email='" + email + '\''
                + ", gender='" + gender + '\''
                + ", status='" + status + '\'' + '}';
    }
}
