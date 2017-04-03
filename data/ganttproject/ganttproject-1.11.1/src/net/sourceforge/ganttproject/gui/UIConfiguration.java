/*
LICENSE:
                                                                 
   This program is free software; you can redistribute it and/or modify  
   it under the terms of the GNU General Public License as published by  
   the Free Software Foundation; either version 2 of the License, or     
   (at your option) any later version.                                   
                                                                         
   Copyright (C) 2004, GanttProject Development Team
 */
package net.sourceforge.ganttproject.gui;

import java.awt.Color;
import java.awt.Font;

/**
 * Created by IntelliJ IDEA.
 * User: bard
 */
public class UIConfiguration {
    private final Font myMenuFont;
    private final Font myChartMainFont;
    private Color myTaskColor;
	private Color myResColor; //default resource color
    private Color myResOverColor; //overload resoure color
    private boolean isRedlineOn;

    public UIConfiguration(Font menuFont, Font chartMainFont, Color taskColor, boolean isRedlineOn) {
        myMenuFont = menuFont==null ? new Font("Dialog", Font.PLAIN, 12) : menuFont;
        myChartMainFont = chartMainFont==null ? new Font("SansSerif", Font.PLAIN, 9) : chartMainFont;
        this.isRedlineOn = isRedlineOn;
        setTaskColor(taskColor);
		myResColor = new Color(140, 182, 206);
        myResOverColor = new Color(229,50,50);
    }


    public Font getMenuFont(){
        return myMenuFont;
    }

    public Font getChartMainFont() {
        return myChartMainFont;
    }

    public Color getTaskColor() {
        return myTaskColor;
    }

    public void setTaskColor(Color myTaskColor) {
        this.myTaskColor = myTaskColor;
    }
    
    public Color getResourceColor() {
        return myResColor;
    }

    public void setResourceColor(Color myResColor) {
        this.myResColor = myResColor;
    }
    
    
    public Color getResourceOverloadColor() {
        return myResOverColor;
    }

    public void setResourceOverloadColor(Color myResOverColor) {
        this.myResOverColor = myResOverColor;		
    }

    public boolean isRedlineOn() {
        return isRedlineOn;
    }

    public void setRedlineOn(boolean redlineOn) {
        isRedlineOn = redlineOn;
    }

}
