package net.sourceforge.ganttproject.task.dependency;

import net.sourceforge.ganttproject.task.Task;

/**
 * Created by IntelliJ IDEA.
 * User: bard
 * Date: 14.02.2004
 * Time: 2:32:17
 * To change this template use File | Settings | File Templates.
 */
public interface TaskDependency {
    Task getDependant();
    Task getDependee();
    void setConstraint(TaskDependencyConstraint constraint);
    TaskDependencyConstraint getConstraint();

    void delete();    
}
