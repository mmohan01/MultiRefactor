
/**
  * @author THOMAS Alexandre
  * @author AUDRU Cedric
  */

import java.lang.String;

/**
  * English class Language
  */
public class GanttLanguageEnglish extends GanttLanguage
{

	public String getLanguage() { return "English"; }

	public String appliTitle() { return "GanttProject"; }

	public String getMonth (int m)
	{
		switch(m)
		{
			case 0: return "January";
			case 1: return "February";
			case 2: return "March";
			case 3: return "April";
			case 4: return "May";
			case 5: return "June";
			case 6: return "Jully";
			case 7: return "August";
			case 8: return "September";
			case 9: return "October";
			case 10:return "November";
		}
		return "December";
	}

	public String getDay (int d)
	{
		switch(d)
		{
			case 1: return "Monday";
			case 2: return "Tuesday";
			case 3: return "Wednesday";
			case 4: return "Thursday";
			case 5: return "Friday";
			case 6: return "Saturday";
		}
		return "Sunday";
	}

	public String week () { return "Week "; }

	public String getOk() { return "Ok"; }

	public String getCancel() { return "Cancel"; }

	public String error() { return "Error"; }

	public String warning() { return "Warning"; }

//////////////////////////////////////////////////////////////////////////

	public String project () { return "Project"; }

	public String newProject () { return "New"; }

	public String openProject () { return "Open"; }

	public String saveProject () { return "Save"; }

	public String saveAsProject () { return "Save as"; }
	
	public String export () { return "Export..."; }

	public String printProject () { return "Print"; }

	public String ProjectProperties () { return "Project settings"; }

	public String quit () { return "Quit"; }

//////////////////////////////////////////////////////////////////////////

	public String task () { return "Task"; }

	public String createTask () { return "New Task"; }

	public String deleteTask () { return "Delete"; }

	public String propertiesTask () { return "Properties"; }

	public String notesTask () { return "Notes"; }

	public String upTask () { return "Up"; }

	public String downTask () { return "Down"; }

//////////////////////////////////////////////////////////////////////////
	
	public String human () { return "Human"; }
	
	public String newHuman () { return "New Human"; }

	public String deleteHuman () { return "Delete Human"; }

	public String propertiesHuman () { return "Human Settings"; }

//////////////////////////////////////////////////////////////////////////

	public String language () { return "Language"; }

//////////////////////////////////////////////////////////////////////////

	public String help () { return "Help"; }

	public String manual () { return "Manual"; }

	public String webPage () { return "Web Page"; }

	public String javadoc () { return "Javadoc"; }

	public String about () { return "About"; }

//////////////////////////////////////////////////////////////////////////

	public String backDate () { return "Previous"; }

	public String forwardDate () { return "Next"; }

	public String zoomIn () { return "Zoom in"; }

	public String zoomOut () { return "Zoom out"; }

//////////////////////////////////////////////////////////////////////////

	public String propertiesFor () { return "Properties for "; }

	public String newTask () { return "New task"; }

	public String notesFor () { return "Notes for "; }

	public String chooseDate () { return "Choose a date"; }

	public String name () { return "Name"; }

	public String motherTask () { return "Mother task"; }

	public String none () { return "None"; }

	public String dateOfBegining () { return "Date of start"; }

	public String length () { return "Duration"; }

	public String meetingPoint () { return "Meeting point"; }

	public String depends () { return "Depends"; }

	public String advancement () { return "Advancement"; }

	public String putDate () { return "Put the date and the time"; }

	public String propertiesMsg (String taskName) { return "A task has already this name ("+taskName+")"; }

//////////////////////////////////////////////////////////////////////////

	public String [] getPersonFunction()
	{
		String [] res = {
			"Undefined",
			"Project Manager",
			"Developer",
			"Doc Writer",	 
			"Tester",
			"Graphic Designer",
			"Doc Translator",
			"Packager (.rpm, .tgz ...)",
			"Analysis",
			"Web Designer",
			"No Specific Role"
		};
		return res;
	}
	
	public String [] getColsName()
	{
		String[] columnNames = {"Name", "Function", "Contact" };
		return columnNames;
	}

//////////////////////////////////////////////////////////////////////////
	
	public String chef() { return "Chef"; }

	public String organization() { return "Organization"; }
	
	public String shortDescription() { return "Description"; }

//////////////////////////////////////////////////////////////////////////

	public String msg1() { return "Do-you want to save the project before??"; }

	public String msg2() { return "Unable to open the file "; }

	public String msg3() { return "Really delete the task "; }

	public String msg4() { return "Can't run the netscape command.\n Run your browser at http://ganttproject.sourceforge.net"; }

	public String msg5() { return "Can't run the netscape command.\n Run your browser at ../doc/index.html"; }
	
	public String msg6() { return "Really delete the human "; }
}



