package com.hierarchy.gmbh.controller;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.ApiTokenEntity;
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

import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

/**
 * @author Tran Minh-Hung
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeRelationshipControllerTest {
    private static final Logger LOGGER =
            LogManager.getLogger(EmployeeRelationshipControllerTest.class);

    @Autowired private WebApplicationContext context;
    @Autowired private ApiTokenRepository apiTokenRepository;
    @Autowired private EmployeeRelationshipRepository employeeRelationshipRepository;

    private MockMvc mockMvc;

    @PostConstruct
    public void setup() {
        LOGGER.info("set up test data");
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        apiTokenRepository.deleteAll();
        employeeRelationshipRepository.deleteAll();
        apiTokenRepository.save(new ApiTokenEntity("correct api key"));
        LOGGER.info("set up test data complete");
    }

    @BeforeEach
    public void resetDatabase() {
        LOGGER.info("reset test data");
        apiTokenRepository.deleteAll();
        employeeRelationshipRepository.deleteAll();
        apiTokenRepository.save(new ApiTokenEntity("correct api key"));
        LOGGER.info("reset test data complete");
    }

    @Test
    public void authorization() throws Exception {
        mockMvc.perform(
                        post("/add-employee-relationship")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-API-KEY", "incorrect api key"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

        mockMvc.perform(
                        get("/employee-supervisor")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-API-KEY", "incorrect api key"))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void addEmployeeRelationshipEmptyDataTest() throws Exception {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

        Map<String, String> data = new TreeMap<>();
        data.put("", "2");
        data.put("", "3");
        data.put("", "4");

        ResultActions resultActions =
                mockMvc.perform(
                        post("/add-employee-relationship")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectWriter.writeValueAsString(data))
                                .header("X-API-KEY", "correct api key"));
        LOGGER.info(
                "result action " + resultActions.andReturn().getResponse().getContentAsString());
        resultActions
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("\"null or empty employee\""));
    }

    @Test
    public void addEmployeeRelationshipDataSmokeTest() throws Exception {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

        Map<String, String> data = new TreeMap<>();
        data.put("1", "2");
        data.put("2", "3");
        data.put("3", "4");

        ResultActions resultActions =
                mockMvc.perform(
                        post("/add-employee-relationship")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectWriter.writeValueAsString(data))
                                .header("X-API-KEY", "correct api key"));
        LOGGER.info(
                "result action " + resultActions.andReturn().getResponse().getContentAsString());
        resultActions
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"4\":{\"3\":{\"2\":{\"1\":{}}}}}"));
    }

    @Test
    public void addEmployeeRelationshipInvalidDataTest() throws Exception {

        ResultActions resultActions =
                mockMvc.perform(
                        post("/add-employee-relationship")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{1:2, 3}")
                                .header("X-API-KEY", "correct api key"));
        LOGGER.info(
                "result action " + resultActions.andReturn().getResponse().getContentAsString());
        resultActions
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void addEmployeeRelationshipMultipleRootFromInputDataTest() throws Exception {
        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

        Map<String, String> data = new TreeMap<>();
        data.put("12", "7");
        data.put("1", "2");
        data.put("2", "3");
        data.put("3", "4");
        data.put("10", "5");
        data.put("11", "6");

        ResultActions resultActions =
                mockMvc.perform(
                        post("/add-employee-relationship")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectWriter.writeValueAsString(data))
                                .header("X-API-KEY", "correct api key"));
        LOGGER.info(
                "result action " + resultActions.andReturn().getResponse().getContentAsString());
        resultActions
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(
                        content()
                                .json(
                                        " [\"6 is left as a root employee.\","
                                                + "\"7 is left as a root employee.\","
                                                + "\"4 is left as a root employee.\","
                                                + "\"5 is left as a root employee.\"]\n"));
    }

    @Test
    public void addEmployeeRelationshipCreateMultipleRootWithDatabaseDataTest() throws Exception {
        addEmployeeRelationshipDataSmokeTest();

        ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        Map<String, String> testData = new TreeMap<>();
        testData.put("5", "6");

        ResultActions resultActions =
                mockMvc.perform(
                        post("/add-employee-relationship")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectWriter.writeValueAsString(testData))
                                .header("X-API-KEY", "correct api key"));
        LOGGER.info(
                "result action " + resultActions.andReturn().getResponse().getContentAsString());
        resultActions
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[\"6 is left as a root employee.\"]\n"));
    }
}
