/***************************************************************************
 * GanttGraphicArea.java  -  description
 * -------------------
 * begin                : dec 2002
 * copyright            : (C) 2002 by Thomas Alexandre
 * email                : alexthomas(at)ganttproject.org
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
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.resource.ResourceManager;
import net.sourceforge.ganttproject.task.ResourceAssignment;
import net.sourceforge.ganttproject.task.Task;


/**
 * Classe for the graphic part of the soft
 */
public class ResourceLoadGraphicArea extends JPanel {
    
    /** Begin of display. */
    public GanttCalendar date, olddate;
    
    /** Reference to the GanttTree */
    public JTable table;
    
    
    
    /** Zoom  to on week */
    public static final int ONE_WEEK=0;
    
    /** Zoom  to two week */
    public static final int TWO_WEEK=1;
    
    /** Zoom  to one month */
    public static final int ONE_MONTH=2;
    
    /** Zoom  to two month */
    public static  final int TWO_MONTH=3;
    
    /** Zoom  to tree month */
    public static  final int THREE_MONTH=4;
    
    /** Zoom  to tree month */
    public static  final int FOUR_MONTH=5;
    
    /** Zoom  to six month */
    public static  final int SIX_MONTH=6;
    
    /** Zoom  to one year */
    public static  final int ONE_YEAR=7;
    
    /** Zoom  to two year */
    public static  final int TWO_YEAR=8;
    
    /** Zoom  to three year */
    public static  final int THREE_YEAR=9;
    
    /** Default color for tasks */
    public static Color taskDefaultColor
    = new Color((float)0.549,(float)0.713,(float)0.807);
    
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
    /** render the 3d border. */
    private boolean draw3dBorders = true; 
		
		
		
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
    
   
    /** Type of selection 0 -> move duration,   1 -> moveDate,   2->add Depend*/
    int typeSeletion;
    
    /** Parmeters to change the duration of the task*/
    private int storeTaskLength;
    private float addTaskLength;
    private GanttCalendar storeTaskStart;
    private int []storeX =new int[3];
    
    /** The task to move duration*/
    //private Task taskToMove=new GanttTask("toto",new GanttCalendar(),10);
    
    /**Color array use to paint */
    private Color [] arrayColor = new Color[15];
    
    /** List of task recup before painting */
    private ArrayList listOfTask;
    
    /** Begin of project */
    public GanttCalendar beg = new GanttCalendar();
    
    /**End date for the project */
    public GanttCalendar end = new GanttCalendar();
    
    private ArrayList loads;
    
    public GanttTree tree;
    
    /** The mouse button press */
    public int mouseButton=0;
    
    /** Constructor */
    public ResourceLoadGraphicArea(GanttProject app, GanttTree tree, JTable table) {
        date = new GanttCalendar();
        olddate=new GanttCalendar();
        date.setDay(1);
        this.table = table;
        this.tree= tree;
        zoomValue = ONE_MONTH;	//zoom to one month by default of the current date
        
        margY = 0;
        this.language = GanttLanguage.getInstance();
        appli = app;
        
        //Listener on wheel mouse
        this.addMouseWheelListener(new MouseWheelListener(){
            public void mouseWheelMoved(MouseWheelEvent e) {
                if(e.getWheelRotation()>0) { zoomMore(); appli.getArea().zoomMore();}
                else if(e.getWheelRotation()<0){zoomLess();appli.getArea().zoomLess();}
                
                if(zoomValue<0) {zoomValue=0;appli.getArea().setZoom(0);}
                if(zoomValue>9) {zoomValue=9;appli.getArea().setZoom(9);}
                
                zoomToBegin();
                
                appli.bZoomIn.setEnabled(zoomValue==0?false:true);
                appli.bZoomOut.setEnabled(zoomValue==9?false:true);
                
                repaint();
            }
        });
            
	    
	//Listener on a mouse click
	this.addMouseListener(new MouseListener() {
		public void mouseClicked(MouseEvent e) {}
		
		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {	    
		    	if (e.getButton() == MouseEvent.BUTTON1) {
				oldX = e.getX();
				oldY = e.getY();
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
			
		}
		
		public void mouseReleased(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				
				int f = 1;
				int x = oldX - e.getX();
				if (x > 2) {
				  f = -1;
				}
				else if (x < -2) {
				  f = 1;
				}
				else { //If the move is too short, do nothing and return
				  repaint();
				  return;
				}

				switch (zoomValue) {
				  case ONE_WEEK:
				  case TWO_WEEK:
				    date.go(Calendar.WEEK_OF_YEAR, 1 * f);
				    break;
				  case ONE_MONTH:
				  case TWO_MONTH:
				  case THREE_MONTH:
				  case SIX_MONTH:
				    date.go(Calendar.MONTH, 1 * f);
				    break;
				  case ONE_YEAR:
				  case TWO_YEAR:
				  case THREE_YEAR:
				    date.go(Calendar.YEAR, 1 * f);
				    break;
				}
				appli.getArea().date=date.Clone();
				repaint();
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
      arrayColor[6] = taskDefaultColor;
      arrayColor[7] = new Color((float)0.741,(float)0.745,(float)0.741);
      arrayColor[8] = new Color((float)0.388,(float)0.396,(float)0.388);
      arrayColor[9] = new Color((float)0.196,(float)0.196,(float)0.196);
      arrayColor[10]= new Color((float)0.192,(float)0.298,(float)0.525);
      arrayColor[11]= new Color((float)0.141,(float)0.141,(float)0.141);
      arrayColor[12]= new Color((float)0.290,(float)0.349,(float)0.643);
      arrayColor[13]= new Color((float)0.851,(float)0.902,(float)0.937);
      arrayColor[14]= new Color((float)0.961,(float)0.961,(float)0.961);
        
    }

   
    /** The size of the panel. */
    public Dimension getPreferredSize() {
        return new Dimension(465, 600);
    }
    
    /** Method to change the date */
    public void changeDate(boolean next) {
        int f=1;
        if(!next)f=-1;
        
        switch(zoomValue) {
            case ONE_WEEK:case TWO_WEEK:date.go(Calendar.WEEK_OF_YEAR,1*f); break;
            case ONE_MONTH:case TWO_MONTH:case THREE_MONTH:case FOUR_MONTH:case SIX_MONTH:date.go(Calendar.MONTH,1*f); break;
            case ONE_YEAR:case TWO_YEAR:case THREE_YEAR:date.go(Calendar.YEAR,1*f);break;
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
							//default:System.out.println("Resource + Should never append -->  zoomValue="+zoomValue);
            }
  }
    
    /** Method when zoomin, to set at the begin for each value */
    public void zoomToBegin() {
        switch(zoomValue) {
            case ONE_WEEK: case TWO_WEEK:
                String d = language.getDay(date.getDayWeek());
                while(!d.equals(language.getDay(2))) {
                    date.add(1);
                    d = language.getDay(date.getDayWeek());
                }
                
                break;
            case ONE_MONTH:case TWO_MONTH:case THREE_MONTH:case FOUR_MONTH:case SIX_MONTH:date.setDay(1);break;
            case ONE_YEAR:case TWO_YEAR:case THREE_YEAR:date.setMonth(0);
            date.setDay(1);
            break;
        }
    }
    
    
    /** draw the panel */
    public void paintComponent(Graphics g) {
        //Super paint component!!!!!!!!!!
        super.paintComponent(g);
        
        //Move if its in printing (for margin)
        /*if(printRendering)
            g.translate((int)upperLeft.getX(),(int)upperLeft.getY());*/
        
        //Vertical bars
        paintCalendar1(g);
        //The tasks
        paintLoads(g);
        //The depends
        //        if(drawdepend) paintDepend(g);
        //The part at top
        paintCalendar2(g);
        
        
        //        arrow.paint(g);
        //        notes.paint(g);
        
        if (drawVersion) {
      		drawGPVersion(g);			
    		}
        
    }
    
    public void drawGPVersion(Graphics g){
		  g.setColor(Color.black);
      g.setFont(new Font("SansSerif", Font.PLAIN, 10));
      g.drawString("GanttProject (" + GanttProject.version + ")", 3, getHeight() - 6);
		}
    
    /** Search for a coef on the arraylist */
    public int indexOf(ArrayList listOfParam, String coef) {
        for(int i=0;i<listOfParam.size();i++)
            if(coef==listOfParam.get(i).toString())
                return i;
        return -1;
    }
    
    /** Change the velue connected to the JTree's Scrollbar */
    public void setScrollBar(int v) { margY = v; }
    
    /** Return the value of the JTree's Scrollbar */
    public int getScrollBar() { return margY; }
    
    /** Change the zoom value */
    public void setZoom(int z) { zoomValue = z; }
    
    /** Add a zoom*/
    public void zoomMore() {
        if(zoomValue==5)olddate=date.Clone();
        zoomValue++;
    }
    
    /**Less a zoom*/
    public void zoomLess() {
        if(zoomValue==6 && date.getYear()==olddate.getYear())date=olddate.Clone();
        zoomValue--;
    }
    
    /** Return  the zoom value */
    public int getZoom() { return zoomValue; }
    
    /** Change the date of the begin to paint */
    public void setDate(GanttCalendar d) { date = d; }
    
    /** Return the date */
    public GanttCalendar getDate() { return date ; }
    
    /** Change language */
    public void setLanguage(GanttLanguage language) { this.language = language; }
    
    /** Return the number of day visible for each level of granularity */
    public int getGranit(boolean day) {
        GanttCalendar cal;
        int res=7;	//by default the 7 days of the week
        switch(zoomValue) {
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
            for(int i=0;i<3;i++) {
                res+=cal.getNumberOfDay();
                cal.goNextMonth();
            }break;
            
            case FOUR_MONTH:cal = date.Clone();
            res=0;
            for(int i=0;i<4;i++) {
                res+=cal.getNumberOfDay();
                cal.goNextMonth();
            }break;
            
            
            case SIX_MONTH:cal = date.Clone();
            res=0;
            for(int i=0;i<6;i++) {
                res+=cal.getNumberOfDay();
                cal.goNextMonth();
            }break;
            
            case ONE_YEAR:if(!day) res=12;
            else res=(date.getYear()%4==0)?366:365;
            break;
            
            case TWO_YEAR:if(!day) res=12*2;
            else {
                cal = date.Clone();
                res=0;
                for(int i=0;i<2;i++) {
                    if(cal.getYear()%4==0) res+=366;
                    else res+=365;
                    cal.go(Calendar.YEAR,1);
                }
            }
            break;
            
            case THREE_YEAR:if(!day) res=12*3;
            else {
                cal = date.Clone();
                res=0;
                for(int i=0;i<3;i++) {
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
    public int getFoot() {
        int res=1;
        switch(zoomValue) {
            case ONE_YEAR:case TWO_YEAR:case THREE_YEAR:res=date.getNumberOfDay();break;
            //default:res=1;break;
        }
        
        return res;
    }
    
    /** Paint the vertical bars */
    public void paintCalendar1(Graphics g) {
        int sizex = getWidth();
        int sizey = getHeight();
        int headery = 45;
        int gra = sizex / getGranit(false);		//The granularity
        int gra2 = getGranit(false);
        float fgra = (float)sizex / (float)getGranit(false);
        int drawDate=-1;
        GanttCalendar dateToPaint=new GanttCalendar();
        
        g.setFont(new Font("SansSerif",Font.PLAIN,12));
        
        //Reset the background to white
        g.setColor(Color.white);
        g.fillRect(0,0,sizex,sizey);
        
        
        beg = date.Clone(); beg.add(getGranit(true));
        end = date.Clone(); end.add(-1);
        
        GanttCalendar tmpdate;
        if(zoomValue==ONE_WEEK || zoomValue==TWO_WEEK) tmpdate = date.Clone();
        else tmpdate = new GanttCalendar(date.getYear(), date.getMonth(), 1);
        g.setFont(new Font("SansSerif",Font.PLAIN,10));
        
        //Draw the vertical bars
        for(int i=0;i<gra2;i++) {
            String sDay = tmpdate.getdayWeek();
            String sMonth = tmpdate.getdayMonth();
            
            //For each day
            if(zoomValue<7) {
                if(sDay.equals(language.getDay(6))){
                    g.setColor(arrayColor[0]);
                    g.fillRect((int)(fgra*i),headery,gra+1/*(int)(fgra+0.5)*/,sizey);
                    
                    g.setColor(arrayColor[1]);
					if(draw3dBorders)
                    	g.drawLine((int)(fgra*i),headery,(int)(fgra*i),sizey+headery);
                    
                } else if(sDay.equals(language.getDay(0))) {
                    g.setColor(arrayColor[0]);
                    g.fillRect((int)(fgra*i),headery,gra+1/*(int)(fgra+0.5)*/,sizey);
                    if(zoomValue==ONE_WEEK || zoomValue==TWO_WEEK){
                        g.setColor(arrayColor[1]);
                        if(draw3dBorders)
                        	g.drawLine((int)(fgra*i),headery,(int)(fgra*i),sizey+headery);
                    }
                }
                
                
            } else { //For each month
                
                if(sMonth.equals(language.getMonth(0))) {
                    g.setColor(arrayColor[0]);
                    if(draw3dBorders)
                    	g.fillRect((int)(fgra*i),headery,(int)fgra,sizey);
                }
                g.setColor(arrayColor[1]);
                if(draw3dBorders)
                	g.drawLine((int)(fgra*i),headery,(int)(fgra*i),sizey+headery);
                
            }
            
            if(zoomValue<7) {
                if(tmpdate.compareTo(new GanttCalendar())==0) {
                    g.setColor(arrayColor[2]);
                    g.fillRect((int)(fgra*i),headery,(int)fgra,sizey);
                    g.setColor(arrayColor[3]);
                    if(draw3dBorders)
                    	g.drawLine((int)(fgra*i),headery,(int)(fgra*i),sizey+headery);
                }
            } else {
                GanttCalendar today=new GanttCalendar();
                float s = fgra/(float)tmpdate.getNumberOfDay();
                if(tmpdate.getYear()==today.getYear() && tmpdate.getMonth()==today.getMonth()) {
                    //Blue rectange on today
                    g.setColor(arrayColor[2]);
                    g.fillRect((int)(fgra*i)+(int)(s*today.getDay()),headery,((s<2)?2:(int)s),sizey);
                    g.setColor(arrayColor[2]);
                    if(draw3dBorders)
                    	g.drawLine((int)(fgra*i)+(int)(s*today.getDay()),headery,(int)(fgra*i)+(int)(s*today.getDay()),sizey+headery);
                    
                }
            }
            
            tmpdate.add(getFoot());
        }
        
        
        
        
        if(zoomValue==ONE_WEEK || zoomValue==TWO_WEEK) tmpdate = date.Clone();
        else tmpdate = new GanttCalendar(date.getYear(), date.getMonth(), 1);
        
        
			beg=appli.getArea().beg;
			end=appli.getArea().end;
	
			//Draw Miscallenous bars
        for(int i=0;i<gra2;i++) {
            //Courant day in blue
            if(zoomValue<7) {
                //A line on begin and end of project
                if(tmpdate.compareTo(beg)==0 || tmpdate.compareTo(end)==0) {
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
                if(tmpdate.getYear()==beg.getYear() && tmpdate.getMonth()==beg.getMonth()) {
                    g.setColor(arrayColor[12]);
                    //g.drawLine((int)(fgra*i)+(int)(s*beg.getDay()),headery,(int)(fgra*i)+(int)(s*beg.getDay()),sizey+headery);
                    drawVerticalLinedash(g,(int)(fgra*i)+(int)(s*beg.getDay()),headery,sizey+headery,5);
                    drawDate=(int)(fgra*i)+(int)(s*beg.getDay())-52;
                    dateToPaint=beg;
                }
                //blue dash line for end
                else if(tmpdate.getYear()==end.getYear() && tmpdate.getMonth()==end.getMonth()) {
                    g.setColor(arrayColor[12]);
                    //g.drawLine((int)(fgra*i)+(int)(s*end.getDay()),headery,(int)(fgra*i)+(int)(s*end.getDay()),sizey+headery);
                    drawVerticalLinedash(g,(int)(fgra*i)+(int)(s*end.getDay()),headery,sizey+headery,5);
                    drawDate=(int)(fgra*i)+(int)(s*end.getDay())+3;
                    dateToPaint=end.newAdd(-1);
                }
            }
            
            //Draw the date in string on calendar
            if(drawDate>=0) {
                g.setFont(new Font("SansSerif",Font.PLAIN,9));
                g.setColor(arrayColor[9]);
                g.drawString(dateToPaint.toString(),drawDate,headery+10);
                drawDate = -1;
            }
            
            tmpdate.add(getFoot());
        }
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
    
    /** Draw the legend of the calendar */
    public void paintCalendar2(Graphics g) {
        int sizex = getWidth();
        int sizey = getHeight();
        int headery = 45;
        int gra2 = getGranit(false);
        float fgra=(float)(sizex) / (float)getGranit(false);		//the granularity
        
        
        g.setFont(new Font("SansSerif",Font.PLAIN,12));
        
        //gray rectangle with nice borders
        g.setColor(arrayColor[0]);
        g.fillRect(0,0,sizex,headery);
        g.setColor(arrayColor[4]);
        g.drawRect(0,0, sizex-1, headery/2);
        g.drawRect(0,headery/2, sizex-1, headery/2);
		/*if(draw3dBorders)
        {
        	g.setColor(arrayColor[5]);        
        	g.drawLine(1,headery/2-1,sizex-2,headery/2-1);
        	g.drawLine(0,headery-2,sizex-2,headery-2);
        	g.setColor(Color.white);
        	g.drawLine(1,1,sizex-2,1);
        	g.drawLine(0,headery/2+1,sizex-2,headery/2+1);
        }*/
        
        g.setColor(Color.black);
        //Set the text at the top (differetns for each zaoom value)
        float posX;
        GanttCalendar cal;
        switch(zoomValue) {
            //The number of week and th month
            case ONE_WEEK: g.drawString(language.getText("week")+date.getWeek()+" - "+date.getdayMonth()+"  "+date.getYear(),2,headery/2-5);
            break;
            
            case TWO_WEEK:
                g.drawString(language.getText("week")+(date.getWeek())+" - "+date.getdayMonth()+"  "+date.getYear(),2,headery/2-5);
                g.drawString(language.getText("week")+(date.getWeek()+1)+" - "+date.getdayMonth()+"  "+date.getYear(),(int)(fgra*7)+2,headery/2-5);
                break;
                
                //The month and the yeay
            case ONE_MONTH: g.drawString(date.getdayMonth()+"  "+date.getYear(),2,headery/2-5);
            break;
            
            // Draw two months
            case TWO_MONTH:cal = date.Clone();
            posX = 0;
            for(int i=0;i<2;i++) {
                g.drawString(cal.getdayMonth()+"  "+cal.getYear(),(int)posX+2/*i*sizex/3+2*/,headery/2-5);
                posX+=((float)cal.getNumberOfDay()*fgra);
                cal.goNextMonth();
            }
            break;
            
            //Draw three months
            case THREE_MONTH:cal = date.Clone();
            posX = 0;
            for(int i=0;i<3;i++) {
                g.drawString(cal.getdayMonth()+"  "+cal.getYear(),(int)posX+2/*i*sizex/3+2*/,headery/2-5);
                posX+=((float)cal.getNumberOfDay()*fgra);
                cal.goNextMonth();
            }
            break;
            
            // draw 4 months
            case FOUR_MONTH:cal = date.Clone();
            posX = 0;
            for(int i=0;i<4;i++) {
                String dm = cal.getdayMonth();
                g.drawString(dm.substring(0,(sizex>480)?(dm.length()<3?dm.length():3):1)+"  "+new
                Integer(cal.getYear()).toString().substring(2,4),(int)posX+2/*i*sizex/3+2*/,headery/2-5);
                posX+=((float)cal.getNumberOfDay()*fgra);
                cal.goNextMonth();
            }
            break;
            
            // draw 6 months
            case SIX_MONTH:cal = date.Clone();
            posX = 0;
            for(int i=0;i<6;i++) {
                String dm = cal.getdayMonth();
                g.drawString(dm.substring(0,(sizex>480)?(dm.length()<3?dm.length():3):1)+"  "+new
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
            for(int i=0;i<2;i++,dy2++) {
                g.setColor(Color.black);
                g.drawString(""+dy2,i*sizex/2+2,headery/2-5);
            }
            break;
            
            //draw thre years
            case THREE_YEAR: int dy = date.getYear();
            for(int i=0;i<3;i++,dy++) {
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
        for(int i=0;i<gra2;i++) {
            String sDay = tmpdate.getdayWeek();
            String sMonth = tmpdate.getdayMonth();
            
            g.setColor(Color.black);
            switch(zoomValue) {
                case ONE_WEEK:
                    g.drawString(sDay.substring(0,(sizex>300)?(sDay.length()<3?sDay.length():3):1)+" "+tmpdate.getDate(),(int)(fgra*i)+2,headery-7);
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
                
                case TWO_MONTH:case THREE_MONTH:case FOUR_MONTH:case SIX_MONTH:
                    if(sDay.equals(language.getDay(1))) {
                        g.drawString(""+tmpdate.getDate(),(int)(fgra*i)+2,headery-7);
                        g.setColor(arrayColor[4]);
                        g.drawLine((int)(fgra*i),headery/2,(int)(fgra*i),headery-1);
                    }
                    break;
                    //case SIX_MONTH:
                    //		if(sDay==language.getDay(1)) g.drawString(""+tmpdate.getDate(),(int)(fgra*i)+1,headery-7);
                    //			break;
                case ONE_YEAR: g.drawString(sMonth.substring(0,(sizex>300)?(sMonth.length()<3?sMonth.length():3):1),(int)(fgra*i)+2,headery-7);
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
    public void paintLoads(Graphics g) {
        int sizex = getWidth();
        int sizey = getHeight();
        int headery = 45;
        float fgra = (float)sizex / (float)getGranit(true); //pixels per day
        
        g.setFont(new Font("SansSerif",Font.PLAIN,9));
        
        //Get all task
        ArrayList listOfTask = tree.getAllTasks();

        if (listOfTask.size()<=1)
            return;
        
        calculateLoad(listOfTask);
        
        //Probably optimised on next release
        listOfParam.clear();
        
        GanttCalendar firstStart=new GanttCalendar(),lastEnd=new GanttCalendar();
        int duration=0;//lastEnd=new GanttCalendar();
        
        
        
        firstStart=((Task) ((DefaultMutableTreeNode)listOfTask.get(1)).getUserObject()).getStart();
        lastEnd=firstStart;
        //Calcul all parameters
        for(int i=2;i<listOfTask.size();i++) {
            Task task =(Task) ((DefaultMutableTreeNode)listOfTask.get(i)).getUserObject();
            
            if (firstStart.compareTo(task.getStart())==1)
                firstStart=task.getStart();
            if (lastEnd.compareTo(task.getEnd())==-1)
                lastEnd=task.getEnd();
        }
        
        duration=firstStart.diff(lastEnd);

        int x1=-10, x2=sizex+10;
        int e1;
        int fois;
        int type = 2;

        
        //difference between the start date of the task and the start of the display area
        e1 = date.diff(firstStart);
        
        //System.out.println ("date: "+date);
        
        //Calcul start and end pixel of each task
        float fx1, fx2;
        
        
        fx1 = 0;
        //                fx1 =1;
        fx2 = fx1 + (float)duration * fgra;
        x1 = (int) fx1;
        x2 = (int) fx2;
        
        
        int percent =0, intLoad=-1;
        
        x2 = (int) ((float)getGranit(true)*fgra);
        
        x2=0;
				
				
				
        
        // hier brauche ich ein Mapping zwischen Usern und ihrer y Position        
        HumanResourceManager resMan=(HumanResourceManager) appli.getHumanResourceManager();
        
        ArrayList users=resMan.getResources();
				
				/** State for rendering each cell of the chart*/
				boolean [] state = new boolean[users.size()>0?users.size():1];
				boolean [] oldState = new boolean[users.size()>0?users.size():1];
				
				for(int i=0;i<users.size();i++){
					state[i]=false;
					oldState[i]=false;
				}
				

        for (int i=0; i<getGranit(true); i++) {
            Hashtable load = (Hashtable)loads.get(i);

            if (load!=null) {
						
                for (int y=0; y<users.size();y++) {
                    
										oldState[y] = state[y];
										
										String key=((HumanResource)users.get(y)).toString();
                    //System.out.println(key);
                    if (load.get(key)!=null) {
                        intLoad = ((Integer)load.get(key)).intValue();
	                    	state[y] = true;
										}
                    else {
                        intLoad=-1;
												state[y] = false;
										}

                    if (intLoad > 100)
                        paintAResourceLoad(g, x1+(int)(i*fgra), x2+(int)((i+1)*fgra), y+1,
                                    "xx",appli.getUIConfiguration().getResourceOverloadColor(), state[y], oldState[y] );
                    else if (intLoad >0) {
                        paintAResourceLoad(g, x1+(int)(i*fgra), x2+(int)((i+1)*fgra), y+1,
                                    "xx",appli.getUIConfiguration().getResourceColor(), state[y], oldState[y] );
                    }
                }
            }
         }
        type=2;
        
    }
    
		public void paintEndBorder(Graphics g, int x2, int y ) {
			y=y*20+40-margY;
			if(y<20 || y > getHeight()) return;	//Not draw if the task is not on the area
			g.setColor(Color.black);
			g.drawLine(x2,y,x2,y+12);
			g.setColor(arrayColor[8]);
			g.drawLine(x2-1,y+2,x2-1,y+11);
		}
		
   
    /** Draw a normal task */
    public void paintAResourceLoad(Graphics g, int x1, int x2, int y, String taskName, Color color,
				boolean state, boolean oldState) {
        x2++;
				int d=y;
        y=y*20+40-margY;
        
        if(y<20 || y > getHeight()) return;	//Not draw if the task is not on the area
        if((x1>getWidth() && x2>getWidth()) || (x1<0 && x2<0)) return;
        
        boolean first_cell = (state && !oldState);
				//boolean end_cell = (!state && oldState);
				
				int xd = x1;
				if(!first_cell) xd-=2;
				
				
				//Blue rectangle
        g.setColor(color);
        g.fillRect(xd, y, (x2-x1)+2,12 );

		if(draw3dBorders)
		{	
				g.setColor(Color.black);
				g.drawLine(xd,y, x2-1, y);
				g.drawLine(xd,y+12, x2-1, y+12);
				
				//Down (color mor dark)
				//g.setColor(arrayColor[8]);
				//g.drawLine(xd,y+11, x2-1, y+11);
				
				
				//Top (color more light)
				//g.setColor(arrayColor[7]);
				//g.drawLine(xd,y+1, x2-1, y+1);
				
				
				//draw the begin of the nice border
				if(first_cell){
					g.setColor(Color.black);
					g.drawLine(x1,y,x1,y+12);
					g.setColor(arrayColor[7]);
					g.drawLine(x1+1,y+1,x1+1,y+11);
				}
				
				//draw the end of the bar (not great algorithm because end is design each timt, but it's a first version ....:(
				g.setColor(Color.black);
				g.drawLine(x2,y,x2,y+12);
				g.setColor(arrayColor[8]);
				g.drawLine(x2-1,y+2,x2-1,y+11);
		}		
      
        if(drawName) {
            g.setColor(Color.black);
            g.drawString(taskName, x2+40,y+10);
        }
        
    }

    
    private void calculateLoad (ArrayList tasks) {
        
        loads=new  ArrayList();
        
        for (int i=0; i<getGranit(true); i++)
            loads.add(null);
        
        for (int i=0;i<tasks.size();i++) {
            Task task = (Task)((DefaultMutableTreeNode)tasks.get(i)).getUserObject();

            GanttCalendar displayStart=date.Clone(),displayEnd=date.Clone();
            displayEnd.add(getGranit(true));
            
            // prüfen ob task im sichtbaren Bereich
            if (task.getStart().compareTo(displayStart)==-1 && task.getEnd().compareTo(date)==-1) {// Task is completely before display area
                continue;
            }
            if (task.getStart().compareTo(displayEnd)==1) {
                continue;
            }
            
            // nicht nur der Tag auch der Monat muss gechecked werden
            
//            Welche Struktur welche Daten ???? Wie die Tage über resourcen auftragen.
//            Start liegt vor Display Start also start=1
            
            int start;
            int duration;
            
            if (task.getStart().compareTo(displayStart)==-1) {
                duration = task.getEnd().diff(displayStart);
                start=0;
            }
            else {
                start = date.diff(task.getStart());
                duration = task.getEnd().diff(task.getStart());
            }
            int intLoad=0;
            
            if (start+duration>getGranit(true)) 
                duration=getGranit(true)-start;
            
            //ArrayList users = task.getUsersList();
            ResourceAssignment[] assignments = task.getAssignments();
            for (int j=0;j<assignments.length;j++) {
                //Hashtable resData = (Hashtable) users.get(j);
                ResourceAssignment next = assignments[j];

                for (int d=start; d<start+duration; d++) {
                    Hashtable load=(Hashtable)loads.get(d);
                    if (load==null) {
                        load=new Hashtable();
                        loads.set(d, load);
                    }
                    Integer lo=(Integer)load.get(next.getResource().getName());
                    if (lo!=null) 
                        intLoad=lo.intValue();
                    else
                        intLoad=0;
                    
                    intLoad+=next.getLoad();
                    
                    load.put(next.getResource().getName(), new Integer(intLoad));
                }
                
            }
        }
    }
    
		
		/** Return an image with the gantt chart*/
	public BufferedImage getChart(GanttExportSettings settings) {
		appli.getArea().calcProjectBegAndEnd();
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
		draw3dBorders = settings.border3d;
		
		ResourceManager resMan=(HumanResourceManager) appli.getHumanResourceManager();
		ArrayList users=resMan.getResources();
		
		
		int sizeTOC=0; //Size of the content table for the resources list
		BufferedImage tmpImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		FontMetrics fmetric = tmpImage.getGraphics().getFontMetrics(new Font("SansSerif", Font.PLAIN, 10));
		
		for (Iterator user = users.iterator(); user.hasNext(); ) {
      String nameOfRes=((HumanResource)user.next()).toString();
			int nbchar = fmetric.stringWidth(nameOfRes);
			if(nbchar>sizeTOC)sizeTOC=nbchar;
		}
		sizeTOC+=20;
	
		//If there is only the root task
		setSize(width, users.size()*20 + 80);
    
		int calculateWidth=width*rendering;
		if(settings.name) {
			rendering++;
			calculateWidth+=sizeTOC;
		}
		
		BufferedImage image = new BufferedImage(calculateWidth, getHeight(), BufferedImage.TYPE_INT_RGB);
		printRendering = true;
		
		start=new GanttCalendar(date);
		date=new GanttCalendar(beg);
		zoomToBegin();
		if(zoomValue==ONE_WEEK || zoomValue==TWO_WEEK) changeDate(false);
		
		
		int transx=0, transx2;
		
		for(int i=0;i<rendering;i++){

			BufferedImage image2 = new BufferedImage(width, getHeight(), BufferedImage.TYPE_INT_RGB);
			if(i==0 && settings.name){
				rowCount=0;
				image2 = null;
				image2 = new BufferedImage(sizeTOC, getHeight(), BufferedImage.TYPE_INT_RGB);
				setSize(sizeTOC, getHeight());
				printResources(image2.getGraphics());
				setSize(width, getHeight());
				transx2=sizeTOC;
			}
			else {paintComponent(image2.getGraphics());	changeDate2(date);	transx2=width;}	
			
			
			Graphics g = image.getGraphics();
			g.translate(transx,0);
			g.drawImage(image2,0,0,null);
			
			image2=null;
			
			transx+=transx2;
			
		}
		upperLeft = new Point(0,0);
		drawGPVersion(image.getGraphics());		
		printRendering = false;
		
		date=new GanttCalendar(start);
		zoomToBegin();
		margY=oldMargY;
		//drawVersion = false;
		draw3dBorders = true;
		setSize(getSize().width, oldHeight);
		repaint();
		
		return image;		
	}
	
	private int rowCount=0;
	
	/** Print the list of tasks */
	private void printResources(Graphics g){
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());

		g.setColor(Color.black);
		g.setFont(new Font("SansSerif", Font.PLAIN, 10));
		
		ResourceManager resMan=(HumanResourceManager) appli.getHumanResourceManager();
		ArrayList users=resMan.getResources();
		
		int y=55;
		
		for(Iterator user = users.iterator(); user.hasNext(); ) {
			String nameOfRes=((HumanResource)user.next()).toString();
			
			if(rowCount%2==1) {
				g.setColor(new Color( (float) 0.933, (float) 0.933, (float) 0.933));
				g.fillRect(0,y,getWidth()-10, 20);	
			}
			g.setColor(Color.black);
			g.drawRect(0,y,getWidth()-10, 20);	
			g.drawString(nameOfRes, 5, y+13);
			g.setColor(arrayColor[5]);
			if(draw3dBorders)
				g.drawLine(1,y+19,getWidth()-11,y+19);
			y+=20;
			rowCount++;
		}
		
	}	
		
    
    /** Function able to export in PNG format the graphic area */
  public void export(File file, String format, GanttExportSettings settings) {
    
		BufferedImage image = getChart(settings);

    try {
      if (!ImageIO.write(image, format, file)) {
        System.out.println("Impossible de sauvegarder dans ce format");

      }
    }
    catch (Exception e) {
      System.out.println(e);
    }
  }
    
    
     /** Print the project */
    public void printProject(GanttExportSettings settings) {
    	
			
			BufferedImage image = getChart(settings);
		
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
}

