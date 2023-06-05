package com.hierarchy.gmbh.api.employee.relationship.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.EmployeeRelationshipEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Service
public class EmployeeRelationshipService {

	private static final Logger logger = LogManager.getLogger(EmployeeRelationshipService.class);

	public String getSupervisorOfSupervisorName(String employee) {
		return "";
	}

	public String transformEmployeeData(String rawDataJsonStr) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			Map<String, String> rawData = objectMapper.readValue(rawDataJsonStr, Map.class);
			for (String key : rawData.keySet()) {
				System.out.println("key_" + key + "_value_" + rawData.get(key) + "_");
			}
		}
		catch (JsonProcessingException e) {
			logger.error("data from user is not a json string", e);
		}
		return null;
	}

	private LinkedList<List<String>> transformEmployeeDataToLinkedList(List<EmployeeRelationshipEntity> entities) {
		return null;
	}

}
