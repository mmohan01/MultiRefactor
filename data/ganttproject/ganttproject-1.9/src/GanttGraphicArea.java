/***************************************************************************
                           GanttGraphicArea.java  -  description
                             -------------------
    begin                : dec 2002
    copyright            : (C) 2002 by Thomas Alexandre
    email                : alex.thomas@netcourrier.com
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/


import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.lang.String;
import javax.swing.tree.*;
import java.util.ArrayList;
import java.lang.Math;
import java.awt.print.*;
import java.lang.Exception;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import java.io.File;
//import java.io.File;
import javax.swing.*;


/**
  * Classe for the graphic part of the soft
  */
public class GanttGraphicArea extends JPanel implements Printable
{

	/** Begin of display. */
	public GanttCalendar date, olddate;

	/** Reference to the GanttTree */
	public GanttTree tree;

	
	
	/** Zoom  to on week */
	public final int ONE_WEEK=0;     
	
	/** Zoom  to two week */
	public final int TWO_WEEK=1;     
	
	/** Zoom  to one month */
	public final int ONE_MONTH=2;     
	
	/** Zoom  to two month */
	public final int TWO_MONTH=3;     
	
	/** Zoom  to tree month */
	public final int THREE_MONTH=4; 
	
	/** Zoom  to six month */
	public final int SIX_MONTH=5;     
	
	/** Zoom  to one year */
	public final int ONE_YEAR=6;     
	
	/** Zoom  to two year */
	public final int TWO_YEAR=7;     
	
	/** Zoom  to three year */
	public final int THREE_YEAR=8;     
 
	/* The Zoom Value */
	private int zoomValue;

	/** Array to store parameters of each task (for display) */
	private ArrayList listOfParam = new ArrayList();

	/** This value is connected to the GanttTRee Scrollbar to move up or down */
	private int margY;

	/** UpperLeft Point (for the margin of printing) */ 
	private Point upperLeft=new Point(0,0);

	/** Render on the window or for printing*/
	private boolean printRendering=false;
	
	/** Render the depends */
	private boolean drawdepend = true;
	/** Render the depends */
	private boolean drawPercent = true;
	/* Render the name*/
	private boolean drawName = false;
	/* Render the ganttproject version*/
	private boolean drawVersion = false;

	/** The language */
	private GanttLanguage language;

	/*! The main application */
	private GanttProject appli;

	/*! Old X and Yposition*/ 
	private int oldX, oldY;
	
	/** Move the view of the calendar or move a task*/
	private boolean moveView=true;
	
	/** the Task number to move */
	private int moveTask=-1;
	
	/** Cursor by default*/
	boolean curs=false;
	
	/** the 2nd task to drag the depend */
	int drag=-1;
	
	/** Type of selection 0 -> move duration,   1 -> moveDate,   2->add Depend*/
	int typeSeletion;
	
	/** Parmeters to change the duration of the task*/
	private int storeTaskLength;
	private float addTaskLength;
	private GanttCalendar storeTaskStart;

	/** The arrow between two task for depend */
	private Arrow arrow=new Arrow();
	
	private Notes notes = new Notes();
	
	/** The task to move duration*/
	private GanttTask taskToMove=new GanttTask("toto",new GanttCalendar(),10);
	
	/**Color array use to paint */
	private Color [] arrayColor = new Color[15];
	
	/** List of task recup before painting */
	private ArrayList listOfTask;
	
	/** Begin of project */
	public GanttCalendar beg = new GanttCalendar();
	
	/**End date for the project */
	public GanttCalendar end = new GanttCalendar();
	

	/** Constructor */
	public GanttGraphicArea (GanttProject app, GanttTree ttree, GanttLanguage language)
	{
		date = new GanttCalendar();
		olddate=new GanttCalendar();
		date.setDay(1);
		this.tree = ttree;
		zoomValue = ONE_MONTH;	//zoom to one month by default of the current date
		
		margY = 0;
		this.language = language;
		appli = app;

		//Listener on wheel mouse
		this.addMouseWheelListener(new MouseWheelListener (){
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				if(e.getWheelRotation()>0)zoomMore();
				else if(e.getWheelRotation()<0)zoomLess();
				
				if(zoomValue<0) zoomValue=0;
				if(zoomValue>8) zoomValue=8;

				zoomToBegin();

				appli.bZoomIn.setEnabled(zoomValue==0?false:true);
				appli.bZoomOut.setEnabled(zoomValue==8?false:true);

				repaint();
			}
		});


		//Listener on a mouse click
		this.addMouseListener(new MouseListener (){
			public void mouseClicked(MouseEvent e){}
          	public void mouseEntered(MouseEvent e){}
          	public void mouseExited(MouseEvent e){}
          	public void mousePressed(MouseEvent e)
			{
				/*if(e.getButton()== MouseEvent.BUTTON3)
					tree.createPopupMenu(e.getX(), e.getY()) ;
				else */if(e.getButton()== MouseEvent.BUTTON1)
				{
					oldX = e.getX();
					oldY = e.getY();
					
					moveView=true;		//move ok
					
					drag = detectPosition(e.getX(), e.getY(),true);
					if(drag!=-1)
						tree.selectTreeRow(drag);
						
					moveTask = detectPosition(e.getX(), e.getY(),false);
					
					if(moveTask!=-1)
					{
						
						GanttPaintParam param = (GanttPaintParam)listOfParam.get(moveTask);
						String taskName = param.name;
						for(int i=0;i<listOfTask.size();i++)
							if(listOfTask.get(i).toString().equals(taskName))
								taskToMove = (GanttTask) ((DefaultMutableTreeNode)listOfTask.get(i)).getUserObject();
						moveView=false;
						
						addTaskLength=0;
						storeTaskLength = taskToMove.getLength();
						
						if(typeSeletion==0)
						{
							setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));	
						} else if(typeSeletion==1){
							setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));	
							storeTaskStart = taskToMove.getStart().Clone();
						}
					}
					else setCursor(new Cursor(Cursor.HAND_CURSOR));
					repaint();
				}
			}
			//Release the mouse button
          	public void mouseReleased(MouseEvent e)
			{
				if(e.getButton()== MouseEvent.BUTTON1)
				{
				
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				
					if(moveView)
					{
						int f = 1;
						int x = oldX - e.getX();
						if(x>2)f=-1;
						else if(x<-2)f=1;
						else {//If the move is too short, do nothing and return
							drag=-1;arrow.setDraw(false);
							notes.setDraw(false);
							repaint(); return;
						}	
		
						switch(zoomValue)
						{
							case ONE_WEEK:case TWO_WEEK:date.go(Calendar.WEEK_OF_YEAR,1*f); break;
							case ONE_MONTH:case TWO_MONTH:case THREE_MONTH:case SIX_MONTH:date.go(Calendar.MONTH,1*f); break;
							case ONE_YEAR:case TWO_YEAR:case THREE_YEAR:date.go(Calendar.YEAR,1*f);break;
						}
					}
					
					if(arrow.getDraw() && typeSeletion==2)
					{
						//Search for the second task to link
						int second = detectPosition(e.getX(), e.getY(),true);
						
						//if a task has been found
						if(second!=-1)
						{
							GanttPaintParam param = (GanttPaintParam)listOfParam.get(second);
							String taskName = param.name;
							//if(!taskToMove.toString().equals(taskName) && 
							//	!(tree.getTask(taskName).getDepend().contains(taskToMove.toString())))
							if(tree.checkDepend(taskToMove,taskName))
							{
								taskToMove.setDepend(taskName);
								tree.refreshAllChild(taskName);
								tree.refreshAllFather(taskName);
							}
						}
					}
					
					arrow.setDraw(false);
					notes.setDraw(false);
					moveTask=-1;
					drag=-1;
					
					tree.refreshAllChild(taskToMove.toString());
					tree.refreshAllFather(taskToMove.toString());
					
					repaint();
					
				}
			}
		});
		
		
		/** When mouse motion on widget */
		this.addMouseMotionListener(new MouseMotionListener(){		
			public void mouseDragged(MouseEvent e) {	
				
					if(moveTask>=0)
					{
						int diff = e.getX() - oldX;					
						float gra = (float)getWidth()/(float)getGranit(true);
						addTaskLength+=(float)diff/gra;
					
						//Change duration of task
						if(typeSeletion==0)
						{
							//change the note
							GanttCalendar enddate= taskToMove.getEnd().newAdd(-1);
							notes.setString(enddate.toString());
							notes.setX(e.getX());
							
							//change the duration of the selected task
							int newDuration = storeTaskLength+(int)(addTaskLength);
							if(newDuration<=0) newDuration=1;
							taskToMove.setLength(newDuration);	
							tree.refreshAllChild(taskToMove.toString());
							tree.refreshAllFather(taskToMove.toString());
							oldX = e.getX();
						} 
						//change the start date of task
						/*else if(typeSeletion==1){
							int taskDuration=storeTaskLength-(int)addTaskLength;
							if(taskDuration>=1)taskToMove.setStart(storeTaskStart.newAdd((int)addTaskLength));
							if(taskDuration<=0) taskDuration=1;
							taskToMove.setLength(taskDuration);
							refreshAllFather();
							oldX = e.getX();
							
							//change the note
							notes.setString(taskToMove.getStart().toString());
							notes.setX(e.getX());
						}*/
						else if(typeSeletion==1){
							if(addTaskLength>0)
							{
								int taskDuration=storeTaskLength-(int)addTaskLength;
								if(taskDuration>=1)taskToMove.setStart(storeTaskStart.newAdd((int)addTaskLength));
								if(taskDuration<=0) taskDuration=1;
								taskToMove.setLength(taskDuration);
								tree.refreshAllChild(taskToMove.toString());
								tree.refreshAllFather(taskToMove.toString());
								
								
								//change the note
								notes.setString(taskToMove.getStart().toString());
								notes.setX(e.getX());
							}
							oldX = e.getX();
						}
						//move the arrow
						else if(typeSeletion==2)
						{
							arrow.changePoint2(e.getX(), e.getY());	
							drag=-1;
							for(int i=0;i<listOfParam.size()&& drag==-1;i++)
							{
								GanttPaintParam param = (GanttPaintParam)listOfParam.get(i);	
								int y = param.y*20+40-margY;
								int x1 = param.x1;
								int x2 = param.x2;
								if((e.getY()>=y && e.getY()<=y+12) && (e.getX()>x1+3 && e.getX()<x2-3))
									drag=i;
							}
						}
						repaint();	
					} 
			}

			//Move the move on the area
			public void mouseMoved(MouseEvent e) 
			{
				int detect = detectPosition(e.getX(), e.getY(),false);
				if(detect==-1) 
				{
					if(curs){setCursor(new Cursor(Cursor.DEFAULT_CURSOR));curs=false;}
					arrow.setDraw(false);
					notes.setDraw(false);
				}
				else {
					if(typeSeletion==0) {setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));curs=true;}
					else if(typeSeletion==1) {setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));curs=true;}
					else setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
		
		//creation of the different color use to paint
		//arrayColor[0] = new Color((float)0.905,(float)0.905,(float)0.905);
		arrayColor[0] = new Color((float)0.930,(float)0.930,(float)0.930);
		arrayColor[1] = new Color((float)0.745,(float)0.745,(float)0.745);
		arrayColor[2] = new Color((float)0.843,(float)0.890,(float)0.910);
		arrayColor[3] = new Color((float)0.722,(float)0.765,(float)0.780);
		arrayColor[4] = new Color((float)0.482,(float)0.482,(float)0.482);
		arrayColor[5] = new Color((float)0.807,(float)0.807,(float)0.807);
		arrayColor[6] = new Color((float)0.549,(float)0.713,(float)0.807);
		arrayColor[7] = new Color((float)0.741,(float)0.745,(float)0.741);
		arrayColor[8] = new Color((float)0.388,(float)0.396,(float)0.388);
		arrayColor[9] = new Color((float)0.196,(float)0.196,(float)0.196);
		arrayColor[10]= new Color((float)0.192,(float)0.298,(float)0.525);
		arrayColor[11]= new Color((float)0.141,(float)0.141,(float)0.141);
		arrayColor[12]= new Color((float)0.290,(float)0.349,(float)0.643);
		arrayColor[13]= new Color((float)0.851,(float)0.902,(float)0.937);
		arrayColor[14]= new Color((float)0.961,(float)0.961,(float)0.961);
	}



	/** Detect if the position of the mouse is on a special place (return -1 if nothing or the numer of the task*/
	public int detectPosition(int mx, int my,boolean all)
	{
		for(int i=0;i<listOfParam.size();i++)
		{
			GanttPaintParam param = (GanttPaintParam)listOfParam.get(i);					
			if((param.type==2 && !all)||all)
			{
				int y = param.y*20+40-margY;
				int x1 = param.x1;
				int x2 = param.x2;
						
				if((my>=y && my<=y+12))
				{
					//The end of the task
					if(mx>=x2-3 && mx<=x2+3)
					{
						typeSeletion=0;
						GanttCalendar enddate= taskToMove.getEnd().newAdd(-1);
						notes = new Notes(enddate.toString(), mx, y-30);
						arrow.setDraw(false);
						return i;
					}
					//the start of the task
					else if(mx>=x1-3 && mx<=x1+3)
					{
						typeSeletion=1;
						notes = new Notes(taskToMove.getStart().toString(), mx, y-30);
						arrow.setDraw(false);
						return i;
					}
					//A depend
					else if(mx>x1+3 && mx<x2-3)
					{
						typeSeletion=2;
						arrow = new Arrow(mx/*x1+(x2-x1)/2*/,y+6,mx/*x1+(x2-x1)/2*/,y+6);
						notes.setDraw(false);
						return i;
					}
				}				
			}
		}
		return -1;
	}



	
	/** The size of the panel. */
	public Dimension getPreferredSize()
	{
		return new Dimension(465, 600);
	}

	/** Method to change the date */
	public void changeDate (boolean next)
	{
		int f=1;
		if(!next)f=-1;

		switch(zoomValue)
		{
			case ONE_WEEK:case TWO_WEEK:date.go(Calendar.WEEK_OF_YEAR,1*f); break;
			case ONE_MONTH:case TWO_MONTH:case THREE_MONTH:case SIX_MONTH:date.go(Calendar.MONTH,1*f); break;
			case ONE_YEAR:case TWO_YEAR:case THREE_YEAR:date.go(Calendar.YEAR,1*f);break;
		}
	}

	/** Method when zoomin, to set at the begin for each value */
	public void zoomToBegin ()
	{
		switch(zoomValue)
		{
			case ONE_WEEK: case TWO_WEEK:
					String d = language.getDay(date.getDayWeek());
					while(!d.equals(language.getDay(2)))
					{
						date.add(1);
						d = language.getDay(date.getDayWeek());
					}
					
					break;
			case ONE_MONTH:case TWO_MONTH:case THREE_MONTH:case SIX_MONTH:date.setDay(1);break;
			case ONE_YEAR:case TWO_YEAR:case THREE_YEAR:date.setMonth(0);
				   date.setDay(1);
				   break;
		}
	}


	/** draw the panel */
	public void paintComponent(Graphics g)
	{
		//Super paint component!!!!!!!!!!
		super.paintComponent(g);

		//Move if its in printing (for margin)
		if(printRendering)
			g.translate((int)upperLeft.getX(),(int)upperLeft.getY());

		//Get all task
		listOfTask = tree.getAllTasks();

		//Vertical bars
		paintCalendar1(g);
		//The tasks
		paintTasks(g);
		//The depends
		if(drawdepend) paintDepend(g);
		//The part at top
		paintCalendar2(g);
		
		
		arrow.paint(g);
		notes.paint(g);
		
		if(drawVersion)
		{
			g.setColor(Color.black);
			g.setFont(new Font("SansSerif",Font.BOLD,10));
			g.drawString("GanttProject ("+appli.version+")",3,getHeight()-6);
		}
				
	}


	/** Is the Task visible on the JTree */
	public boolean isVisible (GanttTask thetask)
	{

		boolean res = true;
		ArrayList expand = tree.getExpand();
		DefaultMutableTreeNode father = tree.getFatherNode(thetask);

		//The roor task is not visible
		if(father==null) return false;

		while(father!=null)
		{
			if(!expand.contains(father.toString())) res=false;
			GanttTask t = tree.getTask(father.toString());
			father = tree.getFatherNode(t);

		}

		return res;

	}


	/** Search for a coef on the arraylist */
	public int indexOf(ArrayList listOfParam, String coef)
	{
		for(int i=0;i<listOfParam.size();i++)
			if(coef==listOfParam.get(i).toString())
				return i;
		return -1;
	}

	/** Change the velue connected to the JTree's Scrollbar */
	public void setScrollBar (int v) { margY = v; }

	/** Return the value of the JTree's Scrollbar */
	public int getScrollBar () { return margY; }

	/** Change the zoom value */
	public void setZoom (int z) { zoomValue = z; }
	
	/** Add a zoom*/
	public void zoomMore()
	{
		if(zoomValue==5)olddate=date.Clone();
		zoomValue++;
	}
	
	/**Less a zoom*/
	public void zoomLess()
	{
		if(zoomValue==6 && date.getYear()==olddate.getYear())date=olddate.Clone();
		zoomValue--;
	}

	/** Return  the zoom value */
	public int getZoom () { return zoomValue; }

	/** Change the date of the begin to paint */
	public void setDate(GanttCalendar d) { date = d; }

	/** Return the date */
	public GanttCalendar getDate() { return date ; }

	/** Change language */
	public void setLanguage (GanttLanguage language) { this.language = language; }

	/** Return the number of day visible for each level of granularity */
	public int getGranit(boolean day)
	{
		GanttCalendar cal;
		int res=7;	//by default the 7 days of the week
		switch(zoomValue)
		{
			case ONE_WEEK: res=7; break;
		
			case TWO_WEEK:res=14;break;
		
			case ONE_MONTH:res=date.getNumberOfDay();break;
			
			case TWO_MONTH:cal = date.Clone();
				   res=cal.getNumberOfDay();
				   cal.goNextMonth();
				   res+=cal.getNumberOfDay();
				   break;

			case THREE_MONTH:cal = date.Clone();
				res=0;
				for(int i=0;i<3;i++)
				{
					res+=cal.getNumberOfDay();
					cal.goNextMonth();
				}break;
			
			case SIX_MONTH:cal = date.Clone();
				res=0;
				for(int i=0;i<6;i++)
				{
					res+=cal.getNumberOfDay();
					cal.goNextMonth();
				}break;

			case ONE_YEAR:if(!day) res=12;
				   else res=(date.getYear()%4==0)?366:365;
				   break;

			case TWO_YEAR:if(!day) res=12*2;
				   else
				   {
				   	cal = date.Clone();
					res=0;
					for(int i=0;i<2;i++)
					{
				   		if(cal.getYear()%4==0) res+=366;
						else res+=365;
						cal.go(Calendar.YEAR,1);
					}
				   }
				   break;

			case THREE_YEAR:if(!day) res=12*3;
				   else
				   {
				   	cal = date.Clone();
					res=0;
					for(int i=0;i<3;i++)
					{
				   		if(cal.getYear()%4==0) res+=366;
						else res+=365;
						cal.go(Calendar.YEAR,1);
					}
				   }
				   break;
		}
		return res;
	}


	/** Return the advance foot  */
	public int getFoot()
	{
		int res=1;
		switch(zoomValue)
		{
			case ONE_YEAR:case TWO_YEAR:case THREE_YEAR:res=date.getNumberOfDay();break;
			//default:res=1;break;
		}
				
		return res;
	}

	/** Paint the vertical bars */
	public void paintCalendar1(Graphics g)
	{
		int sizex = getWidth();
		int sizey = getHeight();
		int headery = 45;
		int gra = sizex / getGranit(false);		//The granularity
		int gra2 = getGranit(false);
		float fgra = (float)sizex / (float)getGranit(false);	
		int drawDate=-1;
		GanttCalendar dateToPaint=new GanttCalendar();

		g.setFont(new Font("SansSerif",Font.BOLD,12));

		//Reset the background to white
		g.setColor(Color.white);
		g.fillRect(0,0,sizex,sizey);


		beg = date.Clone(); beg.add(getGranit(true));
		end = date.Clone(); end.add(-1);


		for(int i=0;i<listOfTask.size();i++)
		{
			GanttTask task =(GanttTask) ((DefaultMutableTreeNode)listOfTask.get(i)).getUserObject();
			if(!((DefaultMutableTreeNode)listOfTask.get(i)).isRoot())
			{
				if(beg.compareTo(task.getStart())>0)
					beg = task.getStart();
				if(end.compareTo(task.getEnd())<0)
					end = task.getEnd();
			}
		}
		//end.add(1);
		

		//Draw Horizontal bar on tasks
		/*g.setColor(arrayColor[14]);
		for(int i=-margY-4;i<getHeight();i+=40)
		{
			g.fillRect(0,i,sizex,20);
		}*/

		
		GanttCalendar tmpdate;
		if(zoomValue==ONE_WEEK || zoomValue==TWO_WEEK) tmpdate = date.Clone();
		else tmpdate = new GanttCalendar(date.getYear(), date.getMonth(), 1);
		g.setFont(new Font("SansSerif",Font.PLAIN,10));

		//Draw the vertical bars
		for(int i=0;i<gra2;i++)
		{
			String sDay = tmpdate.getdayWeek(language);
			String sMonth = tmpdate.getdayMonth(language);

			//For each day
			if(zoomValue<6) {
				if(sDay==language.getDay(6)){
					g.setColor(arrayColor[0]);
					g.fillRect((int)(fgra*i),headery,gra+1/*(int)(fgra+0.5)*/,sizey);

					g.setColor(arrayColor[1]);
					g.drawLine((int)(fgra*i),headery,(int)(fgra*i),sizey+headery);

				} else if(sDay==language.getDay(0)) {
					g.setColor(arrayColor[0]);
					g.fillRect((int)(fgra*i),headery,gra+1/*(int)(fgra+0.5)*/,sizey);
					if(zoomValue==ONE_WEEK || zoomValue==TWO_WEEK){
						g.setColor(arrayColor[1]);
						g.drawLine((int)(fgra*i),headery,(int)(fgra*i),sizey+headery);
					}
				}


			} else { //For each month

				if(sMonth==language.getMonth(0))
				{
					g.setColor(arrayColor[0]);
					g.fillRect((int)(fgra*i),headery,(int)fgra,sizey);
				}
				g.setColor(arrayColor[1]);
				g.drawLine((int)(fgra*i),headery,(int)(fgra*i),sizey+headery);

			}
			
			if(zoomValue<6) {
				if(tmpdate.compareTo(new GanttCalendar())==0)
				{
					g.setColor(arrayColor[2]);
					g.fillRect((int)(fgra*i),headery,(int)fgra,sizey);
					g.setColor(arrayColor[3]);
					g.drawLine((int)(fgra*i),headery,(int)(fgra*i),sizey+headery);
				}
			} else {
				GanttCalendar today=new GanttCalendar();
				float s = fgra/(float)tmpdate.getNumberOfDay();
				if(tmpdate.getYear()==today.getYear() && tmpdate.getMonth()==today.getMonth())
				{
					//Blue rectange on today
					g.setColor(arrayColor[2]);
					g.fillRect((int)(fgra*i)+(int)(s*today.getDay()),headery,((s<2)?2:(int)s),sizey);
					g.setColor(arrayColor[2]);
					g.drawLine((int)(fgra*i)+(int)(s*today.getDay()),headery,(int)(fgra*i)+(int)(s*today.getDay()),sizey+headery);
					
				}			
			}
			
			tmpdate.add(getFoot());
		}
		
		
		
		
		if(zoomValue==ONE_WEEK || zoomValue==TWO_WEEK) tmpdate = date.Clone();
		else tmpdate = new GanttCalendar(date.getYear(), date.getMonth(), 1);
		
		//Draw Miscallenous bars
		for(int i=0;i<gra2;i++)
		{
			//Courant day in blue
			if(zoomValue<6) {
				//A line on begin and end of project
				if(tmpdate.compareTo(beg)==0 || tmpdate.compareTo(end)==0)
				{
					g.setColor(arrayColor[12]);
					//g.drawLine((int)(fgra*i),headery,(int)(fgra*i),sizey+headery);
					drawVerticalLinedash(g,(int)(fgra*i),headery,sizey+headery,5);
					if(tmpdate.compareTo(beg)==0){
						drawDate=(int)(fgra*i)-52;
						dateToPaint=beg;
					} else {
						drawDate=(int)(fgra*i)+3;
						dateToPaint=end.newAdd(-1);
					}
				}				
			} else {
			
				float s = fgra/(float)tmpdate.getNumberOfDay();
				//blue dash line for begin 
				if(tmpdate.getYear()==beg.getYear() && tmpdate.getMonth()==beg.getMonth())
				{
				 	g.setColor(arrayColor[12]);
					//g.drawLine((int)(fgra*i)+(int)(s*beg.getDay()),headery,(int)(fgra*i)+(int)(s*beg.getDay()),sizey+headery);
					drawVerticalLinedash(g,(int)(fgra*i)+(int)(s*beg.getDay()),headery,sizey+headery,5);
					drawDate=(int)(fgra*i)+(int)(s*beg.getDay())-52;
					dateToPaint=beg;
				}
				//blue dash line for end
				else if(tmpdate.getYear()==end.getYear() && tmpdate.getMonth()==end.getMonth())
				{
				 	g.setColor(arrayColor[12]);
					//g.drawLine((int)(fgra*i)+(int)(s*end.getDay()),headery,(int)(fgra*i)+(int)(s*end.getDay()),sizey+headery);
					drawVerticalLinedash(g,(int)(fgra*i)+(int)(s*end.getDay()),headery,sizey+headery,5);
					drawDate=(int)(fgra*i)+(int)(s*end.getDay())+3;
					dateToPaint=end.newAdd(-1);
				}				
			}

			//Draw the date in string on calendar
			if(drawDate>=0)
			{
				g.setFont(new Font("SansSerif",Font.PLAIN,9));
				g.setColor(arrayColor[9]);
				g.drawString(dateToPaint.toString(),drawDate,headery+10);
				drawDate = -1;
			}

			tmpdate.add(getFoot());
		}
	}


	/** Draw the legend of the calendar */
	public void paintCalendar2(Graphics g)
	{
		int sizex = getWidth();
		int sizey = getHeight();
		int headery = 45;
		int gra2 = getGranit(false);
		float fgra=(float)(sizex) / (float)getGranit(false);		//the granularity
		

		g.setFont(new Font("SansSerif",Font.BOLD,12));

		//gray rectangle with nice borders
		g.setColor(arrayColor[0]);
		g.fillRect(0,0,sizex,headery);
		g.setColor(arrayColor[4]);
		g.drawRect(0,0, sizex-1, headery/2);
		g.drawRect(0,headery/2, sizex-1, headery/2);
		g.setColor(arrayColor[5]);
		g.drawLine(1,headery/2-1,sizex-2,headery/2-1);
		g.drawLine(0,headery-2,sizex-2,headery-2);
		g.setColor(Color.white);
		g.drawLine(1,1,sizex-2,1);
		g.drawLine(0,headery/2+1,sizex-2,headery/2+1);

		g.setColor(Color.black);
		//Set the text at the top (differetns for each zaoom value)
		float posX;
		GanttCalendar cal;
		switch(zoomValue)
		{
			//The number of week and th month
			case ONE_WEEK: g.drawString(language.week()+date.getWeek()+" - "+date.getdayMonth(language)+"  "+date.getYear(),2,headery/2-5);
					break;

			case TWO_WEEK:
						g.drawString(language.week()+(date.getWeek())+" - "+date.getdayMonth(language)+"  "+date.getYear(),2,headery/2-5);
						g.drawString(language.week()+(date.getWeek()+1)+" - "+date.getdayMonth(language)+"  "+date.getYear(),(int)(fgra*7)+2,headery/2-5);
				break;

			//The month and the yeay
			case ONE_MONTH: g.drawString(date.getdayMonth(language)+"  "+date.getYear(),2,headery/2-5);
					break;

			// Draw two months
			case TWO_MONTH:cal = date.Clone();
					posX = 0;
					for(int i=0;i<2;i++)
					{
						g.drawString(cal.getdayMonth(language)+"  "+cal.getYear(),(int)posX+2/*i*sizex/3+2*/,headery/2-5);
						posX+=((float)cal.getNumberOfDay()*fgra);
						cal.goNextMonth();
					}
				break;

			//Draw three months
			case THREE_MONTH:cal = date.Clone();
					posX = 0;
					for(int i=0;i<3;i++)
					{
						g.drawString(cal.getdayMonth(language)+"  "+cal.getYear(),(int)posX+2/*i*sizex/3+2*/,headery/2-5);
						posX+=((float)cal.getNumberOfDay()*fgra);
						cal.goNextMonth();
					}
					break;

			// draw 6 months
			case SIX_MONTH:cal = date.Clone();
					posX = 0;
					for(int i=0;i<6;i++)
					{
						g.drawString(cal.getdayMonth(language).substring(0,(sizex>480)?3:1)+"  "+new
							Integer(cal.getYear()).toString().substring(2,4),(int)posX+2/*i*sizex/3+2*/,headery/2-5);
						posX+=((float)cal.getNumberOfDay()*fgra);
						cal.goNextMonth();
					}
				break;

			//Draw One year
			case ONE_YEAR: g.drawString(""+date.getYear(),2,headery/2-5);
					break;

			// Draw two years
			case TWO_YEAR:int dy2 = date.getYear();
					for(int i=0;i<2;i++,dy2++)
					{
						g.setColor(Color.black);
						g.drawString(""+dy2,i*sizex/2+2,headery/2-5);
					}
				break;

			//draw thre years
			case THREE_YEAR: int dy = date.getYear();
					for(int i=0;i<3;i++,dy++)
					{
						g.setColor(Color.black);
						g.drawString(""+dy,i*sizex/3+2,headery/2-5);
					}
					break;
		}

		GanttCalendar tmpdate;
		if(zoomValue==ONE_WEEK || zoomValue==TWO_WEEK) tmpdate = date.Clone();
		else tmpdate = new GanttCalendar(date.getYear(), date.getMonth(), 1);
		g.setFont(new Font("SansSerif",Font.PLAIN,10));

		//Draw each day or each month
		for(int i=0;i<gra2;i++)
		{
			String sDay = tmpdate.getdayWeek(language);
			String sMonth = tmpdate.getdayMonth(language);

			g.setColor(Color.black);
			switch(zoomValue)
			{
				case ONE_WEEK: g.drawString(sDay.substring(0,(sizex>300)?3:1)+" "+tmpdate.getDate(),(int)(fgra*i)+2,headery-7);
						g.setColor(arrayColor[4]);
						g.drawLine((int)(fgra*i),headery/2,(int)(fgra*i),headery-1);
						break;
						
				case TWO_WEEK:g.drawString(sDay.substring(0,1)+" "+tmpdate.getDate(),(int)(fgra*i)+2,headery-7);
						g.setColor(arrayColor[4]);
						g.drawLine((int)(fgra*i),headery/2,(int)(fgra*i),headery-1);
						break;
						
				case ONE_MONTH: g.drawString(""+tmpdate.getDate(),(int)(fgra*i)+3,headery-7);
						g.setColor(arrayColor[4]);
						g.drawLine((int)(fgra*i),headery/2,(int)(fgra*i),headery-1);
						break;
				
				case TWO_MONTH:case THREE_MONTH:case SIX_MONTH:
					 	if(sDay==language.getDay(1))
						{
							g.drawString(""+tmpdate.getDate(),(int)(fgra*i)+2,headery-7);
							g.setColor(arrayColor[4]);
							g.drawLine((int)(fgra*i),headery/2,(int)(fgra*i),headery-1);
						}
							break;
				//case SIX_MONTH:
				//		if(sDay==language.getDay(1)) g.drawString(""+tmpdate.getDate(),(int)(fgra*i)+1,headery-7);
				//			break;
				case ONE_YEAR: g.drawString(sMonth.substring(0,(sizex>300)?3:1),(int)(fgra*i)+2,headery-7);
						g.setColor(arrayColor[4]);
						g.drawLine((int)(fgra*i),headery/2,(int)(fgra*i),headery-1);
						break;
						
				case TWO_YEAR: case THREE_YEAR: g.drawString(sMonth.substring(0,1),(int)(fgra*i)+2,headery-7);
						g.setColor(arrayColor[4]);
						g.drawLine((int)(fgra*i),headery/2,(int)(fgra*i),headery-1);
						break;
			}
			//next date
			tmpdate.add(getFoot());
		}
	}

	/** Paint all tasks  */
	public void paintTasks(Graphics g)
	{
		int sizex = getWidth();
		int sizey = getHeight();
		int headery = 45;
		float fgra = (float)sizex / (float)getGranit(true);

		g.setFont(new Font("SansSerif",Font.PLAIN,9));

		//Get all task
		//ArrayList listOfTask = tree.getAllTasks();

		//Probably optimised on next release
		listOfParam.clear();

		int y=0;

		//Calcul all parameters
		for(int i=0;i<listOfTask.size();i++)
		{
			//Get the task
			GanttTask task =(GanttTask) ((DefaultMutableTreeNode)listOfTask.get(i)).getUserObject();

			//Is the task is visible, the task could be draw
			if(isVisible(task))
			{
				int x1=-10, x2=sizex+10;
				int e1; //ecart entre la date de debut de la tache et la date du debut du calendrier
				int fois;
				int type = 2;
				y++;

				//difference between the start date of the task and the end
				e1 = date.diff(task.getStart());
	
				//Calcul start and end pixel of each task
				float fx1, fx2;

				if(task.getBilan())
				{
					fx1 = (float)e1 * fgra * ((date.compareTo(task.getStart())==1)?-1:1);
					x1 = (int)fx1;
				}
				else
				{
					fx1 = (float)e1 * fgra * ((date.compareTo(task.getStart())==1)?-1:1);
					fx2 = fx1 + (float)task.getLength() * fgra;
					x1 = (int) fx1;
					x2 = (int) fx2;
				}

				
				//Meeting task
				if(task.getBilan()) {
					paintATaskBilan(g, x1, y,task.toString());
					x2=x1+(int)fgra;
					type=0;
				}
				//A mother task
				else if(tree.getAllChildTask(task.toString()).size()!=0){// || tree.getFatherNode(task).isRoot()) {
					paintATaskFather(g, x1, x2, y,task.toString());
					if(drawPercent) paintAdvancement(g, x1, x2, y, task.getPercent(), true);
					type=1;
				}
				//A normal task
				else {
					paintATaskChild(g, x1, x2, y,task.toString());
					if(drawPercent) paintAdvancement(g, x1, x2, y, task.getPercent(), false);
					type=2;
				}

				//Add parameters on the array
				listOfParam.add(new GanttPaintParam(task.toString(), x1, x2, y, type));
			}
		}
		
	}



	/** Draw a monther task */
	public void paintATaskFather(Graphics g, int x1, int x2, int y, String taskName)
	{
		int d=y;
		y=y*20+41-margY;
		
		if(y<20 || y > getHeight()) return;	//Not draw if the task is not on the area
		if((x1>getWidth() && x2>getWidth()) || (x1<0 && x2<0)) return;
		
		//Black rectangle
		//g.setColor(Color.black);
		if(drag==d-1) g.setColor(arrayColor[4]);
		else g.setColor(Color.black);
		g.fillRect(x1, y, x2-x1,2 );

		//Little triangle at begin and end
		int xPoints [] = new int[3];
		int yPoints [] = new int[3];
		xPoints[0] = x1;
		xPoints[1] = x1+5;
		xPoints[2] = x1;
		yPoints[0] = y+2;
		yPoints[1] = y+2;
		yPoints[2] = y+6;
		g.fillPolygon(xPoints, yPoints, 3);
		xPoints[0] = x2;
		xPoints[1] = x2-5;
		xPoints[2] = x2;
		g.fillPolygon(xPoints, yPoints, 3);

		if(drawName/*printRendering*/)
		{
			g.setColor(Color.black);
			g.drawString(taskName, x2+40,y+9);
		}
	}


	/** Draw a normal task */
	public void paintATaskChild(Graphics g, int x1, int x2, int y, String taskName)
	{
		int d=y;
		y=y*20+40-margY;
		
		if(y<20 || y > getHeight()) return;	//Not draw if the task is not on the area
		if((x1>getWidth() && x2>getWidth()) || (x1<0 && x2<0)) return;
		
		//Blue rectangle
		g.setColor(arrayColor[6]);
		g.fillRect(x1, y, (x2-x1-1),12 );

	
		//Draw nice border
		if(drag==d-1) g.setColor(arrayColor[0]);
		else g.setColor(Color.black);
		g.drawRect(x1,y,x2-x1-1,12);
		/*g.drawLine(x1, y, (x2-1), y);
		g.drawLine(x1, y+12, (x2-1), y+12);
		g.drawLine(x1, y, x1, y+12);
		g.drawLine((x2-1), y, (x2-1), y+12);*/

		g.setColor(arrayColor[7]);
		g.drawLine(x1+1, y+1, (x2-1)-1, y+1);
		g.drawLine(x1+1, y+1, x1+1, y+11);

		g.setColor(arrayColor[8]);
		g.drawLine(x1+2, y+11, (x2-1)-2, y+11);
		g.drawLine(x2-2, y+2, x2-2, y+11);

		if(drawName/*printRendering*/)
		{
			g.setColor(Color.black);
			g.drawString(taskName, x2+40,y+10);
		}
		
	}


	/** Draw a meeting task */
	public void paintATaskBilan(Graphics g, int x1, int y, String taskName)
	{
		int d=y;
		y=y*20+45-margY;

		if(y<20 || y-5 > getHeight()) return;	//Not draw if the task is not on the area
		if(x1>getWidth() || x1<0) return;
		
		int gra = getWidth() / getGranit(false);		//the granularity
		float fgra = (float)getWidth() / (float)getGranit(true);
		
		int xPoints [] = new int[4];
		int yPoints [] = new int[4];

		xPoints[0] = (int)((float)x1+fgra/2);
		xPoints[1] = (int)((float)x1+fgra/2-5);
		xPoints[2] = (int)((float)x1+fgra/2);
		xPoints[3] = (int)((float)x1+fgra/2+5);

		yPoints[0] = y-5;
		yPoints[1] = y;
		yPoints[2] = y+5;
		yPoints[3] = y;

		//g.setColor(Color.black);
		if(drag==d-1) g.setColor(arrayColor[4]);
		else g.setColor(Color.black);
		g.fillPolygon(xPoints, yPoints, 4);
		g.setColor(arrayColor[8]);
		g.drawPolygon(xPoints, yPoints, 4);

		if(drawName/*printRendering*/)
		{
			g.setColor(Color.black);
			g.drawString(taskName, x1+16,y+5);
		}
	}

	/** Draw the arrows for depends */
	public void paintDepend(Graphics g)
	{
		//Recup all tasks
		//ArrayList listOfTask = tree.getAllTasks();

		//some parameters
		int l1=6;
		int l2=5;

		//for paint triangles
		int xPoints [] = new int[3];
		int yPoints [] = new int[3];

		//For each tasks
		for(int i=0;i<listOfTask.size();i++)
		{
			//Get the task
			GanttTask task =(GanttTask) ((DefaultMutableTreeNode)listOfTask.get(i)).getUserObject();

			//Only if the task is visible
			if(isVisible(task))
			{
				//Recup the depend array for the task
				ArrayList depend = task.getDepend();

				//For each depend
				for(int j=0;j<depend.size();j++)
				{
					GanttTask task2 = tree.getTask((String)depend.get(j));
					if(this.isVisible(task2))
					{

						//Get the start index and end index
						int index1 = this.indexOf(listOfParam, task.toString());
						int index2 = this.indexOf(listOfParam, task2.toString());

						if(index1!=-1 && index2!=-1)
						{

							//Get x coord
							int xa = ((GanttPaintParam)listOfParam.get(index1)).x2;
							int xb = ((GanttPaintParam)listOfParam.get(index2)).x1;
							//...and y coord
							int ya = ((GanttPaintParam)listOfParam.get(index1)).y;
							int yb = ((GanttPaintParam)listOfParam.get(index2)).y;

							
							ya = ya*20+45-margY;
							yb = yb*20+45-margY;
							xb+=2;
							
							
							xPoints[0] = xb;
							xPoints[1] = xb+3;
							xPoints[2] = xb-3;
							
							if(ya>yb) {
								yb+=7;								
								yPoints[1] = yb+4;
								yPoints[2] = yb+4;
							} else {
								yb-=6;
								yPoints[1] = yb-4;
								yPoints[2] = yb-4;
							}
							yPoints[0] = yb;
							
							g.setColor(Color.black);
							g.fillPolygon(xPoints, yPoints, 3);
							g.drawLine(xa,ya,xb,ya);
							g.drawLine(xb,ya,xb,yb);


							//up or down?????
							/*int e=(yb-ya)>0?10:-10;

							//horizontal at the middle
							g.setColor(Color.black);
							g.drawLine(xa+l1,ya*20+45+e-margY,xb-l1,ya*20+45+e-margY);
							if(xa!=32)
							{
								//horizontal and vertical at start
								g.drawLine(xa,ya*20+45-margY,xa+l1,ya*20+45-margY);
								g.drawLine(xa+l1,ya*20+45-margY,xa+l1,ya*20+45+e-margY);


							} else {
								g.setColor(Color.white);
								g.drawLine(xa+l1,ya*20+45+e-margY,xa+l1-3,ya*20+45+e-margY);
								g.drawLine(xa+l1-8,ya*20+45+e-margY,xa+l1-11,ya*20+45+e-margY);
								g.drawLine(xa+l1-16,ya*20+45+e-margY,xa+l1-19,ya*20+45+e-margY);
								g.setColor(Color.black);
							}

							g.drawLine(xb-l1,ya*20+45+e-margY,xb-l1,yb*20+45-margY);


							g.drawLine(xb-l1,yb*20+45-margY,xb,yb*20+45-margY);
							//triangle at end
							xPoints[0] = xb;
							xPoints[1] = xb-l2;
							xPoints[2] = xb-l2;
							yPoints[0] = yb*20+45-margY;
							yPoints[1] = yb*20+45+3-margY;
							yPoints[2] = yb*20+45-3-margY;
							g.fillPolygon(xPoints, yPoints, 3);*/
						}
					}
				}
			}
		}
	}


	
	/** Paint advance state of the task */
	public void paintAdvancement (Graphics g, int x1, int x2, int y, int percent, boolean justText)
	{
		//draw the value at the end of task
		g.setColor(Color.black);
		g.drawString("["+percent+"%]",x2+8,y*20+50-margY);

		//Paint the bar
		if(!justText)
		{
			float fp = (float)(x2-x1) * (float)percent / 100;
			//g.setColor(arrayColor[9]);
			//g.fillRect(x1, y*20+44-margY, (int)fp-1,4 );
			
			//To set a nice effets of gradient
			Graphics2D g2= (Graphics2D) g;
			g2.setPaint(new GradientPaint(x1+2,y*20+44-margY+2,arrayColor[10],
										  x2-2,y*20+44-margY+2,arrayColor[11]));
			g2.fill(new Rectangle(x1+2, y*20+44-margY, (x2-x1)-4,4 ));
			
			g.setColor(arrayColor[6]);
			g.fillRect(x1+2+(int)fp, y*20+40-margY+2, ((x2-x1-4)-(int)fp),9 );
			
		}
	}

	/** Draw a vertical dash line from point (x1, y1) to (x2, y2) */
	public void drawVerticalLinedash(Graphics g, int x, int y1, int y2, int size)
	{
		int i=y1;
		int end=y2;
		if(y1>y2){i=y2;end=y1;}
		int size2=size+size;
		while(i<end)
		{
			g.drawLine(x,i,x,i+size);
			i=i+size2;
		}
	}


	/** Print the page */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
	{

		if (pageIndex >= 1) {
        	return Printable.NO_SUCH_PAGE;
    	}
		printRendering=true;
		upperLeft=new Point((int)pageFormat.getImageableX(), (int)pageFormat.getImageableY());
		paintComponent(graphics);
		printRendering=false;
		repaint();
		return Printable.PAGE_EXISTS;
	}

	/** Print the project */
	public void printProject(boolean name, boolean percent, boolean depend)
	{
		PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(this);
        if (printJob.printDialog()) {
            try {
				drawdepend = depend;
				drawPercent = percent;
				drawName=name;
				drawVersion=true;
				printJob.print();
				drawdepend = true;
				drawPercent = true;
				drawName = false;
				drawVersion=false;
            } catch (Exception ex) {
                ex.printStackTrace();
				drawdepend = true;
				drawPercent = true;
				drawName = false;	
				drawVersion=false;	
            }
        }

	}
	
	
	/** Draw arrow between two points */
	public class Arrow
	{
		private int x1, x2, y1, y2;
		
		private boolean draw;
		
		public Arrow ()
		{
			x1 = x2 = y1 = y2 = 0;
			draw=false;
		}
		public Arrow (int x1, int y1, int x2, int y2)
		{
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
			this.draw=true;
		}
		
		public void setDraw(boolean d) { draw=d; }		
		public boolean getDraw() { return draw; }
		
		public void changePoint2(int x2, int y2)
		{
			this.x2 = x2;
			this.y2 = y2;
		}
		
		public void paint(Graphics g)
		{
			if(draw)
			{
				//draw the line
				g.setColor(Color.black);
				g.drawLine(x1,y1,x2,y2);
				
				//Draw the triangle
				int xPoints [] = new int[3];
				int yPoints [] = new int[3];
				
				int vx = x2 - x1;
				int vy = y2 - y1;
								
				int px = (int)(0.08f*(float)vx);
				int py = (int)(0.08f*(float)vy);
				
				
				int total=((px<0)?-px:px) + ((py<0)?-py:py);
				
				px = (int)((float)px * 10.f / (float)total);
				py = (int)((float)py * 10.f / (float)total);
		
				
				xPoints[0] = x2;
				yPoints[0] = y2;
				xPoints[1] = x2-px+py/2;
				yPoints[1] = y2-py-px/2;
				xPoints[2] = x2-px-py/2;
				yPoints[2] = y2-py+px/2;
				g.fillPolygon(xPoints, yPoints, 3);
			}
		}
	}
	
	/** Note paint of the graphic Area */
	public class Notes
	{
		/** The notes to paint */
		String n = new String(); 
	
		/** The coords */
		int x, y;
		
		boolean draw;		

		public Notes () { draw =false;}
		
		public Notes (String s, int x, int y)
		{
			this.n = s;
			this.x = x;
			this.y = y;
			this.draw=true;
		}
		
		public void setDraw(boolean d) { draw=d; }		
		public boolean getDraw() { return draw; }
		
		public void setX(int x) { this.x=x; }
		public void setString(String s) { n = s; }
		
		public void paint(Graphics g)
		{
			if(draw)
			{
				g.setColor(arrayColor[0]);
				g.fillRect(x-2,y,61,16);
				g.setColor(Color.black);
				g.drawRect(x-2,y,61,16);
				g.drawString(n,x,y+12);
			}
		}
	}

	/** Class to store parameters of each task (pixel to start, end y value...*/
	public class GanttPaintParam
	{
		public int x1,x2;
		
		public String name;

		public int y;
		
		public int type; //0->Meeting task,   1 mother task,  2 normal task

		public GanttPaintParam(String name, int x1, int x2, int y, int type)
		{
			this.name = name;
			this.x1 = x1;
			this.x2 = x2;
			this.y = y;
			this.type=type;
		}

		public String toString() { return name; }
	}

	/** Function able to export in PNG format the graphic area */
	public void export (File file, boolean name, boolean percent, boolean depend)
	{
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB) ;
		
		drawdepend = depend;
		drawPercent = percent;
		drawName=name;
		drawVersion=true;
		paintComponent(image.getGraphics());
		drawdepend = true;
		drawPercent = true;
		drawName = false;
		drawVersion=false;
		
		try{
			 if(!ImageIO.write(image, "png", file))
				System.out.println("Impossible de sauvegarder dans ce format");
  			
		}catch (Exception e) {
			System.out.println(e);
		}
	}
	
	
	
}

