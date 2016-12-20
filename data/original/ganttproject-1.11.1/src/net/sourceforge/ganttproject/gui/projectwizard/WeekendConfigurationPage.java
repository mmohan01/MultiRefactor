/*
 * Created on 06.01.2005
 */
package net.sourceforge.ganttproject.gui.projectwizard;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Calendar;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import net.sourceforge.ganttproject.calendar.GPCalendar;

/**
 * @author bard
 */
public class WeekendConfigurationPage implements WizardPage {
	private final Box myPanel = Box.createVerticalBox();
	private final GPCalendar myCalendar;
    private final I18N myI18n;
	
	public WeekendConfigurationPage(GPCalendar calendar, I18N i18n) {
        myI18n = i18n;
		myCalendar = calendar;
		String[] dayNames = i18n.getDayNames();
		int nextDay = Calendar.MONDAY;
		for (int i=0; i<7; i++) {
			JCheckBox nextCheckBox = new JCheckBox();
			nextCheckBox.setSelected(calendar.getWeekDayType(nextDay)==GPCalendar.DayType.HOLIDAY);
			nextCheckBox.setAction(new CheckBoxAction(nextDay, dayNames[nextDay-1], nextCheckBox.getModel()));			
			myPanel.add(nextCheckBox);
			if (++nextDay>=8) {
				nextDay = 1;
			}
		}
	}
	public String getTitle() {
		return myI18n.getWeekendPageTitle();
	}

	public Component getComponent() {
		return myPanel;
	}

	public void setActive(boolean b) {
	}

	private class CheckBoxAction extends AbstractAction {
		private int myDay;
		private ButtonModel myModel;
		CheckBoxAction(int day, String dayName, ButtonModel model) {
			super(dayName);
			myDay = day;
			myModel = model;
		}
		public void actionPerformed(ActionEvent e) {
			WeekendConfigurationPage.this.myCalendar.setWeekDayType(myDay, myModel.isSelected() ? GPCalendar.DayType.HOLIDAY : GPCalendar.DayType.WORKING);
		}
		
	}
}
