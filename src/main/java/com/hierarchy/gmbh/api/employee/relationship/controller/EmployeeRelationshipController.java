package com.hierarchy.gmbh.api.employee.relationship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hierarchy.gmbh.api.employee.relationship.service.EmployeeRelationshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeRelationshipController {

	@Autowired
	private EmployeeRelationshipService employeeRelationshipService;

	@GetMapping("/employee-relationship")
	public String setRelationship(@RequestParam(value = "data", defaultValue = "") String data) {
		return employeeRelationshipService.transformEmployeeData(data);
	}

}
