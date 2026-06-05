package vector.StockManagement.services.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vector.StockManagement.model.Employee;
import vector.StockManagement.model.dto.EmployeeDTO;
import vector.StockManagement.repositories.EmployeeRepository;
import vector.StockManagement.services.EmployeeService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;


    public Employee createEmployee(EmployeeDTO employeeDTO){
        Employee employee = new Employee();
        employee.setFirstName(employeeDTO.getFirstName());
        employee.setLastName(employeeDTO.getLastName());
        employee.setSalary(employeeDTO.getSalary());
        employee.setPosition(employeeDTO.getPosition());
        return employeeRepository.save(employee);
    }

    @Override
    public Employee updateEmployee(Long id, EmployeeDTO employeeDTO) {
        if (employeeRepository.findById(id).isPresent()) {
            Employee employee = employeeRepository.findById(id).get();
            employee.setFirstName(employeeDTO.getFirstName());
            employee.setLastName(employeeDTO.getLastName());
            employee.setSalary(employeeDTO.getSalary());
            employee.setPosition(employeeDTO.getPosition());
            return employeeRepository.save(employee);
        }
        else {
            return  null;
        }

    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @Override
    public void deleteEmployee(Long id) {

        employeeRepository.deleteById(id);
    }
}
