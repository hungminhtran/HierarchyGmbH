package com.hierarchy.gmbh.api.employee.relationship.jpa.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class EmployeeRelationshipEntity {

    @Id private String employee;

    private String supervisor;

    public EmployeeRelationshipEntity() {}

    public EmployeeRelationshipEntity(String employee, String supervisor) {
        this.employee = employee;
        this.supervisor = supervisor;
    }

    public String getEmployee() {
        return this.employee;
    }

    public String getSupervisor() {
        return this.supervisor;
    }
}
