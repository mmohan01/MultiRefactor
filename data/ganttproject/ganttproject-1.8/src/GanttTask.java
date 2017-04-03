
/**
  * @author THOMAS Alexandre
  * @author AUDRU Cedric
  */


import java.lang.String;
import java.lang.Object;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.tree.*;
import java.io.*;

/**
  * Class that generate a task
  */
public class GanttTask implements Serializable
{

	/** Notes of the task */
	private String notes;

	/** Begining date of the task */
	private GanttCalendar start;

	/* Duration of the task. */
	private int length;

	/** Name of the task. */
	private String name;

	/** Depend list of the task */
	private ArrayList depend = new ArrayList();

	/** State between 0% and 100%*/
	private int percent;
	
	/**Is it a meeting point( false by default) */
	private boolean bilan;

/////////////////////////////////////////////////////////////////////////////////


	/** Constructor */
	public GanttTask(String name, GanttCalendar start, int length)
	{
		this.name = name;
		this.start = start;
		this.length = length;
		this.percent = 0;
		this.bilan = false;
	}


	/** Return the name. */
	public String toString () { return name; }

	/** Return the duration */
	public int getLength() { return length; }

	/** Return the begining */
	public GanttCalendar getStart() { return start; }
	
	/** Return the end date */
	public GanttCalendar getEnd() 
	{ 
		GanttCalendar end = start.Clone();
		end.add(length);
		return end; 
		//return start.newAdd(length-1);
	}

	/** Return the notes */
	public String getNotes () { return notes; }

	/** Return the state */
	public int getPercent () { return percent; }

	/** Return the list of depends */
	public ArrayList getDepend () { return depend; }

	/** Is it a meeting point?? */
	public boolean getBilan() { return bilan; }


	/** Change the name */
	public void setName (String name) { this.name = name; }

	/** Change the duration */
	public void setLength (int l) { length = l; }

	/** Change the begining */
	public void setStart (GanttCalendar s) { start = s; }

	/** Change notes */
	public void setNotes (String note) { notes = note; }

	/** Change state */
	public void setPercent (int percent) { this.percent = percent; }

	/** Change meeting point */
	public void setBilan (boolean b) { bilan = b; }
	
	/** Add new task on depends list */
	public void setDepend ( String t ) { depend.add(t); }

	/** Clear depends list */
	public void clearDepend() { depend.clear(); };
	
	
	/** This function look on the array if the task named "t" is in. 
	  * In this case, replace by a new name "n". */
	 public void checkDepend (String t, String n)
	 {
	 		for(int i=0;i<depend.size();i++)
				if(depend.get(i).toString() == t)
				{
					depend.remove(i);
					depend.add(n);
				}
	 }

	/**
	  * This function calculate the begin of a task in function of the sub taks.
	  */
	public void refreshDateAndAdvancement (GanttTree tree)
	{
		if(tree.getNode(name).isRoot()) return ;
		ArrayList l = tree.getAllChildTask(name);
		if(l.size()==0) return;

		GanttTask taskMin = (GanttTask) ((DefaultMutableTreeNode)l.get(0)).getUserObject();
		GanttTask taskMax = (GanttTask) ((DefaultMutableTreeNode)l.get(0)).getUserObject();


		int sumDay=taskMin.length;;
		float sum=(float)taskMin.length*(float)taskMin.percent/100;;

		for(int i=1;i<l.size();i++)
		{
			GanttTask task = (GanttTask) ((DefaultMutableTreeNode)l.get(i)).getUserObject();

			//Search for the min task
			if(taskMin.start.compareTo(task.start)==1)
				taskMin = task;

			//Search for the max task
			if(taskMax.start.newAdd(taskMax.length).compareTo(task.start.newAdd(task.length))==-1)
				taskMax = task;

			//Calcul the state
			if(!task.bilan)
			{
				sumDay+=task.length;
				sum+=(float)task.length*(float)task.percent/100;
			}

		}

		//Change the state
		this.percent = (int)(sum*100/(float)sumDay);

		this.setStart(taskMin.start.Clone());
		this.setLength(taskMin.start.diff(taskMax.start.newAdd(taskMax.length)));
	}
}

