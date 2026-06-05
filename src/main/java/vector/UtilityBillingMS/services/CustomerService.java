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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public Customer create(CustomerRequest request) {
        if (customerRepository.existsByNationalId(request.getNationalId())) {
            throw new BusinessException("National ID already exists: " + request.getNationalId());
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
                .user(user)
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .status(request.getStatus() != null ? request.getStatus() : EntityStatus.ACTIVE)
                .build();
        customerRepository.saveAndFlush(customer);
        user.setCustomer(customer);
        userRepository.saveAndFlush(user);
        return customerRepository.saveAndFlush(customer);
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


        if (!customer.getNationalId().equals(request.getNationalId())
                && customerRepository.existsByNationalId(request.getNationalId())) {
            throw new BusinessException("National ID already exists: " + request.getNationalId());
        }
        customer.setFullName(request.getFullName());
        customer.setNationalId(request.getNationalId());
        customer.setPhoneNumber(request.getPhoneNumber());
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }

        User user = customer.getUser();
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setNationalId(request.getNationalId());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setStatus(Enum.valueOf(UserStatus.class, request.getStatus().name()));
        userRepository.save(user);


        return customerRepository.save(customer);
    }

    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new BusinessException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    public void ensureActive(Customer customer) {
        if (customer.getStatus() != EntityStatus.ACTIVE) {
            throw new BusinessException("Customer is inactive and cannot be used");
        }
    }
}
