package net.sourceforge.ganttproject.task;

/**
 * Created by IntelliJ IDEA.
 * User: bard
 */
public interface TaskContainmentHierarchyFacade {
    Task[] getNestedTasks(Task container);
    Task getRoot();

    Task getContainer(Task nestedTask);

    interface Factory {
        TaskContainmentHierarchyFacade createFacede();
    }
}
