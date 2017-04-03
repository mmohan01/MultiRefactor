
/**
  * @author THOMAS Alexandre
  */

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
  * Classe for load/save the project in a GAN file
  */
public class GanttIO
{
	/** Load the project from the filename */
	public static void load(File file, GanttProject prj, 
			 GanttTree tree, GanttPeoplePanel peop,GanttGraphicArea area, GanttLanguage language)
	{
		DefaultMutableTreeNode rootNode;
		GanttCalendar cal;		
		ArrayList listOfPeople;
		
		try {
			ObjectInputStream fin = new ObjectInputStream(new FileInputStream(file));
			
			//Load granularity
			int zoomValue = fin.readInt();
			area.setZoom(zoomValue);
			

			//Load the start date
			cal = (GanttCalendar)fin.readObject();
			area.setDate(cal);

			//Load the Jtree
			rootNode = (DefaultMutableTreeNode) fin.readObject();
			tree.setRoot(rootNode);

			
			//Load project name
			prj.projectName = (String)fin.readObject();
			
			//Load project description
			prj.decription = (String)fin.readObject();
			
			//load organisation
			prj.organization = (String)fin.readObject();
			
			//load list of people
			listOfPeople = (ArrayList)fin.readObject();
			
			//tree.setExpand(expand);
			
			
			peop.setPeople(listOfPeople);
			
			//Refresh graphic area
			area.zoomToBegin();
			area.repaint();

			fin.close();

		} catch (Exception e) {
			JOptionPane.showConfirmDialog(prj,
				language.msg2() + file.getName(), language.error(),
				JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE );
		}
	}
	
	
	
	/** Save the project */
	public static void save(String filename, String name, String desc, String orga, GanttTree tree, GanttPeoplePanel peop,GanttGraphicArea area)
	{
		try
		{
			File file = new File(filename);
			ObjectOutputStream fout = new ObjectOutputStream(new FileOutputStream(file));
			DefaultMutableTreeNode rootNode = tree.getRoot();

			//Write zoom value and date
			fout.writeInt(area.getZoom());
			fout.writeObject(area.getDate());
			//Write the jtree
			fout.writeObject(rootNode);
			
			//write projects parameters
			fout.writeObject(name);
			fout.writeObject(desc);
			fout.writeObject(orga);
			
			//Write people
			fout.writeObject(peop.getPeople());
			
			//fout.writeObject(tree.getExpand());
			
			fout.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
			System.out.println("Error in save file");
		}
	}

}

