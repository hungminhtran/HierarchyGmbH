package com.hierarchy.gmbh.api.employee.relationship.data;

import java.util.Collection;
import java.util.Set;

public class EmployeeRelationshipTreeNode {
    private final String key;
    private final Collection<EmployeeRelationshipTreeNode> children;

    public EmployeeRelationshipTreeNode(String key, Set<EmployeeRelationshipTreeNode> children) {
        this.key = key;
        this.children = children;
    }

    public String getKey() {
        return key;
    }

    public Collection<EmployeeRelationshipTreeNode> getChildren() {
        return children;
    }
}
