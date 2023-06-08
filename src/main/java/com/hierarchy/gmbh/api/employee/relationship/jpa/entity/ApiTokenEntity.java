package com.hierarchy.gmbh.api.employee.relationship.jpa.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ApiTokenEntity {
    @Id private String apiToken;

    public ApiTokenEntity() {}

    public ApiTokenEntity(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
}
