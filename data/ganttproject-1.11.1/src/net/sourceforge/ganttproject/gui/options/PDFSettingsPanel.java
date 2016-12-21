/***************************************************************************
               PDFSettingsPanel.java 
------------------------------------------
begin                : 28 juin 2004
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
import javax.swing.filechooser.FileFilter;

import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.filter.GanttXSLFileFilter;
import net.sourceforge.ganttproject.gui.TestGanttRolloverButton;
import net.sourceforge.ganttproject.language.GanttLanguage;

/**
 * @author athomas
 *
 * Panel for options of PDF export
 */
public class PDFSettingsPanel extends GeneralOptionPanel implements ActionListener{
	JButton buttonXslFo;
	JTextField tfXslFo;
	JCheckBox cbDefault;
	
	private GanttProject appli;
	
	/** Constructor. */
	public PDFSettingsPanel(GanttProject parent)
	{
		super(GanttProject.correctLabel(GanttLanguage.getInstance().getText("pdfexport")),
				  GanttLanguage.getInstance().getText("settingsPDFExport"),parent);
		
		appli = parent;
		
		JPanel pdfTheme = new JPanel(new BorderLayout());
		buttonXslFo = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/open_16.gif")));
		tfXslFo = new JTextField();
		tfXslFo.setColumns(40);
		tfXslFo.setEditable(false);
		
		pdfTheme.add(tfXslFo,BorderLayout.CENTER);
		pdfTheme.add(buttonXslFo,BorderLayout.EAST);
		vb.add(pdfTheme);
		vb.add(new JPanel());
		
		buttonXslFo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileFilter xslFilter = new GanttXSLFileFilter();
				JFileChooser fc = new JFileChooser(new File(tfXslFo.getText()));
				fc.setDialogTitle(language.getText("selectThemeFile"));
				fc.setApproveButtonToolTipText(language.getText("selectThemeFile"));
				fc.addChoosableFileFilter(xslFilter);
				//				Remove the possibility to use a file filter for all files
				FileFilter[] filefilters = fc.getChoosableFileFilters();
				for(int i=0;i<filefilters.length;i++){
				 	if(filefilters[i]!=xslFilter)
				 		System.out.println(fc.removeChoosableFileFilter(filefilters[i]));
				}

				int returnVal = fc.showDialog(appli, language.getText("ok"));
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					tfXslFo.setText(fc.getSelectedFile().toString());
					bHasChange=true;
				}
			}
		});
		
//		automatic launch of task properties
		JPanel defaultPanel = new JPanel(new BorderLayout());
		defaultPanel.add(cbDefault = new JCheckBox(), BorderLayout.WEST);
		defaultPanel.add(new JLabel(language.getText("xslFoFile")), BorderLayout.CENTER);
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
			{
				appli.getOptions().setXslFo(getXslFoFile(cbDefault.isSelected()));
			}
		return bHasChange;
	}
	
	/** Initialize the component. */
	public void initialize()
	{
		try {
			//tfXslFo.setText(GanttProject.xslFo!=null?GanttProject.xslFo:System.getProperty("user.home"));
			tfXslFo.setText(appli.getOptions().getXslFo() != null?
					appli.getOptions().getXslFo():System.getProperty("user.home"));
		} catch (AccessControlException e) {
			// This can happen when running in a sandbox (Java WebStart)
			System.err.println (e + ": " + e.getMessage());
		}	
	}
	
	/** @return the xmlfo file for pdf export. */
	public String getXslFoFile(boolean useDefault){
		if(!useDefault)
			if(tfXslFo.getText().length()>0 && new File(tfXslFo.getText()).exists())
				return tfXslFo.getText();
		
		return PDFSettingsPanel.class.getResource("/xslfo/ganttproject.xsl").toString();
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
