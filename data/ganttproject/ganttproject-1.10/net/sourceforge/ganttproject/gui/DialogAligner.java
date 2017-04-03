package net.sourceforge.ganttproject.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.JDialog;


public class DialogAligner {
    public static void center(JDialog dialog, Container parent) {
    	boolean alignToParent = false;
    	if (parent != null) {
    		alignToParent = parent.isVisible();
    	}
    	
    	if (alignToParent) {
			Point point =  parent.getLocationOnScreen();
			int x = (int)point.getX() + parent.getWidth()/2;
			int y = (int)point.getY() + parent.getHeight()/2;
			dialog.setLocation(x-dialog.getWidth()/2, y-dialog.getHeight()/2);
		} else {
		
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

			dialog.setLocation(screenSize.width/2 - (dialog.getWidth()/2),
	                    screenSize.height/2 - (dialog.getHeight()/2));
		}
    }
}
