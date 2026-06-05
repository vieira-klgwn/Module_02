package vector.StockManagement.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vector.StockManagement.model.Activity;
import vector.StockManagement.model.Employee;
import vector.StockManagement.model.dto.EmployeeDTO;
import vector.StockManagement.services.EmployeeService;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    public ResponseEntity<List<Employee>> getActivities(){
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    public ResponseEntity<Employee> createEmployee(@RequestBody EmployeeDTO employeeDTO){
        return ResponseEntity.ok(employeeService.createEmployee(employeeDTO));
    }

    //other more controllers can be added here


}
