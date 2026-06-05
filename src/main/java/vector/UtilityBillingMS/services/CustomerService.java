package vector.UtilityBillingMS.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vector.UtilityBillingMS.exceptions.BusinessException;
import vector.UtilityBillingMS.model.Customer;
import vector.UtilityBillingMS.model.dto.CustomerRequest;
import vector.UtilityBillingMS.model.enums.EntityStatus;
import vector.UtilityBillingMS.repositories.CustomerRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer create(CustomerRequest request) {
        if (customerRepository.existsByNationalId(request.getNationalId())) {
            throw new BusinessException("National ID already exists: " + request.getNationalId());
        }
        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .nationalId(request.getNationalId())
                .phoneNumber(request.getPhoneNumber())
                .status(request.getStatus() != null ? request.getStatus() : EntityStatus.ACTIVE)
                .build();
        return customerRepository.save(customer);
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
