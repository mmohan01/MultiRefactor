
/**
  * @author THOMAS Alexandre
  * @author AUDRU Cedric
  */

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


