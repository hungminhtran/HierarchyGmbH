package com.hierarchy.gmbh.api.employee.relationship;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeRelationshipController {

	@GetMapping("/employee-relationship")
	public String setRelationship(@RequestParam(value = "data", defaultValue = "hxx") String data) {
		return data + "hxxx asdfasdf";
	}

}
