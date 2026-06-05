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
//import vector.StockManagement.repositories.InvoiceRepository;
//import vector.StockManagement.repositories.PaymentRepository;
//import vector.StockManagement.services.impl.PaymentServiceImpl;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class PaymentServiceTest {
//
//    @Mock
//    private PaymentRepository paymentRepository;
//
//    @Mock
//    private InvoiceRepository invoiceRepository;
//
//    @InjectMocks
//    private PaymentServiceImpl paymentService;
//
//    private Tenant testTenant;
//    private User testUser;
//    private Invoice testInvoice;
//    private Payment testPayment;
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
//        testInvoice = new Invoice();
//        testInvoice.setId(1L);
//        testInvoice.setNumber("INV-001");
//        testInvoice.setStatus(InvoiceStatus.ISSUED);
//        testInvoice.setTenant(testTenant);
//        testInvoice.setCurrency("USD");
//
//        // Set up invoice amounts
//        Map<String, BigDecimal> amounts = new HashMap<>();
//        amounts.put("total", new BigDecimal("1000.00"));
//        amounts.put("paid", new BigDecimal("0.00"));
//        amounts.put("balance", new BigDecimal("1000.00"));
//        testInvoice.setAmounts(amounts);
//
//        testPayment = new Payment();
//        testPayment.setId(1L);
//        testPayment.setInvoice(testInvoice);
//        testPayment.setMethod(PaymentMethod.BANK_TRANSFER);
//        testPayment.setAmount(new BigDecimal("500.00"));
//        testPayment.setCurrency("USD");
//        testPayment.setPostedBy(testUser);
//        testPayment.setTenant(testTenant);
//        testPayment.setPaymentStatus(PaymentStatus.COMPLETED);
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
//    void testProcessPayment_Success() {
//        // Given
//        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
//        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
//        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
//
//        // When
//        Payment result = paymentService.processPayment(1L, new BigDecimal("500.00"),
//                PaymentMethod.BANK_TRANSFER, "TXN-12345");
//
//        // Then
//        assertNotNull(result);
//        assertEquals(PaymentMethod.BANK_TRANSFER, result.getMethod());
//        assertEquals(new BigDecimal("500.00"), result.getAmount());
//        assertEquals("TXN-12345", result.getTxnRef());
//        assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
//        assertNotNull(result.getPaidAt());
//
//        verify(paymentRepository).save(any(Payment.class));
//        verify(invoiceRepository).save(testInvoice);
//    }
//
//    @Test
//    void testProcessPayment_InvoiceNotFound() {
//        // Given
//        when(invoiceRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // When & Then
//        RuntimeException exception = assertThrows(RuntimeException.class, () ->
//                paymentService.processPayment(999L, new BigDecimal("500.00"),
//                        PaymentMethod.BANK_TRANSFER, "TXN-12345"));
//
//        assertTrue(exception.getMessage().contains("Invoice not found"));
//    }
//
//    @Test
//    void testProcessPayment_NoAuthentication() {
//        // Given
//        SecurityContext securityContext = mock(SecurityContext.class);
//        Authentication authentication = mock(Authentication.class);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        when(authentication.getPrincipal()).thenReturn("anonymousUser"); // Not a User object
//        SecurityContextHolder.setContext(securityContext);
//
//        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(testInvoice));
//
//        // When & Then
////        RuntimeException exception = assertThrows(RuntimeException.class, () ->
////                paymentService.processPayment(1L, new BigDecimal("500.00"),
////                        PaymentMethod.BANK_TRANSFER, "TXN-12345"));
////
////        assertTrue(exception.getMessage().contains("User must be authenticated"));
//    }
//
//    @Test
//    void testSavePayment_Success() {
//        // Given
//        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);
//
//        // When
//        Payment result = paymentService.save(testPayment);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(testPayment.getId(), result.getId());
//        verify(paymentRepository).save(testPayment);
//    }
//
//    @Test
//    void testFindById_Success() {
//        // Given
//        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
//
//        // When
//        Payment result = paymentService.findById(1L);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(testPayment.getId(), result.getId());
//        verify(paymentRepository).findById(1L);
//    }
//
//    @Test
//    void testFindById_NotFound() {
//        // Given
//        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());
//
//        // When
//        Payment result = paymentService.findById(999L);
//
//        // Then
//        assertNull(result);
//        verify(paymentRepository).findById(999L);
//    }
//
//    @Test
//    void testDeletePayment() {
//        // When
//        paymentService.delete(1L);
//
//        // Then
//        verify(paymentRepository).deleteById(1L);
//    }
//}
