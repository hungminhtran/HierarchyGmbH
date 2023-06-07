package com.hierarchy.gmbh.api.employee.relationship.service;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.EmployeeRelationshipEntity;
import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.EmployeeRelationshipRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

@Service
public class EmployeeRelationshipService {

    private static final Logger LOGGER = LogManager.getLogger(EmployeeRelationshipService.class);

    private static final Integer PAGE_LIMIT = 1000;

    private static volatile ReentrantLock MUTEX = new ReentrantLock();

    private static volatile Map<String, String> EMPLOYEE_SUPERVISOR_MAP = new HashMap<>();
    private static volatile Map<String, Set<String>> SUPERVISOR_EMPLOYEE_MAP = new HashMap<>();

    private static volatile String rootEmployee;

    @Autowired private EmployeeRelationshipRepository employeeRelationshipRepository;

    private static boolean isEmployeeSupervisorOfSupervisor(
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

    private static String getNewRootEmployee(
            Map<String, String> employeeSupervisorMap, String currentRootEmployee) {
        LOGGER.error("current root " + currentRootEmployee);
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

    private static Set<String> validateMultipleRoot(
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

    private static Set<String> validateCircularRelationship(
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

    private static String validateEmployeeRelationshipEntities(
            Map<String, String> allNewRelationships) {
        List<String> allErrorMessageSet = new ArrayList<>();

        MUTEX.lock();
        try {
            Map<String, String> localEmployeeSupervisorMap = new HashMap<>(EMPLOYEE_SUPERVISOR_MAP);

            String localRootEmployee = getNewRootEmployee(localEmployeeSupervisorMap, rootEmployee);

            allErrorMessageSet.addAll(
                    validateMultipleRoot(
                            allNewRelationships, localRootEmployee, localEmployeeSupervisorMap));
            allErrorMessageSet.addAll(
                    validateCircularRelationship(allNewRelationships, localEmployeeSupervisorMap));

            if (allErrorMessageSet.size() == 0) {
                EMPLOYEE_SUPERVISOR_MAP.putAll(allNewRelationships);
                for (String employee : allNewRelationships.keySet()) {
                    if (!SUPERVISOR_EMPLOYEE_MAP.containsKey(allNewRelationships.get(employee))) {
                        SUPERVISOR_EMPLOYEE_MAP.put(
                                allNewRelationships.get(employee), new HashSet<>());
                    }
                    SUPERVISOR_EMPLOYEE_MAP.get(allNewRelationships.get(employee)).add(employee);
                }
                rootEmployee = localRootEmployee;
                Map<String, Set<String>> localSupervisorEmployeeMap =
                        new HashMap<>(SUPERVISOR_EMPLOYEE_MAP);
                MUTEX.unlock();
                try {
                    ObjectMapper mapper = new ObjectMapper();

                    SimpleModule module = new SimpleModule();
                    module.addSerializer(Node.class, new TreeNodeSerializer());

                    mapper.registerModule(module);

                    ObjectWriter ow = mapper.writer();
                    return ow.writeValueAsString(
                            buildTheResultTree(
                                    allNewRelationships,
                                    localSupervisorEmployeeMap,
                                    localRootEmployee));
                } catch (Exception e) {
                    LOGGER.error("error when convert result to json", e);
                    return null;
                }
            }

            MUTEX.unlock();
            try {
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                return ow.writeValueAsString(allErrorMessageSet);
            } catch (Exception e) {
                LOGGER.error("error when convert error message to json", e);
                return null;
            }

        } finally {
            if (MUTEX.isHeldByCurrentThread()) {
                MUTEX.unlock();
            }
        }
    }

    static class Node {
        private String key;
        private Collection<Node> children;

        public Node(String key, Set<Node> children) {
            this.key = key;
            this.children = children;
        }

        public String getKey() {
            return key;
        }

        public Collection<Node> getChildren() {
            return children;
        }
    }

    static class TreeNodeSerializer extends JsonSerializer<Node> {

        @Override
        public void serialize(Node value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeStartObject();

            for (Node child : value.getChildren()) {
                gen.writeObjectField(child.getKey(), child);
            }

            gen.writeEndObject();
        }
    }

    private static Node buildTheResultTree(
            Map<String, String> allNewRelationships,
            Map<String, Set<String>> supervisorEmployeeMap,
            String rootEmployee) {

        Set<String> allNewEmployees = new HashSet<>(allNewRelationships.keySet());

        allNewEmployees.addAll(allNewRelationships.values());

        Queue<String> supervisorQueue = new ArrayDeque<>();
        supervisorQueue.add(rootEmployee);

        Node result;
        Map<String, Node> nodeMap = new HashMap<>();

        if (allNewEmployees.contains(rootEmployee)) {
            result = new Node(rootEmployee, new HashSet<>());
            Node rootNode = new Node(rootEmployee, new HashSet<>());
            result.getChildren().add(rootNode);
            nodeMap.put(rootEmployee, rootNode);
        } else {
            result = new Node(rootEmployee, new HashSet<>());
            nodeMap.put(rootEmployee, result);
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
                Node currentNode = nodeMap.get(supervisor);
                for (String worker : newWorkers) {
                    Node childNode = new Node(worker, new HashSet<>());
                    currentNode.getChildren().add(childNode);
                    nodeMap.put(worker, childNode);
                }
            }
        }

        return result;
    }

    @PostConstruct
    private void init() {
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
                    LOGGER.error(
                            "entity "
                                    + entity
                                    + "_"
                                    + entity.getEmployee()
                                    + "_"
                                    + entity.getSupervisor()
                                    + "|");
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

    public String getSupervisorOfSupervisorName(String employee) {
        return getSupervisorOfEmployee(employee, 0, 1);
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
        //        if (result == null || !result.isEmpty()) {
        //            return result;
        //        }

        List<EmployeeRelationshipEntity> entities = new ArrayList<>();
        for (String key : rawData.keySet()) {
            entities.add(new EmployeeRelationshipEntity(key, rawData.get(key)));
        }
        employeeRelationshipRepository.saveAll(entities);

        return result;
    }
}
