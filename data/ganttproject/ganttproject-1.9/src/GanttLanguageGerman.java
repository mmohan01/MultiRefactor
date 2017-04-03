/***************************************************************************
                           GanttLanguageGerman.java  -  description
                             -------------------
    begin                : jan 2003
    copyright            : (C) 2003 by Thomas Alexandre
    email                : alex.thomas@netcourrier.com
	help by              : Uwe Nathanael  Uwe.Nathanael@t-online.de
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/


import java.lang.String;

/**
  * English class Language
  */
public class GanttLanguageGerman extends GanttLanguage
{

	public String getLanguage() { return "Deutsch"; }

	public String appliTitle() { return "GanttProject"; }

	public String getMonth (int m)
	{
		switch(m)
		{
			case 0: return "Januar";
			case 1: return "Februar";
			case 2: return "März";
			case 3: return "April";
			case 4: return "Mai";
			case 5: return "Juni";
			case 6: return "Juli";
			case 7: return "August";
			case 8: return "September";
			case 9: return "Oktober";
			case 10:return "November";
		}
		return "Dezember";
}

	public String getDay (int d)
	{
		switch(d)
		{
			case 1: return "Montag";
			case 2: return "Dienstag";
			case 3: return "Mittwoch";
			case 4: return "Donnerstag";
			case 5: return "Freitag";
			case 6: return "Samstag";
		}
		return "Sonntag";
	}

	public String week () { return "Woche "; }

	public String getOk() { return "Ok"; }

	public String getCancel() { return "Abbruch"; }

	public String getYes() { return "Ja"; }
	
	public String getNo() { return "Nein"; }

	public String error() { return "Fehler"; }

	public String warning() { return "Warnung"; }

//////////////////////////////////////////////////////////////////////////

	public String project () { return "Projekt"; }

	public String newProject () { return "Neu"; }

	public String openProject () { return "Öffnen"; }

	public String saveProject () { return "Sichern"; }

	public String saveAsProject () { return "Sichern als"; }
	
	public String export () { return "Export..."; }

	public String printProject () { return "Druck"; }

	public String ProjectProperties () { return "Projekt Eigenschaften"; }

	public String quit () { return "Ende"; }

//////////////////////////////////////////////////////////////////////////

	public String task () { return "Aufgabe"; }

	public String createTask () { return "Neue Aufgabe"; }

	public String deleteTask () { return "Löschen"; }

	public String propertiesTask () { return "Eigenschaften"; }

	public String notesTask () { return "Notizen"; }

	public String upTask () { return "Hoch"; }

	public String downTask () { return "Runter"; }

//////////////////////////////////////////////////////////////////////////
	
	public String human () { return "Person"; }
	
	public String newHuman () { return "Neue Person"; }

	public String deleteHuman () { return "Lösche Person"; }

	public String propertiesHuman () { return "Eigenschaften Person"; }

//////////////////////////////////////////////////////////////////////////

	public String language () { return "Sprache"; }

//////////////////////////////////////////////////////////////////////////

	public String help () { return "Hilfe"; }

	public String manual () { return "Handbuch"; }

	public String webPage () { return "Web Seite"; }

	public String javadoc () { return "Javadoc"; }

	public String about () { return "Über"; }

//////////////////////////////////////////////////////////////////////////

	public String backDate () { return "Vorwärts"; }

	public String forwardDate () { return "Rückwärts"; }

	public String zoomIn () { return "Vergrößern"; }

	public String zoomOut () { return "Verkleinern"; }

//////////////////////////////////////////////////////////////////////////

	public String propertiesFor () { return "Einstellungen für "; }

	public String newTask () { return "Neue Aufgabe"; }

	public String notesFor () { return "Notiz für "; }

	public String chooseDate () { return "Wähle ein Datum"; }

	public String name () { return "Name"; }

	public String motherTask () { return "Übergeordnete Aufgabe"; }

	public String none () { return "Nichts"; }

	public String dateOfBegining () { return "Startdatum"; }

	public String length () { return "Dauer"; }

	public String meetingPoint () { return "Meilenstein"; }

	public String depends () { return "Abhängigkeit"; }

	public String advancement () { return "Fortschritt"; }

	public String putDate () { return "Gib Datum und Zeit an"; }

	public String propertiesMsg (String taskName) { return "Eine Aufgabe benutzt den Namen ("+taskName+")"; }

//////////////////////////////////////////////////////////////////////////

	public String [] getPersonFunction()
	{
		String [] res = {
			"Nicht definiert",
			"Projekt Manager",
			"Entwickler",
			"Doc Writer",	 
			"Tester",
			"Graphic Designer",
			"Doc Translator",
			"Packager (.rpm, .tgz ...)",
			"Analysis",
			"Web Designer",
			"keine bestimmte Aufgabe"
		};
		return res;
	}
	
	public String [] getColsName()
	{
		String[] columnNames = {"Name", "Funktion", "Kontakt" };
		return columnNames;
	}

//////////////////////////////////////////////////////////////////////////
	
	public String chef() { return "Chef"; }

	public String organization() { return "Organisation"; }
	
	public String shortDescription() { return "Beschreibung"; }

//////////////////////////////////////////////////////////////////////////

	public String msg1() { return "Soll das Projekt vorher gesichert werden?"; }

	public String msg2() { return "Datei kann nicht geöffnet werden "; }

	public String msg3() { return "Aufgabe wirklich löschen "; }

	public String msg4() { return "Netscape kann nicht gestartet werden.\n Starte den Browser mit http://ganttproject.sourceforge.net"; }

	public String msg5() { return "Netscape kann nicht gestartet werden.\n Starte den Browser mit ../doc/index.html"; }
	
	public String msg6() { return "Person wirklich löschen? "; }
	
}




