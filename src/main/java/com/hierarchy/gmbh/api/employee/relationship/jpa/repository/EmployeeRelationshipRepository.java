package com.hierarchy.gmbh.api.employee.relationship.jpa.repository;

import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.EmployeeRelationshipEntity;
import org.springframework.data.repository.CrudRepository;

public interface EmployeeRelationshipRepository extends CrudRepository<EmployeeRelationshipEntity, String> {

}
