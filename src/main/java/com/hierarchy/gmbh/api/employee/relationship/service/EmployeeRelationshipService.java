package com.hierarchy.gmbh.api.employee.relationship.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hierarchy.gmbh.api.employee.relationship.data.EmployeeRelationshipRestResponse;
import com.hierarchy.gmbh.api.employee.relationship.exception.HierarchyGmbHException;
import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.EmployeeRelationshipEntity;
import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.EmployeeRelationshipRepository;
import com.hierarchy.gmbh.api.employee.relationship.jpa.validation.EmployeeRelationshipDataValidator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

@Service
public class EmployeeRelationshipService {

    private static final Logger LOGGER = LogManager.getLogger(EmployeeRelationshipService.class);

    private static volatile EmployeeRelationshipDataValidator employeeRelationshipDataValidator;

    @Autowired private EmployeeRelationshipRepository employeeRelationshipRepository;

    @PostConstruct
    private void init() {
        if (employeeRelationshipDataValidator == null) {
            synchronized (EmployeeRelationshipService.class) {
                if (employeeRelationshipDataValidator == null) {
                    employeeRelationshipDataValidator =
                            new EmployeeRelationshipDataValidator(employeeRelationshipRepository);
                }
            }
        }

        LOGGER.info("data was initialized");
    }

    public EmployeeRelationshipRestResponse getSupervisorOfSupervisorName(String employee) {

        try {
            return new EmployeeRelationshipRestResponse(
                    false, getSupervisorOfEmployee(employee, 0, 2));
        } catch (HierarchyGmbHException e) {
            if (e.getMessage().equals(employee)) {
                return new EmployeeRelationshipRestResponse(
                        true, "Employee doesn't have supervisor");
            } else {
                return new EmployeeRelationshipRestResponse(
                        true, "Supervisor of the employee doesn't have supervisor");
            }
        }
    }

    private String getSupervisorOfEmployee(String employee, int depth, int limitation)
            throws HierarchyGmbHException {
        if (depth == limitation || employee == null) {
            return employee;
        }
        Optional<EmployeeRelationshipEntity> employeeRelationshipEntity =
                employeeRelationshipRepository.findById(employee);
        if (employeeRelationshipEntity.isPresent()) {
            String supervisor = employeeRelationshipEntity.get().getSupervisor();
            return getSupervisorOfEmployee(supervisor, depth + 1, limitation);
        }
        throw new HierarchyGmbHException(employee, HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public String saveEmployeeData(String rawDataJsonStr) throws HierarchyGmbHException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> rawData;
        try {
            rawData = objectMapper.readValue(rawDataJsonStr, Map.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("data from user is not a json string " + e.getMessage());
            throw new HierarchyGmbHException(
                    "data from user is not a json string" + e.getMessage(),
                    HttpStatus.UNPROCESSABLE_ENTITY.value());
        }
        List<EmployeeRelationshipEntity> entities = new ArrayList<>();
        for (String key : rawData.keySet()) {
            entities.add(new EmployeeRelationshipEntity(key, rawData.get(key)));
        }

        EmployeeRelationshipRestResponse response =
                employeeRelationshipDataValidator.basicEmptyDataCheck(rawData);
        if (response.isError()) {
            throw new HierarchyGmbHException(
                    response.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY.value());
        }
        EmployeeRelationshipDataValidator.lock();

        try {
            response =
                    employeeRelationshipDataValidator.validateEmployeeRelationshipEntities(rawData);

            if (response.isError()) {
                throw new HierarchyGmbHException(
                        response.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY.value());
            }
            employeeRelationshipRepository.saveAll(entities);
            employeeRelationshipDataValidator.updateStaticData(rawData);

            return response.getMessage();
        } finally {
            EmployeeRelationshipDataValidator.unlock();
        }
    }
}
