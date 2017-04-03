/***************************************************************************
                           GanttHTMLExport.java  -  description
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

import java.lang.String;
import javax.swing.tree.*;
import java.util.ArrayList;
import java.lang.Exception;
import java.io.*;



/** 
  * Class able to export the project in HTML
  */
public class GanttHTMLExport
{
	
	/** REturn a string with all \n replace with a <br> tags */
	private static String getLN( String t)
	{
		return t.replaceAll("\n","<br>");
	}

	/** Write the style.css file */
	private static void writeStyle (String path)
	{
		try{
			DataOutputStream fout = new DataOutputStream(new FileOutputStream(new File(path+"/style.css")));
			fout.writeBytes("A:link {\n");
			fout.writeBytes("	FONT-WEIGHT: bold;  TEXT-DECORATION: none;\n");
			fout.writeBytes("	FONT-SIZE: 12px ; COLOR: black;\n");
			fout.writeBytes("}\n");
			fout.writeBytes("A:visited {\n");
			fout.writeBytes("	FONT-WEIGHT: bold;  TEXT-DECORATION: none;\n");
			fout.writeBytes("	FONT-SIZE: 12px ; COLOR: black; BACKGROUND: none\n");
			fout.writeBytes("}\n");
			fout.writeBytes("A:hover {\n");
			fout.writeBytes("	FONT-WEIGHT: bold;  TEXT-DECORATION: none;\n");
			fout.writeBytes("	FONT-SIZE: 12px ; COLOR: white ; BACKGROUND: #4959a4\n");
			fout.writeBytes("}\n");
			fout.writeBytes("TD {\n");
			fout.writeBytes("	FONT-SIZE: 12px; COLOR: black; FONT-STYLE: normal; FONT-FAMILY: Arial, Helvetica, Geneva, sans-serif\n");
			fout.writeBytes("}\n");
			fout.writeBytes("UL {\n");
			fout.writeBytes("	FONT-SIZE: 12px; FONT-FAMILY: Arial, Helvetica, Geneva, sans-serif\n");
			fout.writeBytes("}\n");
			fout.writeBytes("LI {\n");
			fout.writeBytes("	FONT-SIZE: 12px; FONT-FAMILY: Arial, Helvetica, Geneva, sans-serif\n");
			fout.writeBytes("}\n");
			
			fout.writeBytes("H1 {\n");
			fout.writeBytes("	FONT-WEIGHT: bold; FONT-SIZE: 16pt; COLOR: #4959a4;\n");
			fout.writeBytes("}\n");
			fout.writeBytes("H2 {\n");
			fout.writeBytes("	FONT-WEIGHT: bold; FONT-SIZE: 16pt; COLOR: #000000;\n");
			fout.writeBytes("}\n");
			fout.writeBytes("H3 {\n");
			fout.writeBytes("	FONT-WEIGHT: bold; FONT-SIZE: 13pt; COLOR: #4959a4\n");
			fout.writeBytes("}\n");
			fout.writeBytes("H4 {\n");
			fout.writeBytes("	FONT-WEIGHT: bold; FONT-SIZE: 13px; COLOR: #000000\n");
			fout.writeBytes("}\n");
			fout.writeBytes("H5 {\n");
			fout.writeBytes("	FONT-WEIGHT: bold; FONT-SIZE: 10pt; COLOR: #4959a4\n");
			fout.writeBytes("}\n");
			fout.writeBytes("H6 {\n");
			fout.writeBytes("	FONT-WEIGHT: bold; FONT-SIZE: 10pt; COLOR: #000000\n");
			fout.writeBytes("}\n");
			
			fout.close();
		}catch(Exception e)	{
			System.out.println(e);
		}
	}
		
	/** Write the header on a stream */
	private static void writeHeader(DataOutputStream fout, String title,String file,String name)
	{
		try{
			fout.writeBytes("<html>\n <head>\n<title>"+title+"</title>\n</head>\n");
			fout.writeBytes("<LINK href=\"style.css\" type=text/css rel=stylesheet>\n");
			fout.writeBytes("<body bgcolor=white>\n");
			fout.writeBytes("<br><br><center><h1>"+name+"</h1>");
			
			fout.writeBytes("<a href=\""+file+".html\">Home</a> | \n");
			fout.writeBytes("<a href=\""+file+"-gantt.html\">Gantt chart</a> | \n");
			fout.writeBytes("<a href=\""+file+"-tasks.html\">Tasks</a> | \n");
			fout.writeBytes("<a href=\""+file+"-resources.html\">Resources</a><br>\n");
			
			fout.writeBytes("<hr width=\"800\"><br>\n");
		}catch(Exception e)	{
			System.out.println(e);
		}
	}
	
	/** Write the footer on a stream */
	private static void writeFooter(DataOutputStream fout,String version)
	{
		try{
			fout.writeBytes("<hr width=\"800\"></center>");
			fout.writeBytes("<b>GanttProject ("+version+")</b><br>\n");
			fout.writeBytes("<b><a href=\"http://ganttproject.sourceforge.net\">ganttproject.sourceforge.net</a></b><br>\n");
			fout.writeBytes("<b>"+GanttCalendar.getDateAndTime()+"</b><br>\n");
			fout.writeBytes("</body>\n</html>");
		}catch(Exception e)	{
			System.out.println(e);
		}
	}
	
	/** Write the index page */
	private static void writeIndex(DataOutputStream fout,String name, String desc,String orga)
	{
		try{
			fout.writeBytes("<table width=\"550\" border=1>\n");
			fout.writeBytes(" <tr>\n");
			fout.writeBytes("  <td><h3>Name</h3></td>\n");
			fout.writeBytes("  <td><h6>"+name+"</td>\n");
			fout.writeBytes(" </tr>\n");
			fout.writeBytes(" <tr>\n");
			fout.writeBytes("  <td><h3>Organization</h3></td>\n");
			fout.writeBytes("  <td><h6>"+orga+"</td>\n");
			fout.writeBytes(" </tr>\n");
			fout.writeBytes(" <tr>\n");
			fout.writeBytes("  <td><h3>Description</h3></td>\n");
			fout.writeBytes("  <td><h6>"+getLN(desc)+"</td>\n");
			fout.writeBytes(" </tr>\n");
			fout.writeBytes("</table>\n");
			fout.writeBytes("\n");
			fout.writeBytes("<br><br>\n");			
		}catch(Exception e)	{
			System.out.println(e);
		}
	}
	
	
	/** Write the gantt chart */
	private static void writeGanttChar(DataOutputStream fout, String imagefile)
	{
		try{
			fout.writeBytes("<br>\n<img src=\""+imagefile+"\"><br><br>");			
		}catch(Exception e)	{
			System.out.println(e);
		}
	}

	
	/** Write the tasks */
	private static void writeTasks(DataOutputStream fout, GanttTree tree)
	{	
		try{
			ArrayList lot=tree.getAllTasks();
			fout.writeBytes("<table width=800 border=1>\n");
			fout.writeBytes(" <tr>\n");
			fout.writeBytes("  <td><h5>Name</h5></td>\n");
			fout.writeBytes("  <td><h5>Start Date</h5></td>\n");
			fout.writeBytes("  <td><h5>End Date</h5></td>\n");
			fout.writeBytes("  <td><h5>Meeting</h5></td>\n");
			fout.writeBytes("  <td><h5>Precent-complete</h5></td>\n");
			fout.writeBytes("  <td><h5>Notes</h5></td>\n");
			fout.writeBytes(" </tr>\n");
			
			for(int i=0;i<lot.size();i++)
			{
				GanttTask t = (GanttTask) ((DefaultMutableTreeNode)lot.get(i)).getUserObject();
				if(!t.toString().equals("None"))
				{
					fout.writeBytes(" <tr>\n");
					fout.writeBytes("  <td valign=top><b>"+t.toString()+"</b></td>\n");
					fout.writeBytes("  <td valign=top>"+t.getStart()+"</td>\n");
					fout.writeBytes("  <td valign=top>"+t.getEnd()+"</td>\n");
					fout.writeBytes("  <td valign=top><center>"+(t.getBilan()?"X":"")+"</center></td>\n");
					fout.writeBytes("  <td valign=top><center>"+t.getPercent()+"</center></td>\n");
					fout.writeBytes("  <td valign=top>"+(t.getNotes()==null?"":getLN(t.getNotes()))+"</td>\n");
					fout.writeBytes(" </tr>\n");
				}
			}
			
			fout.writeBytes("</table>\n");		
		}catch(Exception e)	{
			System.out.println(e);
		}
	}
	
	/** Write the resources */
	private static void writeResources(DataOutputStream fout, GanttPeoplePanel peop)
	{
		try{
			ArrayList lor = peop.getPeople();
			GanttLanguage language = new GanttLanguageEnglish();
			
			String []function=language.getPersonFunction();
			
			fout.writeBytes("<table width=600 border=1>\n");
			fout.writeBytes(" <tr>\n");
			fout.writeBytes("  <td><h5>Name</h5></td>\n");
			fout.writeBytes("  <td><h5>Function</h5></td>\n");
			fout.writeBytes("  <td><h5>Contact</h5></td>\n");
			fout.writeBytes(" </tr>\n");
			
			for(int i=0;i<lor.size();i++)
			{
				GanttPeople p = (GanttPeople)lor.get(i);
				fout.writeBytes(" <tr>\n");
				fout.writeBytes("  <td valign=top><b>"+p.toString()+"</b></td>\n");
				fout.writeBytes("  <td valign=top>"+function[p.getFunction()]+"</td>\n");
				fout.writeBytes("  <td valign=top>"+p.getMail()+"</td>\n");
				fout.writeBytes(" </tr>\n");
			}
			
			fout.writeBytes("</table>\n");
		}catch(Exception e)	{
			System.out.println(e);
		}
	}
	
	
	/**Save the project in HTML */
	public static void save(File file, String name, String desc, String orga, 
		GanttProject prj, GanttTree tree, GanttPeoplePanel peop,GanttGraphicArea area,
		GanttStoreBoolean bool)
	{
		try{
			DataOutputStream fout = new DataOutputStream(new FileOutputStream(file));
			String path=file.getParent();			//the directory 
			String absolute = getFileName(file);	//file without the extention
			
			
			DataOutputStream fout_g = new DataOutputStream(new FileOutputStream(new File(path+"/"+absolute+"-gantt.html")));
			DataOutputStream fout_t = new DataOutputStream(new FileOutputStream(new File(path+"/"+absolute+"-tasks.html")));
			DataOutputStream fout_r = new DataOutputStream(new FileOutputStream(new File(path+"/"+absolute+"-resources.html")));
			
			
			//Write the index
			writeHeader(fout, "GanttProject - "+absolute, absolute,name);
			writeIndex(fout,name, desc, orga);
			writeFooter(fout, prj.version);
			
			//write the gantt chart
			writeHeader(fout_g, "GanttProject - "+absolute, absolute,name);
			writeGanttChar(fout_g,path+"/"+absolute+".png");
			writeFooter(fout_g, prj.version);
			
			//write the tasks
			writeHeader(fout_t, "GanttProject - "+absolute, absolute,name);
			writeTasks(fout_t,tree);
			writeFooter(fout_t, prj.version);
			
			//write the resources
			writeHeader(fout_r, "GanttProject - "+absolute, 	absolute,name);
			writeResources(fout_r, peop);
			writeFooter(fout_r, prj.version);
			
			//Write the style file
			writeStyle(path);
			
			//write the image of the calendar
			area.export(new File(path+"/"+absolute+".png"), bool.name, bool.percent, bool.depend);
			
			fout_r.close();
			fout_t.close();
			fout_g.close();
			fout.close();
		
		}catch(Exception e)	{
			System.out.println(e);
		}
	}


	/** Extention return */	
	public static String getFileName(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(0,i).toLowerCase();
        }
        return ext;
    }

}


