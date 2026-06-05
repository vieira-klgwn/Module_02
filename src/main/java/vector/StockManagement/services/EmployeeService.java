package vector.StockManagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vector.StockManagement.model.Employee;
import vector.StockManagement.model.dto.EmployeeDTO;

import java.util.List;


public interface EmployeeService {
    public Employee createEmployee(EmployeeDTO employee);
    public Employee updateEmployee(Long employeeId, EmployeeDTO employee);
    public List<Employee> getAllEmployees();
    public void deleteEmployee(Long id);
}
