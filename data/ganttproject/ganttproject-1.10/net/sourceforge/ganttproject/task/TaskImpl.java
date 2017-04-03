package net.sourceforge.ganttproject.task;

import net.sourceforge.ganttproject.GanttCalendar;
import net.sourceforge.ganttproject.GanttTaskRelationship;
import net.sourceforge.ganttproject.shape.ShapePaint;
import net.sourceforge.ganttproject.shape.ShapeConstants;
import net.sourceforge.ganttproject.task.dependency.*;
import net.sourceforge.ganttproject.task.hierarchy.TaskHierarchyItem;
import net.sourceforge.ganttproject.time.TimeUnitManager;
//import net.sourceforge.ganttproject.resource.ProjectResource;

import java.util.List;
import java.util.ArrayList;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * @author bard
 * Date: 31.01.2004
 */
public class TaskImpl implements Task {
    private int myID;
    private final TaskManagerImpl myManager;
    private String myName;
    private String myWebLink = new String("http://");
    private boolean isMilestone;
    private int myPriority;
    private GanttCalendar myStart;
    private GanttCalendar myEnd;
    private int myCompletionPercentage;
    private TaskLength myLength;
    private boolean isStartFixed;

	private boolean bExpand;
    
    private TimeUnitManager myTimeUnitManager;
    //private final TaskDependencyCollection myDependencies = new TaskDependencyCollectionImpl();
    private final ResourceAssignmentCollectionImpl myAssignments;
    private final TaskDependencySlice myDependencySlice;
    private final TaskDependencySlice myDependencySliceAsDependant;
    private final TaskDependencySlice myDependencySliceAsDependee;
    private boolean myEventsEnabled;
    private final TaskHierarchyItem myTaskHierarchyItem;
    private ShapePaint myShape;
    private Color myColor;
    private String myNotes;


    protected TaskImpl(TaskManager taskManager) {
        myManager = (TaskManagerImpl) taskManager;
        myID = myManager.getMaxID();
        myManager.increaseMaxID();
        myTimeUnitManager = myManager.getTimeUnitManager();
        myAssignments = new ResourceAssignmentCollectionImpl(this);
        myDependencySlice = new TaskDependencySliceImpl(this, myManager.getDependencyCollection());
        myDependencySliceAsDependant = new TaskDependencySliceAsDependant(this, myManager.getDependencyCollection());
        myDependencySliceAsDependee = new TaskDependencySliceAsDependee(this, myManager.getDependencyCollection());
        myPriority = 1;
        myTaskHierarchyItem = myManager.getHierarchyManager().createItem(this);
        isStartFixed = false;
        myNotes = "";
		bExpand = true;
        myColor=null;
    }

    protected TaskImpl(TaskImpl copy) {
        myManager = copy.myManager;
        myAssignments = copy.myAssignments.copy();
        myID = copy.myID;
        myName = copy.myName;
        myWebLink = copy.myWebLink;
        isMilestone = copy.isMilestone;
        myPriority = copy.myPriority;
        myStart = copy.myStart;
        myEnd = copy.myEnd;
        myCompletionPercentage = copy.myCompletionPercentage;
        myLength = copy.myLength;
        isStartFixed = copy.isStartFixed;
        myShape = copy.myShape;
        myColor = copy.myColor;
        myNotes = copy.myNotes;
		bExpand = copy.bExpand;
        //
        myTimeUnitManager = copy.myTimeUnitManager;
        myDependencySlice = copy.myDependencySlice;
        myDependencySliceAsDependant = copy.myDependencySliceAsDependant;
        myDependencySliceAsDependee = copy.myDependencySliceAsDependee;
        myTaskHierarchyItem = myManager.getHierarchyManager().createItem(this);
    }

    public TaskMutator createMutator() {
        return new MutatorImpl();
    }

    // main properties
    public int getTaskID() {
        return myID;
    }

    public String getName() {
        return myName;
    }
    
    public String getWebLink() {
        return myWebLink;
    }

    public boolean isMilestone() {
        return isMilestone;
    }

    public int getPriority() {
        return myPriority;
    }

    public GanttCalendar getStart() {
        return myStart;
    }

    public GanttCalendar getEnd() {
        if (myEnd==null) {
            myEnd = getStart().Clone();
            myEnd.add((int) getDuration().getLength());
        }
        return myEnd;
    }

    public TaskLength getDuration() {
        //System.err.println("[TaskImp] this="+this+" duration="+myLength+" id="+myID);
        return myLength;
    }

    public int getCompletionPercentage() {
        return myCompletionPercentage;
    }

    public boolean isStartFixed() {
        return isStartFixed;
    }

	public boolean getExpand(){
    	return bExpand;
    }

    public ShapePaint getShape() {
        return myShape==null ?
                new ShapePaint(ShapeConstants.BACKSLASH, getColor() , getColor()) :
                myShape;
    }

    public Color getColor() {
        return myColor==null ? myManager.getConfig().getDefaultColor() : myColor;
    }

    public String getNotes() {
        return myNotes;
    }

    public GanttTaskRelationship[] getPredecessors() {
        return new GanttTaskRelationship[0];  //To change body of implemented methods use Options | File Templates.
    }

    public GanttTaskRelationship[] getSuccessors() {
        return new GanttTaskRelationship[0];  //To change body of implemented methods use Options | File Templates.
    }

    public ResourceAssignment[] getAssignments() {
        return myAssignments.getAssignments();
    }

    public ResourceAssignmentCollection getAssignmentCollection() {
        return myAssignments;
    }

    //
    public Task getSupertask() {
        TaskHierarchyItem container = myTaskHierarchyItem.getContainerItem();
        return container.getTask();
    }

    public Task[] getNestedTasks() {
        TaskHierarchyItem[] nestedItems = myTaskHierarchyItem.getNestedItems();
        Task[] result = new Task[nestedItems.length];
        for (int i=0; i<nestedItems.length; i++) {
            result[i] = nestedItems[i].getTask();
        }
        return result;
    }

    public void move(Task targetSupertask) {
        TaskImpl supertaskImpl = (TaskImpl) targetSupertask;
        TaskHierarchyItem targetItem = supertaskImpl.myTaskHierarchyItem;
        myTaskHierarchyItem.delete();
        targetItem.addNestedItem(myTaskHierarchyItem);
    }

    public TaskDependencySlice getDependencies() {
        return myDependencySlice;
    }

    public TaskDependencySlice getDependenciesAsDependant() {
        return myDependencySliceAsDependant;
    }

    public TaskDependencySlice getDependenciesAsDependee() {
        return myDependencySliceAsDependee;
    }

    public TaskManager getManager() {
        return myManager;
    }

    //TODO: remove this hack. ID must never be changed
    protected void setTaskIDHack(int taskID) {
        myID = taskID;
    }

    protected TimeUnitManager getTimeUnitManager() {
        return myTimeUnitManager;
    }

    private class MutatorImpl implements TaskMutator {
        private final List myCommands = new ArrayList();
        public void commit() {
            for (int i=0; i<myCommands.size(); i++) {
                Runnable next = (Runnable) myCommands.get(i);
                next.run();
            }
            myCommands.clear();
        }

        public void setName(final String name) {
            myCommands.add(new Runnable() {
                public void run() {
                    TaskImpl.this.setName(name);
                }
            });
        }

        public void setMilestone(final boolean milestone) {
            myCommands.add(new Runnable() {
                public void run() {
                    TaskImpl.this.setMilestone(milestone);
                }
            });
        }

        public void setPriority(final int priority) {
            myCommands.add(new Runnable() {
                public void run() {
                    TaskImpl.this.setPriority(priority);
                }
            });
        }

        public void setStart(final GanttCalendar start) {
            myCommands.add(new Runnable() {
                public void run() {
                    TaskImpl.this.setStart(start);
                }
            });
        }

        public void setEnd(final GanttCalendar end) {
            myCommands.add(new Runnable() {
                public void run() {
                    TaskImpl.this.setEnd(end);
                }
            });
        }

        public void setDuration(final TaskLength length) {
            myCommands.add(new Runnable() {
                public void run() {
                    TaskImpl.this.setDuration(length);
                }
            });
        }

		public void setExpand(final boolean expand) {
            myCommands.add(new Runnable() {
                public void run() {
                    TaskImpl.this.setExpand(expand);
                }
            });
        }
    
        public void setCompletionPercentage(final int percentage) {
            myCommands.add(new Runnable() {
                public void run() {
                    TaskImpl.this.setCompletionPercentage(percentage);
                }
            });
        }

        public void setStartFixed(final boolean isFixed) {
            myCommands.add(new Runnable() {
                public void run() {
                    TaskImpl.this.setStartFixed(isFixed);
                }
            });
        }

        public void setShape(final ShapePaint shape) {
            myCommands.add(new Runnable() {
                public void run() {
                    TaskImpl.this.setShape(shape);
                }
            });
        }

        public void setColor(final Color color) {
            myCommands.add(new Runnable() {
                public void run() {
                    TaskImpl.this.setColor(color);
                }
            });
        }

        public void setNotes(final String notes) {
            myCommands.add(new Runnable() {
                public void run() {
                    TaskImpl.this.setNotes(notes);
                }
            });
        }

        public void addNotes(final String notes) {
            myCommands.add(new Runnable() {
                public void run() {
                    TaskImpl.this.addNotes(notes);
                }
            });
        }

    }

    public void setName(String name) {

        myName = name;
    }
    public void setWebLink(String webLink) {

        myWebLink = webLink;
    }
    public void setMilestone(boolean milestone) {
        isMilestone = milestone;
    }

    public void setPriority(int priority) {
        myPriority = priority;
    }

    public void setStart(GanttCalendar start) {
        GanttCalendar oldStart = myStart==null ? null : myStart.Clone();
        myStart = start;
        if (areEventsEnabled()) {
            myManager.fireTaskScheduleChanged(this, oldStart, getEnd());
        }
    }

    public void setEnd(GanttCalendar end) {
        GanttCalendar oldFinish = myEnd==null ? null : myEnd.Clone();
        myEnd = end;
        int length = myStart.diff(end);
        myLength = getManager().createLength(myLength.getTimeUnit(), length);
        if (areEventsEnabled()) {
            myManager.fireTaskScheduleChanged(this, myStart.Clone(), oldFinish);
        }
    }

    public void setDuration(TaskLength length) {
        GanttCalendar oldFinish = myEnd==null ? null : myEnd.Clone();
        myLength = length;
        myEnd = myStart.newAdd((int) length.getLength());
        if (areEventsEnabled()) {
            myManager.fireTaskScheduleChanged(this, myStart.Clone(), oldFinish);
        }
    }

    public void setCompletionPercentage(int percentage) {
        myCompletionPercentage = percentage;
    }

    public void setStartFixed(boolean isFixed) {
        isStartFixed = isFixed;
    }

    public void setShape(ShapePaint shape) {
        myShape = shape;
    }

    public void setColor(Color color) {
        myColor = color;
    }

    public void setNotes(String notes) {
        myNotes = notes;
	}
    
    public void setExpand(boolean expand){
    	bExpand = expand;
    }

    public void addNotes(String notes) {
        myNotes += notes;
    }

    protected void enableEvents(boolean enabled) {
        myEventsEnabled = enabled;
    }

    protected boolean areEventsEnabled() {
        return myEventsEnabled;
    }

    /**
     * Allows to determine, if a special shape is defined for this task.
     * @return true, if this task has its own shape defined.
     */
    public boolean shapeDefined()
    {
      return (myShape != null);
    }

    /**
     * Allows to determine, if a special color is defined for this task.
     * @return true, if this task has its own color defined.
     */

    public boolean colorDefined() {

      return (myColor != null);

    }

}
