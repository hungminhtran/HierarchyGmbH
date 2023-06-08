package com.hierarchy.gmbh.api.employee.relationship.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hierarchy.gmbh.api.employee.relationship.data.EmployeeRelationshipRestResponse;
import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.EmployeeRelationshipEntity;
import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.EmployeeRelationshipRepository;
import com.hierarchy.gmbh.api.employee.relationship.jpa.validation.EmployeeRelationshipDataValidator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

        LOGGER.info("initializing data");
    }

    public String getSupervisorOfSupervisorName(String employee) {
        return getSupervisorOfEmployee(employee, 0, 2);
    }

    private String getSupervisorOfEmployee(String employee, int depth, int limitation) {
        if (depth == limitation || employee == null) {
            return employee;
        }
        Optional<EmployeeRelationshipEntity> employeeRelationshipEntity =
                employeeRelationshipRepository.findById(employee);
        if (employeeRelationshipEntity.isPresent()) {
            String supervisor = employeeRelationshipEntity.get().getSupervisor();
            return getSupervisorOfEmployee(supervisor, depth + 1, limitation);
        }
        return null;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public String saveEmployeeData(String rawDataJsonStr) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> rawData;
        try {
            rawData = objectMapper.readValue(rawDataJsonStr, Map.class);
        } catch (Exception e) {
            LOGGER.error("data from user is not a json string", e.getMessage());
            return null;
        }
        List<EmployeeRelationshipEntity> entities = new ArrayList<>();
        for (String key : rawData.keySet()) {
            entities.add(new EmployeeRelationshipEntity(key, rawData.get(key)));
        }

        EmployeeRelationshipDataValidator.lock();

        try {
            EmployeeRelationshipRestResponse response =
                    employeeRelationshipDataValidator.validateEmployeeRelationshipEntities(rawData);

            if (response.isError()) {
                return response.getMessage();
            }
            employeeRelationshipRepository.saveAll(entities);
            employeeRelationshipDataValidator.updateStaticData(rawData);

            return response.getMessage();
        } finally {
            EmployeeRelationshipDataValidator.unlock();
        }
    }
}
