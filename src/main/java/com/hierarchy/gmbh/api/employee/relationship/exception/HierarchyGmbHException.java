package com.hierarchy.gmbh.api.employee.relationship.exception;

public class HierarchyGmbHException extends Exception {
    private final int returnCode;

    public HierarchyGmbHException(String message, int returnCode) {
        super(message);
        this.returnCode = returnCode;
    }

    public int getReturnCode() {
        return returnCode;
    }
}
