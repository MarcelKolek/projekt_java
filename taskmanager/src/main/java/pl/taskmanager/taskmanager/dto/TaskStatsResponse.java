package pl.taskmanager.taskmanager.dto;

public class TaskStatsResponse {
    public long total;
    public long todo;
    public long inProgress;
    public long done;
    public double percentDone;

    public TaskStatsResponse(long total, long todo, long inProgress, long done, double percentDone) {
        this.total = total;
        this.todo = todo;
        this.inProgress = inProgress;
        this.done = done;
        this.percentDone = percentDone;
    }
}
