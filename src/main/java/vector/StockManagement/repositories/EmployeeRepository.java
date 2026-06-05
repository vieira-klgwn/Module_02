package vector.StockManagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vector.StockManagement.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

}
