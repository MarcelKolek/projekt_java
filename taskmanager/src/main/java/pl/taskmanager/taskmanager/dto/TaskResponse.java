package pl.taskmanager.taskmanager.dto;

import pl.taskmanager.taskmanager.entity.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskResponse {
    public Long id;
    public String title;
    public String description;
    public TaskStatus status;
    public LocalDate dueDate;
    public CategoryResponse category;
    public String attachmentFilename;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public TaskResponse() {}
    public TaskResponse(pl.taskmanager.taskmanager.entity.Task entity) {
        if (entity != null) {
            this.id = entity.getId();
            this.title = entity.getTitle();
            this.description = entity.getDescription();
            this.status = entity.getStatus();
            this.dueDate = entity.getDueDate();
            this.attachmentFilename = entity.getAttachmentFilename();
            this.createdAt = entity.getCreatedAt();
            this.updatedAt = entity.getUpdatedAt();
            if (entity.getCategory() != null) {
                this.category = new CategoryResponse(entity.getCategory());
            }
        }
    }
}
