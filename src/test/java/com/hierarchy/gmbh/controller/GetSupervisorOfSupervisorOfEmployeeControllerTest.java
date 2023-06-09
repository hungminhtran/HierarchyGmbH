package com.hierarchy.gmbh.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.ApiTokenEntity;
import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.EmployeeRelationshipEntity;
import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.ApiTokenRepository;
import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.EmployeeRelationshipRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import javax.annotation.PostConstruct;

/**
 * @author Tran Minh-Hung
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetSupervisorOfSupervisorOfEmployeeControllerTest {
    private static final Logger LOGGER =
            LogManager.getLogger(AddEmployeeRelationshipControllerTest.class);
    private static final String API_URL = "/employee-supervisor-of-supervisor";
    @Autowired private WebApplicationContext context;
    @Autowired private ApiTokenRepository apiTokenRepository;
    @Autowired private EmployeeRelationshipRepository employeeRelationshipRepository;
    private MockMvc mockMvc;

    @PostConstruct
    public void setup() {
        LOGGER.info("set up test data");
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        LOGGER.info("set up test data complete");
    }

    @BeforeEach
    public void resetDatabase() {
        LOGGER.info("reset test data");
        apiTokenRepository.deleteAll();
        employeeRelationshipRepository.deleteAll();
        apiTokenRepository.save(new ApiTokenEntity("correct api key"));
        employeeRelationshipRepository.save(new EmployeeRelationshipEntity("1", "2"));
        employeeRelationshipRepository.save(new EmployeeRelationshipEntity("2", "3"));
        employeeRelationshipRepository.save(new EmployeeRelationshipEntity("3", "4"));
        LOGGER.info("reset test data complete");
    }

    @Test
    public void authorization() throws Exception {

        mockMvc.perform(
                        get(API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-API-KEY", "incorrect api key"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void getEmployeeSupervisorOfSupervisorSmokeCase() throws Exception {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

        ResultActions resultActions =
                mockMvc.perform(
                        get(API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("employee", "1")
                                .header("X-API-KEY", "correct api key"));
        LOGGER.info(
                "result action " + resultActions.andReturn().getResponse().getContentAsString());
        resultActions
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("\"3\""));
    }

    @Test
    public void getEmployeeSupervisorOfRootCase() throws Exception {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

        ResultActions resultActions =
                mockMvc.perform(
                        get(API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("employee", "4")
                                .header("X-API-KEY", "correct api key"));
        LOGGER.info(
                "result action " + resultActions.andReturn().getResponse().getContentAsString());
        resultActions
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("\"Employee doesn't have supervisor\""));
    }

    @Test
    public void getEmployeeSupervisorOfChildOfRootCase() throws Exception {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

        ResultActions resultActions =
                mockMvc.perform(
                        get(API_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("employee", "3")
                                .header("X-API-KEY", "correct api key"));
        LOGGER.info(
                "result action " + resultActions.andReturn().getResponse().getContentAsString());
        resultActions
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                        content().json("\"Supervisor of the employee doesn't have supervisor\""));
    }
}
