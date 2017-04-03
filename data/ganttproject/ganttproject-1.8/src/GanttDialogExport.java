
/**
  * @author THOMAS Alexandre
  */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.lang.Integer;
import javax.swing.tree.*;


/**
  * Dialog to edit the properties for export the calendar and print
  */
public class GanttDialogExport extends JDialog
{

	JCheckBox cbName, cbPercent, cbDepend;
	GanttStoreBoolean bool;

	/** Constructor */
	public GanttDialogExport(JFrame parent, GanttStoreBoolean sb, GanttLanguage language)
	{
		super(parent, language.propertiesFor()+language.export(),true);
	
		Box b1 = Box.createVerticalBox();
		
		
		bool = sb;
		
		
		cbName = new JCheckBox(language.name());
		cbName.setSelected(true);
		b1.add(cbName);
		
		cbPercent = new JCheckBox(language.advancement());
		cbPercent.setSelected(true);
		b1.add(cbPercent);
				
		cbDepend = new JCheckBox(language.depends());
		cbDepend.setSelected(true);				
		b1.add(cbDepend);
		
		
		getContentPane().add(b1,"Center");
		
		JPanel p = new JPanel();
		JButton ok = new JButton(language.getOk());
		getRootPane().setDefaultButton(ok);
		p.add(ok);
		
		final JDialog theDialog = this;
		//Listener on the ok bouton 
		ok.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{
					bool.name = cbName.isSelected();
					bool.percent = cbPercent.isSelected();
					bool.depend = cbDepend.isSelected();
					bool.ok=true;
					setVisible (false); 					
				}
			});
			
		
		//cancel button
		JButton cancel = new JButton(language.getCancel());
		p.add(cancel);
		cancel.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{ 
					bool.ok=false;
					setVisible (false); 
				}
			});

		getContentPane().add(p,BorderLayout.SOUTH);

		pack();	
		
		setResizable(false);
		Point point = parent.getLocationOnScreen();
		int x = (int)point.getX() + parent.getWidth()/2;
		int y = (int)point.getY() + parent.getHeight()/2;
		setLocation(x-getWidth()/2, y-getHeight()/2);	
	}


	
}

/** Class to store 3 boolean values */
	class GanttStoreBoolean {
	  public boolean name, percent, depend,ok;
	  public GanttStoreBoolean()
	  {
	  	name = percent = depend = ok = true;
	  }
	}
