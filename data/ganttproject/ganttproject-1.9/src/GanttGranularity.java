/***************************************************************************
                           GanttGranularity.java  -  description
                             -------------------
    begin                : feb 2003
    copyright            : (C) 2003 by Thomas Alexandre
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

import java.awt.Graphics;
import java.util.Calendar;


/**
  * Class use to manage the granularity of the graphic area 
  */
public abstract class GanttGranularity
{

	/** Get the size of a single part of the granularity */
	abstract public int size();
	
	/**Move the date of granularity */
	abstract public void changeDate(GanttCalendar date, boolean next);
	
	/** Zoom the dateto the begin of granularity */
	abstract public void zoomToBegin(GanttCalendar date);
	
	/**Return the number of day visible for the granularity */
	abstract public int getGranit (GanttCalendar date,boolean day);

	/** Return the advance foot  */
	abstract public int getFoot(GanttCalendar date);
	
	/** Paint the first part of the header */
	abstract public void paintHeader1(GanttCalendar date,Graphics g, int x1, int x2);
	
	/** Paint the second part of the header */
	abstract public void paintHeader2(GanttCalendar date,Graphics g, int x1, int x2);


//--------------------------------------------------------------------------------------------------------//

	/** Class for show one month */	
	class GanttGranularityOneMonth extends GanttGranularity
	{
		/** A single constructor tha do nothing */
		public GanttGranularityOneMonth(){}
		
		/** Get the size of a single part of the granularity */
		public int size()
		{
			return 22;
		}
	
		/**Move the date of granularity */
		public void changeDate(GanttCalendar date, boolean next)
		{
			date.go(Calendar.MONTH,((next)?1:-1));
		}
	
		/** Zoom the dateto the begin of granularity */
		public void zoomToBegin(GanttCalendar date)
		{
			date.setDay(1);
		}
	
		/**Return the number of day visible for the granularity */
		public int getGranit (GanttCalendar date,boolean day)
		{
			return date.getNumberOfDay();
		}

		/** Return the advance foot  */
		public int getFoot(GanttCalendar date)
		{
			return 1;
		}
	
		/** Paint the first part of the header */
		public void paintHeader1(GanttCalendar date,Graphics g, int x1, int x2)
		{
			
		}
	
		/** Paint the second part of the header */
		public void paintHeader2(GanttCalendar date,Graphics g, int x1, int x2)
		{
		
		}	
	}
//--------------------------------------------------------------------------------------------------------//



}


