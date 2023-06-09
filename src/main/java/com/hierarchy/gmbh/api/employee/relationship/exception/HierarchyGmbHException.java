package com.hierarchy.gmbh.api.employee.relationship.exception;

public class HierarchyGmbHException extends Exception {
    private int returnCode;

    public int getReturnCode() {
        return returnCode;
    }

    public HierarchyGmbHException(String message, int returnCode) {
        super(message);
        this.returnCode = returnCode;
    }
}
