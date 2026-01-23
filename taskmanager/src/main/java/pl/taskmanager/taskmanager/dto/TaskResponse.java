package pl.taskmanager.taskmanager.dto;

public class TaskResponse {
    public java.lang.Long id;
    public java.lang.String title;
    public java.lang.String description;
    public pl.taskmanager.taskmanager.entity.TaskStatus status;
    public java.time.LocalDate dueDate;
    public pl.taskmanager.taskmanager.dto.CategoryResponse category;
    public java.lang.String attachmentFilename;
    public java.time.LocalDateTime createdAt;
    public java.time.LocalDateTime updatedAt;

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
                this.category = new pl.taskmanager.taskmanager.dto.CategoryResponse(entity.getCategory());
            }
        }
    }
}
