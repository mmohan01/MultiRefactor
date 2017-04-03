/***************************************************************************
                           GanttDialogNewTask.java  -  description
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





/**
  * Dialog to generate a new Task on the project
  */
public class GanttDialogNewTask extends JDialog implements ActionListener
{

	/** JTextField for the name of task */
	private JTextField jtfname;
	/** JConboBox for the mother task */
	private JComboBox jcbfather;
	/** JTextField for the begining of the task */
	private JTextField jtfbegin;
	/** JTextField for duration of task */
	private JTextField jtflength;
	/** JList for depends of task */
	private JList jldepend;
	/** JSlider for advent state of task */
	private JSlider spercent;
	/** JCheckBox . Is is a meetin point??? */
	private JCheckBox jcbbilan;

	/** Graphicarea of the application */
	private GanttGraphicArea aarea;
	/** GanttTree of the application. */
	private GanttTree ttree;
	/** Boutton to select the date */
	private JButton button;
	/** The language */
	GanttLanguage lang;
	JFrame prj;

	/** static field in put avec the name of the task (two task can't have the same name) */
	static int tasknumber=0;

	/** Construtor */
	public GanttDialogNewTask(JFrame parent,GanttTree tree,GanttGraphicArea area,GanttLanguage language)
	{

		super(parent, language.newTask(),true);
		setResizable(false);
		this.ttree = tree;
		this.aarea = area;
		this.lang = language;
		this.prj=parent;

		final String [] lot = tree.getAllTaskString(null);

		Box b1 = Box.createVerticalBox();

		//The name of the task
		b1.add(new JLabel(language.name()));
		jtfname = new JTextField (language.newTask()+(++tasknumber));
		b1.add(jtfname);

		//The mother task
		b1.add(new JLabel(language.motherTask()));
		jcbfather = new JComboBox(lot);
		GanttTask current = tree.getSelectedTask();
		if(current==null) jcbfather.setSelectedItem(language.none());
		else jcbfather.setSelectedItem(tree.getSelectedTask().toString());
		b1.add(jcbfather);

		//The begining of the task
		b1.add(new JLabel(language.dateOfBegining()));
		GanttCalendar today = new GanttCalendar();
		if(current==null) jtfbegin=new JTextField(today.toString());
		else jtfbegin=new JTextField(current.getStart().toString());
		jtfbegin.setEnabled(false);
		Box hb1 = Box.createHorizontalBox();
		hb1.add(jtfbegin);
		button = new JButton(new ImageIcon("icons/calendar.gif"));
		button.addActionListener(this);
		button.setToolTipText("<html><body bgcolor=#FFFFBD>"+language.chooseDate()+"</body></html>");
		hb1.add(button);
		b1.add(hb1);

		//Duration of the task
		b1.add(new JLabel(language.length()));
		jtflength=new JTextField("10");
		JScrollPane scrollpane = new JScrollPane(jtflength);
		b1.add(scrollpane);

		//Is it a meeting point???
		jcbbilan = new JCheckBox(language.meetingPoint());
		jcbbilan.setSelected(false);
		b1.add(jcbbilan);
		/*jcbbilan.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{
					//System.out.println("CheckBox pressé");
				}
			});*/


		Box b2 = Box.createVerticalBox();

		//Depends list
		b2.add(new JLabel(language.depends()));
		String lot2 [] = new String[lot.length-1];
		for(int i=1;i<lot.length;i++) lot2[i-1]=lot[i];
		jldepend = new JList(lot2);
		jldepend.setSelectionBackground(new Color((float)0.29,(float)0.349,(float)0.643));
		jldepend.setSelectionForeground(new Color((float)1,(float)1,(float)1));
		JScrollPane scrollPane = new JScrollPane(jldepend);
		b2.add(scrollPane);

		//Advant state
		b2.add(new JLabel(language.advancement()));
		spercent = new JSlider(JSlider.HORIZONTAL, 0, 100, 0) ;
		spercent.setPaintLabels(true);
		spercent.setPaintTicks(true);
		spercent.setPaintTrack(true);
		b2.add(spercent);

		getContentPane().add(b1,"West");

		getContentPane().add(b2,"East");


		JPanel p = new JPanel();
		JButton ok = new JButton(language.getOk(), new ImageIcon("icons/yes.gif") );
		getRootPane().setDefaultButton(ok);
		p.add(ok);

		final JDialog theDialog = this;

		//Listener on ok button
		ok.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{

					ArrayList alt = ttree.getArryListTaskString(null);
					String theName = jtfname.getText();

					//chek the name of the task
					if(!alt.contains((Object)(theName)))
					{
						setVisible (false);
						
						//change parameters of task
						GanttCalendar cal = new GanttCalendar(jtfbegin.getText());
						Integer length = new Integer(jtflength.getText());

						//Create new task
						GanttTask task = new GanttTask(theName, cal , length.hashCode());
						task.setPercent(spercent.getValue());
						task.setBilan(jcbbilan.isSelected());
						if(task.getBilan()) task.setLength(1);


						//Create depends
						int [] indices = jldepend.getSelectedIndices();
						for(int i=0;i<indices.length;i++)
						{
							//if(!(ttree.getTask(lot[indices[i]]).getDepend().contains(task.toString())))
							if(ttree.checkDepend(task, lot[indices[i]]))
							{
								task.setDepend(lot[indices[i]]);
								ttree.refreshAllChild(lot[indices[i]]);
								ttree.refreshAllFather(lot[indices[i]]);
							}
						}

						//Add task on the tree
						DefaultMutableTreeNode father = ttree.getNode((String)jcbfather.getSelectedItem());
						GanttTask f=(GanttTask)father.getUserObject();
						f.setBilan(false);
						ttree.addObject(task, father);


						ttree.refreshAllChild(theName);

						GanttTask taskFather = new GanttTask("__Bidon__",null,0);
						//Fo refresh all the mother task
						while(ttree.getNode(task.toString()).isRoot()==false)
						{
							father = ttree.getFatherNode(task);
							ttree.refreshAllChild(father.toString());
							taskFather = (GanttTask)father.getUserObject();
							taskFather.refreshDateAndAdvancement(ttree);
							father.setUserObject(taskFather);
							task = taskFather;
						}

						//Refresh the graphic area
						aarea.repaint();
					}
					else
					{
						/*JOptionPane.showConfirmDialog(theDialog,
								lang.propertiesMsg(theName), lang.error(),
								JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE );*/
						GanttDialogInfo gdi = new GanttDialogInfo(prj,lang, GanttDialogInfo.ERROR,
											GanttDialogInfo.YES_OPTION,lang.propertiesMsg(theName),lang.error());
						gdi.show();
					}
				}
			});
		//cancel bouton
		JButton cancel = new JButton(language.getCancel(), new ImageIcon("icons/no.gif") );
		p.add(cancel);
		cancel.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{ setVisible (false); }
			});

		getContentPane().add(p,BorderLayout.SOUTH);

		pack();
		setResizable(false);
		Point point = parent.getLocationOnScreen();
		int x = (int)point.getX() + parent.getWidth()/2;
		int y = (int)point.getY() + parent.getHeight()/2;
		setLocation(x - getWidth()/2, y-getHeight()/2);
	}

	/** When click on date button, it open the dialog to select date. */
	public void actionPerformed (ActionEvent evt)
	{
		if(evt.getSource() == button)
		{
			GanttCalendar date = new GanttCalendar(jtfbegin.getText());
			GanttDialogDate dd = new GanttDialogDate(this,date,lang);
			dd.show();
			jtfbegin.setText(dd.getDate().toString());
		}
	}

}



