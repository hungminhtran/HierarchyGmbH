package com.hierarchy.gmbh.api.employee.relationship.data;

import java.util.Collection;
import java.util.Set;

public class EmployeeRelationshipTreeNode {
    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        EmployeeRelationshipTreeNode objTreeNode = (EmployeeRelationshipTreeNode) obj;
        return key.equals(objTreeNode.getKey());
    }

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
