package com.hierarchy.gmbh.repository;

import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.EmployeeRelationshipEntity;
import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.EmployeeRelationshipRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class EmployeeRelationshipEntityRepositoryTest {
    @Mock private EmployeeRelationshipRepository entityRepository;

    @Test
    public void whenFindAll() {
        EmployeeRelationshipEntity demoEntity1 = new EmployeeRelationshipEntity("id1", "sup1");
        EmployeeRelationshipEntity demoEntity2 = new EmployeeRelationshipEntity("id2", "sup2");

        when(entityRepository.findAll()).thenReturn(List.of(demoEntity1, demoEntity2));
        List<EmployeeRelationshipEntity> entities =
                (List<EmployeeRelationshipEntity>) entityRepository.findAll();

        assertEquals(
                "check if input and output data equals",
                List.of(demoEntity1, demoEntity2),
                entities);
        verify(this.entityRepository).findAll();
    }
}
