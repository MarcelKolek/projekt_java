package pl.taskmanager.taskmanager.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository
        extends org.springframework.data.jpa.repository.JpaRepository<
                pl.taskmanager.taskmanager.entity.Task,
                java.lang.Long
        > {

    @Modifying
    @Query("update Task t set t.category = null where t.category.id = :categoryId")
    int clearCategoryForTasks(
            @Param("categoryId")
            java.lang.Long categoryId
    );

    @Query("""
        select t from Task t
        where t.user.username = :username
          and (:status is null or t.status = :status)
          and (:categoryId is null or t.category.id = :categoryId)
          and (:q is null or lower(t.title) like concat('%', lower(cast(:q as string)), '%'))
          and (:dueBefore is null or t.dueDate < :dueBefore)
          and (:dueAfter is null or t.dueDate > :dueAfter)
        """)
    org.springframework.data.domain.Page<
            pl.taskmanager.taskmanager.entity.Task
    > search(
            @Param("username")
            java.lang.String username,

            @Param("status")
            pl.taskmanager.taskmanager.entity.TaskStatus status,

            @Param("categoryId")
            java.lang.Long categoryId,

            @Param("q")
            java.lang.String q,

            @Param("dueBefore")
            java.time.LocalDate dueBefore,

            @Param("dueAfter")
            java.time.LocalDate dueAfter,

            org.springframework.data.domain.Pageable pageable
    );

    java.util.List<pl.taskmanager.taskmanager.entity.Task>
    findAllByUser(pl.taskmanager.taskmanager.entity.User user);
}
