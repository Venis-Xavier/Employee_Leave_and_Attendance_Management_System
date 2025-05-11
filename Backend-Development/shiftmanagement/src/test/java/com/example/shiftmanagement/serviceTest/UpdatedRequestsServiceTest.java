package com.example.shiftmanagement.serviceTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.*;

import com.example.shiftmanagement.client.EmployeeManagementClient;
import com.example.shiftmanagement.dao.UpdatedRequestsDao;
import com.example.shiftmanagement.entity.UpdatedRequests;
import com.example.shiftmanagement.service.UpdatedRequestsService;
import com.example.shiftmanagement.utils.ResultResponse;

class UpdatedRequestsServiceTest {

    @InjectMocks
    private UpdatedRequestsService updatedRequestsService;

    @Mock
    private UpdatedRequestsDao updatedRequestsDao;

    @Mock
    private EmployeeManagementClient employeeManagementClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUpdatedRequestsByEmployeeId() {
        List<UpdatedRequests> updates = new ArrayList<>();
        UpdatedRequests request = new UpdatedRequests();
        request.setEmployeeId(1);
        request.setShiftRequestedName("Night Shift");
        updates.add(request);

        when(updatedRequestsDao.findByEmployeeId(1)).thenReturn(updates);

        ResultResponse<List<UpdatedRequests>> response = updatedRequestsService.getUpdatedRequestsByEmployeeId(1);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        assertEquals("Night Shift", response.getData().get(0).getShiftRequestedName());
    }



    @Test
    void testExampleMethod() {
        List<UpdatedRequests> updates = new ArrayList<>();
        UpdatedRequests request = new UpdatedRequests();
        request.setEmployeeId(1);
        request.setShiftRequestedName("Afternoon Shift");
        updates.add(request);

        when(updatedRequestsDao.findByEmployeeId(1)).thenReturn(updates);

        List<UpdatedRequests> result = updatedRequestsService.example(1);

        assertEquals(1, result.size());
        assertEquals("Afternoon Shift", result.get(0).getShiftRequestedName());
    }
}