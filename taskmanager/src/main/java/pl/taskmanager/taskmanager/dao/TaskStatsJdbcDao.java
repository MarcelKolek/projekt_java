package pl.taskmanager.taskmanager.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import pl.taskmanager.taskmanager.dto.TaskStatsResponse;

@Repository
public class TaskStatsJdbcDao {

    private final JdbcTemplate jdbcTemplate;

    public TaskStatsJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TaskStatsResponse getStats() {

        String sql = """
            select
              count(*) as total,
              sum(case when status = 'TODO' then 1 else 0 end) as todo,
              sum(case when status = 'IN_PROGRESS' then 1 else 0 end) as in_progress,
              sum(case when status = 'DONE' then 1 else 0 end) as done
            from tasks
        """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            long total = rs.getLong("total");
            long todo = rs.getLong("todo");
            long inProgress = rs.getLong("in_progress");
            long done = rs.getLong("done");

            double percentDone =
                    total == 0 ? 0.0 : (done * 100.0 / total);

            return new TaskStatsResponse(
                    total,
                    todo,
                    inProgress,
                    done,
                    percentDone
            );
        });
    }
}
