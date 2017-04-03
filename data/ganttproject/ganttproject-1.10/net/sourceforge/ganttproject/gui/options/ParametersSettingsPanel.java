/***************************************************************************
               ParametersSettingsPanel.java 
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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.document.HttpDocument;
import net.sourceforge.ganttproject.language.GanttLanguage;

/**
 * @author athomas
 *
 * Panel to edit the project properties.
 */
public class ParametersSettingsPanel extends GeneralOptionPanel {

	JCheckBox cbAutomatic;
	JCheckBox cbDrag;
	JCheckBox cbRedLine;
	JSpinner spLockDAV;
	JTextField tfTaskPrefix;
	
	private GanttProject appli;
	
	/** Constructor. */
	public ParametersSettingsPanel(GanttProject parent)
	{
		super(GanttProject.correctLabel(GanttLanguage.getInstance().getText("parameters")),
			  GanttLanguage.getInstance().getText("settingsParameters"),parent);
			  
		appli = parent;
		
		//automatic launch of task properties
		JPanel autoPanel = new JPanel(new BorderLayout());
		autoPanel.add(cbAutomatic = new JCheckBox(), BorderLayout.WEST);
		autoPanel.add(new JLabel(language.getText("automaticLaunch")), BorderLayout.CENTER);
		vb.add(autoPanel);
		vb.add(new JPanel());
		
		//move on the graphic area with mouse option
		JPanel movePanel = new JPanel(new BorderLayout());
		movePanel.add(cbDrag = new JCheckBox(), BorderLayout.WEST);
		movePanel.add(new JLabel(language.getText("dragTime")), BorderLayout.CENTER);
		vb.add(movePanel);
		vb.add(new JPanel());
		
		//red line today
		JPanel redLinePanel = new JPanel(new BorderLayout());
		redLinePanel.add(cbRedLine = new JCheckBox(), BorderLayout.WEST);
		redLinePanel.add(new JLabel(language.getText("redLine")), BorderLayout.CENTER);
		vb.add(redLinePanel);
		vb.add(new JPanel());
		
		//webdav time block
		JPanel webDavPanel = new JPanel(new BorderLayout());
		webDavPanel.add(spLockDAV = new JSpinner(new SpinnerNumberModel(240, 1, 1440, 1)), 
					BorderLayout.WEST);
		webDavPanel.add(new JLabel(language.getText("lockDAV")), BorderLayout.CENTER);
		vb.add(webDavPanel);
		vb.add(new JPanel());	
		
		//task name prefix
		JPanel taskPrefixPanel = new JPanel(new BorderLayout());
		taskPrefixPanel.add(new JLabel(language.getText("taskNamePrefix")), BorderLayout.WEST);
		taskPrefixPanel.add(tfTaskPrefix = new JTextField(), BorderLayout.CENTER);
		vb.add(taskPrefixPanel);
		vb.add(new JPanel());	

		applyComponentOrientation(language.getComponentOrientation());
	}
	
	/** This method check if the value has changed, and assk for commit changes. */
	public boolean applyChanges(boolean askForApply)
	{
		if(getAutomatic() == appli.getOptions().getAutomatic() && 
		   getRedLine() == appli.getUIConfiguration().isRedlineOn() &&
		   getDragTime() == appli.getOptions().getDragTime() && 
		   getLockDAVMinutes() == appli.getOptions().getLockDAVMinutes() && 
		   (getTaskNamePrefix()==null || getTaskNamePrefix().equals(appli.getOptions().getTrueTaskNamePrefix())))
		{
			bHasChange = false;
		}
		else 
		{
			bHasChange = true;
			if(!askForApply || (askForApply && askForApplyChanges()))
			{			
				appli.getOptions().setAutomatic(getAutomatic());
				appli.getUIConfiguration().setRedlineOn(getRedLine());				
				appli.getOptions().setDragTime(getDragTime());
				appli.getOptions().setTaskNamePrefix(getTaskNamePrefix());
				
//				WebDAV Locking
				appli.getOptions().setLockDAVMinutes(getLockDAVMinutes());
				HttpDocument.setLockDAVMinutes(getLockDAVMinutes());				
			}
		}
		
		return bHasChange;
	}
	
	/** Initialize the component. */
	public void initialize()
	{
		cbAutomatic.setSelected(appli.getOptions().getAutomatic());
		cbRedLine.setSelected(appli.getUIConfiguration().isRedlineOn());
		cbDrag.setSelected(appli.getOptions().getDragTime());
		tfTaskPrefix.setText(appli.getOptions().getTaskNamePrefix());
		spLockDAV.setValue(new Integer(appli.getOptions().getLockDAVMinutes()));
	}
	
	/**@return the automatic launch value.*/
	public boolean getAutomatic() {
		return cbAutomatic.isSelected();
	}
	
	/**@return the drag time value.*/
	public boolean getDragTime() {
		return cbDrag.isSelected();
	}
	
	/**@return the red line value. */
	public boolean getRedLine() {
		return cbRedLine.isSelected();
	}
	
	/** @return the web dav locking value.*/
	public int getLockDAVMinutes() {
		return ((Integer) spLockDAV.getValue()).intValue();
	}
	
	/** @return the prefix task name. */
	public String getTaskNamePrefix(){
		String res = tfTaskPrefix.getText();
		if(res.equals(language.getText("newTask")))
			return null;
		return res;
	}
}
