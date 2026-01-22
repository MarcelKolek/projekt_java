error id: file://<WORKSPACE>/taskmanager/src/main/java/pl/taskmanager/taskmanager/repository/TaskRepository.java
file://<WORKSPACE>/taskmanager/src/main/java/pl/taskmanager/taskmanager/repository/TaskRepository.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[30,1]

error in qdox parser
file content:
```java
offset: 1116
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
    int clearCategoryForTasks(@Param("categoryId") Long categoryId);
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
P@@age<Task> search(
        @Param("status") TaskStatus status,
        @Param("categoryId") Long categoryId,
        @Param("q") String q,
        @Param("dueBefore") LocalDate dueBefore,
        @Param("dueAfter") LocalDate dueAfter,
        Pageable pageable
);

```

```



#### Error stacktrace:

```
com.thoughtworks.qdox.parser.impl.Parser.yyerror(Parser.java:2025)
	com.thoughtworks.qdox.parser.impl.Parser.yyparse(Parser.java:2147)
	com.thoughtworks.qdox.parser.impl.Parser.parse(Parser.java:2006)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:232)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:190)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:94)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:89)
	com.thoughtworks.qdox.library.SortedClassLibraryBuilder.addSource(SortedClassLibraryBuilder.java:162)
	com.thoughtworks.qdox.JavaProjectBuilder.addSource(JavaProjectBuilder.java:174)
	scala.meta.internal.mtags.JavaMtags.indexRoot(JavaMtags.scala:49)
	scala.meta.internal.metals.SemanticdbDefinition$.foreachWithReturnMtags(SemanticdbDefinition.scala:99)
	scala.meta.internal.metals.Indexer.indexSourceFile(Indexer.scala:546)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3(Indexer.scala:677)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3$adapted(Indexer.scala:674)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:630)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:628)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1313)
	scala.meta.internal.metals.Indexer.reindexWorkspaceSources(Indexer.scala:674)
	scala.meta.internal.metals.MetalsLspService.$anonfun$onChange$2(MetalsLspService.scala:912)
	scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:691)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:500)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1144)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:642)
	java.base/java.lang.Thread.run(Thread.java:1583)
```
#### Short summary: 

QDox parse error in file://<WORKSPACE>/taskmanager/src/main/java/pl/taskmanager/taskmanager/repository/TaskRepository.java