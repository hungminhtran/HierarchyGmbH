package com.hierarchy.gmbh.api.employee.relationship.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.EmployeeRelationshipEntity;
import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.EmployeeRelationshipRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

@Service
public class EmployeeRelationshipService {

    private static final Logger LOGGER = LogManager.getLogger(EmployeeRelationshipService.class);

    private static final Integer PAGE_LIMIT = 1000;

    private static final ReentrantLock MUTEX = new ReentrantLock();

    private static final Map<String, String> EMPLOYEE_RELATIONSHIP_MAP = new HashMap<>();

    private static volatile String rootEmployee;

    @Autowired private EmployeeRelationshipRepository employeeRelationshipRepository;

    private static boolean isSupervisor(
            String employee, String supervisor, Map<String, String> employeeRelationshipMap) {
        Set<String> travelled = new HashSet<>();
        while (employeeRelationshipMap.containsKey(employee)) {
            if (travelled.contains(employee)) {
                return false;
            }
            travelled.add(employee);
            String localSupervisor = employeeRelationshipMap.get(employee);
            if (localSupervisor.equals(supervisor)) {
                return true;
            }
            employee = localSupervisor;
        }
        return false;
    }

    private static String getNewRootEmployee(
            Map<String, String> employeeRelationshipMapArg, String currentRootEmployee) {
        String localRootEmployee = currentRootEmployee;
        while (employeeRelationshipMapArg.containsKey(localRootEmployee)) {
            String supervisor = employeeRelationshipMapArg.get(localRootEmployee);
            if (supervisor == null || supervisor.isEmpty()) {
                break;
            }
            localRootEmployee = supervisor;
        }
        return localRootEmployee;
    }

    private static Set<String> validateMultipleRoot(
            Map<String, String> entities,
            String currentRootEmployee,
            Map<String, String> employeeRelationshipMap) {
        Set<String> multipleRootErrorSet = new HashSet<>();

        for (String employee : entities.keySet()) {
            String supervisor = entities.get(employee);
            if (supervisor == null || supervisor.isEmpty()) {
                if (currentRootEmployee == null || currentRootEmployee.isEmpty()) {
                    currentRootEmployee = employee;
                } else if (!currentRootEmployee.equals(employee)) {
                    multipleRootErrorSet.add(employee + " is left as a root employee.");
                }
            } else {
                String supervisorOfSupervisor = employeeRelationshipMap.get(supervisor);

                if ((supervisorOfSupervisor == null || supervisorOfSupervisor.isEmpty())) {
                    if (currentRootEmployee == null || currentRootEmployee.isEmpty()) {
                        currentRootEmployee = employee;
                    } else if (!currentRootEmployee.equals(supervisor)) {
                        multipleRootErrorSet.add(supervisor + " is left as a root employee.");
                    }
                }
            }
        }
        return multipleRootErrorSet;
    }

    private static Set<String> validateCircularRelationship(
            Map<String, String> entities, Map<String, String> employeeRelationshipMap) {
        Set<String> circularErrorSet = new HashSet<>();
        for (String employee : entities.keySet()) {
            String supervisor = entities.get(employee);
            if (isSupervisor(supervisor, employee, employeeRelationshipMap)) {
                circularErrorSet.add(employee + " and " + supervisor + " are created a cycle.");
            }
        }
        return circularErrorSet;
    }

    private static String validateEmployeeRelationshipEntities(Map<String, String> entities) {
        MUTEX.lock();
        try {
            Map<String, String> localEmployeeRelationshipMap =
                    new HashMap<>(EMPLOYEE_RELATIONSHIP_MAP);
            localEmployeeRelationshipMap.putAll(entities);
            String localRootEmployee =
                    getNewRootEmployee(localEmployeeRelationshipMap, rootEmployee);
            List<String> allErrorMessageSet = new ArrayList<>();
            // check if there are multiple root node
            allErrorMessageSet.addAll(
                    validateMultipleRoot(
                            entities, localRootEmployee, localEmployeeRelationshipMap));
            // check if a relationship is circular
            allErrorMessageSet.addAll(
                    validateCircularRelationship(entities, localEmployeeRelationshipMap));

            if (allErrorMessageSet.size() == 0) {
                EMPLOYEE_RELATIONSHIP_MAP.putAll(entities);
                rootEmployee = localRootEmployee;
                MUTEX.unlock();
                return "";
            } else {
                MUTEX.unlock();
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                try {
                    return ow.writeValueAsString(allErrorMessageSet);
                } catch (Exception e) {
                    LOGGER.error("error when convert error message to json", e.getMessage());
                    return null;
                }
            }
        } finally {
            if (MUTEX.isHeldByCurrentThread()) {
                MUTEX.unlock();
            }
        }
    }

    @PostConstruct
    private void init() {
        LOGGER.info("initializing data");
        if (EMPLOYEE_RELATIONSHIP_MAP.size() > 0) {
            return;
        }
        MUTEX.lock();
        if (EMPLOYEE_RELATIONSHIP_MAP.size() > 0) {
            MUTEX.unlock();
            return;
        }
        try {
            List<EmployeeRelationshipEntity> entities;
            int page = 0;
            while ((entities =
                            employeeRelationshipRepository.findAll(
                                    PageRequest.of(page, PAGE_LIMIT)))
                    != null) {
                if (entities.size() == 0) {
                    break;
                }
                entities.stream()
                        .forEach(
                                entity ->
                                        EMPLOYEE_RELATIONSHIP_MAP.put(
                                                entity.getEmployee(), entity.getSupervisor()));
                page++;
            }
            // Data logic should be valid when reading from database
            for (String employee : EMPLOYEE_RELATIONSHIP_MAP.keySet()) {
                String supervisor = EMPLOYEE_RELATIONSHIP_MAP.get(employee);
                if (supervisor == null || supervisor.isEmpty()) {
                    rootEmployee = employee;
                    break;
                }
                String supervisorOfSupervisor = EMPLOYEE_RELATIONSHIP_MAP.get(supervisor);
                if (supervisorOfSupervisor == null || supervisorOfSupervisor.isEmpty()) {
                    rootEmployee = supervisorOfSupervisor;
                    break;
                }
            }
            MUTEX.unlock();
        } finally {
            if (MUTEX.isHeldByCurrentThread()) {
                MUTEX.unlock();
            }
            LOGGER.info("init data complete");
        }
    }

    public String getSupervisorOfSupervisorName(String employee) {
        String supervisor = EMPLOYEE_RELATIONSHIP_MAP.get(employee);
        if (supervisor != null) {
            if (EMPLOYEE_RELATIONSHIP_MAP.containsKey(supervisor)) {
                return EMPLOYEE_RELATIONSHIP_MAP.get(supervisor);
            } else {
                return "";
            }
        }
        return null;
    }

    public String saveEmployeeData(String rawDataJsonStr) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> rawData;
        try {
            rawData = objectMapper.readValue(rawDataJsonStr, Map.class);
        } catch (Exception e) {
            LOGGER.error("data from user is not a json string", e.getMessage());
            return null;
        }

        String result = validateEmployeeRelationshipEntities(rawData);
        if (result == null || !result.isEmpty()) {
            return result;
        }

        List<EmployeeRelationshipEntity> entities = new ArrayList<>();
        for (String key : rawData.keySet()) {
            entities.add(new EmployeeRelationshipEntity(key, rawData.get(key)));
        }
        employeeRelationshipRepository.saveAll(entities);

        return result;
    }
}
