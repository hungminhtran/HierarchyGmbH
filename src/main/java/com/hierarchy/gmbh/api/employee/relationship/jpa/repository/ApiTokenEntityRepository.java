package com.hierarchy.gmbh.api.employee.relationship.jpa.repository;

import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.ApiTokenEntity;

import org.springframework.data.repository.CrudRepository;

public interface ApiTokenEntityRepository extends CrudRepository<ApiTokenEntity, String> {}
