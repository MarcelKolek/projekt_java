package pl.taskmanager.taskmanager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import pl.taskmanager.taskmanager.entity.Task;
import pl.taskmanager.taskmanager.entity.TaskStatus;
import pl.taskmanager.taskmanager.entity.User;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Modifying
    @Query("update Task t set t.category = null where t.category.id = :categoryId")
    int clearCategoryForTasks(
            @Param("categoryId") Long categoryId
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
    Page<Task> search(
            @Param("username") String username,
            @Param("status") TaskStatus status,
            @Param("categoryId") Long categoryId,
            @Param("q") String q,
            @Param("dueBefore") LocalDate dueBefore,
            @Param("dueAfter") LocalDate dueAfter,
            Pageable pageable
    );

    List<Task> findAllByUser(User user);
}
