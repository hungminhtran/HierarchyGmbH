package com.hierarchy.gmbh.api.employee.relationship.data;

public class EmployeeRelationshipRestResponse {
    private final boolean isError;
    private final String message;

    public EmployeeRelationshipRestResponse(boolean isError, String message) {
        this.isError = isError;
        this.message = message;
    }

    public boolean isError() {
        return isError;
    }

    public String getMessage() {
        return message;
    }
}
