package com.hierarchy.gmbh.repository;

import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import com.hierarchy.gmbh.api.employee.relationship.jpa.entity.ApiTokenEntity;
import com.hierarchy.gmbh.api.employee.relationship.jpa.repository.ApiTokenRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ApiTokenRepositoryTest {
    @Mock private ApiTokenRepository entityRepository;

    @Test
    public void whenFindAll() {
        ApiTokenEntity demoEntity1 = new ApiTokenEntity("id1");
        ApiTokenEntity demoEntity2 = new ApiTokenEntity("id2");

        when(entityRepository.findAll()).thenReturn(List.of(demoEntity1, demoEntity2));
        List<ApiTokenEntity> entities = (List<ApiTokenEntity>) entityRepository.findAll();

        assertEquals(
                "check if input and output data equals",
                List.of(demoEntity1, demoEntity2),
                entities);
        verify(this.entityRepository).findAll();
    }
}
