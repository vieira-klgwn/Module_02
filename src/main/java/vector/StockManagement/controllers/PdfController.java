package vector.StockManagement.controllers;




import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import vector.StockManagement.model.Student;
import vector.StockManagement.services.PdfService;

import java.util.List;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {
    private final PdfService pdfService;

    @GetMapping("/students")
    public ResponseEntity<InputStreamResource>
    generatePdf() {

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
                        pdfService.generatePdf(
                                students));

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=students.pdf")
                .contentType(
                        MediaType.APPLICATION_PDF)
                .body(file);
    }
}