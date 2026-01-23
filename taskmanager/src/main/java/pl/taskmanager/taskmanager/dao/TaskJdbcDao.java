package pl.taskmanager.taskmanager.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TaskJdbcDao {

    private final JdbcTemplate jdbcTemplate;

    public TaskJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int clearCategoryForTasks(Long categoryId) {
        String sql = "UPDATE tasks SET category_id = NULL WHERE category_id = ?";
        return jdbcTemplate.update(sql, categoryId);
    }

    public void insertLog(String message, Long userId) {
        // Przykład operacji INSERT z update() zgodnie z wymaganiem
        String sql = "INSERT INTO tasks (title, status, user_id, created_at, updated_at) VALUES (?, 'TODO', ?, NOW(), NOW())";
        jdbcTemplate.update(sql, "[LOG] " + message, userId);
    }
    
    public void deleteByTaskId(Long taskId) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        jdbcTemplate.update(sql, taskId);
    }
}
