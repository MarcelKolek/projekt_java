package pl.taskmanager.taskmanager.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import pl.taskmanager.taskmanager.dto.TaskStatsResponse;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(TaskStatsJdbcDao.class)
class TaskStatsJdbcDaoTest {

    @Autowired
    private TaskStatsJdbcDao taskStatsJdbcDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldGetStats() {
        jdbcTemplate.execute("CREATE TABLE tasks (id IDENTITY PRIMARY KEY, status VARCHAR(255), user_id BIGINT)");
        jdbcTemplate.execute("INSERT INTO tasks (status, user_id) VALUES ('TODO', 1)");
        jdbcTemplate.execute("INSERT INTO tasks (status, user_id) VALUES ('IN_PROGRESS', 1)");
        jdbcTemplate.execute("INSERT INTO tasks (status, user_id) VALUES ('DONE', 1)");
        jdbcTemplate.execute("INSERT INTO tasks (status, user_id) VALUES ('TODO', 2)"); // other user

        TaskStatsResponse stats = taskStatsJdbcDao.getStats(1L);

        assertThat(stats.total).isEqualTo(3);
        assertThat(stats.todo).isEqualTo(1);
        assertThat(stats.inProgress).isEqualTo(1);
        assertThat(stats.done).isEqualTo(1);
        assertThat(stats.percentDone).isEqualTo(33.333333333333336);
    }
}
