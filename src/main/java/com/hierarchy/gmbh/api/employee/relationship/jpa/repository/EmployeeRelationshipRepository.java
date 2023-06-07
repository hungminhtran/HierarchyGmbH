package com.hierarchy.gmbh.api.employee.relationship.jpa.repository;

import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.EmployeeRelationshipEntity;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EmployeeRelationshipRepository
        extends CrudRepository<EmployeeRelationshipEntity, String> {

    List<EmployeeRelationshipEntity> findAll(Pageable pageable);
}
