/***************************************************************************
                           GanttDialogInfo.java  -  description
                             -------------------
    begin                : mar 2003
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
import java.lang.Integer;  
  
  
/**
  * Class to show a message for the user
  */
public class GanttDialogInfo extends JDialog
{
	
	/** An error message */
	public static int ERROR = 0;

	/** A warning message */
	public static int WARNING = 1;
	
	/** An info message */
	public static int INFO = 2;	

	/** Ok result */
	public static int YES = 0;
	
	/** Cancel result */
	public static int NO = 1;
	
	/** Undo result */
	public static int CANCEL = 2;
	
	/** The result of the dialog box */
	public int res=0;
		
	/** only ok button */
	public static int YES_OPTION=0;

	/** ok and cancel button */
	public static int YES_NO_OPTION=1;
	
	/** ok and cancel button */
	public static int YES_NO_CANCEL_OPTION=2;

	/** Constructor */
	public GanttDialogInfo (JFrame parent, GanttLanguage language, int msgtype, int button,
		String message, String title)
	{
		
		super(parent, title,true);

		res = button;
	
		Box b1 = Box.createVerticalBox();
		
		if(msgtype==ERROR)
			b1.add(new JLabel( new ImageIcon("icons/error.gif")));
		else if(msgtype==WARNING)
			b1.add(new JLabel( new ImageIcon("icons/warning.gif")));
		else if(msgtype==INFO)
			b1.add(new JLabel( new ImageIcon("icons/info.gif")));
		
		
		getContentPane().add(b1,"West");
		
		
		Box b2 = Box.createVerticalBox();
		
		int oldIndex=0,newIndex=0;
		
		newIndex=message.indexOf("\n",oldIndex);
		if(newIndex==-1)
		{
			b2.add(new JLabel(" "));
			b2.add(new JLabel(message));
		}
			
		while(newIndex!=-1)
		{
			newIndex=message.indexOf("\n",oldIndex);
			
			if(newIndex!=-1)
				b2.add(new JLabel(message.substring(oldIndex,newIndex)));
			else b2.add(new JLabel(message.substring(oldIndex)));
			
			oldIndex=newIndex+1;
		}
		
		
		
		
		getContentPane().add(b2,"Center");
		
		
		JPanel p = new JPanel();
		//YES BUTTON
		JButton yes = new JButton((button==0)?language.getOk():language.getYes(), new ImageIcon("icons/yes.gif") );
		getRootPane().setDefaultButton(yes);
		p.add(yes);
		yes.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{
					setVisible (false);
					res=YES;
				}
			});
		
		if(button>0)
		{
			//NO BUTTON
			JButton no = new JButton(language.getNo(), new ImageIcon("icons/no.gif") );
			p.add(no);
			no.addActionListener(new ActionListener()
			 	{	public void actionPerformed(ActionEvent evt)
					{
						setVisible (false);
						res=NO;
					}
				});
			
			if(button>1)
			{
				//CANCEL BUTTON
				JButton cancel = new JButton(language.getCancel(), new ImageIcon("icons/no.gif") );
				p.add(cancel);
				cancel.addActionListener(new ActionListener()
				 	{	public void actionPerformed(ActionEvent evt)
						{
							setVisible (false);
							res=CANCEL;
						}
					});			
			}		
		}
		
		getContentPane().add(p,BorderLayout.SOUTH);

		pack();
		setResizable(false);
		Point point = parent.getLocationOnScreen();
		int x = (int)point.getX() + parent.getWidth()/2;
		int y = (int)point.getY() + parent.getHeight()/2;
		setLocation(x-getWidth()/2, y-getHeight()/2);
	}
}



  
