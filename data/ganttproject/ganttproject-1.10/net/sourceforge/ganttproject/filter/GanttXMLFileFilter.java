/***************************************************************************
                           GanttXMLFileFilter.java  -  description
                             -------------------
    begin                : feb 2003
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

package net.sourceforge.ganttproject.filter;
import java.io.File;

import javax.swing.filechooser.FileFilter;



/** 
  * Class to select a filter for the FileChooser objet (*.xml)
  */
public class GanttXMLFileFilter extends FileFilter 
{
 	
	/** Is the file OK??. */
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String extension = getExtension(f);
		
		if (extension != null && (extension.equals("xml") || extension.equals("gan")) )
			return true;
						
		return false;
	}
    
	/** Description of the file */
	public String getDescription() 
	{
		return "GanttProject Files (xml, gan)";
	}

	/** Extention return */	
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}
}


