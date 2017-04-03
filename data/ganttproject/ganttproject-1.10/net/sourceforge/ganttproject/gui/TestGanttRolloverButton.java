package net.sourceforge.ganttproject.gui;

/**
 * This class is from jedit.org
  * RolloverButton.java - Class for buttons that implement rollovers
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2002 Kris Kopicki
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import net.sourceforge.ganttproject.action.RolloverAction;

/**
  * Special button for tests on TaskPropertiesBeans
  */
public class TestGanttRolloverButton
    extends JButton {
	
	protected Icon _iconOn = null;
	protected Icon _iconOff = null;
	
	public TestGanttRolloverButton(Action action) {		
		this();
		setAction(action);
		Icon smallIcon = (Icon) action.getValue(Action.SMALL_ICON);
		if (smallIcon!=null) {
			setIcon(smallIcon);
            _iconOff = smallIcon;
		}
        if (action instanceof RolloverAction) {
        	_iconOn = ((RolloverAction)action).getIconOnMouseOver();
        }
		
	}
	
  /**
   * Setup the border (invisible initially)
   */
  public TestGanttRolloverButton() {
    setBorder(new EtchedBorder());
    setBorderPainted(false);
    setMargin(new Insets(0, 0, 0, 0));

    setRequestFocusEnabled(false);

    addMouseListener(new MouseOverHandler());
    setHorizontalTextPosition(SwingConstants.CENTER);
    setVerticalTextPosition(SwingConstants.BOTTOM);
  }

  /**
   * Setup the border (invisible initially)
   */
  public TestGanttRolloverButton(Icon icon) {
    this();
    setIcon(icon);
    _iconOn = icon;
  }
  
  /**
   * Setup the border (invisible initially)
   */
  public TestGanttRolloverButton(Icon iconOn, Icon iconOff) {
    this();
    setIcon(iconOff);
    _iconOn = iconOn;
    _iconOff = iconOff;
  }
  
  /**
   * Setup the border (invisible initially)
   */
  public TestGanttRolloverButton(Icon icon, String text) {
    this();
    setIcon(icon);
    _iconOn = icon;
    setText(text);    
  }

  
  public void setIcon(Icon iconOn, Icon iconOff)
  {
  	setIcon(iconOff);
    _iconOn = iconOn;
    _iconOff = iconOff;
  }
  
  public void setDefaultIcon(Icon iconOn)
  {
  	setIcon(iconOn);
    _iconOn = iconOn;
  }
  
  public boolean isOpaque() {
    return false;
  }

  public void setEnabled(boolean b) {
    super.setEnabled(b);
    setBorderPainted(false);
    repaint();
  }

  public void paint(Graphics g) {
    if (isEnabled()) {
      super.paint(g);
    }
    else {
      Graphics2D g2 = (Graphics2D) g;
      g2.setComposite(c);
      super.paint(g2);
    }
  }

  private static AlphaComposite c = AlphaComposite.getInstance(
      AlphaComposite.SRC_OVER, 0.5f);

  /**
   * Make the border visible/invisible on rollovers
   */
  class MouseOverHandler
      extends MouseAdapter {
    public void mouseEntered(MouseEvent e) {
      if (isEnabled()) {
        setBorderPainted(true);
        setIcon(_iconOn);
      }
    }

    public void mouseExited(MouseEvent e) {
      setBorderPainted(false);
      if(_iconOff!=null)
		setIcon(_iconOff);
    }
  }
}
