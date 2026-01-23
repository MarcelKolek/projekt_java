package pl.taskmanager.taskmanager.service;

@org.springframework.stereotype.Service
public class PdfService {

    public byte[] exportTasksToPdf(java.util.List<pl.taskmanager.taskmanager.dto.TaskResponse> tasks, String username) throws java.io.IOException {
        try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            com.lowagie.text.Document document = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(document, out);
            document.open();
            document.add(new com.lowagie.text.Paragraph("Lista Zadań - " + username));
            document.add(new com.lowagie.text.Paragraph(" "));

            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(4);
            table.addCell("ID");
            table.addCell("Tytuł");
            table.addCell("Status");
            table.addCell("Termin");

            for (pl.taskmanager.taskmanager.dto.TaskResponse t : tasks) {
                table.addCell(String.valueOf(t.id));
                table.addCell(t.title);
                table.addCell(t.status.name());
                table.addCell(t.dueDate != null ? t.dueDate.toString() : "");
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }
}
