package vector.UtilityBillingMS.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vector.UtilityBillingMS.exceptions.BusinessException;
import vector.UtilityBillingMS.model.Customer;
import vector.UtilityBillingMS.model.User;
import vector.UtilityBillingMS.model.dto.CustomerRequest;
import vector.UtilityBillingMS.model.enums.EntityStatus;
import vector.UtilityBillingMS.model.enums.Role;
import vector.UtilityBillingMS.model.enums.UserStatus;
import vector.UtilityBillingMS.repositories.CustomerRepository;
import vector.UtilityBillingMS.repositories.UserRepository;
import vector.UtilityBillingMS.validation.PasswordValidator;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NationalIdValidationService nationalIdValidationService;
    private final PasswordValidator passwordValidator = new PasswordValidator();

    public Customer create(CustomerRequest request) {
        validatePassword(request.getPassword(), true);

        nationalIdValidationService.ensureUnique(request.getNationalId());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already taken: " + request.getEmail());
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .nationalId(request.getNationalId())
                .status(UserStatus.ACTIVE)
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .role(Role.CUSTOMER)
                .build();
        userRepository.saveAndFlush(user);

        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .nationalId(request.getNationalId())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .status(request.getStatus() != null ? request.getStatus() : EntityStatus.ACTIVE)
                .build();
        customerRepository.saveAndFlush(customer);

        user.setCustomer(customer);
        userRepository.saveAndFlush(user);
        return customer;
    }

    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Customer not found with id: " + id));
    }

    public Customer update(Long id, CustomerRequest request) {
        Customer customer = findById(id);
        User user = customer.getUser();
        if (user == null) {
            throw new BusinessException("No user account linked to this customer");
        }

        if (!customer.getNationalId().equals(request.getNationalId())) {
            nationalIdValidationService.ensureUnique(request.getNationalId(), user.getId(), customer.getId());
        }
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already taken: " + request.getEmail());
        }

        customer.setFullName(request.getFullName());
        customer.setNationalId(request.getNationalId());
        customer.setAddress(request.getAddress());
        customer.setPhoneNumber(request.getPhoneNumber());
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
            user.setStatus(mapToUserStatus(request.getStatus()));
        }

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setNationalId(request.getNationalId());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            validatePassword(request.getPassword(), true);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);
        return customerRepository.save(customer);
    }

    public void delete(Long id) {
        Customer customer = findById(id);
        User user = customer.getUser();
        customerRepository.delete(customer);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    public void ensureActive(Customer customer) {
        if (customer.getStatus() != EntityStatus.ACTIVE) {
            throw new BusinessException("Customer is inactive and cannot be used");
        }
    }

    private void validatePassword(String password, boolean required) {
        if (password == null || password.isBlank()) {
            if (required) {
                throw new BusinessException("Password is required");
            }
            return;
        }
        if (!passwordValidator.isValid(password, null)) {
            throw new BusinessException("Password must be at least 8 characters with uppercase, lowercase, number and special character");
        }
    }

    private UserStatus mapToUserStatus(EntityStatus status) {
        return status == EntityStatus.ACTIVE ? UserStatus.ACTIVE : UserStatus.INACTIVE;
    }
}
