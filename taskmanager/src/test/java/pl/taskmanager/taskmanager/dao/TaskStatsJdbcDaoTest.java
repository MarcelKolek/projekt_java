package pl.taskmanager.taskmanager.dao;

@org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
@org.springframework.context.annotation.Import(TaskStatsJdbcDao.class)
class TaskStatsJdbcDaoTest {

    @org.springframework.beans.factory.annotation.Autowired
    private TaskStatsJdbcDao taskStatsJdbcDao;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @org.junit.jupiter.api.Test
    void shouldGetStats() {
        jdbcTemplate.execute("CREATE TABLE tasks (id IDENTITY PRIMARY KEY, status VARCHAR(255), user_id BIGINT)");
        jdbcTemplate.execute("INSERT INTO tasks (status, user_id) VALUES ('TODO', 1)");
        jdbcTemplate.execute("INSERT INTO tasks (status, user_id) VALUES ('IN_PROGRESS', 1)");
        jdbcTemplate.execute("INSERT INTO tasks (status, user_id) VALUES ('DONE', 1)");
        jdbcTemplate.execute("INSERT INTO tasks (status, user_id) VALUES ('TODO', 2)"); // other user

        pl.taskmanager.taskmanager.dto.TaskStatsResponse stats = taskStatsJdbcDao.getStats(1L);

        org.assertj.core.api.Assertions.assertThat(stats.total).isEqualTo(3);
        org.assertj.core.api.Assertions.assertThat(stats.todo).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(stats.inProgress).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(stats.done).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(stats.percentDone).isEqualTo(33.333333333333336);
    }
}
