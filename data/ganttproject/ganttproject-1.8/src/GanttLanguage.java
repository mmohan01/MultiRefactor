
/**
  * @author THOMAS Alexandre
  * @author AUDRU Cedric
  */

import java.lang.String;

/** 
  * Abstract class for the language
  */
public abstract class GanttLanguage
{

	abstract public String getLanguage();

	abstract public String appliTitle();

	abstract public String getMonth (int m);

	abstract public String getDay (int d);

	abstract public String week ();

	abstract public String getOk();

	abstract public String getCancel();

	abstract public String error();

	abstract public String warning();

//////////////////////////////////////////////////////////////////////////

	abstract public String project ();

	abstract public String newProject ();

	abstract public String openProject ();

	abstract public String saveProject ();

	abstract public String saveAsProject ();
	
	abstract public String export ();

	abstract public String printProject ();
	
	abstract public String ProjectProperties ();

	abstract public String quit ();

//////////////////////////////////////////////////////////////////////////

	abstract public String task ();

	abstract public String createTask ();

	abstract public String deleteTask ();

	abstract public String propertiesTask ();

	abstract public String notesTask ();

	abstract public String upTask ();

	abstract public String downTask ();

//////////////////////////////////////////////////////////////////////////
	
	abstract public String human ();
	
	abstract public String newHuman ();

	abstract public String deleteHuman ();

	abstract public String propertiesHuman ();


//////////////////////////////////////////////////////////////////////////

	abstract public String language ();

//////////////////////////////////////////////////////////////////////////

	abstract public String help ();

	abstract public String manual ();

	abstract public String webPage ();

	abstract public String javadoc ();

	abstract public String about ();

//////////////////////////////////////////////////////////////////////////

	abstract public String backDate ();

	abstract public String forwardDate ();

	abstract public String zoomIn ();

	abstract public String zoomOut ();

//////////////////////////////////////////////////////////////////////////

	abstract public String propertiesFor ();

	abstract public String newTask ();

	abstract public String notesFor ();

	abstract public String chooseDate ();

	abstract public String name ();

	abstract public String motherTask ();

	abstract public String none ();

	abstract public String dateOfBegining ();

	abstract public String length ();

	abstract public String meetingPoint ();

	abstract public String depends ();

	abstract public String advancement ();

	abstract public String putDate ();

	abstract public String propertiesMsg (String taskName);

//////////////////////////////////////////////////////////////////////////

	abstract public String [] getPersonFunction();
	
	abstract public String [] getColsName();
	
//////////////////////////////////////////////////////////////////////////
	
	abstract public String chef();

	abstract public String organization();
	
	abstract public String shortDescription();

//////////////////////////////////////////////////////////////////////////

	abstract public String msg1();

	abstract public String msg2();

	abstract public String msg3();

	abstract public String msg4();

	abstract public String msg5();
	
	abstract public String msg6();
}


