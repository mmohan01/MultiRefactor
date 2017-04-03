/***************************************************************************
                           GanttDialogNotes.java  -  description
                             -------------------
    begin                : jan 2003
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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.lang.Integer;
import javax.swing.tree.*;

/**
  * Dialog to edit notes for the task
  */
public class GanttDialogNotes extends JDialog
{

	/** JTexteArea to edit notes */
	JTextArea tanotes;
	
	/** The specify GanttTask*/
	private GanttTask ttask;
	/** The GanttTree of the application. */
	private GanttTree ttree;
	
	
	/** Constructor. */
	public GanttDialogNotes(JFrame parent, GanttTree tree, GanttTask task,GanttLanguage language)
	{
		super(parent, language.notesFor()+task.toString(),true);
		
		this.ttask = task;
		this.ttree = tree;
		
		Box b1 = Box.createVerticalBox();
		
		b1.add(new JLabel("Notes"));
		tanotes = new JTextArea (task.getNotes());
		JScrollPane scrollPane = new JScrollPane(tanotes);
		scrollPane.setPreferredSize(new Dimension(300, 250));
		b1.add(scrollPane);
		
		getContentPane().add(b1,"Center");
		
		JPanel p = new JPanel();
		
		
		//Add the current date and hour at the end of the notes
		JButton bdate = new JButton (new ImageIcon("icons/clock.gif"));
		bdate.setToolTipText("<html><body bgcolor=#FFFFBD>"+language.putDate()+"</body></html>");
		p.add(bdate);
		bdate.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{ 
					tanotes.append("\n\n"+GanttCalendar.getDateAndTime()+"\n-------------------------------\n");
				}
			});
		
		
		
		JButton ok = new JButton(language.getOk(), new ImageIcon("icons/yes.gif") );
		p.add(ok);
		getRootPane().setDefaultButton(ok);
		//Listener on ok button
		ok.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{ 
					//hide the window
					setVisible (false); 
										
					//Change parameters
					ttask.setNotes(tanotes.getText());
										
					DefaultMutableTreeNode node = ttree.getNode(ttask.toString());
					node.setUserObject(ttask);
					
				}
			});
		
		//cancel button
		JButton cancel = new JButton(language.getCancel(), new ImageIcon("icons/no.gif") );
		p.add(cancel);
		cancel.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{ setVisible (false); }
			});
		
		getContentPane().add(p,BorderLayout.SOUTH);
		
		pack();
		setResizable(true);
		Point point = parent.getLocationOnScreen();
		int x = (int)point.getX() + parent.getWidth()/2;
		int y = (int)point.getY() + parent.getHeight()/2;
		setLocation(x - getWidth()/2, y-getHeight()/2);
	}

}
