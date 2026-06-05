package vector.StockManagement.services;




import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import org.springframework.stereotype.Service;
import vector.StockManagement.model.Student;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfService {

    public ByteArrayInputStream generatePdf(List<Student> students) {

        Document document = new Document();

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        try {

            PdfWriter.getInstance(document, out);

            document.open();

            Font font =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD);

            Paragraph title =
                    new Paragraph(
                            "Student Report",
                            font);

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);

            document.add(new Paragraph(" "));

            PdfPTable table =
                    new PdfPTable(3);

            table.addCell("ID");
            table.addCell("Name");
            table.addCell("Email");

            for (Student student : students) {

                table.addCell(
                        String.valueOf(student.getId()));

                table.addCell(
                        student.getName());

                table.addCell(
                        student.getEmail());
            }

            document.add(table);

            document.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ByteArrayInputStream(
                out.toByteArray());
    }
}
