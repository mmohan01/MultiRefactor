/***************************************************************************
               GeneralOptionPanel.java 
------------------------------------------
begin                : 24 juin 2004
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
import java.awt.Component;
import java.awt.Frame;

import javax.swing.Box;
import javax.swing.JPanel;

import net.sourceforge.ganttproject.gui.GanttDialogInfo;
import net.sourceforge.ganttproject.language.GanttLanguage;

/**
 * @author athomas
 *
 * Abstract class for the Options panels
 */
public abstract class GeneralOptionPanel extends JPanel {

	// -- ATTRIBUTES --
	
	protected GanttLanguage language = GanttLanguage.getInstance();
	
	/** General vertical box. */
	protected Box vb = Box.createVerticalBox();
	
	/** Tell if the parameters of the panel have change. */
	protected boolean bHasChange = false;
	
	/** Ganttproject object. */
	private Frame appli;

	private String myTitle;

	private String myComment;
	
	
	// -- METHODS --
	
	/** Constructor with the title, the comment and the GanttProject object. */
	public GeneralOptionPanel(String title, String comment, Frame parent)
	{
		super();
		appli = parent;
		setLayout(new BorderLayout());
		add(vb,BorderLayout.CENTER);
		myTitle = title;
		myComment = comment;
		
	}
	
	public Component getComponent() {
		return this;
	}
	/** This method check if the value has changed, and assk for commit changes. */
	public abstract boolean applyChanges(boolean askForApply);
	
	/** Initialize the component. */
	public abstract void initialize();
	
	/** This method ask for saving the changes. */
	public boolean askForApplyChanges()
	{
		GanttDialogInfo gdi = new GanttDialogInfo(
				appli, GanttDialogInfo.QUESTION,
				GanttDialogInfo.YES_NO_OPTION, language
						.getText("msg20"),
						language.getText("question"));
		gdi.show();
		return (gdi.res == GanttDialogInfo.YES);
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return myTitle;
	}

	/**
	 * @return
	 */
	public String getComment() {
		return myComment;
	}
		
}
