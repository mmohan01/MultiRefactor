/***************************************************************************
                           GanttXMLSaver.java  -  description
                             -------------------
    begin                : feb 2003
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


import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.String;
import java.util.ArrayList;
import java.lang.Exception;
import java.io.*;
import java.awt.event.*;
import javax.swing.tree.*;







/**
  * Classe for save the project in a XML file
  */
public class GanttXMLSaver
{

	private GanttTree tree;
	private GanttPeoplePanel peop;
	private GanttGraphicArea area;
	private String name;
	private String desc;
	private String orga;

	ArrayList number= new ArrayList();
	ArrayList lot = new ArrayList();
	ArrayList lots = new ArrayList();
	
	int cpt;
	
	String s="    "; //the marge

	/** The constructor */
	public GanttXMLSaver(String name, String desc, String orga, GanttTree tree, GanttPeoplePanel peop,GanttGraphicArea area)
	{
		this.tree = tree;
		this.peop = peop;
		this.area = area;
		this.name = name;
		this.desc = desc;
		this.orga = orga;
	}
	
	/** Replace a part of the string by another one */
	public String replaceAll(String notes,String s1, String s2)
	{
		return notes.replaceAll(s1,s2);
	}
	
	
	/** Simple write information of tasks */
	public void writeTask(DataOutputStream fout, int id, String space)
	{
		String space2 = s+space;
		try{
			if(id>=lot.size()) return;
			GanttTask task = (GanttTask) ((DefaultMutableTreeNode)lot.get(id)).getUserObject();
			boolean haschild=false;
		
			ArrayList child = tree.getAllChildTask(task.toString());
			if(child.size()!=0)
				haschild=true;
		
			
			number.add(new Integer(id));		
			cpt++;
			
			//boolean one= (task.getDepend().size()!=0 || task.getNotes()!=null || haschild);
			
			//Writes data of task
			fout.writeBytes(space+"<task id=\""+lots.indexOf(task.toString())+"\" ");
			fout.writeBytes("name=\""+task.toString()+"\" ");
			fout.writeBytes("meeting=\""+((task.getBilan())?"true":"false")+"\" ");
			fout.writeBytes("start=\""+task.getStart()+"\" ");
			fout.writeBytes("duration=\""+ task.getLength()+"\" ");
			fout.writeBytes("complete=\""+task.getPercent()+"\"");
			/*if(one)fout.writeBytes(">\n");
			else fout.writeBytes("/>\n");*/
			fout.writeBytes(">\n");
			
			//Write notes
			if(task.getNotes()!=null)
			{
				fout.writeBytes(space2+"<notes>");
				fout.writeBytes("\n"+space2+s+replaceAll(task.getNotes(),"\n","\n"/*+space2+s*/));
				fout.writeBytes("\n"+space2+"</notes>\n");
			}
			
			//Write the depends of the task
			if(task.getDepend().size()!=0)
			{				
				//fout.writeBytes(space2+"<depends>\n");
				for(int i=0;i<task.getDepend().size();i++)
					fout.writeBytes(space2/*+s*/+"<depend id=\""+lots.indexOf((String)task.getDepend().get(i))+"\"/>\n");
				//fout.writeBytes(space2+"</depends>\n");
			}
					
			
			//Write the child of the task
			if(haschild)
			{
				for(int i=0;i<child.size();i++)
				{
					GanttTask task2 = (GanttTask) ((DefaultMutableTreeNode)child.get(i)).getUserObject();
					int newid = -1;//lot.lastIndexOf(task2);
					
					for(int j=0;j<lot.size();j++)
					{
						String a = task2.toString();
						String b = lot.get(j).toString();
						
						if(a.equals(b))
							newid=j;
					}
					writeTask(fout,newid,space+s);
				}
			
				//if(haschild)fout.writeBytes(space+"</task>\n");
			}
			
			//end of task section
			//if(one)fout.writeBytes(space+"</task>\n");
			fout.writeBytes(space+"</task>\n");
			
			if(tree.getNode(task.toString()).isLeaf() && !tree.getFatherNode(task).isRoot())
				return;
			
			if(id==lot.size()-1) return;			
			else writeTask(fout,cpt,space);
			
		
		}catch(Exception e){System.out.println(e);}
	}
	
	/** Write the resources on the file */
	public void writeResources(DataOutputStream fout)
	{
		try{
			ArrayList resources = peop.getPeople();

			for(int i=0;i<resources.size();i++)	
			{
				GanttPeople p = (GanttPeople)resources.get(i);
			
				fout.writeBytes(s+s+"<resource name=\""+p.getName()+"\" function=\""+p.getFunction()+"\" contacts=\""+p.getMail()+"\"/>\n");
			}
		}catch(Exception e){System.out.println(e);}
	}
	
	/** Save the project on a XML file */
	public void save(String filename, String version)
	{
		try{
			
			
			File file = new File (filename);			
			DataOutputStream fout = new DataOutputStream(new FileOutputStream(file));
			//String space="    ";
			number.clear();
			
			//write header
			//fout.writeBytes("<?xml version=\"1.0\"?>\n");
			fout.writeBytes("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE ganttproject.sourceforge.net>\n");
			
			fout.writeBytes("<project name=\""+name+"\" company=\""+orga+"\" view-date=\""+area.getDate()+"\" view-zoom=\""+area.getZoom()+"\" version=\""+version+"\">\n");
		
			fout.writeBytes(s+"<description>");
			if(!desc.equals("")) fout.writeBytes("\n"+s+s+replaceAll(desc,"\n", "\n"/*+s+s*/)+"\n");
			fout.writeBytes(s+"</description>\n\n");
			
			
		
			lot = tree.getAllTasks();
			lots = tree.getArryListTaskString(null);
			
			//begin of tasks
			fout.writeBytes(s+"<tasks>\n");
			
			cpt=1;
			writeTask(fout,1,s+s);
						
			//end of tasks
			fout.writeBytes(s+"</tasks>\n\n");
			
			
			//Nothing for the moment
			fout.writeBytes(s+"<resources>\n");
			writeResources(fout);			
			fout.writeBytes(s+"</resources>\n\n");

		
			//end of project
			fout.writeBytes("</project>\n");
			fout.close();
		
		
		}catch(Exception e)
		{
			System.out.println(e);
			//System.out.println("Erreur dans la sauvegarde du fichier");
		}
	}

}


