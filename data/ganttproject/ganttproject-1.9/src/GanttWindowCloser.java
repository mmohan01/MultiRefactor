/***************************************************************************
                           GanttWindowCloser.java  -  description
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

import java.awt.event.*;


/**
  * Class allow to close the window
  */
public class GanttWindowCloser extends WindowAdapter {

	/** 
	  * Constructor 
	  */
	public void windowClosing( WindowEvent e) {
		System. exit(0);
	}
}



