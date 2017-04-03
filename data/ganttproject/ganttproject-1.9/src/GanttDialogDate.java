/***************************************************************************
                           GanttDialogDate.java  -  description
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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.lang.Integer;
import javax.swing.tree.*;
import java.util.Calendar;



/** 
  * Dialog allow you to select a date
  */
public class GanttDialogDate extends JDialog
{

	
	/** Combobox for the month */
	private JComboBox jcbmonth;
	
	/** Combobox for the year */
	private JComboBox jcbyear;
	
	/** Graphic area for display the year */
	private GanttDialogDateDay ddd;
	
	/** Save the date */
	private GanttCalendar save;
	
	
	/** Constructor */
	public GanttDialogDate(JDialog parent, GanttCalendar date, GanttLanguage language)
	{
		super(parent, language.chooseDate(), true);
		setResizable(false);
		
		//this.date=theDate;
		this.save = date.Clone();
	
		String [] year = new String[100];
		for(int i=0;i<100;i++)
		{
			Integer integer = new Integer(i+2000);
			year[i] = integer.toString();
		}
		
		String [] month = date.getDayMonthLanguage(language);
	
		Box vb1 = Box.createVerticalBox();
		Box hb1 = Box.createHorizontalBox();
		
		jcbmonth = new JComboBox(month);
		jcbmonth.setSelectedItem(language.getMonth(date.getMonth()));
		jcbyear = new JComboBox(year);
		jcbyear.setSelectedItem(new Integer(date.getYear()).toString());
		
		jcbmonth.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				changeMonth();
			}
		});
		
		jcbyear.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				changeYear();
			}
		});
		
		
		hb1.add(jcbmonth);
		hb1.add(jcbyear);
		
		ddd = new GanttDialogDateDay(date,language);
		
		vb1.add(hb1);
		vb1.add(ddd);	
	
	
		JPanel p = new JPanel();
		JButton ok = new JButton(language.getOk(), new ImageIcon("icons/yes.gif") );
		getRootPane().setDefaultButton(ok);
		p.add(ok);
		ok.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{
					setVisible (false); 
					save = ddd.date;
				}
			});
			

		JButton cancel = new JButton(language.getCancel(), new ImageIcon("icons/no.gif") );
		p.add(cancel);
		cancel.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{ 
					setVisible (false); 
				}
			});
		
	
		getContentPane().add(vb1,"Center");
		getContentPane().add(p,BorderLayout.SOUTH);
		pack();
		setResizable(false);
		Point point = parent.getLocationOnScreen();
		int x = (int)point.getX() + parent.getWidth()/2;
		int y = (int)point.getY() + parent.getHeight()/2;
		setLocation(x - getWidth()/2, y-getHeight()/2);
	}
	
	/** Return The selected date. */
	public GanttCalendar getDate() { return save; }
	
	/** This function was call when we change the item on a combobox of the month. */
	public void changeMonth()
	{
		//on fait une manipulation pour eviter un probleme
		//ex : si on est le 31 janvier et on desire afficher le moi de fevrier
		//  si on ne fait pas attention on change simplement de moi
		//Or il n'existe pas de 31 fevrier et la date se mettra le 31-28=3 mars et la pb!!!		
		GanttCalendar tmpdate = ddd.date.Clone();
		tmpdate.setMonth(jcbmonth.getSelectedIndex());
		tmpdate.setDay(1);		
		
		if(ddd.date.getDay()>tmpdate.getNumberOfDay())
			ddd.date.setDay(tmpdate.getNumberOfDay());
		
		ddd.date.setMonth(jcbmonth.getSelectedIndex());

		//display again
		ddd.repaint();
	}
	
	/** This function was call when we change the item on a combobox of the year */
	public void changeYear()
	{
		
		int year = jcbyear.getSelectedIndex()+2000;
		
		//voir explication au dessus mais dans le cas des années il n'y a que le moi de fevrier qui est litigieu
		if(ddd.date.getMonth()==1 && ddd.date.getDay()==29 && (year)%4!=0)
			ddd.date.setDay(28);
		
		ddd.date.setYear(year);
		
		//display again
		ddd.repaint();
	}
	
	
	/**
	  * Class use for display the day
	  */
	public class GanttDialogDateDay extends JPanel
	{
		/** The selected date */
		public GanttCalendar date;
		
		/** The language uses. */
		GanttLanguage language;
		
		
		/** Constructor */		
		public GanttDialogDateDay(GanttCalendar date,GanttLanguage language)
		{
			this.date = date;
			this.language = language;
			
			
			MouseListener ml = new MouseAdapter() {
		     public void mousePressed(MouseEvent e) {
				 clickFunction(e.getX(), e.getY());
		       }
		 	};
		 	this.addMouseListener(ml);
		}
		
		/** The default size. */
		public Dimension getPreferredSize()
		{
			return new Dimension(210, 105);
		}
		
		
		/** When the user click on the widget */
		public void clickFunction (int x, int y)
		{
			//Has he click on the panel???
			if(x<getWidth()/7*7 && y>15 && y < 7*15)
			{
			 	int X = x / (getWidth()/7);
				int Y = (y-15)/15;
					
				//Recup the first monday
				GanttCalendar tmpdate = date.Clone();
				tmpdate.setDay(1);
				String d = tmpdate.getdayWeek(language);
				while(!d.equals(language.getDay(1)))
				{
					tmpdate.go(Calendar.DATE,-1);
					d = tmpdate.getdayWeek(language);
				}	
				//Search the exact day
				for(int i=0;i<Y*7+X;i++) tmpdate.go(Calendar.DATE,1);
				
				//Check the validity of the month
				if(tmpdate.getMonth() == date.getMonth())	
					date = tmpdate;	
			 }
			 
			 repaint();
		
		}
	
		/** draw the panel */
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
	
			int sizex = getWidth();
			int sizey = getHeight();
			
			//Display the legend at top
			g.setColor(Color.white);
			g.fillRect(0,0,sizex,sizey);
			
			//two colors uses
			Color gris = new Color((float)0.827,(float)0.827,(float)0.827);
			Color bleu = new Color((float)0.29,(float)0.349,(float)0.643);
			
			g.setColor(bleu);
			g.fillRect(0,0,sizex,15);
			
			String [] dayWeek = date.getDayWeekLanguage(language);
			g.setColor(Color.white);
			
			for(int i=0;i<dayWeek.length;i++)
				g.drawString(dayWeek[(i+1)%7].substring(0,3),i*sizex/7+3,12);
			
			GanttCalendar tmpdate = date.Clone();
			tmpdate.setDay(1);
			String d = tmpdate.getdayWeek(language);
			while(!d.equals(language.getDay(1)))
			{
				tmpdate.go(Calendar.DATE,-1);
				d = tmpdate.getdayWeek(language);
			}
			
			for(int i=0;i<6;i++)
			{
				for(int j=0;j<7;j++)
				{
					if(tmpdate.getMonth()!=date.getMonth())
						g.setColor(gris);
					else
					{
						if(tmpdate.getDay() == date.getDay())
						{
							g.setColor(bleu);
							g.fillRect(j* sizex/7-1, 15+i*15, sizex/7,15);
							g.setColor(Color.white);
						}else g.setColor(Color.black);
					}
					
					g.drawString(""+tmpdate.getDate(), j* sizex/7+4, 30+i*15-3);
					tmpdate.go(Calendar.DATE,1);
				}			
			}
	
		}
	}	
}

