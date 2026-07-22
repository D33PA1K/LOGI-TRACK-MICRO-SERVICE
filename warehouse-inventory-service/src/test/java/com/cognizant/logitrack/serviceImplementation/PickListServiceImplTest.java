package com.cognizant.logitrack.serviceImplementation;

import com.cognizant.logitrack.repository.PickListRepository;
import com.cognizant.logitrack.client.NotificationClient;
import com.cognizant.logitrack.client.FreightOrderClient;
import com.cognizant.logitrack.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PickListServiceImplTest {

    @Mock
    private PickListRepository pickListRepository;
    @Mock
    private NotificationClient notificationClient;
    @Mock
    private FreightOrderClient freightOrderClient;

    @InjectMocks
    private PickListServiceImpl pickListService;

    @Test
    void testAssignPickList_NotFound() {
        when(pickListRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> pickListService.assignPickList(1, 2));
    }
}
