/***************************************************************************
LanguageSettingsPanel 
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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.language.GanttLanguage;

/**
 * @author athomas
 *
 * Panel to choose the language for GanttProject
 */
public class LanguageSettingsPanel 
	extends GeneralOptionPanel 
	implements ItemListener{

	// combo box to store all languages data
	private JComboBox cbLanguage;    
	
	private GanttProject appli;
	
	/** Constructor. */
	public LanguageSettingsPanel(GanttProject parent)
	{
		super(GanttLanguage.getInstance().getText("languages"),
			  GanttLanguage.getInstance().getText("settingsLanguages"),parent);
        
		appli = parent;
		
		//create the combo box with all languages
		cbLanguage = new JComboBox();
		cbLanguage.addItem("Traditional Chinese");
		cbLanguage.addItem("Simplified Chinese");
		cbLanguage.addItem("\u010cesky");
		cbLanguage.addItem("Dansk");
		cbLanguage.addItem("Deutsch");
		cbLanguage.addItem("English");
		cbLanguage.addItem("Espa\u00f1ol");
		cbLanguage.addItem("Estonian");
		cbLanguage.addItem("Fran\u00e7ais");
		cbLanguage.addItem("Hungarian");
		cbLanguage.addItem("\u05e2\u05d1\u05e8\u05d9\u05ea");
		cbLanguage.addItem("Italiano");
		cbLanguage.addItem("Japanese");
		cbLanguage.addItem("Nederlands");
		cbLanguage.addItem("Norsk");
		cbLanguage.addItem("Polski");
		cbLanguage.addItem("Portugues");
		cbLanguage.addItem("Portugu\u00eas do Brasil");
		cbLanguage.addItem("\u0420\u0443\u0441\u0441\u043a\u0438\u0439");
		cbLanguage.addItem("Svenska");
		cbLanguage.addItem("T\u00FCrk\u00E7e");
		
		cbLanguage.addItemListener(this);
		
		JPanel languagePanel = new JPanel(new BorderLayout());
		languagePanel.add(cbLanguage,BorderLayout.NORTH);
		vb.add(languagePanel);
		
		applyComponentOrientation(language.getComponentOrientation());
	}
	
	/** This method check if the value has changed, and assk for commit changes. */
	public boolean applyChanges(boolean askForApply)
	{
		// if there is changes
		if(bHasChange){
			if(!askForApply || (askForApply && askForApplyChanges())) 		
				changeLanguage();
		}
		return bHasChange;
	}
	
	/** Initialize the component. */
	public void initialize()
	{
		cbLanguage.setSelectedItem(language.getText("longLanguage"));
		bHasChange = false;
	}
	
	/** The language has changed. */
	public void itemStateChanged(ItemEvent e)
	{
		if(e.getStateChange()==ItemEvent.SELECTED) 
			this.bHasChange = true;
	}
	
	/**Appli the new language*/
	public void changeLanguage(){
		String lang=(String)cbLanguage.getSelectedItem();
		
		if(lang.equals("Traditional Chinese"))  		language.setLocale(Locale.TAIWAN);
		else if(lang.equals("Simplified Chinese"))		language.setLocale(Locale.CHINA);
		else if(lang.equals("\u010cesky"))  	language.setLocale(new Locale("cz", "CZ"));
		else if(lang.equals("Dansk")) 			language.setLocale(new Locale("da", "DK"));
		else if(lang.equals("Deutsch"))  		language.setLocale(Locale.GERMANY);
		else if(lang.equals("English"))  		language.setLocale(Locale.US);
		else if(lang.equals("Espa\u00f1ol"))  	language.setLocale(new Locale("es", "ES"));
		else if(lang.equals("Estonian"))  		language.setLocale(new Locale("et", "ET"));
		else if(lang.equals("Fran\u00e7ais"))  	language.setLocale(Locale.FRANCE);
		else if(lang.equals("Hungarian"))  		language.setLocale(new Locale("hu", "HU"));
		else if(lang.equals("\u05e2\u05d1\u05e8\u05d9\u05ea"))  language.setLocale(new Locale("iw", "iW"));
		else if(lang.equals("Italiano"))  		language.setLocale(Locale.ITALY);
		else if(lang.equals("Japanese"))  		language.setLocale(new Locale("ja", "JP"));
		else if(lang.equals("Nederlands"))  	language.setLocale(new Locale("nl", "NL"));
		else if(lang.equals("Norsk"))  			language.setLocale(new Locale("no", "NO"));
		else if(lang.equals("Polski"))  		language.setLocale(new Locale("pl", "PL"));
		else if(lang.equals("Portugues"))  		language.setLocale(new Locale("pt", "PT"));
		else if(lang.equals("Portugu\u00eas do Brasil"))  language.setLocale(new Locale("pt", "BR"));
		else if(lang.equals("Svenska"))  		language.setLocale(new Locale("sv", "SV"));
		else if(lang.equals("T\u00FCrk\u00E7e"))  language.setLocale(new Locale("tr", "TR"));
		else if(lang.equals("\u0420\u0443\u0441\u0441\u043a\u0438\u0439"))  language.setLocale(new Locale("ru", "RU"));
		appli.changeLanguage();
	}
}
