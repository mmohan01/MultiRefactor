/*
 * Created on 05.07.2003
 *
 */
package net.sourceforge.ganttproject.task;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.ganttproject.GanttCalendar;
import net.sourceforge.ganttproject.GanttTask;
import net.sourceforge.ganttproject.GanttTaskRelationship;
import net.sourceforge.ganttproject.task.algorithm.AdjustTaskBoundsAlgorithm;
import net.sourceforge.ganttproject.task.algorithm.AlgorithmCollection;
import net.sourceforge.ganttproject.task.algorithm.FindPossibleDependeesAlgorithm;
import net.sourceforge.ganttproject.task.algorithm.FindPossibleDependeesAlgorithmImpl;
import net.sourceforge.ganttproject.task.algorithm.RecalculateTaskCompletionPercentageAlgorithm;
import net.sourceforge.ganttproject.task.algorithm.RecalculateTaskScheduleAlgorithm;
import net.sourceforge.ganttproject.task.dependency.EventDispatcher;
import net.sourceforge.ganttproject.task.dependency.TaskDependency;
import net.sourceforge.ganttproject.task.dependency.TaskDependencyCollection;
import net.sourceforge.ganttproject.task.dependency.TaskDependencyCollectionImpl;
import net.sourceforge.ganttproject.task.dependency.TaskDependencyConstraint;
import net.sourceforge.ganttproject.task.dependency.constraint.FinishFinishConstraintImpl;
import net.sourceforge.ganttproject.task.dependency.constraint.FinishStartConstraintImpl;
import net.sourceforge.ganttproject.task.dependency.constraint.StartFinishConstraintImpl;
import net.sourceforge.ganttproject.task.dependency.constraint.StartStartConstraintImpl;
import net.sourceforge.ganttproject.task.event.TaskDependencyEvent;
import net.sourceforge.ganttproject.task.event.TaskHierarchyEvent;
import net.sourceforge.ganttproject.task.event.TaskListener;
import net.sourceforge.ganttproject.task.event.TaskScheduleEvent;
import net.sourceforge.ganttproject.task.hierarchy.TaskHierarchyManagerImpl;
import net.sourceforge.ganttproject.time.TimeUnit;
import net.sourceforge.ganttproject.time.TimeUnitManager;
import net.sourceforge.ganttproject.time.TimeUnitManagerImpl;
import net.sourceforge.ganttproject.time.gregorian.GregorianCalendar;

/**
 * @author bard
 *
 */
public class TaskManagerImpl implements TaskManager {
    private final TimeUnitManager myTimeUnitManager;
    private final TaskHierarchyManagerImpl myHierarchyManager;
    private final TaskDependencyCollectionImpl myDependencyCollection;
    private final AlgorithmCollection myAlgorithmCollection;
    private final List myListeners = new ArrayList();
    private int myMaxID = -1;
    private Map myId2task = new HashMap();
    private Task myRoot;
    private final TaskManagerConfig myConfig;


    TaskManagerImpl(TaskContainmentHierarchyFacade.Factory containmentFacadeFactory, TaskManagerConfig config) {
        myConfig = config;
        myTimeUnitManager = new TimeUnitManagerImpl();
        myHierarchyManager = new TaskHierarchyManagerImpl();
        final TaskContainmentHierarchyFacade.Factory factory =
                containmentFacadeFactory==null ?
                new FacadeFactoryImpl() : containmentFacadeFactory;
        myDependencyCollection = new TaskDependencyCollectionImpl(new EventDispatcher() {
            public void fireDependencyAdded(TaskDependency dep) {
                TaskManagerImpl.this.fireDependencyAdded(dep);
            }

            public void fireDependencyRemoved(TaskDependency dep) {
                TaskManagerImpl.this.fireDependencyRemoved(dep);
            }
        });

        FindPossibleDependeesAlgorithm alg1 = new FindPossibleDependeesAlgorithmImpl() {
            protected TaskContainmentHierarchyFacade createContainmentFacade() {
                return factory.createFacede();
            }

        };
        AdjustTaskBoundsAlgorithm alg3 = new AdjustTaskBoundsAlgorithm() {
            protected TaskContainmentHierarchyFacade createContainmentFacade() {
                return factory.createFacede();
            }
        };
        RecalculateTaskScheduleAlgorithm alg2 = new RecalculateTaskScheduleAlgorithm(alg3) {
            protected TaskContainmentHierarchyFacade createContainmentFacade() {
                return factory.createFacede();
            }
        };
        RecalculateTaskCompletionPercentageAlgorithm alg4 = new RecalculateTaskCompletionPercentageAlgorithm() {
            protected TaskContainmentHierarchyFacade createContainmentFacade() {
                return factory.createFacede();
            }
        };
        myAlgorithmCollection = new AlgorithmCollection(alg1, alg2, alg3, alg4);
        clear();
    }

    TaskManagerImpl() {
        this(null, new TaskManagerConfig() {
            public Color getDefaultColor() {
                return TaskImpl.DEFAULT_COLOR;
            }
        });
    }


    /* (non-Javadoc)
     * @see net.sourceforge.ganttproject.task.TaskManager#getTask(int)
     */
    public GanttTask getTask(int taskId) {
        return (GanttTask)myId2task.get(new Integer(taskId));
    }

    public Task getRootTask() {
        if (myRoot==null) {
            Date today = GregorianCalendar.getInstance().getTime();
            myRoot =  new GanttTask(null, new GanttCalendar(today), 1, this);
            myRoot.setStart(new GanttCalendar(GregorianCalendar.getInstance().getTime()));
            myRoot.setDuration(createLength(getTimeUnitManager().getTimeUnit(0), 1));
			myRoot.setExpand(true);
        }
        return myRoot;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.ganttproject.task.TaskManager#clear()
     */
    public void clear() {
        myId2task.clear();
		setMaxID(-1);
        myDependencyCollection.clear();
        myRoot = null;
    }


    public GanttTask createTask() {
        GanttTask result = createTask(getMaxID());
        if (getMaxID()<=result.getTaskID()) {
            increaseMaxID();
        }
        return result;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.ganttproject.task.TaskManager#createTask(int)
     */
    public GanttTask createTask(int taskId) {
        GanttTask result = new GanttTask("", new GanttCalendar(), 1, this);
        result.setTaskID(taskId);
        result.move(getRootTask());
        fireTaskAdded(result);
		return result;
    }

    /* (non-Javadoc)
     * @see net.sourceforge.ganttproject.task.TaskManager#registerTask(net.sourceforge.ganttproject.GanttTask)
     */
    public void registerTask(Task task) {
        int taskID = task.getTaskID();
        if (myId2task.get(new Integer(taskID)) == null) { //if the taskID is not in the map
          myId2task.put(new Integer(taskID), task);
          if (getMaxID() < taskID) {
            setMaxID(taskID+1);
          }
        }
        else { //taskID has been in the map. the newTask will not be added
            throw new RuntimeException("There is a task that already has the ID " + taskID);
        }
    }

    /* (non-Javadoc)
     * @see net.sourceforge.ganttproject.task.TaskManager#setTask(net.sourceforge.ganttproject.GanttTask)
     */
    public void setTask(Task task) {
    	int taskID = task.getTaskID();
		myId2task.put(new Integer(taskID), task);
		if (taskID > getMaxID()) {
		  setMaxID(taskID);
		}
    }

    public TimeUnitManager getTimeUnitManager() {
        return myTimeUnitManager;
    }

    public TaskLength createLength(TimeUnit unit, long count) {
        return new TaskLengthImpl(unit, count);
    }

    public TaskDependencyCollection getDependencyCollection() {
        return myDependencyCollection;
    }

    public AlgorithmCollection getAlgorithmCollection() {
        return myAlgorithmCollection;
    }

    public TaskHierarchyManagerImpl getHierarchyManager() {
        return myHierarchyManager;
    }
    public TaskDependencyConstraint createConstraint(final int constraintID) {
        TaskDependencyConstraint result;
        switch (constraintID) {
            case GanttTaskRelationship.FS: {
                result = new FinishStartConstraintImpl();
                break;
            }
            case GanttTaskRelationship.FF: {
                result = new FinishFinishConstraintImpl();
                break;
            }
            case GanttTaskRelationship.SF: {
                result = new StartFinishConstraintImpl();
                break;
            }
            case GanttTaskRelationship.SS: {
                result = new StartStartConstraintImpl();
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown constraint ID="+constraintID);
            }
        }
        return result;
    }

    public int getMaxID() {
        return myMaxID;
    }

    private void setMaxID(int id) {
        myMaxID = id;
    }

    void increaseMaxID() {
    	myMaxID++;
    }

    public void addTaskListener(TaskListener listener) {
        myListeners.add(listener);
    }

    void fireTaskScheduleChanged(Task changedTask, GanttCalendar oldStartDate, GanttCalendar oldFinishDate) {
        TaskScheduleEvent e = new TaskScheduleEvent(changedTask, oldStartDate, oldFinishDate, changedTask.getStart(), changedTask.getEnd());
        //List copy = new ArrayList(myListeners);
        //myListeners.clear();
        for (int i=0; i<myListeners.size(); i++) {
            TaskListener next = (TaskListener) myListeners.get(i);
            next.taskScheduleChanged(e);
        }
    }

    private void fireDependencyAdded(TaskDependency newDependency) {
        TaskDependencyEvent e = new TaskDependencyEvent(getDependencyCollection(), newDependency);
        for (int i=0; i<myListeners.size(); i++) {
            TaskListener next = (TaskListener) myListeners.get(i);
            next.dependencyAdded(e);
        }
    }


    private void fireDependencyRemoved(TaskDependency dep) {
        TaskDependencyEvent e = new TaskDependencyEvent(getDependencyCollection(), dep);
        for (int i=0; i<myListeners.size(); i++) {
            TaskListener next = (TaskListener) myListeners.get(i);
            next.dependencyRemoved(e);
        }
    }

    private void fireTaskAdded(Task task) {
        TaskHierarchyEvent e = new TaskHierarchyEvent(this, task, null, task.getSupertask());
        for (int i=0; i<myListeners.size(); i++) {
            TaskListener next = (TaskListener) myListeners.get(i);
            next.taskAdded(e);
        }
    }

    private void fireTaskRemoved(Task task, Task oldSupertask) {
        TaskHierarchyEvent e = new TaskHierarchyEvent(this, task, oldSupertask, null);
        for (int i=0; i<myListeners.size(); i++) {
            TaskListener next = (TaskListener) myListeners.get(i);
            next.taskRemoved(e);
        }
    }

    public TaskManagerConfig getConfig() {
        return myConfig;
    }

    private final class FacadeImpl implements TaskContainmentHierarchyFacade {
        private final Task myRoot;

        public FacadeImpl(Task root) {
            myRoot = root;
        }

        public Task[] getNestedTasks(Task container) {
            return container.getNestedTasks();
        }

        public Task getRoot() {
            return myRoot;
        }

        public Task getContainer(Task nestedTask) {
            return nestedTask.getSupertask();
        }
    }

    private class FacadeFactoryImpl implements TaskContainmentHierarchyFacade.Factory {
        public TaskContainmentHierarchyFacade createFacede() {
            return new FacadeImpl(TaskManagerImpl.this.myRoot);
        }

    }

}
