package com.hierarchy.gmbh.api.employee.relationship.controller;

import com.hierarchy.gmbh.api.employee.relationship.service.EmployeeRelationshipService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class EmployeeRelationshipController {

    @Autowired private EmployeeRelationshipService employeeRelationshipService;

    @PostMapping(value = "/add-employee-relationship", produces = "application/json")
    @ResponseBody
    public String setRelationship(@RequestBody String data) {
        return employeeRelationshipService.saveEmployeeData(data);
    }

    @GetMapping(value = "/employee-supervisor", produces = "application/json")
    @ResponseBody
    public String getEmployeeSupervisor(@RequestParam String employee) {
        return employeeRelationshipService.getSupervisorOfSupervisorName(employee);
    }
}
