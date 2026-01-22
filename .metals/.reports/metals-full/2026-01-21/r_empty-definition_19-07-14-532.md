error id: file://<WORKSPACE>/taskmanager/src/main/java/pl/taskmanager/taskmanager/repository/TaskRepository.java:_empty_/Param#
file://<WORKSPACE>/taskmanager/src/main/java/pl/taskmanager/taskmanager/repository/TaskRepository.java
empty definition using pc, found symbol in pc: _empty_/Param#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 530
uri: file://<WORKSPACE>/taskmanager/src/main/java/pl/taskmanager/taskmanager/repository/TaskRepository.java
text:
```scala
package pl.taskmanager.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.taskmanager.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Modifying
    @Query("update Task t set t.category = null where t.category.id = :categoryId")
    int clearCategoryForTasks(@Param@@("categoryId") Long categoryId);
}

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

@Query("""
select t from Task t
where (:status is null or t.status = :status)
  and (:categoryId is null or t.category.id = :categoryId)
  and (:q is null or lower(t.title) like lower(concat('%', :q, '%')))
  and (:dueBefore is null or t.dueDate < :dueBefore)
  and (:dueAfter is null or t.dueDate > :dueAfter)
""")
Page<Task> search(
        @Param("status") TaskStatus status,
        @Param("categoryId") Long categoryId,
        @Param("q") String q,
        @Param("dueBefore") LocalDate dueBefore,
        @Param("dueAfter") LocalDate dueAfter,
        Pageable pageable
);

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/Param#