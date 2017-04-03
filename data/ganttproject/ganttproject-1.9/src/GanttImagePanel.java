/***************************************************************************
                           GanttImagePanel.java  -  description
                             -------------------
    begin                : dec 2002
    copyright            : (C) 2002 by Thomas Alexandre
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

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
  * Class to create a panel with the image above the Tree
  */
public class GanttImagePanel extends JPanel 
{

	/** Constructor of the panel. */
	public GanttImagePanel()
	{
		Box box = Box.createVerticalBox();
		JLabel label = new JLabel(new ImageIcon("icons/big.gif"));
		this.setBackground(Color.white);
		box.add(label);
		add(box,"Center");
	}

	/** The prefered size of this panel */
	public Dimension getPreferredSize()
	{
		return new Dimension(300, 53);
	}
	
}


