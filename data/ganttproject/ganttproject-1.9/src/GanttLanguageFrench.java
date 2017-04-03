/***************************************************************************
                           GanttLanguageFrench.java  -  description
                             -------------------
    begin                : jan 2003
    copyright            : (C) 2003 by Thomas Alexandre
    email                : alex.thomas@netcourrier.com
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
  * French class Language
  */
public class GanttLanguageFrench extends GanttLanguage
{

	public String getLanguage() { return "Français"; }

	public String appliTitle() { return "GanttProject"; }

	public String getMonth (int m)
	{
		switch(m)
		{
			case 0: return "Janvier";
			case 1: return "Fevrier";
			case 2: return "Mars";
			case 3: return "Avril";
			case 4: return "Mai";
			case 5: return "Juin";
			case 6: return "Juillet";
			case 7: return "Aout";
			case 8: return "Septembre";
			case 9: return "Octobre";
			case 10:return "Novembre";
		}
		return "Decembre";
	}

	public String getDay (int d)
	{
		switch(d)
		{
			case 1: return "Lundi";
			case 2: return "Mardi";
			case 3: return "Mercredi";
			case 4: return "Jeudi";
			case 5: return "Vendredi";
			case 6: return "Samedi";
		}
		return "Dimanche";
	}

	public String week () { return "Semaine "; }

	public String getOk() { return "Ok"; }

	public String getCancel() { return "Annuler"; }
	
	public String getYes() { return "Oui"; }
	
	public String getNo() { return "Non"; }

	public String error() { return "Erreur"; }

	public String warning() { return "Attention"; }

//////////////////////////////////////////////////////////////////////////

	public String project () { return "Projet"; }

	public String newProject () { return "Nouveau"; }

	public String openProject () { return "Ouvrir"; }

	public String saveProject () { return "Sauver"; }

	public String saveAsProject () { return "Sauver sous"; }
	
	public String export () { return "Exporter..."; }

	public String printProject () { return "Imprimer"; }
		
	public String ProjectProperties () { return "Paramètres du project"; }

	public String quit () { return "Quitter"; }

//////////////////////////////////////////////////////////////////////////

	public String task () { return "Tache"; }

	public String createTask () { return "Nouvelle"; }

	public String deleteTask () { return "Supprimer"; }

	public String propertiesTask () { return "Proprietes"; }

	public String notesTask () { return "Notes"; }

	public String upTask () { return "Monter"; }

	public String downTask () { return "Descendre"; }
	
//////////////////////////////////////////////////////////////////////////

	public String human () { return "Acteur"; }
	
	public String newHuman () { return "Nouvel Acteur"; }

	public String deleteHuman () { return "Supprimer Acteur"; }

	public String propertiesHuman () { return "Proprietes Acteur"; }

//////////////////////////////////////////////////////////////////////////

	public String language () { return "Langue"; }

//////////////////////////////////////////////////////////////////////////

	public String help () { return "Aide"; }

	public String manual () { return "Manuel"; }

	public String webPage () { return "Page Web"; }

	public String javadoc () { return "Javadoc"; }

	public String about () { return "A propos"; }

//////////////////////////////////////////////////////////////////////////

	public String backDate () { return "Reculer dans le temps"; }

	public String forwardDate () { return "Avancer dans le temps"; }

	public String zoomIn () { return "Zoomer"; }

	public String zoomOut () { return "Dezoomer"; }

//////////////////////////////////////////////////////////////////////////

	public String propertiesFor () { return "Proprietes pour "; }

	public String newTask () { return "Nouvelle tache"; }

	public String notesFor () { return "Notes pour "; }

	public String chooseDate () { return "Choix d'une date"; }

	public String name () { return "Nom"; }

	public String motherTask () { return "Tache mère"; }

	public String none () { return "None"; }

	public String dateOfBegining () { return "Date de début"; }

	public String length () { return "Durée"; }

	public String meetingPoint () { return "Point bilan"; }

	public String depends () { return "Dépendances"; }

	public String advancement () { return "Avancement"; }

	public String putDate () { return "Ajouter la date du jour"; }

	public String propertiesMsg (String taskName) { return "Il existe deja une tache portant le même nom ("+taskName+")"; }
	

//////////////////////////////////////////////////////////////////////////

	public String [] getPersonFunction()
	{
		String [] res = {
			"Non defini",
			"Chef de projet",
			"Developpeur",
			"Editeur de doc",
			"Testeur",
			"Graphiste",
			"Traducteur",
			"Packageur",
			"Analyste",
			"Web Designer",
			"Pas de role specifique"
		};
		return res;
	}
	
	public String [] getColsName()
	{
		String[] columnNames = {"Nom", "Fonction", "Contact" };
		return columnNames;
	}
	
//////////////////////////////////////////////////////////////////////////
	
	public String chef() { return "Chef"; }

	public String organization() { return "Oragnisation"; }
	
	public String shortDescription() { return "Description"; }

//////////////////////////////////////////////////////////////////////////

	public String msg1() { return "Voulez-vous sauvegarder le projet avant??"; }

	public String msg2() { return "Impossible d'ouvrir le fichier "; }

	public String msg3() { return "Etes-vous sur de vouloir supprimer la tache "; }

	public String msg4() { return "Impossible de lancer la commande netscape.\nLancer votre naviguateur à la page http://ganttproject.sourceforge.net "; }

	public String msg5() { return "Impossible de lancer la commande netscape.\nLancer votre naviguateur à la page ../doc/index.html"; }
	
	public String msg6() { return "Etes-vous sur de vouloir supprimer l'acteur "; }
	
}


