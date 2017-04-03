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
public class StartFinishConstraintImpl extends ConstraintImpl implements TaskDependencyConstraint {
    public StartFinishConstraintImpl() {
        super(GanttTaskRelationship.SF, GanttLanguage.getInstance().getText("startfinish"));
    }

    public TaskDependencyConstraint.Collision getCollision() {
        TaskDependencyConstraint.Collision result = null;
        Task dependee = getDependency().getDependee();
        Task dependant = getDependency().getDependant();
        GanttCalendar dependeeStart = dependee.getStart();
        GanttCalendar dependantEnd = dependant.getEnd();
        //
        boolean isActive = dependant.isStartFixed() ?
                dependantEnd.compareTo(dependeeStart)<0 :
                dependantEnd.compareTo(dependeeStart)!=0;
        GanttCalendar acceptableStart = dependant.getStart();
        if (isActive) {
            acceptableStart = dependeeStart.newAdd((int)-dependant.getDuration().getLength());
        }
        result = new TaskDependencyConstraint.DefaultCollision(acceptableStart, TaskDependencyConstraint.Collision.START_LATER_VARIATION, isActive);
        return result;
    }
}
