/***************************************************************************
               ColorSettingsPanel.java 
------------------------------------------
begin                : 27 juin 2004
copyright            : (C) 2004 by Thomas Alexandre
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
package net.sourceforge.ganttproject.gui.options;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.language.GanttLanguage;

/**
 * @author athomas
 *
 * Panel for choose the default color of a new task.
 */
public class ColorSettingsPanel extends GeneralOptionPanel {

	JButton bTaskColor;
	JButton bResourceColor;
	JButton bResourceOverloadColor;
	
	private GanttProject appli;
	
	protected static JColorChooser colorChooser = new JColorChooser();
	Box vb1 = Box.createVerticalBox();
	Box vb2 = Box.createVerticalBox();
	
	/** Constructor. */
	public ColorSettingsPanel(GanttProject parent)
	{
		super(GanttProject.correctLabel(GanttLanguage.getInstance().getText("colors")),
				  GanttLanguage.getInstance().getText("settingsDefaultColor"),parent);
		
		appli = parent;
		
		bTaskColor = new JButton(GanttLanguage.getInstance().getText("colorButton"));		
		vb1.add(new JLabel(GanttProject.correctLabel(language.getText("task"))+"  "));
		vb1.add(new JPanel());
		vb2.add(bTaskColor, BorderLayout.CENTER);
		
		bResourceColor = new JButton(GanttLanguage.getInstance().getText("colorButton"));		
		vb1.add(new JLabel(GanttProject.correctLabel(language.getText("resources"))+"  "));
		vb1.add(new JPanel());
		vb2.add(bResourceColor, BorderLayout.CENTER);
		
		bResourceOverloadColor = new JButton(GanttLanguage.getInstance().getText("colorButton"));		
		vb1.add(new JLabel(GanttProject.correctLabel(language.getText("resourcesOverload"))+"  "));
		vb1.add(new JPanel());
		vb2.add(bResourceOverloadColor, BorderLayout.CENTER);
				
		final String colorChooserTitle = GanttLanguage.getInstance().getText("selectColor");
		bTaskColor.addActionListener(new ActionListener() {
      		public void actionPerformed(ActionEvent e) {
      			JDialog dialog;
		        dialog = JColorChooser.createDialog(null, colorChooserTitle, true,
		                                            colorChooser,new ActionListener() {
			          public void actionPerformed(ActionEvent e) {
			          	bTaskColor.setBackground(colorChooser.getColor());
			          	bHasChange=true;
		    	      }
		        }
        	, new ActionListener() {
		          public void actionPerformed(ActionEvent e) {
		            // nothing to do for "Cancel"
		          }
        	});
		    
		        colorChooser.setColor(bTaskColor.getBackground());
        	dialog.show();
      	  }
    	});
		
		bResourceColor.addActionListener(new ActionListener() {
      		public void actionPerformed(ActionEvent e) {
		        JDialog dialog;
		        dialog = JColorChooser.createDialog(null, colorChooserTitle, true,
		                                            colorChooser,new ActionListener() {
			          public void actionPerformed(ActionEvent e) {
			          	bResourceColor.setBackground(colorChooser.getColor());
			          	bHasChange=true;
		    	      }
		        }
        	, new ActionListener() {
		          public void actionPerformed(ActionEvent e) {
		            // nothing to do for "Cancel"
		          }
        	});
		        colorChooser.setColor(bResourceColor.getBackground());
        	dialog.show();
      	  }
    	});
		
		bResourceOverloadColor.addActionListener(new ActionListener() {
      		public void actionPerformed(ActionEvent e) {
		        JDialog dialog;
		        dialog = JColorChooser.createDialog(null, colorChooserTitle, true,
		                                            colorChooser,new ActionListener() {
			          public void actionPerformed(ActionEvent e) {
			          	bResourceOverloadColor.setBackground(colorChooser.getColor());
			          	bHasChange=true;
		    	      }
		        }
        	, new ActionListener() {
		          public void actionPerformed(ActionEvent e) {
		            // nothing to do for "Cancel"
		          }
        	});
		        colorChooser.setColor(bResourceOverloadColor.getBackground());
        	dialog.show();
      	  }
    	});
		

		JPanel finalPanel = new JPanel(new BorderLayout());
		finalPanel.add(vb1, BorderLayout.WEST);
		finalPanel.add(vb2, BorderLayout.CENTER);
		this.vb.add(finalPanel);
		
		applyComponentOrientation(language.getComponentOrientation());
	}
	
	
	/** This method check if the value has changed, and assk for commit changes. */
	public boolean applyChanges(boolean askForApply)
	{
		if(bHasChange){
			if(!askForApply || (askForApply && askForApplyChanges())){
				appli.getUIConfiguration().setTaskColor(bTaskColor.getBackground());
				appli.getUIConfiguration().setResourceColor(bResourceColor.getBackground());
				appli.getUIConfiguration().setResourceOverloadColor(bResourceOverloadColor.getBackground());
			}
		}
		return bHasChange;
	}
	
	/** Initialize the component. */
	public void initialize()
	{
		bTaskColor.setBackground(appli.getUIConfiguration().getTaskColor());
		bResourceColor.setBackground(appli.getUIConfiguration().getResourceColor());
		bResourceOverloadColor.setBackground(appli.getUIConfiguration().getResourceOverloadColor());
	}
}
