//package vector.StockManagement.services;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import vector.StockManagement.model.*;
//import vector.StockManagement.model.enums.*;
//import vector.StockManagement.repositories.*;
//import vector.StockManagement.services.impl.OrderServiceImpl;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class OrderServiceTest {
//
//    @Mock
//    private OrderRepository orderRepository;
//
//    @Mock
//    private OrderLineRepository orderLineRepository;
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private TenantRepository tenantRepository;
//
//    @Mock
//    private InvoiceRepository invoiceRepository;
//
//    @Mock
//    private StoreRepository storeRepository;
//
//    @Mock
//    private WarehouseRepository warehouseRepository;
//
//    @Mock
//    private InventoryService inventoryService;
//
//    @Mock
//    private NotificationSerivice notificationService;
//
//    @Mock
//    private InventoryRepository inventoryRepository;
//
//    @InjectMocks
//    private OrderServiceImpl orderService;
//
//    private Tenant testTenant;
//    private User testUser;
//    private Product testProduct;
//    private Warehouse testWarehouse;
//    private Order testOrder;
//    private OrderLine testOrderLine;
//    private Inventory testInventory;
//
//    @BeforeEach
//    void setUp() {
//        testTenant = new Tenant();
//        testTenant.setId(1L);
//        testTenant.setName("Test Tenant");
//
//        testUser = new User();
//        testUser.setId(1L);
//        testUser.setFirstName("John");
//        testUser.setLastName("Doe");
//        testUser.setTenant(testTenant);
//
//        testProduct = new Product();
//        testProduct.setId(1L);
//        testProduct.setSku("TEST-001");
//        testProduct.setName("Test Product");
//        testProduct.setPrice(100L);
//        testProduct.setTenant(testTenant);
//
//        testWarehouse = new Warehouse();
//        testWarehouse.setId(1L);
//        testWarehouse.setName("Test Warehouse");
//
//        testOrder = new Order();
//        testOrder.setId(1L);
//        testOrder.setNumber("ORD-001");
//        testOrder.setStatus(OrderStatus.SUBMITTED);
//        testOrder.setTenant(testTenant);
//        testOrder.setWarehouse(testWarehouse);
//
//        testOrderLine = new OrderLine();
//        testOrderLine.setId(1L);
//        testOrderLine.setOrder(testOrder);
//        testOrderLine.setProduct(testProduct);
//        testOrderLine.setQty(10);
//        testOrderLine.setUnitPrice(100L);
//        testOrderLine.setLineTotal(1000L);
//
//        testOrder.setOrderLines(Arrays.asList(testOrderLine));
//
//        testInventory = new Inventory();
//        testInventory.setId(1L);
//        testInventory.setProduct(testProduct);
//        testInventory.setLocationType(LocationType.WAREHOUSE);
//        testInventory.setLocationId(1L);
//        testInventory.setQtyOnHand(20);
//        testInventory.setQtyReserved(0);
//        testInventory.setTenant(testTenant);
//
//        // Mock security context
//        SecurityContext securityContext = mock(SecurityContext.class);
//        Authentication authentication = mock(Authentication.class);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        when(authentication.getPrincipal()).thenReturn(testUser);
//        SecurityContextHolder.setContext(securityContext);
//    }
//
//    @Test
//    void testApproveOrder_Success() {
//        // Given
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
//        when(inventoryRepository.findByProductAndLocationTypeAndLocationId(
//                testProduct, LocationType.WAREHOUSE, 1L)).thenReturn(testInventory);
//        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
//        when(invoiceRepository.save(any(Invoice.class))).thenReturn(new Invoice());
//
//        // When
//        Order result = orderService.approve(1L, testOrder);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(OrderStatus.APPROVED, result.getStatus());
//        assertNotNull(result.getApprovedBy());
//        assertNotNull(result.getApprovedAt());
//
//        verify(inventoryRepository).save(any(Inventory.class));
//        verify(invoiceRepository).save(any(Invoice.class));
//        verify(notificationService).save(any(Notification.class));
//    }
//
//    @Test
//    void testApproveOrder_InsufficientInventory() {
//        // Given
//        testInventory.setQtyOnHand(5); // Less than required 10
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
//        when(inventoryRepository.findByProductAndLocationTypeAndLocationId(
//                testProduct, LocationType.WAREHOUSE, 1L)).thenReturn(testInventory);
//
//        // When & Then
//        RuntimeException exception = assertThrows(RuntimeException.class, () ->
//                orderService.approve(1L, testOrder));
//
//        assertTrue(exception.getMessage().contains("Insufficient inventory"));
//    }
//
//    @Test
//    void testFulfillOrder_Success() {
//        // Given
//        testOrder.setStatus(OrderStatus.APPROVED);
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
//        when(inventoryRepository.findByProductAndLocationTypeAndLocationId(
//                testProduct, LocationType.WAREHOUSE, 1L)).thenReturn(testInventory);
//        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
//
//        // When
//        Order result = ((OrderServiceImpl) orderService).fulfillOrder(1L, 1L);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(OrderStatus.FULFILLED, result.getStatus());
//        verify(inventoryRepository, times(2)).save(any(Inventory.class)); // Once for release, once for remove
//    }
//
//    @Test
//    void testFulfillOrder_NotApproved() {
//        // Given
//        testOrder.setStatus(OrderStatus.DRAFT);
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
//
//        // When & Then
//        RuntimeException exception = assertThrows(RuntimeException.class, () ->
//                ((OrderServiceImpl) orderService).fulfillOrder(1L, 1L));
//
//        assertTrue(exception.getMessage().contains("Can only fulfill approved orders"));
//    }
//
//    @Test
//    void testSubmitOrder_Success() {
//        // Given
//        testOrder.setStatus(OrderStatus.DRAFT);
//        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
//        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
//        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
//
//        // When
//        Order result = ((OrderServiceImpl) orderService).submitOrder(1L, 1L);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(OrderStatus.SUBMITTED, result.getStatus());
//        assertNotNull(result.getSubmittedAt());
//        verify(notificationService).save(any(Notification.class));
//    }
//}
