package vector.StockManagement.services;


import org.apache.commons.csv.*;

import org.springframework.stereotype.Service;
import vector.StockManagement.model.Student;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

@Service
public class CsvService {

    public ByteArrayInputStream
    generateCsv(List<Student> students) {

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        try {

            CSVPrinter csvPrinter =
                    new CSVPrinter(
                            new PrintWriter(out),
                            CSVFormat.DEFAULT
                                    .withHeader(
                                            "ID",
                                            "Name",
                                            "Email"));

            for (Student student : students) {

                csvPrinter.printRecord(
                        student.getId(),
                        student.getName(),
                        student.getEmail());
            }

            csvPrinter.flush();

        } catch (Exception e) {

            throw new RuntimeException(e);
        }

        return new ByteArrayInputStream(
                out.toByteArray());
    }
}