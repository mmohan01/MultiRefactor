package net.sourceforge.ganttproject.task.dependency.constraint;

import net.sourceforge.ganttproject.task.dependency.TaskDependencyConstraint;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.GanttTaskRelationship;
import net.sourceforge.ganttproject.GanttCalendar;
import net.sourceforge.ganttproject.language.GanttLanguage;

/**
 * Created by IntelliJ IDEA.
 * User: bard
 */
public class StartStartConstraintImpl extends ConstraintImpl implements TaskDependencyConstraint {
    public StartStartConstraintImpl() {
        super(GanttTaskRelationship.SS, GanttLanguage.getInstance().getText("startstart"));
    }

    public TaskDependencyConstraint.Collision getCollision() {
        TaskDependencyConstraint.Collision result = null;
        Task dependee = getDependency().getDependee();
        Task dependant = getDependency().getDependant();
        GanttCalendar dependeeStart = dependee.getStart();
        GanttCalendar dependantStart = dependant.getStart();
        //
        boolean isActive = dependant.isStartFixed() ?
                dependantStart.compareTo(dependeeStart)<0 :
                dependantStart.compareTo(dependeeStart)!=0;
        GanttCalendar acceptableStart = dependee.getStart();
        result = new TaskDependencyConstraint.DefaultCollision(acceptableStart, TaskDependencyConstraint.Collision.START_LATER_VARIATION, isActive);
        return result;
    }


}
