/***************************************************************************
 *
 * GanttDialogProperties.java  -  description
 *
 * -------------------
 *
 * begin                : dec 2002
 *
 * copyright            : (C) 2002 by Thomas Alexandre
 *
 * email                : alexthomas(at)ganttproject.org
 *
 ***************************************************************************/



/***************************************************************************
 *
 *                                                                         *
 *
 *   This program is free software; you can redistribute it and/or modify  *
 *
 *   it under the terms of the GNU General Public License as published by  *
 *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *
 *   (at your option) any later version.                                   *
 *
 *                                                                         *
 *
 ***************************************************************************/


package net.sourceforge.ganttproject.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.ganttproject.GanttCalendar;
import net.sourceforge.ganttproject.GanttGraphicArea;
import net.sourceforge.ganttproject.GanttTask;
import net.sourceforge.ganttproject.GanttTree;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;

/**
 *
 * Dialog to edit the properties of a task
 *
 */

public class GanttDialogProperties

extends JDialog

implements ActionListener {

    /** JTextField for the name of task */

    private JTextField jtfname;

    /** JConboBox for the mother task */

    private JComboBox jcbfather;

    /** JTextField for the begining of the task */

    private JTextField jtfbegin;

    /** JTextField for duration of task */

    private JTextField jtflength;

    /** JList for depends of task */

    //private JList jldepend;

    /** JSlider for advent state of task */

    private JSlider spercent;

    /** JPanel for advancement label */

    private JLabel advancementLabel;

    /** JCheckBox . Is is a meetin point??? */

    private JCheckBox jcbbilan;

    /** Boolean to say if the task has child */

    boolean haschild = false;





    /** The GanttTree of the application. */

    private GanttTree ttree;

    /** GanttTask to edit */

    private Task ttask;

    /** GanttGraphicArea of the application. */

    private GanttGraphicArea aarea;

    /** Button to choose the date */

    private JButton button;

    /** Store old percent state value */

    int percentValue;

    /** The language */

    private GanttLanguage lang = GanttLanguage.getInstance();

    JFrame prj;

   int saveDuration;


    Color saveColor;



    /**true if the ok button was pressed */

    public boolean change = false;



    /** Color chooser dialog */

    protected static JColorChooser colorChooser = new JColorChooser();



    //By CL

    private GanttTaskPropertiesBean taskPropertiesBean;

    private Task task;

    private GanttTree tree;

    private GanttGraphicArea area;

	private JFrame parent;

    /** Constructor*/

    public GanttDialogProperties(JFrame parent, GanttTree tree, Hashtable managerHash,
        GanttTask task, GanttGraphicArea area) {

        super(parent,GanttLanguage.getInstance().getText("propertiesFor")+" '"+task.toString()+"'",true);

		this.tree = tree;

        this.task = task;
	
        saveDuration = task.getLength();

        this.area=area;
				
		this.parent = parent;


        //tree.forwardScheduling();


        taskPropertiesBean = new GanttTaskPropertiesBean(this,task, tree, managerHash);

        Container cp = getContentPane();

        taskPropertiesBean.addActionListener(this);

        cp.add(taskPropertiesBean, BorderLayout.CENTER);



        //this.setSize(600, 350);
        this.pack();
		setResizable(false);
		DialogAligner.center(this, getParent());

	    applyComponentOrientation(lang.getComponentOrientation());
        //GanttTask returnTask = taskPropertiesBean.getReturnTask();

	    //set the ok button as default action for enter
	    if(getRootPane()!=null)
	    	getRootPane().setDefaultButton(taskPropertiesBean.okButton);
    }



    /** Search a string on an array */

    private int findTheIndex(Object s, String[] lot) {

        for (int i = 0; i < lot.length; i++) {

            if (s.toString() == lot[i]) {

                return i;

            }
        }

        return 0;

    }



    /** When click on date button, it open the dialog to select date. */

    public void actionPerformed(ActionEvent evt) {

        if (evt.getSource()instanceof TestGanttRolloverButton) {

            TestGanttRolloverButton button = (TestGanttRolloverButton) evt.getSource();

            if (button.getName().equals("start")) {

                GanttCalendar date = taskPropertiesBean.getStart();

                GanttDialogDate dd = new GanttDialogDate(this, date, true);

                dd.show();

                taskPropertiesBean.setStart(dd.getDate(), false);

            }

            else if (button.getName().equals("finish")) {

                GanttCalendar date = taskPropertiesBean.getEnd();

                GanttDialogDate dd = new GanttDialogDate(this, date);

                dd.show();

                taskPropertiesBean.setEnd(dd.getDate(), false);

            }

        }

        else if (evt.getSource()instanceof JButton) {

            JButton button = (JButton) evt.getSource();

            if (button.getName().equals("ok")) {

                this.setVisible(false);
								dispose();

                Task returnTask = taskPropertiesBean.getReturnTask();
                //System.err.println("[GanttDialogProperties] returnTask="+returnTask);
                //returnTask.setTaskID(this.task.getTaskID());
                //getTaskManager().setTask(returnTask);

                

                tree.getNode(task.getTaskID()).setUserObject(returnTask);

				//Refresh all father                
				DefaultMutableTreeNode father=tree.getFatherNode(tree.getNode(task.getTaskID()));				
				while(father!=null){
					//tree.forwardScheduling();
					father=tree.getFatherNode(father);
				}

				
				tree.getJTree().repaint();

				tree.getJTree().updateUI();
				tree.getJTree().setRowHeight(20);
                area.repaint();

				change = true;
            }

            else if (button.getName().equals("cancel")) {

                this.setVisible(false);
								dispose();

            }

        }

	else if (evt.getSource()instanceof JTextField) {

        	JTextField textfield = (JTextField) evt.getSource();

			if(textfield.getName().equals("length"))
			{
				try{
					int duration = Integer.parseInt(textfield.getText().trim());
					taskPropertiesBean.changeLength(duration);
					saveDuration=duration;
				} catch (Exception e) {
					textfield.setText(saveDuration+"");
					GanttDialogInfo gdiError = new GanttDialogInfo(parent,GanttDialogInfo.ERROR,GanttDialogInfo.YES_OPTION,
								lang.getText("msg16"),lang.getText("error"));
					gdiError.show();
					taskPropertiesBean.changeLength(saveDuration);
					
				}
			} 
			//Change the name of task
			else  if(textfield.getName().equals("name_of_task")) {
				taskPropertiesBean.changeNameOfTask();			
			}
	}

    }
    
    private TaskManager getTaskManager() {
    	return this.task.getManager();
    }


}

