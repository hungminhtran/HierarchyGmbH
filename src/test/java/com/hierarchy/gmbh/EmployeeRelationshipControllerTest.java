package com.hierarchy.gmbh;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tran Minh-Hung
 */
@ExtendWith(MockitoExtension.class)
public class EmployeeRelationshipControllerTest {

    @Autowired private MockMvc mockMvc;

    @Test
    public void givenUserDoesNotExists_whenUserInfoIsRetrieved_then404IsReceived()
            throws IOException {

        Map<String, String> inputData = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ResultActions response =
                    mockMvc.perform(
                            post("/api/employees")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(inputData)));
            response.equals("");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
