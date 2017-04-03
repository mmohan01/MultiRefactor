/***************************************************************************
               HTMLSettingsPanel.java 
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
import java.io.File;
import java.security.AccessControlException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.gui.TestGanttRolloverButton;
import net.sourceforge.ganttproject.language.GanttLanguage;

/**
 * @author athomas
 *
 * Choose the xsl directory for the html export
 */
public class HTMLSettingsPanel extends GeneralOptionPanel implements ActionListener{

	JButton buttonXsl;
	JTextField tfXsl;
	JCheckBox cbDefault;
    
	private GanttProject appli;
	
	/** Constructor. */
	public HTMLSettingsPanel(GanttProject parent)
	{
		super(GanttProject.correctLabel(GanttLanguage.getInstance().getText("htmlexport")),
				  GanttLanguage.getInstance().getText("settingsHTMLExport"),parent);

		appli = parent;
		
		JPanel htmlTheme = new JPanel(new BorderLayout());
		buttonXsl = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/open_16.gif")));
		tfXsl = new JTextField();
		tfXsl.setColumns(40);
		tfXsl.setEditable(false);
		
		htmlTheme.add(tfXsl,BorderLayout.CENTER);
		htmlTheme.add(buttonXsl,BorderLayout.EAST);
		vb.add(htmlTheme);
		vb.add(new JPanel());
		
		buttonXsl.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(new File(tfXsl.getText()));
				fc.setDialogTitle(language.getText("selectThemeDirectory"));
				fc.setApproveButtonToolTipText(language.getText("selectThemeDirectory"));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = fc.showDialog(appli, language.getText("ok"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					tfXsl.setText(fc.getSelectedFile().toString());
					bHasChange=true;
				}
			}
		});
		
//		automatic launch of task properties
		JPanel defaultPanel = new JPanel(new BorderLayout());
		defaultPanel.add(cbDefault = new JCheckBox(), BorderLayout.WEST);
		defaultPanel.add(new JLabel(language.getText("xslDirectory")), BorderLayout.CENTER);
		vb.add(defaultPanel);
		vb.add(new JPanel());
		
		cbDefault.addActionListener(this);
		

		applyComponentOrientation(language.getComponentOrientation());
	}
	
	/** This method check if the value has changed, and assk for commit changes. */
	public boolean applyChanges(boolean askForApply)
	{
		if(bHasChange)
			if(!askForApply || (askForApply && askForApplyChanges())) 
				appli.getOptions().setXslDir(getXslDirectory(cbDefault.isSelected()));

		return bHasChange;
	}
	
	/** Initialize the component. */
	public void initialize()
	{
		try {
			//tfXsl.setText(GanttProject.xslDir!=null?GanttProject.xslDir:System.getProperty("user.home"));
			tfXsl.setText(appli.getOptions().getXslDir()!=null?
					appli.getOptions().getXslDir():System.getProperty("user.home"));
		} catch (AccessControlException e) {
			// This can happen when running in a sandbox (Java WebStart)
			System.err.println (e + ": " + e.getMessage());
		}
	}
	
	/** Return the xsl directory */
	public String getXslDirectory(boolean useDefault) {
		if(!useDefault)
			if(tfXsl.getText().length()>0 && new File(tfXsl.getText()).exists())
				return tfXsl.getText();
		
		return HTMLSettingsPanel.class.getResource("/xslt").toString();
	}
	
	/** Action callback. */
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==cbDefault)
		{
			bHasChange=true;
		}
	}
}
