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
//import vector.StockManagement.repositories.InventoryRepository;
//import vector.StockManagement.repositories.ProductRepository;
//import vector.StockManagement.repositories.TenantRepository;
//import vector.StockManagement.services.impl.InventoryServiceImpl;
//
//import java.math.BigDecimal;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class InventoryServiceTest {
//
//    @Mock
//    private InventoryRepository inventoryRepository;
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @Mock
//    private TenantRepository tenantRepository;
//
//    @InjectMocks
//    private InventoryServiceImpl inventoryService;
//
//    private Tenant testTenant;
//    private User testUser;
//    private Product testProduct;
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
//        testInventory = new Inventory();
//        testInventory.setId(1L);
//        testInventory.setProduct(testProduct);
//        testInventory.setLocationType(LocationType.WAREHOUSE);
//        testInventory.setLocationId(1L);
//        testInventory.setQtyOnHand(50);
//        testInventory.setQtyReserved(10);
//        testInventory.setReorderLevel(20);
//        testInventory.setMaxLevel(100);
//        testInventory.setAvgUnitCost(new BigDecimal("10.00"));
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
//    void testSaveInventory_Success() {
//        // Given
//        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
//
//        // When
//        Inventory result = inventoryService.save(testInventory);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(testTenant, result.getTenant());
//        verify(inventoryRepository).save(testInventory);
//    }
//
//    @Test
//    void testSaveInventory_NoTenant() {
//        // Given
//        testUser.setTenant(null);
//
//        // When & Then
//        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
//                inventoryService.save(testInventory));
//
//        assertTrue(exception.getMessage().contains("Authenticated user has no tenant assigned"));
//    }
//
//    @Test
//    void testSaveInventory_NotAuthenticated() {
//        // Given
//        SecurityContext securityContext = mock(SecurityContext.class);
//        Authentication authentication = mock(Authentication.class);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        when(authentication.getPrincipal()).thenReturn("anonymousUser");
//        SecurityContextHolder.setContext(securityContext);
//
//        // When & Then
//        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
//                inventoryService.save(testInventory));
//
//        assertTrue(exception.getMessage().contains("Authenticated principal is not a domain User"));
//    }
//
//    @Test
//    void testSaveInventory_ProductWithoutTenant() {
//        // Given
//        testProduct.setTenant(null);
//        testInventory.setProduct(testProduct);
//
//        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
//        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
//
//        // When
//        Inventory result = inventoryService.save(testInventory);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(testTenant, result.getTenant());
//        assertEquals(testTenant, testProduct.getTenant());
//        verify(productRepository).save(testProduct);
//        verify(inventoryRepository).save(testInventory);
//    }
//
//    @Test
//    void testFindById_Success() {
//        // Given
//        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
//
//        // When
//        Inventory result = inventoryService.findById(1L);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(testInventory.getId(), result.getId());
//        verify(inventoryRepository).findById(1L);
//    }
//
//    @Test
//    void testFindById_NotFound() {
//        // Given
//        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // When
//        Inventory result = inventoryService.findById(999L);
//
//        // Then
//        assertNull(result);
//        verify(inventoryRepository).findById(999L);
//    }
////
////    @Test
////    void testFindInventoryByProduct_Success() {
////        // Given
////        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
////        when(inventoryRepository.findByProduct(testProduct)).thenReturn(testInventory);
////
////        // When
//////        Inventory result = inventoryService.findInventoryByProductAndUser(productRepository.findById())
////
////        // Then
////        assertNotNull(result);
////        assertEquals(testInventory.getId(), result.getId());
////        verify(productRepository).findById(1L);
////        verify(inventoryRepository).findByProduct(testProduct);
////    }
//
//    @Test
//    void testFindInventoryByProduct_ProductNotFound() {
//        // Given
//        when(productRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // When & Then
//        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
//                inventoryService.findInventoryByProduct(999L));
//
//        assertTrue(exception.getMessage().contains("Product not found"));
//    }found
//
//    @Test
//    void testDeleteInventory() {
//        // When
//        inventoryService.delete(1L);
//
//        // Then
//        verify(inventoryRepository).deleteById(1L);
//    }
//
//    @Test
//    void testInventoryHelperMethods() {
//        // Test getQtyAvailable
//        assertEquals(40, testInventory.getQtyAvailable()); // 50 - 10
//
//        // Test isLowStock
//        testInventory.setQtyOnHand(15); // Below reorder level of 20
//        assertTrue(testInventory.isLowStock());
//
//        testInventory.setQtyOnHand(25); // Above reorder level
//        assertFalse(testInventory.isLowStock());
//
//        // Test canReserve
//        testInventory.setQtyOnHand(50);
//        testInventory.setQtyReserved(10);
//        assertTrue(testInventory.canReserve(30)); // 50 - 10 >= 30
//        assertFalse(testInventory.canReserve(50)); // 50 - 10 < 50
//
//        // Test addStock
//        testInventory.addStock(20); //removed , new BigDecimal("15.00")
//        assertEquals(70, testInventory.getQtyOnHand());
//        assertNotNull(testInventory.getLastStockIn());
//
//        // Test removeStock
//        testInventory.removeStock(10);
//        assertEquals(60, testInventory.getQtyOnHand());
//        assertNotNull(testInventory.getLastStockOut());
//
//        // Test reserveStock
//        testInventory.reserveStock(15);
//        assertEquals(25, testInventory.getQtyReserved()); // 10 + 15
//
//        // Test releaseReservedStock
//        testInventory.releaseReservedStock(5);
//        assertEquals(20, testInventory.getQtyReserved()); // 25 - 5
//    }
//}
