/***************************************************************************
LnFSettingsPanel 
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
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.gui.GanttLookAndFeelInfo;
import net.sourceforge.ganttproject.gui.GanttLookAndFeels;
import net.sourceforge.ganttproject.language.GanttLanguage;

/**
 * @author athomas
 *
 * Panel to choose the look'n'feel for GanttProject
 */
public class LnFSettingsPanel 
	extends GeneralOptionPanel 
	implements ItemListener {

	// combo box to store all looknfeel data
	private JComboBox cbLnf;
	private JCheckBox cbSmallIcon;
	private JComboBox cbButtonType;
    private JCheckBox cbShowStatus;
	
    private GanttProject appli;
    
	/** Constructor. */
	public LnFSettingsPanel(GanttProject parent)
	{
		super(GanttLanguage.getInstance().getText("looknfeel"),
				GanttLanguage.getInstance().getText("settingsLooknFeel"),parent);

		appli = parent;
		
		//create the combo box with all languages
		cbLnf = new JComboBox();
		cbLnf.setName("comboLnf");
		//cbLnf.addActionListener(this);
		
		
		GanttLookAndFeelInfo[] lookAndFeels =
			GanttLookAndFeels.getGanttLookAndFeels().getInstalledLookAndFeels();
		for(int i = 0; i < lookAndFeels.length; i++) {
			cbLnf.addItem(lookAndFeels[i]);
		}
		
		cbLnf.addItemListener(this);
		
		JPanel languagePanel = new JPanel(new BorderLayout());
		languagePanel.add(cbLnf,BorderLayout.NORTH);
		vb.add(languagePanel);
		vb.add(new JPanel());
		
		
//		use small icons
		
		JPanel iconTextPanel = new JPanel(new FlowLayout());
		iconTextPanel.add(new JLabel(language.getText("show")));
		iconTextPanel.add(cbButtonType = new JComboBox());
		
		cbButtonType.addItem(language.getText("buttonIcon"));
		cbButtonType.addItem(language.getText("buttonIconText"));
		cbButtonType.addItem(language.getText("buttonText"));
		
		iconTextPanel.add(new JLabel("  "));
		iconTextPanel.add(cbSmallIcon = new JCheckBox());
		iconTextPanel.add(new JLabel(language.getText("useSmalIcons")));
		JPanel iconTextPanel2 = new JPanel(new BorderLayout());
		iconTextPanel2.add(iconTextPanel, BorderLayout.WEST);
		vb.add(iconTextPanel2);
		vb.add(new JPanel());
		
		//status bar setting
		JPanel statusPanel = new JPanel(new BorderLayout());
		statusPanel.add(cbShowStatus = new JCheckBox(), BorderLayout.WEST);
		statusPanel.add(new JLabel(language.getText("showStatusBar")), BorderLayout.CENTER);
		vb.add(statusPanel);
		vb.add(new JPanel());

		applyComponentOrientation(language.getComponentOrientation());
	}
	
	/** This method check if the value has changed, and assk for commit changes. */
	public boolean applyChanges(boolean askForApply)
	{
		if(cbSmallIcon.isSelected() != appli.getOptions().getIconSize().equals("16"))
			bHasChange = true;
		
		if(cbButtonType.getSelectedIndex() != appli.getOptions().getButtonShow())
			bHasChange = true;
		
		if(getShowStatusBar() != appli.getOptions().getShowStatusBar())
			bHasChange = true;

		// if there is changes
		if(bHasChange){
			if(!askForApply || (askForApply && askForApplyChanges())) {		
				appli.changeLookAndFeel(getLookAndFeel());
				appli.getOptions().setIconSize(cbSmallIcon.isSelected()?"16":"24");
				appli.getOptions().setButtonShow(cbButtonType.getSelectedIndex());
				appli.getOptions().setShowStatusBar(cbShowStatus.isSelected());
				appli.getStatusBar().setVisible(getShowStatusBar());
				appli.applyButtonOptions();
			}
		}
		return bHasChange;
	}

	/** The look'n'feel has changed. */
	public void itemStateChanged(ItemEvent e)
	{
		if(e.getStateChange()==ItemEvent.SELECTED) { 
			this.bHasChange = true;
		}
	}
	
	/** Return the class of the style */
	public GanttLookAndFeelInfo getLookAndFeel() 
	{
		return ((GanttLookAndFeelInfo) cbLnf.getSelectedItem());
	}
	
	boolean getShowStatusBar(){
		return cbShowStatus.isSelected();
	}

	/** Initialize the component. */
	public void initialize()
	{
		cbLnf.setSelectedItem(appli.lookAndFeel);
		cbSmallIcon.setSelected(appli.getOptions().getIconSize().equals("16"));
		cbButtonType.setSelectedIndex(appli.getOptions().getButtonShow());
		cbShowStatus.setSelected(appli.getOptions().getShowStatusBar());
		bHasChange = false;
	}
}
