/*
 * Created on 05.07.2003
 *
 */
package net.sourceforge.ganttproject.task;

import net.sourceforge.ganttproject.GanttTask;
//import net.sourceforge.ganttproject.task.dependency.TaskDependency;
import net.sourceforge.ganttproject.task.dependency.TaskDependencyCollection;
import net.sourceforge.ganttproject.task.dependency.TaskDependencyConstraint;
import net.sourceforge.ganttproject.task.algorithm.AlgorithmCollection;
import net.sourceforge.ganttproject.task.event.TaskListener;
import net.sourceforge.ganttproject.time.TimeUnit;
import net.sourceforge.ganttproject.time.TimeUnitManager;

/**
 * @author bard
 *
 */
public interface TaskManager {
    public Task getRootTask();
    public void clear();
    public GanttTask getTask(int taskId);
    public void registerTask(Task task);
    public GanttTask createTask();
    public GanttTask createTask(int taskId);
    public TimeUnitManager getTimeUnitManager();
    TaskLength createLength(TimeUnit unit, long count);

    TaskDependencyCollection getDependencyCollection();
    AlgorithmCollection getAlgorithmCollection();

    TaskDependencyConstraint createConstraint(int constraintID);

    void addTaskListener(TaskListener listener);
    
    public class Access {
    	public static TaskManager getInstance() {
    		return ourInstance;
    	}

        public static TaskManager newInstance() {
            return new TaskManagerImpl();
        }

        public static TaskManager newInstance(TaskContainmentHierarchyFacade.Factory containmentFacadeFactory, TaskManagerConfig config) {
            return new TaskManagerImpl(containmentFacadeFactory, config);
        }

    	private static TaskManager ourInstance = newInstance();
    }
}
