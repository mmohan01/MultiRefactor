/***************************************************************************
                           GanttPeople.java  -  description
                             -------------------
    begin                : feb 2002
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
import java.lang.Object;
import java.util.ArrayList;
import java.io.*;

/** Class Person 
  */
public class GanttPeople implements Serializable
{
	/** The name of the person */
	private String name;
	
	/** The mail of the person */
	private String mail;
	
	/** The function reference of this person */
	int function;
	
	
	/** Default constructor */
	public GanttPeople() 
	{ 
		name = new String();
		mail = new String();
		function =0;
	}
	
	/** A util constructor */
	public GanttPeople(String name, String mail, int function) 
	{
		this.name = name;
		this.mail = mail;				
		this.function = function;
	}
	
	
	/** Return the name of ther person */
	public String getName() { return name; }
	
	/** Change the name of the person */
	public void setName( String n ) { name = n; }

	/** Return the mail of ther person */
	public String getMail() { return mail; }
	
	/** Change the name of the person */
	public void setMail( String m ) { mail = m; }
	
	/** Return the function of the person */
	public int getFunction () { return function; }
	
	/** Change the function of the person */
	public void setFunction (int f) { function = f; }
	
	/** Method to String */
	public String toString() { return name; }

}



