package com.ata.qa.gorest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A single validation error returned by GoRest on a 422 response.
 * The body is an array of these, e.g.:
 * <pre>
 * [
 *   {"field": "email", "message": "has already been taken"},
 *   {"field": "name",  "message": "can't be blank"}
 * ]
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiFieldError {

    private String field;
    private String message;

    public ApiFieldError() {
        // Required by Jackson.
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "{field='" + field + "', message='" + message + "'}";
    }
}
