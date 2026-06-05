package vector.StockManagement.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;

import org.springframework.web.bind.annotation.*;
import vector.StockManagement.model.Student;
import vector.StockManagement.services.CsvService;

import java.util.List;

@RestController
@RequestMapping("/api/csv")
public class CsvController {

    @Autowired
    private CsvService csvService;

    @GetMapping("/students")
    public ResponseEntity<InputStreamResource>
    generateCsv() {

        List<Student> students = List.of(
                new Student(
                        1L,
                        "John",
                        "john@gmail.com"),

                new Student(
                        2L,
                        "Alice",
                        "alice@gmail.com")
        );

        InputStreamResource file =
                new InputStreamResource(
                        csvService.generateCsv(
                                students));

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=students.csv")
                .contentType(
                        MediaType.parseMediaType(
                                "text/csv"))
                .body(file);
    }
}