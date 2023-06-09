package com.hierarchy.gmbh.api.employee.relationship.jpa.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hierarchy.gmbh.api.employee.relationship.data.EmployeeRelationshipRestResponse;
import com.hierarchy.gmbh.api.employee.relationship.data.EmployeeRelationshipTreeNode;
import com.hierarchy.gmbh.api.employee.relationship.data.EmployeeRelationshipTreeNodeJsonSerializer;
import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.EmployeeRelationshipEntity;
import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.EmployeeRelationshipRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.PageRequest;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class EmployeeRelationshipDataValidator {

    private static final Logger LOGGER =
            LogManager.getLogger(EmployeeRelationshipDataValidator.class);

    private static final Integer PAGE_LIMIT = 1000;

    private static final ReentrantLock MUTEX = new ReentrantLock();

    private static final Map<String, String> EMPLOYEE_SUPERVISOR_MAP = new HashMap<>();
    private static final Map<String, Set<String>> SUPERVISOR_EMPLOYEE_MAP = new HashMap<>();

    private static volatile String rootEmployee;

    public EmployeeRelationshipDataValidator(
            EmployeeRelationshipRepository employeeRelationshipRepository) {
        LOGGER.info("initializing data");
        if (EMPLOYEE_SUPERVISOR_MAP.size() > 0) {
            return;
        }
        MUTEX.lock();
        if (EMPLOYEE_SUPERVISOR_MAP.size() > 0) {
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
                LOGGER.error("init page size " + entities.size());

                for (EmployeeRelationshipEntity entity : entities) {
                    EMPLOYEE_SUPERVISOR_MAP.put(entity.getEmployee(), entity.getSupervisor());
                    if (!SUPERVISOR_EMPLOYEE_MAP.containsKey(entity.getSupervisor())) {
                        SUPERVISOR_EMPLOYEE_MAP.put(entity.getSupervisor(), new HashSet<>());
                    }
                    SUPERVISOR_EMPLOYEE_MAP.get(entity.getSupervisor()).add(entity.getEmployee());
                }
                page++;
                if (entities.size() < PAGE_LIMIT) {
                    break;
                }
            }

            rootEmployee = getNewRootEmployee(EMPLOYEE_SUPERVISOR_MAP, null);
            MUTEX.unlock();
        } finally {
            if (MUTEX.isHeldByCurrentThread()) {
                MUTEX.unlock();
            }
            LOGGER.info("init data complete");
        }
    }

    public static void lock() {
        if (!MUTEX.isHeldByCurrentThread()) {
            MUTEX.lock();
        }
    }

    public static void unlock() {
        if (MUTEX.isHeldByCurrentThread()) {
            MUTEX.unlock();
        }
    }

    private boolean isEmployeeSupervisorOfSupervisor(
            String employee, String supervisor, Map<String, String> employeeSupervisorMap) {
        Set<String> travelled = new HashSet<>();
        while (employeeSupervisorMap.containsKey(employee)) {
            if (travelled.contains(employee)) {
                return false;
            }
            travelled.add(employee);
            String localSupervisor = employeeSupervisorMap.get(employee);
            if (localSupervisor.equals(supervisor)) {
                return true;
            }
            employee = localSupervisor;
        }
        return false;
    }

    private String getNewRootEmployee(
            Map<String, String> employeeSupervisorMap, String currentRootEmployee) {
        if (currentRootEmployee == null) {
            for (String employee : employeeSupervisorMap.keySet()) {
                String supervisor = employeeSupervisorMap.get(employee);
                if (supervisor == null || supervisor.isEmpty()) {
                    return employee;
                } else {
                    String supervisorOfSupervisor = employeeSupervisorMap.get(supervisor);
                    if (supervisorOfSupervisor == null || supervisorOfSupervisor.isEmpty()) {
                        return supervisor;
                    }
                }
            }
        }

        while (employeeSupervisorMap.containsKey(currentRootEmployee)) {
            String rootSupervisor = employeeSupervisorMap.get(currentRootEmployee);
            if (rootSupervisor == null || rootSupervisor.isEmpty()) {
                return currentRootEmployee;
            }
            currentRootEmployee = rootSupervisor;
        }

        return currentRootEmployee;
    }

    private Set<String> validateMultipleRoot(
            Map<String, String> allNewRelationship,
            String currentRootEmployee,
            Map<String, String> employeeRelationshipMap) {
        Set<String> multipleRootErrorSet = new HashSet<>();

        for (String employee : allNewRelationship.keySet()) {
            String supervisor = allNewRelationship.get(employee);
            if (supervisor == null
                    || supervisor.isEmpty() && !employee.equals(currentRootEmployee)) {
                if (currentRootEmployee == null) {
                    currentRootEmployee = employee;
                } else {
                    multipleRootErrorSet.add(employee + " is left as a root employee.");
                }

            } else {
                String supervisorOfSupervisor = employeeRelationshipMap.get(supervisor);

                if ((supervisorOfSupervisor == null || supervisorOfSupervisor.isEmpty())
                        && !supervisor.equals(currentRootEmployee)) {
                    if (currentRootEmployee == null) {
                        currentRootEmployee = supervisor;
                    } else {
                        multipleRootErrorSet.add(supervisor + " is left as a root employee.");
                    }
                }
            }
        }
        return multipleRootErrorSet;
    }

    private Set<String> validateCircularRelationship(
            Map<String, String> allNewRelationships, Map<String, String> employeeRelationshipMap) {
        Set<String> circularErrorSet = new HashSet<>();
        for (String employee : allNewRelationships.keySet()) {
            String supervisor = allNewRelationships.get(employee);
            if (isEmployeeSupervisorOfSupervisor(supervisor, employee, employeeRelationshipMap)) {
                circularErrorSet.add(employee + " and " + supervisor + " are created a cycle.");
            }
        }
        return circularErrorSet;
    }

    public void updateStaticData(Map<String, String> allNewRelationships) {
        EMPLOYEE_SUPERVISOR_MAP.putAll(allNewRelationships);
        rootEmployee = getNewRootEmployee(EMPLOYEE_SUPERVISOR_MAP, rootEmployee);

        for (String employee : allNewRelationships.keySet()) {
            if (!SUPERVISOR_EMPLOYEE_MAP.containsKey(allNewRelationships.get(employee))) {
                SUPERVISOR_EMPLOYEE_MAP.put(allNewRelationships.get(employee), new HashSet<>());
            }
            SUPERVISOR_EMPLOYEE_MAP.get(allNewRelationships.get(employee)).add(employee);
        }
    }

    public EmployeeRelationshipRestResponse validateEmployeeRelationshipEntities(
            Map<String, String> allNewRelationships) {
        List<String> allErrorMessageSet = new ArrayList<>();

        Map<String, String> localEmployeeSupervisorMap = new HashMap<>(EMPLOYEE_SUPERVISOR_MAP);
        localEmployeeSupervisorMap.putAll(allNewRelationships);

        String localRootEmployee = getNewRootEmployee(localEmployeeSupervisorMap, rootEmployee);

        allErrorMessageSet.addAll(
                validateMultipleRoot(
                        allNewRelationships, localRootEmployee, localEmployeeSupervisorMap));
        allErrorMessageSet.addAll(
                validateCircularRelationship(allNewRelationships, localEmployeeSupervisorMap));

        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(
                EmployeeRelationshipTreeNode.class,
                new EmployeeRelationshipTreeNodeJsonSerializer());

        mapper.registerModule(module);

        ObjectWriter ow = mapper.writer();

        if (allErrorMessageSet.size() == 0) {
            Map<String, Set<String>> localSupervisorEmployeeMap =
                    new HashMap<>(SUPERVISOR_EMPLOYEE_MAP);
            for (String employee : allNewRelationships.keySet()) {
                if (!localSupervisorEmployeeMap.containsKey(allNewRelationships.get(employee))) {
                    localSupervisorEmployeeMap.put(
                            allNewRelationships.get(employee), new HashSet<>());
                }
                localSupervisorEmployeeMap.get(allNewRelationships.get(employee)).add(employee);
            }

            try {
                return new EmployeeRelationshipRestResponse(
                        true,
                        ow.writeValueAsString(
                                buildTheResultTree(
                                        allNewRelationships,
                                        localSupervisorEmployeeMap,
                                        localRootEmployee)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return new EmployeeRelationshipRestResponse(
                    false, ow.writeValueAsString(allErrorMessageSet));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private EmployeeRelationshipTreeNode buildTheResultTree(
            Map<String, String> allNewRelationships,
            Map<String, Set<String>> supervisorEmployeeMap,
            String rootEmployee) {

        Set<String> allNewEmployees = new HashSet<>(allNewRelationships.keySet());

        allNewEmployees.addAll(allNewRelationships.values());

        Queue<String> supervisorQueue = new ArrayDeque<>();
        supervisorQueue.add(rootEmployee);

        EmployeeRelationshipTreeNode result;
        Map<String, EmployeeRelationshipTreeNode> employeeRelationshipTreeNodeMap = new HashMap<>();

        if (allNewEmployees.contains(rootEmployee)) {
            result = new EmployeeRelationshipTreeNode(rootEmployee, new HashSet<>());
            EmployeeRelationshipTreeNode rootEmployeeRelationshipTreeNode =
                    new EmployeeRelationshipTreeNode(rootEmployee, new HashSet<>());
            result.getChildren().add(rootEmployeeRelationshipTreeNode);
            employeeRelationshipTreeNodeMap.put(rootEmployee, rootEmployeeRelationshipTreeNode);
        } else {
            result = new EmployeeRelationshipTreeNode(rootEmployee, new HashSet<>());
            employeeRelationshipTreeNodeMap.put(rootEmployee, result);
        }

        while (supervisorQueue.size() > 0) {
            String supervisor = supervisorQueue.poll();

            Set<String> allWorkers = supervisorEmployeeMap.get(supervisor);

            if (allWorkers == null || allWorkers.size() == 0) {
                continue;
            }
            supervisorQueue.addAll(allWorkers);

            Set<String> newWorkers =
                    allWorkers.stream()
                            .filter(allNewEmployees::contains)
                            .collect(Collectors.toSet());

            if (newWorkers != null && newWorkers.size() > 0) {
                EmployeeRelationshipTreeNode currentEmployeeRelationshipTreeNode =
                        employeeRelationshipTreeNodeMap.get(supervisor);
                for (String worker : newWorkers) {
                    EmployeeRelationshipTreeNode childEmployeeRelationshipTreeNode =
                            new EmployeeRelationshipTreeNode(worker, new HashSet<>());
                    currentEmployeeRelationshipTreeNode
                            .getChildren()
                            .add(childEmployeeRelationshipTreeNode);
                    employeeRelationshipTreeNodeMap.put(worker, childEmployeeRelationshipTreeNode);
                }
            }
        }

        return result;
    }
}
