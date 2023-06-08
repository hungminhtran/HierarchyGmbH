package com.hierarchy.gmbh.api.employee.relationship.data;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class EmployeeRelationshipTreeNodeJsonSerializer
        extends JsonSerializer<EmployeeRelationshipTreeNode> {

    @Override
    public void serialize(
            EmployeeRelationshipTreeNode value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();

        for (EmployeeRelationshipTreeNode child : value.getChildren()) {
            gen.writeObjectField(child.getKey(), child);
        }

        gen.writeEndObject();
    }
}
