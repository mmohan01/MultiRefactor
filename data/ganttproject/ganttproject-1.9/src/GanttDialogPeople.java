/***************************************************************************
                           GanttDialogPeople.java  -  description
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
  * Class to edit setting for the project
  */
public class GanttDialogPeople extends JDialog
{

	/** JTextField for the name of person. */
	private JTextField jtfname;

	/** JConboBox for the function of person */
	private JComboBox jcbfunction;

	/** JTextField for the contact of person. */
	private JTextField jtfcontact;

	/** La langue utilisee. */
	GanttLanguage lang;
	
	/** The main class */
	GanttProject appli;
	
	/** The person to edit */
	GanttPeople people;
	
	/** The result --> cancel or ok pressed */
	boolean res= false;
	
	/** The constructor*/
	public GanttDialogPeople(GanttProject parent,GanttLanguage language, GanttPeople person)
	{
		super(parent, language.human(),true);
		
		this.lang = language;
		this.appli = parent;
		this.people = person;
		
		String [] cols = language.getColsName();
		
		
		Box b1 = Box.createVerticalBox();
		
		b1.add(new JLabel(cols[0]));
		jtfname = new JTextField (person.getName());
		b1.add(jtfname);
		
		String [] lof = language.getPersonFunction();
		b1.add(new JLabel(cols[1]));
		jcbfunction = new JComboBox(lof);
		jcbfunction.setSelectedIndex(person.getFunction());
		jcbfunction.setRenderer(new GanttCellRenderer());
		b1.add(jcbfunction);
		
		b1.add(new JLabel(cols[2]));
		jtfcontact = new JTextField (person.getMail());
		b1.add(jtfcontact);
		
		getContentPane().add(b1,"Center");
		
		JPanel p = new JPanel();
		JButton ok = new JButton(language.getOk(), new ImageIcon("icons/yes.gif") );
		getRootPane().setDefaultButton(ok);
		p.add(ok);
		
		//Listener sur le bouton ok
		ok.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{
					setVisible(false);	
					
					people.setName(jtfname.getText());
					people.setMail(jtfcontact.getText());
					people.setFunction(jcbfunction.getSelectedIndex());
					res=true;
				}
			});

		//bouton cancel
		JButton cancel = new JButton(language.getCancel(), new ImageIcon("icons/no.gif") );
		p.add(cancel);
		cancel.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{ 
					setVisible (false); 
					res=false;
				}
			});

		getContentPane().add(p,BorderLayout.SOUTH);

		pack();
		setResizable(false);
		
		Point point = parent.getLocationOnScreen();
		int x = (int)point.getX() + parent.getWidth()/2;
		int y = (int)point.getY() + parent.getHeight()/2;
		setLocation(x-getWidth()/2, y-getHeight()/2);
			
	}
	
	/** The result of the dialog */
	public boolean result() { return res; }


	/** Class to render the background of selected cell to special blue */
	class GanttCellRenderer extends JLabel implements ListCellRenderer {
     	public GanttCellRenderer() {
	         setOpaque(true);
    	 }
	     public Component getListCellRendererComponent(
	         JList list,
	         Object value,
    	     int index,
		     boolean isSelected,
	         boolean cellHasFocus)
	     {
	         setText(value.toString());
	         setBackground(isSelected ? new Color((float)0.290,(float)0.349,(float)0.643) : Color.white);
	         setForeground(isSelected ? Color.white : Color.black);
	         return this;
	     }
 }
 
}


