package pl.taskmanager.taskmanager.service;

import org.springframework.stereotype.Service;

import pl.taskmanager.taskmanager.dto.TaskResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class CsvService {

    public byte[] exportTasksToCsv(List<TaskResponse> tasks) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,title,description,status,dueDate,categoryId,categoryName,createdAt,updatedAt\n");

        for (TaskResponse t : tasks) {
            sb.append(csv(t.id)).append(",");
            sb.append(csv(t.title)).append(",");
            sb.append(csv(t.description)).append(",");
            sb.append(csv(t.status != null ? t.status.name() : null)).append(",");
            sb.append(csv(t.dueDate != null ? t.dueDate.toString() : null)).append(",");

            Long catId = (t.category != null ? t.category.id : null);
            String catName = (t.category != null ? t.category.name : null);
            sb.append(csv(catId)).append(",");
            sb.append(csv(catName)).append(",");

            sb.append(csv(t.createdAt != null ? t.createdAt.toString() : null)).append(",");
            sb.append(csv(t.updatedAt != null ? t.updatedAt.toString() : null)).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String csv(Object value) {
        if (value == null) {
            return "";
        }
        String s = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
