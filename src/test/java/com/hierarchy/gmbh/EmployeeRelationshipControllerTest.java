package com.hierarchy.gmbh;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.ApiTokenEntity;
import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.ApiTokenRepository;
import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.EmployeeRelationshipRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Tran Minh-Hung
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeRelationshipControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private ApiTokenRepository apiTokenRepository;
    @Autowired private EmployeeRelationshipRepository employeeRelationshipRepository;

    ObjectWriter objectWriter = new ObjectMapper().writer();

    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        apiTokenRepository.save(new ApiTokenEntity("correct api key"));
    }

    @Test
    public void authorization() throws Exception {
        mvc.perform(
                        post("/add-employee-relationship")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-API-KEY", "incorrect api key"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void addEmployeeRelationshipDataSmokeTest() throws Exception {
        Map<String, String> data = new TreeMap<>();
        data.put("1", "2");
        data.put("2", "3");
        data.put("3", "4");

        ResultActions resultActions =
                mvc.perform(
                                post("/add-employee-relationship")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectWriter.writeValueAsString(data))
                                        .header("X-API-KEY", "correct api key"))
                        .andExpect(status().is2xxSuccessful())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        //                .andExpect(content().json(""));
    }
}
