package pl.taskmanager.taskmanager.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import pl.taskmanager.taskmanager.dto.TaskResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfService {

    public byte[] exportTasksToPdf(List<TaskResponse> tasks, String username) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();
            document.add(new Paragraph("Lista Zadań - " + username));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.addCell("ID");
            table.addCell("Tytuł");
            table.addCell("Status");
            table.addCell("Termin");

            for (TaskResponse t : tasks) {
                table.addCell(String.valueOf(t.id));
                table.addCell(t.title);
                table.addCell(t.status != null ? t.status.name() : "");
                table.addCell(t.dueDate != null ? t.dueDate.toString() : "");
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        }
    }
}
