/***************************************************************************
                           GanttGraphicArea.java  -  description
                             -------------------
    begin                : dec 2002
    copyright            : (C) 2002 by Thomas Alexandre
    email                : alexthomas(at)ganttproject.org
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

package net.sourceforge.ganttproject;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import net.sourceforge.ganttproject.chart.ChartModel;
import net.sourceforge.ganttproject.chart.ChartModelImpl;
import net.sourceforge.ganttproject.gui.UIConfiguration;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.resource.ProjectResource;
import net.sourceforge.ganttproject.shape.ShapePaint;
import net.sourceforge.ganttproject.task.ResourceAssignment;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskLength;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.algorithm.RecalculateTaskScheduleAlgorithm;
import net.sourceforge.ganttproject.task.dependency.TaskDependencyException;
import net.sourceforge.ganttproject.task.event.TaskDependencyEvent;
import net.sourceforge.ganttproject.task.event.TaskListenerAdapter;
import net.sourceforge.ganttproject.task.event.TaskScheduleEvent;
import net.sourceforge.ganttproject.time.gregorian.GregorianCalendar;
import net.sourceforge.ganttproject.time.gregorian.GregorianTimeUnitStack;


/**
 * Class for the graphic part of the soft
 */
public class GanttGraphicArea extends JPanel {

  /** Begin of display. */
  public GanttCalendar date, olddate;

  /** Reference to the GanttTree */
  public GanttTree tree;

  /** Zoom  to on week */
  public static final int ONE_WEEK = 0;

  /** Zoom  to two week */
  public static final int TWO_WEEK = 1;

  /** Zoom  to one month */
  public static final int ONE_MONTH = 2;

  /** Zoom  to two month */
  public static final int TWO_MONTH = 3;

  /** Zoom  to tree month */
  public static final int THREE_MONTH = 4;

  /** Zoom  to tree month */
  public static final int FOUR_MONTH = 5;

  /** Zoom  to six month */
  public static final int SIX_MONTH = 6;

  /** Zoom  to one year */
  public static final int ONE_YEAR = 7;

  /** Zoom  to two year */
  public static final int TWO_YEAR = 8;

  /** Zoom  to three year */
  public static final int THREE_YEAR = 9;

  /** Default color for tasks */
  public static Color taskDefaultColor
   //   = new Color( (float) 0.549, (float) 0.713, (float) 0.807);
	= new Color( 140, 182, 206);
  /* The Zoom Value */
  private int zoomValue;

  /** Array to store parameters of each task (for display) */
  private ArrayList listOfParam = new ArrayList();

  /** This value is connected to the GanttTRee Scrollbar to move up or down */
  private int margY;

  /** UpperLeft Point (for the margin of printing) */
  private Point upperLeft = new Point(0, 0);

  /** Render on the window or for printing*/
  private boolean printRendering = false;

  /** Render the depends */
  private boolean drawdepend = true;
  /** Render the depends */
  private boolean drawPercent = true;
  /** Render the name*/
  private boolean drawName = false;
  /** render the 3d borders. */
  private boolean draw3dBorders = true;
  /* Render the ganttproject version*/
  private boolean drawVersion = false;

  /** The language */
  private GanttLanguage language = GanttLanguage.getInstance();

  /*! The main application */
  private GanttProject appli;

  /*! Old X and Yposition*/
  private int oldX, oldY;

  /** Move the view of the calendar or move a task*/
  private boolean moveView = true;

  /** the Task number to move */
  private int moveTask = -1;
	
	/** If the user want to show the red line corresponding to today */
	//public static final boolean redline=true;

  /** Cursor by default*/
  boolean curs = false;

  /** the 2nd task to drag the depend */
  int drag = -1;

  /** Type of selection 0 -> move duration,   1 -> moveDate,   2->add Depend*/
  int typeSeletion;

  /** Parmeters to change the duration of the task*/
  private int storeTaskLength;
  private float addTaskLength;
  private GanttCalendar storeTaskStart;
  private int[] storeX = new int[3];

  /** The arrow between two task for depend */
  private Arrow arrow = new Arrow();

  private Notes notes = new Notes();

  /** The task to move duration*/
  private GanttTask taskToMove;

  /**Color array use to paint */
  private Color[] arrayColor = new Color[15];

  /** List of task recup before painting */
  private ArrayList listOfTask;

  /** Begin of project */
  public GanttCalendar beg = new GanttCalendar();

  /**End date for the project */
  public GanttCalendar end = new GanttCalendar();

  /** The mouse button press */
  public int mouseButton = 0;
    private final UIConfiguration myUIConfiguration;
    private Color myProjectLevelTaskColor;


    private TaskManager getTaskManager() {
        return myTaskManager;
    }

    private final TaskManager myTaskManager;


    /** Constructor */
  public GanttGraphicArea(GanttProject app, GanttTree ttree, TaskManager taskManager, UIConfiguration uiConfiguration) {
        myTaskManager = taskManager;
        myUIConfiguration = uiConfiguration;
        myTaskManager.addTaskListener(new TaskListenerAdapter() {
            public void taskScheduleChanged(TaskScheduleEvent e) {
                adjustDependencies((Task) e.getSource());
            }

            public void dependencyAdded(TaskDependencyEvent e) {
                adjustDependencies(e.getDependency().getDependee());
            }


            private void adjustDependencies(Task task) {
                RecalculateTaskScheduleAlgorithm alg = myTaskManager.getAlgorithmCollection().getRecalculateTaskScheduleAlgorithm();
                if (!alg.isRunning()) {
                    try {
                        alg.run(task);
                    } catch (TaskDependencyException e1) {
                        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    repaint();
                }
            }
        });
        date = new GanttCalendar();
    olddate = new GanttCalendar();
    date.setDay(1);
    this.tree = ttree;
    zoomValue = ONE_MONTH; //zoom to one month by default of the current date

    margY = 0;
    appli = app;

    //Listener on wheel mouse
    this.addMouseWheelListener(new MouseWheelListener() {
      public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() > 0) {
          zoomMore();
	  appli.getResourcePanel().area.zoomMore();
        }
        else if (e.getWheelRotation() < 0) {
          zoomLess();
	  appli.getResourcePanel().area.zoomLess();

        }
        if (zoomValue < 0) {
          zoomValue = 0;
	  appli.getResourcePanel().area.setZoom(0);
        }
        if (zoomValue > 9) {
          zoomValue = 9;
	  appli.getResourcePanel().area.setZoom(9);
        }
        zoomToBegin();

        appli.bZoomIn.setEnabled(zoomValue == 0 ? false : true);
        appli.bZoomOut.setEnabled(zoomValue == 9 ? false : true);

        repaint();
      }
    });

    //Listener on a mouse click
    this.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
          appli.propertiesTask();
        }
      }

      public void mouseEntered(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}

      public void mouseExited(MouseEvent e) {setCursor(new Cursor(Cursor.DEFAULT_CURSOR));}

      public void mousePressed(MouseEvent e) {
        mouseButton = e.getButton();

        if (e.getButton() == MouseEvent.BUTTON1) {
          oldX = e.getX();
          oldY = e.getY();

          moveView = true; //move ok

          drag = detectPosition(e.getX(), e.getY(), true);
          if (drag != -1) {
            tree.selectTreeRow(drag);

          }
          moveTask = detectPosition(e.getX(), e.getY(), false);

          if (moveTask != -1) {

            GanttPaintParam param = (GanttPaintParam) listOfParam.get(moveTask);
            setTaskToMove(moveTask);

            moveView = false;

            addTaskLength = 0;
            storeTaskLength = taskToMove.getLength();

            if (param.type == 0) {
              setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
              storeTaskStart = taskToMove.getStart().Clone();
            }
            else if (typeSeletion == 0) {
              setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
            }
            else if (typeSeletion == 3) {
              //setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
              Toolkit toolkit = Toolkit.getDefaultToolkit();
              try {
                setCursor(toolkit.createCustomCursor(toolkit.getImage(getClass().
                    getClassLoader().getResource("icons/cursorpercent.gif")),
                    new Point(10, 5), "CursorPercent"));
              }
              catch (Exception exept) {
                setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
              }

              storeX[0] = param.x1;
              storeX[1] = param.x2;
              storeX[2] = param.x3;
            }
            else if (typeSeletion == 1) {
              setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
              storeTaskStart = taskToMove.getStart().Clone();
            }
          }
          else {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
          }
          repaint();
        }
        // update 18-03-2003
        else if (e.getButton() == MouseEvent.BUTTON3) {
          oldX = e.getX();
          oldY = e.getY();

          moveView = true; //move ok

          drag = detectPosition(e.getX(), e.getY(), true);
          if (drag != -1) {
            tree.selectTreeRow(drag);
            tree.createPopupMenu( (int) getLocation().getX() + e.getX(),
                                 (int) getLocation().getY() + e.getY() - 45 +
                                 margY,true);
          }

          repaint();
        }
        else if (e.getButton() == MouseEvent.BUTTON2) {
          //System.out.println("Must drag all the tasks by this clik and motion");
          oldX = e.getX();
          moveView = true; //move ok
          moveTask = detectPosition(e.getX(), e.getY(), false);
					storeTaskStart = taskToMove.getStart().Clone();
					addTaskLength = 0;
          if (moveTask != -1) {

            GanttPaintParam param = (GanttPaintParam) listOfParam.get(moveTask);
            setTaskToMove(moveTask);
            moveView = false;

          }
        }
      }

      //Release the mouse button
      public void mouseReleased(MouseEvent e) {
          if (e.getButton() == MouseEvent.BUTTON1) {
              setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

              if (moveView) {
                  int f = 1;
                  int x = oldX - e.getX();
                  if (x > 2) {
                      f = -1;
                  } else if (x < -2) {
                      f = 1;
                  } else { //If the move is too short, do nothing and return
                      drag = -1;
                      arrow.setDraw(false);
                      notes.setDraw(false);
                      repaint();
                      return;
                  }
                  if (appli.getOptions().getDragTime()) {
                      switch (zoomValue) {
                          case ONE_WEEK:
                          case TWO_WEEK:
                              date.go(Calendar.WEEK_OF_YEAR, 1 * f);
                              break;
                          case ONE_MONTH:
                          case TWO_MONTH:
                          case THREE_MONTH:
                          case FOUR_MONTH:
                          case SIX_MONTH:
                              date.go(Calendar.MONTH, 1 * f);
                              break;
                          case ONE_YEAR:
                          case TWO_YEAR:
                          case THREE_YEAR:
                              date.go(Calendar.YEAR, 1 * f);
                              break;
                      }

                      appli.getResourcePanel().area.date = date.Clone();
                  }
              }

              if (arrow.getDraw() && typeSeletion == 2) {
                  //Search for the second task to link
                  int second = detectPosition(e.getX(), e.getY(), true);

                  //if a task has been found
                  if (second != -1) {
                      GanttPaintParam param = (GanttPaintParam) listOfParam.get(second);
                      //String taskName = param.name;

                      //GanttTask secondTask1 = GanttTask.getTaskByTaskID(param.taskID);
                      GanttTask secondTask = getTaskManager().getTask(param.taskID);//.createTask();

                      //if(!taskToMove.toString().equals(taskName) &&
                      //	!(tree.getTask(taskName).getDepend().contains(taskToMove.toString())))

                      if (taskToMove != null && secondTask != null) {

                          if (tree.checkDepend(taskToMove, secondTask /*taskName*/)) {

                              //GanttTask secondTask = tree.getTask(taskName);
                              try {
                                  getTaskManager().getDependencyCollection().createDependency(secondTask, taskToMove);
                              } catch (TaskDependencyException e1) {
                                  e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                              }
//                   taskToMove.addSuccessor(new GanttTaskRelationship(//
//	                    -1
//	                    //taskToMove.getTaskID()
//	                    , secondTask.getTaskID(), GanttTaskRelationship.FS, getTaskManager()));

                              //tree.forwardScheduling();

                              appli.setAskForSave(true);
                          }
                      }
                  }
              }

              arrow.setDraw(false);
              notes.setDraw(false);
              moveTask = -1;
              drag = -1;

              tree.forwardScheduling();

              repaint();

          }
      }
    });

    /** When mouse motion on widget */
    this.addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent e) {

        //Left button
        if (mouseButton == MouseEvent.BUTTON1) {
          if (moveTask >= 0) {
            int diff = e.getX() - oldX;
            float gra = (float) getWidth() / (float) getGranit(true);
            addTaskLength += (float) diff / gra;

            //drag a milestion task
            if (taskToMove.isMilestone()) {
              taskToMove.setStart(storeTaskStart.newAdd( (int) addTaskLength));
              oldX = e.getX();
              appli.setAskForSave(true);
              //tree.forwardScheduling();

              //change the note
              notes.setString(taskToMove.getStart().toString());
              notes.setX(e.getX());
            }

            //Change duration of task
            else if (typeSeletion == 0) {
              //change the note
              GanttCalendar enddate = taskToMove.getEnd().newAdd( -1);
              notes.setString(enddate.toString());
              notes.setX(e.getX());

              //change the duration of the selected task
              int newDuration = storeTaskLength + (int) (addTaskLength);
              if (newDuration <= 0) {
                newDuration = 1;
              }
              taskToMove.setLength(newDuration);
              //tree.forwardScheduling();
              //tree.refreshAllChild(taskToMove.toString());
              //tree.refreshAllFather(taskToMove.toString());
              oldX = e.getX();
              appli.setAskForSave(true);
            }
            //change the start date of task
            /*else if(typeSeletion==1){
             int taskDuration=storeTaskLength-(int)addTaskLength;
             //GanttCalendar beginDate = taskToMove.getStart().Clone();
             GanttCalendar endDate = taskToMove.getEnd().Clone();
             if(taskDuration>=1)taskToMove.setStart(storeTaskStart.newAdd((int)addTaskLength));
             if(taskDuration<=0) taskDuration=1;
             //taskToMove.setLength(taskDuration);
             //tree.refreshAllChild(taskToMove.toString());
             tree.refreshAllFather(taskToMove.toString());
             oldX = e.getX();
             tree.checkTheDepend(taskToMove);
             taskToMove.setLength(endDate.diff(taskToMove.getStart()));
             appli.seAskForSave(true);
             //change the note
             notes.setString(taskToMove.getStart().toString());
             notes.setX(e.getX());
                   }*/
            else if (typeSeletion == 1) {
              if (addTaskLength > 0) {
                int taskDuration = storeTaskLength - (int) addTaskLength;
                if (taskDuration >= 1) {
                  taskToMove.setStart(storeTaskStart.newAdd( (int)
                      addTaskLength));
                }
                if (taskDuration <= 0) {
                  taskDuration = 1;
                }
                taskToMove.setLength(taskDuration);
                //tree.forwardScheduling();
                //tree.refreshAllChild(taskToMove.toString());
                //tree.refreshAllFather(taskToMove.toString());

                //change the note
                notes.setString(taskToMove.getStart().toString());
                notes.setX(e.getX());
              }
              // joshh Added else statement April 20, 2003
              else if (addTaskLength < 0) {
                int taskDuration = storeTaskLength - (int) addTaskLength;
                int temp = storeTaskLength + (int) addTaskLength;

                if (taskDuration >= 1) {
                  taskToMove.setStart(storeTaskStart.newAdd( (int)
                      addTaskLength));
                }
                else {
                  taskDuration = 1;

                }
                taskToMove.setLength(taskDuration);
                //tree.forwardScheduling();
                //tree.refreshAllChild(taskToMove.toString());
                //!@#
                //tree.refreshAllParent(taskToMove.toString()); // To fix reverse dependencies
                //taskToMove.setLength(storeTaskLength);

                //tree.refreshAllFather(taskToMove.toString());

                //change the note
                notes.setString(taskToMove.getStart().toString());
                notes.setX(e.getX());

              }
              oldX = e.getX();
              appli.setAskForSave(true);
            }
            //drage the percent of the task
            else if (typeSeletion == 3) {
              int percent = e.getX();
              if (percent < storeX[0]) {
                percent = storeX[0];
              }
              if (percent > storeX[1]) {
                percent = storeX[1];

              }
              notes.setX(percent);

              percent = (percent - storeX[0]) * 100 / (storeX[1] - storeX[0]);
              taskToMove.setCompletionPercentage(percent);

              notes.setString("  " + percent + "%");

              oldX = e.getX();
              appli.setAskForSave(true);
            }

            //move the arrow
            else if (typeSeletion == 2) {
              arrow.changePoint2(e.getX(), e.getY());
              drag = -1;
              for (int i = 0; i < listOfParam.size() && drag == -1; i++) {
                GanttPaintParam param = (GanttPaintParam) listOfParam.get(i);
                int y = param.y * 20 + 28 - margY;
                int x1 = param.x1;
                int x2 = param.x2;
                if ( (e.getY() >= y && e.getY() <= y + 12) &&
                    (e.getX() > x1 + 3 && e.getX() < x2 - 3)) {
                  drag = i;
                }
              }
            }
            repaint();
          }
        }
        //middle button drag move all the project
        else if (mouseButton == MouseEvent.BUTTON2) {
          int diff = e.getX() - oldX;
          float gra = (float) getWidth() / (float) getGranit(true);
          addTaskLength += (float) diff / gra;
            TaskLength duration = taskToMove.getDuration();
          taskToMove.setStart(storeTaskStart.newAdd( (int)addTaskLength));
            taskToMove.setDuration(duration);
					oldX = e.getX();
          appli.setAskForSave(true);
					//tree.forwardScheduling();
          //repaint();
        }
      }

      //Move the move on the area
      public void mouseMoved(MouseEvent e) {
        int detect = detectPosition(e.getX(), e.getY(), false);
        if (detect == -1) {
          if (curs) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            curs = false;
          }
          arrow.setDraw(false);
          notes.setDraw(false);
        }
        else {
          if (typeSeletion == 0) {
            setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
            curs = true;
          }
          //special cursor
          else if (typeSeletion == 3) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            try {
              setCursor(toolkit.createCustomCursor(toolkit.getImage(getClass().
                  getClassLoader().getResource("icons/cursorpercent.gif")),
                  new Point(10, 5), "CursorPercent"));
            }
            catch (Exception exept) {
              setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
            }
          }
          else if (typeSeletion == 1) {
            setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
            curs = true;
          }
          else {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
          }
        }
      }
    });

    //creation of the different color use to paint
    //arrayColor[0] = new Color((float)0.905,(float)0.905,(float)0.905);
    arrayColor[0] = new Color( (float) 0.930, (float) 0.930, (float) 0.930);
    arrayColor[1] = new Color( (float) 0.745, (float) 0.745, (float) 0.745);
    arrayColor[2] = new Color( (float) 0.843, (float) 0.890, (float) 0.910);
    arrayColor[3] = new Color( (float) 0.722, (float) 0.765, (float) 0.780);
    arrayColor[4] = new Color( (float) 0.482, (float) 0.482, (float) 0.482);
    arrayColor[5] = new Color( (float) 0.807, (float) 0.807, (float) 0.807);
    arrayColor[6] = taskDefaultColor;
    arrayColor[7] = new Color( (float) 0.741, (float) 0.745, (float) 0.741);
    arrayColor[8] = new Color( (float) 0.388, (float) 0.396, (float) 0.388);
    arrayColor[9] = new Color( (float) 0.196, (float) 0.196, (float) 0.196);
    arrayColor[10] = new Color( (float) 0.192, (float) 0.298, (float) 0.525);
    arrayColor[11] = new Color( (float) 0.141, (float) 0.141, (float) 0.141);
    arrayColor[12] = new Color( (float) 0.290, (float) 0.349, (float) 0.643);
    arrayColor[13] = new Color( (float) 0.851, (float) 0.902, (float) 0.937);
    arrayColor[14] = new Color( (float) 0.961, (float) 0.961, (float) 0.961);
  }

  /** Return the color of the task */
  public Color getTaskColor() {
    return myProjectLevelTaskColor==null ? myUIConfiguration.getTaskColor() : myProjectLevelTaskColor;
  }

  /** Change the color of the task */
  public void setProjectLevelTaskColor(Color c) {
    myProjectLevelTaskColor = c;
  }

  /** Detect if the position of the mouse is on a special place (return -1 if nothing or the numer of the task*/
  public int detectPosition(int mx, int my, boolean all) {
    for (int i = 0; i < listOfParam.size(); i++) {
      GanttPaintParam param = (GanttPaintParam) listOfParam.get(i);
      if ( ( (param.type == 2 || param.type == 0) && !all) || all) {
        int y = param.y * 20 + 27 - margY;
        int x1 = param.x1;
        int x2 = param.x2;
        int x3 = param.x3;

        if ( (my >= y && my <= y + 12)) {
          //The end of the task
          if (mx >= x2 /*-2*/ && mx <= x2 + 2) {
            typeSeletion = 0;
            setTaskToMove(i);
            GanttCalendar enddate = taskToMove.getEnd().newAdd( -1);
            notes = new Notes(enddate.toString(), mx, y - 30);
            arrow.setDraw(false);
            return i;
          }
          //the start of the task
          else if (mx >= x1 - 2 && mx <= x1 /*+2*/) {
            typeSeletion = 1;
            setTaskToMove(i);
            notes = new Notes(taskToMove.getStart().toString(), mx, y - 30);
            arrow.setDraw(false);
			      return i;
          }
          //the percent length
          else if (mx >= x3 - 2 && mx <= x3 + 2) {
            typeSeletion = 3;
            setTaskToMove(i);
            notes = new Notes("  " + taskToMove.getCompletionPercentage() + "%", mx, y - 30);
            arrow.setDraw(false);
            return i;
          }

          //A depend
          else if (mx > x1 + 3 && mx < x2 - 3) {
            typeSeletion = 2;

            if (param.type != 0) {
              arrow = new Arrow(mx, y + 6, mx, y + 6);
              notes.setDraw(false);
            }
            else {
              setTaskToMove(i);
              notes = new Notes(taskToMove.getStart().toString(), mx, y - 30);
            }

            return i;
          }
        }
      }
    }
    return -1;
  }

  /** The te task we want to move */
  public void setTaskToMove(int index) {
    
	GanttPaintParam param = (GanttPaintParam) listOfParam.get(index);
	taskToMove=getTaskManager().getTask(param.taskID);
	
    /*for (Iterator it = listOfTask.iterator(); it.hasNext(); ) {
      DefaultMutableTreeNode nextTreeNode = (DefaultMutableTreeNode) it.next();
      GanttTask task = (GanttTask) nextTreeNode.getUserObject();

      //if(!task.toString().equals("None"))
      {
        int ID2 = task.getTaskID();

        if (ID2 == tID) {

          //taskToMove = GanttTask.getTaskByTaskID(ID2);
          taskToMove = task;
        }
      }
    }*/
  }

  /** The size of the panel. */
  public Dimension getPreferredSize() {
    return new Dimension(465, 600);
  }

  /** Method to change the date */
  public void changeDate(boolean next) {
    int f = 1;
    if (!next) {
      f = -1;

    }
    switch (zoomValue) {
      case ONE_WEEK:
      case TWO_WEEK:
        date.go(Calendar.WEEK_OF_YEAR, 1 * f);
        break;
      case ONE_MONTH:
      case TWO_MONTH:
      case THREE_MONTH:
      case FOUR_MONTH:
      case SIX_MONTH:
        date.go(Calendar.MONTH, 1 * f);
        break;
      case ONE_YEAR:
      case TWO_YEAR:
      case THREE_YEAR:
        date.go(Calendar.YEAR, 1 * f);
        break;
    }
  }
	
	/** Method to change the date */
  public void changeDate2(GanttCalendar gc) {

		switch (zoomValue) {
              case ONE_WEEK:    gc.go(Calendar.WEEK_OF_YEAR, 1); break;
              case TWO_WEEK:    gc.go(Calendar.WEEK_OF_YEAR, 2); break;
              case ONE_MONTH:   gc.go(Calendar.MONTH, 1); break;
              case TWO_MONTH:   gc.go(Calendar.MONTH, 2); break;
              case THREE_MONTH: gc.go(Calendar.MONTH, 3); break;
							case FOUR_MONTH:  gc.go(Calendar.MONTH, 4); break;
              case SIX_MONTH:   gc.go(Calendar.MONTH, 6); break;
              case ONE_YEAR:    gc.go(Calendar.YEAR, 1);break;
              case TWO_YEAR:    gc.go(Calendar.YEAR, 2);break;
              case THREE_YEAR:  gc.go(Calendar.YEAR, 3);break;
//							default:System.out.println("Gantt : Should never append -->  zoomValue="+zoomValue);
            }
  }

  /** Method when zoomin, to set at the begin for each value */
  public void zoomToBegin() {
    switch (zoomValue) {
      case ONE_WEEK:
      case TWO_WEEK:
        String d = language.getDay(date.getDayWeek());
        while (!d.equals(language.getDay(2))) {
          date.add(1);
          d = language.getDay(date.getDayWeek());
        }

        break;
      case ONE_MONTH:
      case TWO_MONTH:
      case THREE_MONTH:
      case FOUR_MONTH:
      case SIX_MONTH:
        date.setDay(1);
        break;
      case ONE_YEAR:
      case TWO_YEAR:
      case THREE_YEAR:
        date.setMonth(0);
        date.setDay(1);
        break;
    }
  }

  /** draw the panel */
  public void paintComponent(Graphics g) {
    //Super paint component!!!!!!!!!!
    super.paintComponent(g);

    //Get all task
    listOfTask = tree.getAllTasks();

    //Vertical bars
    paintCalendar1(g);
    //The tasks
    paintTasks(g);
    //The depends
    if (drawdepend) {
      paintDepend(g);
      //The part at top
    }
    paintCalendar2(g);

    arrow.paint(g);
    notes.paint(g);

    if (drawVersion) {
      	drawGPVersion(g);			
    }
  }
	
	public void drawGPVersion(Graphics g){
		g.setColor(Color.black);
      g.setFont(new Font("SansSerif", Font.PLAIN, 10));
      g.drawString("GanttProject (" + GanttProject.version + ")", 3, getHeight() - 6);
	}
	

  /** Is the Task visible on the JTree */
  public boolean isVisible(Task thetask) {

    boolean res = true;
    //ArrayList expand = tree.getExpand();
    DefaultMutableTreeNode father = tree.getFatherNode(thetask);

    //The roor task is not visible
    if (father == null) {
      return false;
    }

    while (father != null) {
      //if (!expand.contains(new Integer(((Task)(father.getUserObject())).getTaskID()))) {
      Task taskFather = (Task)(father.getUserObject()); 
      if(!taskFather.getExpand()) 
        res = false;
      
      //Task t = (Task)father.getUserObject();
      //father = tree.getFatherNode(t);
      	father = (DefaultMutableTreeNode)(father.getParent());
    }

    return res;

  }

  /** Search for a coef on the arraylist */
  public int indexOf(ArrayList listOfParam, int id) { //String coef)
    int i = 0;

    for (Iterator it = listOfParam.iterator(); it.hasNext(); ++i) {
      if (id == ( (GanttPaintParam) it.next()).taskID) {
        return i;
      }
    }
    return -1;
  }

  /** Change the velue connected to the JTree's Scrollbar */
  public void setScrollBar(int v) {
    margY = v;
  }

  /** Return the value of the JTree's Scrollbar */
  public int getScrollBar() {
    return margY;
  }

  /** Change the zoom value */
  public void setZoom(int z) {
    zoomValue = z;
  }

  /** Add a zoom*/
  public void zoomMore() {
    if (zoomValue == 5) {
      olddate = date.Clone();
    }
    zoomValue++;
  }

  /**Less a zoom*/
  public void zoomLess() {
    if (zoomValue == 6 && date.getYear() == olddate.getYear()) {
      date = olddate.Clone();
    }
    zoomValue--;
  }

  /** Return  the zoom value */
  public int getZoom() {
    return zoomValue;
  }

  /** Change the date of the begin to paint */
  public void setDate(GanttCalendar d) {
    date = d;
  }

  /** Return the date */
  public GanttCalendar getDate() {
    return date;
  }

  /** Return the number of day visible for each level of granularity */
  public int getGranit(boolean day) {
    GanttCalendar cal;
    int res = 7; //by default the 7 days of the week
    switch (zoomValue) {
      case ONE_WEEK:
        res = 7;
        break;

      case TWO_WEEK:
        res = 14;
        break;

      case ONE_MONTH:
        res = date.getNumberOfDay();
        break;

      case TWO_MONTH:
        cal = date.Clone();
        res = cal.getNumberOfDay();
        cal.goNextMonth();
        res += cal.getNumberOfDay();
        break;

      case THREE_MONTH:
        cal = date.Clone();
        res = 0;
        for (int i = 0; i < 3; i++) {
          res += cal.getNumberOfDay();
          cal.goNextMonth();
        }
        break;

      case FOUR_MONTH:
        cal = date.Clone();
        res = 0;
        for (int i = 0; i < 4; i++) {
          res += cal.getNumberOfDay();
          cal.goNextMonth();
        }
        break;

      case SIX_MONTH:
        cal = date.Clone();
        res = 0;
        for (int i = 0; i < 6; i++) {
          res += cal.getNumberOfDay();
          cal.goNextMonth();
        }
        break;

      case ONE_YEAR:
        if (!day) {
          res = 12;
        }
        else {
          res = date.getActualMaximum(Calendar.DAY_OF_YEAR);
        }
        break;

      case TWO_YEAR:
        if (!day) {
          res = 12 * 2;
        }
        else {
          cal = date.Clone();
          res = 0;
          for (int i = 0; i < 2; i++) {
            res += cal.getActualMaximum(Calendar.DAY_OF_YEAR);
            cal.go(Calendar.YEAR, 1);
          }
        }
        break;

      case THREE_YEAR:
        if (!day) {
          res = 12 * 3;
        }
        else {
          cal = date.Clone();
          res = 0;
          for (int i = 0; i < 3; i++) {
            res += cal.getActualMaximum(Calendar.DAY_OF_YEAR);
            cal.go(Calendar.YEAR, 1);
          }
        }
        break;
    }
    return res;
  }

  /** Return the advance foot  */
  public int getFoot() {
    int res = 1;
    switch (zoomValue) {
      case ONE_YEAR:
      case TWO_YEAR:
      case THREE_YEAR:
        res = date.getNumberOfDay();
        break;
        //default:res=1;break;
    }

    return res;
  }


  public void calcProjectBegAndEnd() {
			beg = date.Clone();
	    beg.add(getGranit(true));
	    end = date.Clone();
	    end.add( -1);

			if(listOfTask==null) 
				listOfTask=tree.getAllTasks();
			
			
	    for (int i = 0; i < listOfTask.size(); i++) {
	      Task task = (Task) ( (DefaultMutableTreeNode) listOfTask.get(i)).
	          getUserObject();
	      if (! ( (DefaultMutableTreeNode) listOfTask.get(i)).isRoot()) {
	        if (beg.compareTo(task.getStart()) > 0) {
	          beg = task.getStart();
	        }
	        if (end.compareTo(task.getEnd()) < 0) {
  	        end = task.getEnd();
	        }
		      }
	    }			
			appli.getResourcePanel().area.beg = beg;
			appli.getResourcePanel().area.end = end;
	}


  /** Paint the vertical bars */
  public void paintCalendar1(Graphics g) {
		int sizex = getWidth();
    int sizey = getHeight();	
    int headery = 45;
    int gra = sizex / getGranit(false); //The granularity
    int gra2 = getGranit(false);
    float fgra = (float) sizex / (float) getGranit(false);
    int drawDate = -1;
    GanttCalendar dateToPaint = new GanttCalendar();
		

		
    g.setFont(new Font("SansSerif", Font.PLAIN, 12));

    //Reset the background to white
    g.setColor(Color.white);
		g.fillRect(0, 0, sizex, sizey);
		

    if(!printRendering) {
			calcProjectBegAndEnd();
		}
    //end.add(1);

    //Draw Horizontal bar on tasks
    /*g.setColor(arrayColor[14]);
       for(int i=-margY-4;i<getHeight();i+=40)
       {
     g.fillRect(0,i,sizex,20);
       }*/

    GanttCalendar tmpdate;
    if (zoomValue == ONE_WEEK || zoomValue == TWO_WEEK) {
      tmpdate = date.Clone();
    }
    else {
      tmpdate = new GanttCalendar(date.getYear(), date.getMonth(), 1);
    }
    g.setFont(new Font("SansSerif", Font.PLAIN, 10));


    //Draw the vertical bars
    for (int i = 0; i < gra2; i++) {
      String sDay = tmpdate.getdayWeek();
      String sMonth = tmpdate.getdayMonth();

      //For each day
      if (zoomValue < 7) {
        if (sDay.equals(language.getDay(6))) {
          g.setColor(arrayColor[0]);
          g.fillRect( (int) (fgra * i), headery, gra + 1 /*(int)(fgra+0.5)*/,sizey);

          g.setColor(arrayColor[1]);
          if(draw3dBorders)
          	g.drawLine( (int) (fgra * i), headery, (int) (fgra * i),sizey + headery);

        }
        else if (sDay.equals(language.getDay(0))) {
          g.setColor(arrayColor[0]);
          g.fillRect( (int) (fgra * i), headery, gra + 1 /*(int)(fgra+0.5)*/,sizey);
          if (zoomValue == ONE_WEEK || zoomValue == TWO_WEEK) {
            g.setColor(arrayColor[1]);
            if(draw3dBorders)
            	g.drawLine( (int) (fgra * i), headery, (int) (fgra * i),sizey + headery);
          }
        }

      }
      else { //For each month

        if (sMonth.equals(language.getMonth(0))) {
          g.setColor(arrayColor[0]);
          g.fillRect( (int) (fgra * i), headery, (int) fgra, sizey);
        }
        g.setColor(arrayColor[1]);
        if(draw3dBorders)
        	g.drawLine( (int) (fgra * i), headery, (int) (fgra * i),sizey + headery);

      }

      if (zoomValue < 7) {
        if (tmpdate.compareTo(new GanttCalendar()) == 0) {
          g.setColor(arrayColor[2]);
          g.fillRect( (int) (fgra * i), headery, (int) fgra, sizey);
          g.setColor(arrayColor[3]);
          if(draw3dBorders)
          	g.drawLine( (int) (fgra * i), headery, (int) (fgra * i),sizey + headery);
        }
				// stavrides - red line at all zooms
        if(myUIConfiguration.isRedlineOn())
				{
					GanttCalendar today = new GanttCalendar();
	        float s = fgra / (float) tmpdate.getNumberOfDay();
	        if (tmpdate.equals(today)) {
	              g.setColor(Color.red);
	              g.drawLine(
	                    (int) (fgra * i) + (int) (s * today.getDay()),      headery,
	                    (int) (fgra * i) + (int) (s * today.getDay()),      sizey + headery);
        	}
				}
      }
      else if(myUIConfiguration.isRedlineOn()){
        GanttCalendar today = new GanttCalendar();
        float s = fgra / (float) tmpdate.getNumberOfDay();
        if (tmpdate.getYear() == today.getYear() &&
            tmpdate.getMonth() == today.getMonth()) {
	  //stavrides - remove blue box in favor of line
          //Blue rectange on today
          //g.setColor(arrayColor[2]);
          //g.fillRect( (int) (fgra * i) + (int) (s * today.getDay()), headery,
          //           ( (s < 2) ? 2 : (int) s), sizey);
          //g.setColor(arrayColor[2]);
          g.setColor(Color.red);
          g.drawLine( (int) (fgra * i) + (int) (s * today.getDay()), headery,
                     (int) (fgra * i) + (int) (s * today.getDay()),
                     sizey + headery);

        }
      }

      tmpdate.add(getFoot());
    }
		

    if (zoomValue == ONE_WEEK || zoomValue == TWO_WEEK) {
      tmpdate = date.Clone();
    }
    else {
      tmpdate = new GanttCalendar(date.getYear(), date.getMonth(), 1);

      //Draw Miscallenous bars
    }
    for (int i = 0; i < gra2; i++) {
      //Courant day in blue
      if (zoomValue < 7) {
        //A line on begin and end of project
        if (tmpdate.compareTo(beg) == 0 || tmpdate.compareTo(end) == 0) {
          g.setColor(arrayColor[12]);
          //g.drawLine((int)(fgra*i),headery,(int)(fgra*i),sizey+headery);
          drawVerticalLinedash(g, (int) (fgra * i), headery, sizey + headery, 5);
          if (tmpdate.compareTo(beg) == 0) {
            drawDate = (int) (fgra * i) - 52;
            dateToPaint = beg;
          }
          else {
            drawDate = (int) (fgra * i) + 3;
            dateToPaint = end.newAdd( -1);
          }
        }
      }
      else {

        float s = fgra / (float) tmpdate.getNumberOfDay();
        //blue dash line for begin
        if (tmpdate.getYear() == beg.getYear() &&
            tmpdate.getMonth() == beg.getMonth()) {
          g.setColor(arrayColor[12]);
          //g.drawLine((int)(fgra*i)+(int)(s*beg.getDay()),headery,(int)(fgra*i)+(int)(s*beg.getDay()),sizey+headery);
          int xValue = (int) (fgra * i) + (int) (s * beg.getDay());
		  		drawVerticalLinedash(g, (int) xValue,
                               headery, sizey + headery, 5);
          drawDate = xValue - 52;
          dateToPaint = beg;
        }
        //blue dash line for end
        else if (tmpdate.getYear() == end.getYear() &&
                 tmpdate.getMonth() == end.getMonth()) {
          g.setColor(arrayColor[12]);
          //g.drawLine((int)(fgra*i)+(int)(s*end.getDay()),headery,(int)(fgra*i)+(int)(s*end.getDay()),sizey+headery);
          int xValue = (int) (fgra * i) + (int) (s * end.getDay());
		  		drawVerticalLinedash(g, xValue,
                               headery, sizey + headery, 5);
          drawDate = xValue;
          dateToPaint = end.newAdd( -1);
        }
      }

      //Draw the date in string on calendar
      if (drawDate >= 0) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g.setColor(arrayColor[9]);
        g.drawString(dateToPaint.toString(), drawDate, headery + 20);
        drawDate = -1;
      }

      tmpdate.add(getFoot());
    }
  }

  public void paintCalendar2New(Graphics g) {
      ChartModel model = new ChartModelImpl();
      model.setStartDate(GregorianCalendar.getInstance().getTime());
      model.setBounds(getSize());
      model.setBottomUnitWidth(20);
      model.setTopTimeUnit(GregorianTimeUnitStack.MONTH);
      model.setBottomTimeUnit(GregorianTimeUnitStack.DAY);
      model.getChartHeader().paint(g);
  }
  
  public void paintCalendar2(Graphics g) {
      paintCalendar2Old(g);
  }
  
  /** Draw the legend of the calendar */
  public void paintCalendar2Old(Graphics g) {
    int sizex = getWidth();
    int sizey = getHeight();
    
		
		
		int headery = 45;
    int gra2 = getGranit(false);
    float fgra = (float) (sizex) / (float) getGranit(false); //the granularity
		
		
		
    g.setFont(new Font("SansSerif", Font.PLAIN, 12));

		
    //gray rectangle with nice borders
    g.setColor(arrayColor[0]);
    g.fillRect(0, 0, sizex, headery);
    g.setColor(arrayColor[4]);
    g.drawRect(0, 0, sizex - 1, headery / 2);
    g.drawRect(0, headery / 2, sizex - 1, headery / 2);
    /*if(draw3dBorders) {
    	g.setColor(arrayColor[5]);
    	g.drawLine(1, headery / 2 - 1, sizex - 2, headery / 2 - 1);
    	g.drawLine(0, headery - 2, sizex - 2, headery - 2);
    	g.setColor(Color.white);
    	g.drawLine(1, 1, sizex - 2, 1);
    	g.drawLine(0, headery / 2 + 1, sizex - 2, headery / 2 + 1);
    }*/

    g.setColor(Color.black);
    //Set the text at the top (differetns for each zaoom value)
    float posX;
    GanttCalendar cal;
    switch (zoomValue) {
      //The number of week and th month
      case ONE_WEEK:
        g.drawString(language.getText("week") + date.getWeek() + " - " +
                     date.getdayMonth() + "  " + date.getYear(), 2,
                     headery / 2 - 5);
        break;

      case TWO_WEEK:
      	GanttCalendar date_2 = date.newAdd(7);
        g.drawString(language.getText("week") + (date.getWeek()) + " - " +
                     date.getdayMonth() + "  " + date.getYear(), 2,
                     headery / 2 - 5);
        g.drawString(language.getText("week") + (date_2.getWeek() + 1) + " - " +
        		date_2.getdayMonth() + "  " + date_2.getYear(),
                     (int) (fgra * 7) + 2, headery / 2 - 5);
        break;

        //The month and the yeay
      case ONE_MONTH:
        g.drawString(date.getdayMonth() + "  " + date.getYear(), 2,
                     headery / 2 - 5);
        break;

        // Draw two months
      case TWO_MONTH:
        cal = date.Clone();
        posX = 0;
        for (int i = 0; i < 2; i++) {
          g.drawString(cal.getdayMonth() + "  " + cal.getYear(),
                       (int) posX + 2 /*i*sizex/3+2*/, headery / 2 - 5);
          posX += ( (float) cal.getNumberOfDay() * fgra);
          cal.goNextMonth();
        }
        break;

        //Draw three months
      case THREE_MONTH:
        cal = date.Clone();
        posX = 0;
        for (int i = 0; i < 3; i++) {
          g.drawString(cal.getdayMonth() + "  " + cal.getYear(),
                       (int) posX + 2 /*i*sizex/3+2*/, headery / 2 - 5);
          posX += ( (float) cal.getNumberOfDay() * fgra);
          cal.goNextMonth();
        }
        break;

        // draw 4 months
      case FOUR_MONTH:
        cal = date.Clone();
        posX = 0;
        for (int i = 0; i < 4; i++) {
          String dm = cal.getdayMonth();
          g.drawString(dm.substring(0,
                                    (sizex > 480) ?
                                    (dm.length() < 3 ? dm.length() : 3) : 1) +
                       "  " + new
                       Integer(cal.getYear()).toString().substring(2, 4),
                       (int) posX + 2 /*i*sizex/3+2*/, headery / 2 - 5);
          posX += ( (float) cal.getNumberOfDay() * fgra);
          cal.goNextMonth();
        }
        break;

        // draw 6 months
      case SIX_MONTH:
        cal = date.Clone();
        posX = 0;
        for (int i = 0; i < 6; i++) {
          String dm = cal.getdayMonth();
          g.drawString(dm.substring(0,
                                    (sizex > 480) ?
                                    (dm.length() < 3 ? dm.length() : 3) : 1) +
                       "  " + new
                       Integer(cal.getYear()).toString().substring(2, 4),
                       (int) posX + 2 /*i*sizex/3+2*/, headery / 2 - 5);
          posX += ( (float) cal.getNumberOfDay() * fgra);
          cal.goNextMonth();
        }
        break;

        //Draw One year
      case ONE_YEAR:
        g.drawString("" + date.getYear(), 2, headery / 2 - 5);
        break;

        // Draw two years
      case TWO_YEAR:
        int dy2 = date.getYear();
        for (int i = 0; i < 2; i++, dy2++) {
          g.setColor(Color.black);
          g.drawString("" + dy2, i * sizex / 2 + 2, headery / 2 - 5);
        }
        break;

        //draw thre years
      case THREE_YEAR:
        int dy = date.getYear();
        for (int i = 0; i < 3; i++, dy++) {
          g.setColor(Color.black);
          g.drawString("" + dy, i * sizex / 3 + 2, headery / 2 - 5);
        }
        break;
    }

    GanttCalendar tmpdate;
    if (zoomValue == ONE_WEEK || zoomValue == TWO_WEEK) {
      tmpdate = date.Clone();
    }
    else {
      tmpdate = new GanttCalendar(date.getYear(), date.getMonth(), 1);
    }
    g.setFont(new Font("SansSerif", Font.PLAIN, 10));

    //Draw each day or each month
    for (int i = 0; i < gra2; i++) {
      String sDay = tmpdate.getdayWeek();
      String sMonth = tmpdate.getdayMonth();

      g.setColor(Color.black);
      switch (zoomValue) {
        case ONE_WEEK:
          g.drawString(sDay.substring(0,
                                      (sizex > 300) ?
                                      (sDay.length() < 3 ? sDay.length() : 3) :
                                      1) + " " + tmpdate.getDate(),
                       (int) (fgra * i) + 2, headery - 7);
          g.setColor(arrayColor[4]);
          g.drawLine( (int) (fgra * i), headery / 2, (int) (fgra * i),
                     headery - 1);
          break;
        case TWO_WEEK:
          g.drawString(sDay.substring(0, 1) + " " + tmpdate.getDate(),
                       (int) (fgra * i) + 2, headery - 7);
          g.setColor(arrayColor[4]);
          g.drawLine( (int) (fgra * i), headery / 2, (int) (fgra * i),
                     headery - 1);
          break;

        case ONE_MONTH:
          g.drawString("" + tmpdate.getDate(), (int) (fgra * i) + 3,
                       headery - 7);
          g.setColor(arrayColor[4]);
          g.drawLine( (int) (fgra * i), headery / 2, (int) (fgra * i),
                     headery - 1);
          break;

        case TWO_MONTH:
        case THREE_MONTH:
        case FOUR_MONTH:
        case SIX_MONTH:
          if (sDay.equals(language.getDay(1))) {
            g.drawString("" + tmpdate.getDate(), (int) (fgra * i) + 2,
                         headery - 7);
            g.setColor(arrayColor[4]);
            g.drawLine( (int) (fgra * i), headery / 2, (int) (fgra * i),
                       headery - 1);
          }
          break;          
        case ONE_YEAR:
          g.drawString(sMonth.substring(0,
                                        (sizex > 300) ?
                                        (sMonth.length() < 3 ? sMonth.length() :
                                         3) : 1), (int) (fgra * i) + 2,
                       headery - 7);
          g.setColor(arrayColor[4]);
          g.drawLine( (int) (fgra * i), headery / 2, (int) (fgra * i),
                     headery - 1);
          break;

        case TWO_YEAR:
        case THREE_YEAR:
          g.drawString(sMonth.substring(0, 1), (int) (fgra * i) + 2,
                       headery - 7);
          g.setColor(arrayColor[4]);
          g.drawLine( (int) (fgra * i), headery / 2, (int) (fgra * i),
                     headery - 1);
          break;
      }
      //next date
      tmpdate.add(getFoot());
    }
		
  }

  /** Paint all tasks  */
  public void paintTasks(Graphics g) {
      int sizex = getWidth();
      int sizey = getHeight();
      int headery = 45;
      float fgra = (float) sizex / (float) getGranit(true);

      g.setFont(myUIConfiguration.getChartMainFont());

      //Get all task

      //Probably optimised on next release
      listOfParam.clear();

      int y = 0;

      for (Iterator tasks = listOfTask.iterator(); tasks.hasNext();) {
          DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) tasks.next();
          GanttTask task = (GanttTask) treeNode.getUserObject();

//Is the task is visible, the task could be draw
          if (isVisible(task)) {
              int x1 = -10, x2 = sizex + 10;
              int e1; //ecart entre la date de debut de la tache et la date du debut du calendrier
              int fois;
              int type = 2;
              y++;

//difference between the start date of the task and the end
              e1 = date.diff(task.getStart());

//Calcul start and end pixel of each task
              float fx1, fx2;

              if (task.isMilestone()) {
                  fx1 = (float) e1 * fgra *
                          ((date.compareTo(task.getStart()) == 1) ? -1 : 1);
                  x1 = (int) fx1;
              } else {
                  fx1 = (float) e1 * fgra *
                          ((date.compareTo(task.getStart()) == 1) ? -1 : 1);
                  fx2 = fx1 + (float) task.getLength() * fgra;
                  x1 = (int) fx1;
                  x2 = (int) fx2;
              }

              int percent = 0;

//Meeting task
              if (task.isMilestone()) {
                  paintATaskBilan(g, x1, y, task);
                  x2 = x1 + (int) fgra;
                  type = 0;
              }
//A mother task
              else if (tree.getAllChildTask(treeNode).size() != 0) {

                  //Compute percent-complete
                  tree.computePercentComplete(treeNode);

                  paintATaskFather(g, x1, x2, y, task);
                  if (drawPercent) {
                      percent = paintAdvancement(g, x1, x2, y, task.getCompletionPercentage(), task.getShape(),
                              task.getColor(), true);
                  }
                  type = 1;
              }
//A normal task
              else {
                  paintATaskChild(g, x1, x2, y, task);
                  if (drawPercent) {

                      percent = paintAdvancement(g, x1, x2, y, task.getCompletionPercentage(), task.getShape(),
                              task.getColor(), false);
                  }
                  type = 2;
              }

//Add parameters on the array
              listOfParam.add(new GanttPaintParam(task.getName(), task.getTaskID(),
                      x1, x2, percent, y, type));
          }
      }

  }

  /** Draw a monther task */
  public void paintATaskFather(Graphics g, int x1, int x2, int y, Task task) {
    int d = y;
    y = y * 20 + 35 - margY;

    if (y < 20 || y > getHeight()) {
      return; //Not draw if the task is not on the area
    }
    if ( (x1 > getWidth() && x2 > getWidth()) || (x1 < 0 && x2 < 0)) {
      return;
    }

    //Black rectangle
    if (drag == d - 1) {
      g.setColor(arrayColor[4]);
    }
    else {
      if (task.getColor().equals(myUIConfiguration.getTaskColor())) {
        g.setColor(Color.black);
      }
      else {
        g.setColor( /*Color.black*/task.getColor());
      }
    }
    g.fillRect(x1, y, x2 - x1, 2);

    //Little triangle at begin and end
    int xPoints[] = new int[3];
    int yPoints[] = new int[3];
    xPoints[0] = x1;
    xPoints[1] = x1 + 5;
    xPoints[2] = x1;
    yPoints[0] = y + 2;
    yPoints[1] = y + 2;
    yPoints[2] = y + 6;
    g.fillPolygon(xPoints, yPoints, 3);
    xPoints[0] = x2;
    xPoints[1] = x2 - 5;
    xPoints[2] = x2;
    g.fillPolygon(xPoints, yPoints, 3);

    //Draw the resource list after the task
		paintResources (x2+40, y+9, task, g);

  }

  /** Draw a normal task */
  public void paintATaskChild(Graphics g, int x1, int x2, int y, Task task) {
    int d = y;
    y = y * 20 + 27 - margY;

    if (y < 20 || y > getHeight()) {
      return; //Not draw if the task is not on the area
    }
    if ( (x1 > getWidth() && x2 > getWidth()) || (x1 < 0 && x2 < 0)) {
      return;
    }

    //Blue rectangle
      Color c = ((GanttTask)task).colorDefined() ? task.getColor() :myUIConfiguration.getTaskColor();
    g.setColor(c);
    g.fillRect(x1, y, (x2 - x1 - 1), 12);

    //Draw nice border
    //if(draw3dBorders)
    {	
    	if (drag == d - 1) {
    		g.setColor(arrayColor[0]);
    	}
    	else {
    		g.setColor(Color.black);
    	}
    	g.drawRect(x1, y, x2 - x1 - 1, 12);
    	
    	//AT
    	//This code print a gray border around the task
    	//BTW I've comment it for several reason
    	//- It' increase the time of rendering
    	//- when printing the char, this border isn't good
    	//- for the rendering, it's only beautiful with the default color, but not with another one
    	//- finally I found it nicer now :)
    	/*g.setColor(arrayColor[7]);
    	g.drawLine(x1 + 1, y + 1, (x2 - 1) - 1, y + 1);
    	g.drawLine(x1 + 1, y + 1, x1 + 1, y + 11);
    	
    	g.setColor(arrayColor[8]);
    	g.drawLine(x1 + 2, y + 11, (x2 - 1) - 2, y + 11);
    	g.drawLine(x2 - 2, y + 2, x2 - 2, y + 11);*/
  	}
    
	//Draw the resource list after the task
	paintResources (x2+40, y+10, task, g);

  }

  /** Draw a meeting task */
  public void paintATaskBilan(Graphics g, int x1, int y, Task task) {
    int d = y;
    y = y * 20 + 32 - margY;

    if (y < 20 || y - 5 > getHeight()) {
      return; //Not draw if the task is not on the area
    }
    if (x1 > getWidth() || x1 < 0) {
      return;
    }

    int gra = getWidth() / getGranit(false); //the granularity
    float fgra = (float) getWidth() / (float) getGranit(true);

    int xPoints[] = new int[4];
    int yPoints[] = new int[4];

    xPoints[0] = (int) ( (float) x1 + fgra / 2);
    xPoints[1] = (int) ( (float) x1 + fgra / 2 - 5);
    xPoints[2] = (int) ( (float) x1 + fgra / 2);
    xPoints[3] = (int) ( (float) x1 + fgra / 2 + 5);

    yPoints[0] = y - 5;
    yPoints[1] = y;
    yPoints[2] = y + 5;
    yPoints[3] = y;


    if (drag == d - 1) {
      g.setColor(arrayColor[4]);
    }
    else {
      if (task.getColor().equals(myUIConfiguration.getTaskColor())) {
        g.setColor(Color.black);
      }
      else {
        g.setColor(task.getColor());
      }
    }
    g.fillPolygon(xPoints, yPoints, 4);
    g.setColor(arrayColor[8]);
    g.drawPolygon(xPoints, yPoints, 4);

		//Draw the resource list after the task
		paintResources (x1+16, y+5, task, g);

  }
	
	/** Paint the assigned resources after the task */
	public void paintResources (int x, int y, Task task, Graphics g) {
		//Draw the assigned resources name
		//ArrayList users = task.getUsersList();
        ResourceAssignment[] assignments = task.getAssignments();
		if(assignments.length>0) {
			
			String resourceList="";
			
			for(int i=assignments.length-1;i>=0;i--) {
                ProjectResource next = assignments[i].getResource();
                resourceList+=next.getName();
                if(i!=0) {
                    resourceList+=", ";
                }
			}
		
			g.setColor(Color.black);
			g.drawString(resourceList,x,y);		
		}
	}
	
  /** Draw the arrows for depends */
  public void paintDepend(Graphics g) {
  
  
  	//for paint triangles
    int xPoints[] = new int[3];
    int yPoints[] = new int[3];
	
  
  	//Set the color to black
     g.setColor(Color.black);
	
	//Parsing all tasks
	for(Iterator tasks=listOfTask.iterator(); tasks.hasNext(); ) {
		//Get the task
		GanttTask task = (GanttTask)(((DefaultMutableTreeNode)tasks.next()).getUserObject());
		//Only if the task is visible
		if(isVisible(task)) {
			//Get all sucessors for the task
			Vector successors = task.getSuccessorsOld();
			//Parsing the sucessors
			for(Iterator suc=successors.iterator(); suc.hasNext();){
				//Get the relashionship
				GanttTaskRelationship relationship = (GanttTaskRelationship)suc.next();
				//Get the second task
				Task task2 = relationship.getSuccessorTask();
				//Only if the second task is visible
				if (this.isVisible(task2)) {
					//Get the start index and end index for param values
          int index1 = this.indexOf(listOfParam, task.getTaskID());
          int index2 = this.indexOf(listOfParam, task2.getTaskID());
					
					//System.out.println(task+"  "+task2+"  "+index1+" "+index2);
					try{
					//Y coords
					int yt1 = ( (GanttPaintParam) listOfParam.get(index1)).y;
          int yt2 = ( (GanttPaintParam) listOfParam.get(index2)).y;
					yt1 = yt1 * 20 + 32 - margY;
					yt2 = yt2 * 20 + 32 - margY;
					
					//Start-Start relashion
					if(relationship.getRelationshipType()==GanttTaskRelationship.SS) {
						//Get x coord
						int x1t1 = ( (GanttPaintParam) listOfParam.get(index1)).x1;
						int x1t2 = ( (GanttPaintParam) listOfParam.get(index2)).x1;
						
						int xa=(x1t1<x1t2)?x1t1-7:x1t2-7;
						
						//Draw Lines
						g.drawLine(x1t1, yt1, xa, yt1);
			            g.drawLine(xa, yt1, xa, yt2);
						g.drawLine(xa, yt2, x1t2, yt2);
						
						//Traiangle for task 1
						/*x1t1--;
						xPoints[0] = x1t1;
              			xPoints[1] = x1t1 - 3;
			            xPoints[2] = x1t1 - 3;
						yPoints[0] = yt1;
						yPoints[1] = yt1 - 4;
						yPoints[2] = yt1 + 4;
						g.fillPolygon(xPoints, yPoints, 3);*/
						//Traiangle for task 2
						x1t2--;
						xPoints[0] = x1t2;
              			xPoints[1] = x1t2 - 3;
			            xPoints[2] = x1t2 - 3;
						yPoints[0] = yt2;
						yPoints[1] = yt2 - 4;
						yPoints[2] = yt2 + 4;
						g.fillPolygon(xPoints, yPoints, 3);
					
					//Finish-Start relashion
					} else if(relationship.getRelationshipType()==GanttTaskRelationship.FS) {
						//Get x coord
						int x2t1 = ( (GanttPaintParam) listOfParam.get(index1)).x2;
						int x1t2 = ( (GanttPaintParam) listOfParam.get(index2)).x1;
						
						x1t2 += 2;
						
						xPoints[0] = x1t2;
              			xPoints[1] = x1t2 + 3;
			            xPoints[2] = x1t2 - 3;

			            if (yt1 > yt2) {
			              yt2 += 7;
			              yPoints[1] = yt2 + 4;
			              yPoints[2] = yt2 + 4;
			            }
			            else {
			              yt2 -= 6;
			              yPoints[1] = yt2 - 4;
			              yPoints[2] = yt2 - 4;
			            }
			            yPoints[0] = yt2;
			
						g.fillPolygon(xPoints, yPoints, 3);
			            g.drawLine(x2t1, yt1, x1t2, yt1);
			            g.drawLine(x1t2, yt1, x1t2, yt2);
					
					//Finish-Finish relashion
					} else if(relationship.getRelationshipType()==GanttTaskRelationship.FF) {
						//Get x coord
						int x2t1 = ( (GanttPaintParam) listOfParam.get(index1)).x2;
						int x2t2 = ( (GanttPaintParam) listOfParam.get(index2)).x2;
						
						int xa=(x2t1>x2t2)?x2t1+7:x2t2+7;
						
						//Draw Lines
						g.drawLine(x2t1, yt1, xa, yt1);
			            g.drawLine(xa, yt1, xa, yt2);
						g.drawLine(xa, yt2, x2t2, yt2);
						
						//Traiangle for task 1
						/*x2t1++;
						xPoints[0] = x2t1;
              			xPoints[1] = x2t1 + 3;
			            xPoints[2] = x2t1 + 3;
						yPoints[0] = yt1;
						yPoints[1] = yt1 - 4;
						yPoints[2] = yt1 + 4;
						g.fillPolygon(xPoints, yPoints, 3);*/
						//Traiangle for task 2
						x2t2++;
						xPoints[0] = x2t2;
              			xPoints[1] = x2t2 + 3;
			            xPoints[2] = x2t2 + 3;
						yPoints[0] = yt2;
						yPoints[1] = yt2 - 4;
						yPoints[2] = yt2 + 4;
						g.fillPolygon(xPoints, yPoints, 3);
					
					//Start-Finish relashion
					} else if(relationship.getRelationshipType()==GanttTaskRelationship.SF) {
						//Get x coord
						int x1t1 = ( (GanttPaintParam) listOfParam.get(index1)).x1;
						int x2t2 = ( (GanttPaintParam) listOfParam.get(index2)).x2;
					
						x2t2 -= 3;
						
						xPoints[0] = x2t2;
              			xPoints[1] = x2t2 + 3;
			            xPoints[2] = x2t2 - 3;

			            if (yt1 > yt2) {
			              yt2 += 7;
			              yPoints[1] = yt2 + 4;
			              yPoints[2] = yt2 + 4;
			            }
			            else {
			              yt2 -= 6;
			              yPoints[1] = yt2 - 4;
			              yPoints[2] = yt2 - 4;
			            }
			            yPoints[0] = yt2;
			
						g.fillPolygon(xPoints, yPoints, 3);
			            g.drawLine(x1t1, yt1, x2t2, yt1);
			            g.drawLine(x2t2, yt1, x2t2, yt2);
					
					}
					
					}catch(Exception e){}
				
				}//End of visible Task2			
			}//End of parsing the successors
		}//End of isVisible Task1
	}//End of parsing the iterator
	
  } //Enf of paintDepend function

  /** Paint advance state of the task */
  public int paintAdvancement(Graphics g, int x1, int x2, int y, int percent, ShapePaint shape,
                              Color color, boolean justText) {
    //draw the value at the end of task
    if(percent>0) {
			g.setColor(Color.black);
	    g.drawString("[" + percent + "%]", x2 + 8, y * 20 + 36 - margY);
		}

    float fp = (float) (x2 - x1) * (float) percent / 100;

    //Paint the bar
    if (!justText) {
	Graphics2D g2 = (Graphics2D) g;
      g2.setPaint(shape);
      g.fillRect(x1 + 1 , y * 20 + 27 - margY + 1,
                ( (x2 - x1 - 4) /*- (int) fp*/+1), 10);


      //To set a nice effets of gradient
      /*g2.setPaint(new GradientPaint(x1 + 2, y * 20 + 31 - margY + 2,
                                    arrayColor[10],
                                    x2 - 2, y * 20 + 31 - margY + 2,
                                    arrayColor[11]));
      g2.fill(new Rectangle(x1 + 2, y * 20 + 31 - margY, (x2 - x1) - 4, 4));*/
	  
	  g.setColor(Color.black);
	  g.fillRect(x1 + 2, y * 20 + 31 - margY+1, (x2 - x1) - 4, 3);

      g2.setPaint(shape);
      g.fillRect(x1 + 1 + (int) fp, y * 20 + 27 - margY + 1,
                 ( (x2 - x1 - 4) - (int) fp)+1, 10);

    }
    return (int) (x1 + fp);
  }

  /** Draw a vertical dash line from point (x1, y1) to (x2, y2) */
  public void drawVerticalLinedash(Graphics g, int x, int y1, int y2, int size) {
    int i = y1;
    int end = y2;
    if (y1 > y2) {
      i = y2;
      end = y1;
    }
    int size2 = size + size;
    while (i < end) {
      g.drawLine(x, i, x, i + size);
      i = i + size2;
    }
  }


	/** Return an image with the gantt chart*/
	public BufferedImage getChart(GanttExportSettings settings) {	
		
		calcProjectBegAndEnd();
		int rendering = 0;
		
		GanttCalendar date2 = new GanttCalendar(date);
		date = new GanttCalendar(beg);
		zoomToBegin();
		GanttCalendar  start =  new GanttCalendar(date);
		date = new GanttCalendar(date2);
		
		
		while(start.compareTo(end)<=0){
			
			changeDate2(start);
			rendering++;
		}
		
		
		//Make save of parameters
		int oldMargY = margY;
		int oldHeight=getHeight();
		int height = 20;
		int width=getWidth();
		margY=0;
		
		drawdepend = settings.depend;
		drawPercent = settings.percent;
		drawName = settings.name;
		draw3dBorders = settings.border3d;
    
		//change the Height of the panel
		
		int sizeTOC=0; //Size of the content table for the tasks list
		BufferedImage tmpImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		FontMetrics fmetric = tmpImage.getGraphics().getFontMetrics(new Font("SansSerif", Font.PLAIN, 10));
		
		ArrayList lot = getTree().getAllTasks();
    for (Iterator tasks = lot.iterator(); tasks.hasNext(); ) {
      DefaultMutableTreeNode nextTreeNode = (DefaultMutableTreeNode) tasks.next();
      Task next = (Task) nextTreeNode.getUserObject();
      if ("None".equals(next.toString())) {
        continue;
      }
      if (isVisible(next)) {
        height += 20;
				int nbchar = fmetric.stringWidth(next.getName());
				if(nbchar>sizeTOC)sizeTOC=nbchar;
      }
		}
		sizeTOC+=50;	//for the indentation of the tasks, but I fiw 50 pixel correcpond to 7 sub-taks indentation
	
		//If there is only the root task
		if(lot.size()==1){ setSize(width, oldHeight); rendering=1;}
		else setSize(width, height + 40);
    		
		int calculateWidth=width*rendering;
		if(drawName){
			rendering++;
			calculateWidth+=sizeTOC;
		}
		
		BufferedImage image = new BufferedImage(calculateWidth, getHeight(), BufferedImage.TYPE_INT_RGB);
		
		
		start=new GanttCalendar(date);
		date=new GanttCalendar(beg);
		zoomToBegin();
		if(zoomValue==ONE_WEEK || zoomValue==TWO_WEEK) changeDate(false);
		
		
		int transx=0, transx2;
		
		for(int i=0;i<rendering;i++){
			
			
			BufferedImage image2 = new BufferedImage(width, getHeight(), BufferedImage.TYPE_INT_RGB);
			if(i==0 && drawName){
				rowCount=0;
				image2 = null;
				image2 = new BufferedImage(sizeTOC, getHeight(), BufferedImage.TYPE_INT_RGB);
				setSize(sizeTOC, getHeight());
				printTasks(image2.getGraphics());
				setSize(width, getHeight());
				transx2=sizeTOC;
			}
			else { 
				paintComponent(image2.getGraphics());
				changeDate2(date);
				transx2=width;
			}
			
			
			Graphics g = image.getGraphics();
			g.translate(transx,0);
			g.drawImage(image2,0,0,null);
			
			image2=null;
			
			transx+=transx2;
			
		}
		upperLeft = new Point(0,0);
		drawGPVersion(image.getGraphics());		
		
		
		date=new GanttCalendar(start);
		zoomToBegin();
		margY=oldMargY;
		drawdepend = true;
		drawPercent = true;
		drawName = false;
		draw3dBorders = true;
		setSize(getSize().width, oldHeight);
		repaint();
		
		return image;		
	}

	private int rowCount=0;
	
	/** Print the list of tasks */
	private void printTasks(Graphics g){
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());

		g.setColor(Color.black);
		g.setFont(myUIConfiguration.getChartMainFont());
		
		printTask(g,5,42,getTree().getAllChildTask(getTree().getRoot()));
		
	}	
		
	private int printTask (Graphics g, int x, int y, ArrayList child) {
		
			for(Iterator tasks = child.iterator(); tasks.hasNext(); ) {
				DefaultMutableTreeNode nextTreeNode = (DefaultMutableTreeNode) tasks.next();
				Task next = (Task) nextTreeNode.getUserObject();
				
				if(isVisible(next)) {
					if(rowCount%2==1) {
						g.setColor(new Color( (float) 0.933, (float) 0.933, (float) 0.933));
						g.fillRect(0,y,getWidth()-10, 20);	
					}
					g.setColor(Color.black);
					g.drawRect(0,y,getWidth()-10, 20);	
					g.drawString(next.getName(), x, y+13);
					g.setColor(arrayColor[5]);
					if(draw3dBorders)
						g.drawLine(1,y+19,getWidth()-11,y+19);
					y+=20;
					rowCount++;
					if(nextTreeNode.getChildCount()!=0) {
						y=printTask(g, x+10, y, getTree().getAllChildTask(nextTreeNode));
					} 
				}					
			}			
			return y;
	}
		
		


  /** Print the project */
  public void printProject(GanttExportSettings settings) {
		//For printing the project, begin to create temporary BufferedImage for the entire graphics
		//Then use a class to print it 
		
		printRendering = true;
		BufferedImage image = getChart(settings);
		printRendering = false;
		
		PrinterJob printJob = PrinterJob.getPrinterJob();

		printJob.setPrintable(new GanttPrintable(image));
		if(printJob.printDialog()){
       try { 
					printJob.print(); 
			 
			 } catch (Exception PrintException) {
			 	System.out.println("Print Error" + PrintException);
				PrintException.printStackTrace(); 
			 }
    }

  }

  /** Draw arrow between two points */
  public class Arrow {
    private int x1, x2, y1, y2;

    private boolean draw;

    public Arrow() {
      x1 = x2 = y1 = y2 = 0;
      draw = false;
    }

    public Arrow(int x1, int y1, int x2, int y2) {
      this.x1 = x1;
      this.x2 = x2;
      this.y1 = y1;
      this.y2 = y2;
      this.draw = true;
    }

    public void setDraw(boolean d) {
      draw = d;
    }

    public boolean getDraw() {
      return draw;
    }

    public void changePoint2(int x2, int y2) {
      this.x2 = x2;
      this.y2 = y2;
    }

    public void paint(Graphics g) {
      if (draw) {
        //draw the line
        g.setColor(Color.black);
        g.drawLine(x1, y1, x2, y2);

        //Draw the triangle
        int xPoints[] = new int[3];
        int yPoints[] = new int[3];

        int vx = x2 - x1;
        int vy = y2 - y1;

        int px = (int) (0.08f * (float) vx);
        int py = (int) (0.08f * (float) vy);

        int total = ( (px < 0) ? -px : px) + ( (py < 0) ? -py : py);

        px = (int) ( (float) px * 10.f / (float) total);
        py = (int) ( (float) py * 10.f / (float) total);

        xPoints[0] = x2;
        yPoints[0] = y2;
        xPoints[1] = x2 - px + py / 2;
        yPoints[1] = y2 - py - px / 2;
        xPoints[2] = x2 - px - py / 2;
        yPoints[2] = y2 - py + px / 2;
        g.fillPolygon(xPoints, yPoints, 3);
      }
    }
  }

  /** Note paint of the graphic Area */
  public class Notes {
    /** The notes to paint */
    String n = new String();

    /** The coords */
    int x, y;

    boolean draw;

    public Notes() {
      draw = false;
    }

    public Notes(String s, int x, int y) {
      this.n = s;
      this.x = x;
      this.y = y;
      this.draw = true;
    }

    public void setDraw(boolean d) {
      draw = d;
    }

    public boolean getDraw() {
      return draw;
    }

    public void setX(int x) {
      this.x = x;
    }

    public void setString(String s) {
      n = s;
    }

    public void paint(Graphics g) {
      if (draw) {
        g.setColor(arrayColor[0]);
        g.fillRect(x - 2, y, 70, 16);
        g.setColor(Color.black);
        g.drawRect(x - 2, y, 70, 16);
        g.drawString(n, x, y + 12);
      }
    }
  }

  /** Class to store parameters of each task (pixel to start, end y value...*/
  public class GanttPaintParam {
    public int x1 /**start of task*/,
        x2
        /** End of task*/,
        x3
        /** Percent complete */;

    public String name;

    public int taskID;

    public int y;

    public int type; //0->Meeting task,   1 mother task,  2 normal task

    public GanttPaintParam(String name, int taskID, int x1, int x2, int x3,
                           int y, int type) {
      this.name = name;
      this.taskID = taskID;
      this.x1 = x1;
      this.x2 = x2;
      this.x3 = x3;
      this.y = y;
      this.type = type;
    }

    public String toString() {
      return name;
    }
  }

  /** Function able to export in PNG format the graphic area */
  public void export(File file, GanttExportSettings settings, String format) {  
  	
  	
  	/*BufferedImage image = new BufferedImage(getWidth(), getHeight(),
                                            BufferedImage.TYPE_INT_RGB);

    drawdepend = depend;
    drawPercent = percent;
    drawName = name;
    drawVersion = true;
    paintComponent(image.getGraphics());
    drawdepend = true;
    drawPercent = true;
    drawName = false;
    drawVersion = false;*/
		

	
	BufferedImage image = getChart(settings);
		

    try {
		
    		if(file==null)
				ImageIO.write(image, format, System.out);
			else ImageIO.write(image, format, file);
			
    }
    catch (Exception e) {
      System.out.println(e);
    }
  }

  private GanttTree getTree() {
    return this.tree;
  }

  /** Resizes the area to fit the whole project. Height is set according to the number of tasks.
   * Width is not changed, but zooming is set instead
   */
  public void fitWholeProject(boolean resize) {
    GanttCalendar minStart = null;
    GanttCalendar maxFinish = null;
    int height = 20;
    for (Iterator tasks = getTree().getAllTasks().iterator(); tasks.hasNext(); ) {
      DefaultMutableTreeNode nextTreeNode = (DefaultMutableTreeNode) tasks.next();
      Task next = (Task) nextTreeNode.getUserObject();
      if ("None".equals(next.toString())) {
        continue;
      }
      if (isVisible(next)) {
        height += 20;
      }
      GanttCalendar start = next.getStart();
      GanttCalendar finish = next.getEnd();
      if (minStart == null) {
        minStart = start;
      }
      if (maxFinish == null) {
        maxFinish = finish;
      }
      if (minStart.compareTo(start) >= 0) {
        minStart = start;
      }
      if (maxFinish.compareTo(finish) <= 0) {
        maxFinish = finish;
      }
    }

    if (minStart == null || maxFinish == null) {
      return;
    }

    minStart = minStart.Clone();
    maxFinish = maxFinish.Clone();

    int projectLengthDays = minStart.diff(maxFinish);
    int projectLengthWeeks = projectLengthDays / 7;
    int zoom = GanttGraphicArea.ONE_MONTH;
    if (projectLengthWeeks < 1) {
      zoom = GanttGraphicArea.ONE_WEEK;
    }
    else if (projectLengthWeeks < 2) {
      zoom = GanttGraphicArea.TWO_WEEK;
    }
    else if (projectLengthWeeks < 4) {
      zoom = GanttGraphicArea.ONE_MONTH;
    }
    else if (projectLengthWeeks < 9) {
      zoom = GanttGraphicArea.TWO_MONTH;
    }
    else if (projectLengthWeeks < 13) {
      zoom = GanttGraphicArea.THREE_MONTH;
    }
    else if (projectLengthWeeks < 17) {
      zoom = GanttGraphicArea.FOUR_MONTH;
    }
    else if (projectLengthWeeks < 26) {
      zoom = GanttGraphicArea.SIX_MONTH;
    }
    else if (projectLengthWeeks < 52) {
      zoom = GanttGraphicArea.ONE_YEAR;
    }
    else if (projectLengthWeeks < 104) {
      zoom = GanttGraphicArea.TWO_YEAR;
    }
    else if (projectLengthWeeks < 156) {
      zoom = GanttGraphicArea.THREE_YEAR;
    }

    if (resize) {
      setSize(getSize().width, height + 40);
    }
    setZoom(zoom);
    setDate(minStart);
    zoomToBegin();
    setScrollBar(0);
  }

}
