package com.hierarchy.gmbh.api.employee.relationship.controller;

import com.hierarchy.gmbh.api.employee.relationship.data.EmployeeRelationshipRestResponse;
import com.hierarchy.gmbh.api.employee.relationship.exception.HierarchyGmbHException;
import com.hierarchy.gmbh.api.employee.relationship.service.EmployeeRelationshipService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
public class EmployeeRelationshipController {

    @Autowired private EmployeeRelationshipService employeeRelationshipService;

    @PostMapping(value = "/add-employee-relationship", produces = "application/json")
    public String setRelationship(@RequestBody String data, HttpServletResponse response) {
        try {
            return employeeRelationshipService.saveEmployeeData(data);
        } catch (HierarchyGmbHException e) {
            response.setStatus(e.getReturnCode());
            return e.getMessage();
        }
    }

    @GetMapping(value = "/employee-supervisor-of-supervisor", produces = "application/json")
    public String getEmployeeSupervisor(
            @RequestParam String employee, HttpServletResponse response) {

        EmployeeRelationshipRestResponse restResponse =
                employeeRelationshipService.getSupervisorOfSupervisorName(employee);
        if (restResponse.isError()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
        return restResponse.getMessage();
    }
}
