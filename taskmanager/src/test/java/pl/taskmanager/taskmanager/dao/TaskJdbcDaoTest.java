package pl.taskmanager.taskmanager.dao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskJdbcDaoTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private TaskJdbcDao taskJdbcDao;

    @Test
    void shouldClearCategoryForTasks() {
        taskJdbcDao.clearCategoryForTasks(1L);
        verify(jdbcTemplate).update(anyString(), eq(1L));
    }

    @Test
    void shouldDeleteByTaskId() {
        taskJdbcDao.deleteByTaskId(2L);
        verify(jdbcTemplate).update(anyString(), eq(2L));
    }
}
