
/**
  * @author THOMAS Alexandre
  * @author AUDRU Cedric
  */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.lang.Integer;
import javax.swing.tree.*;


/**
  * Class to edit setting for the project
  */
public class GanttDialogSettings extends JDialog
{
	/** JTextField for the name of projet. */
	private JTextField jtfname;
	
	/** JTextArea for the description of the project */
	private JTextArea tadescript;

	
	/** JTextField for the organization */
	private JTextField jtorga;

	/** La langue utilisee. */
	GanttLanguage lang;
	
	/** The main class */
	GanttProject appli;

	/** The constructor*/
	public GanttDialogSettings(GanttProject parent,GanttLanguage language)
	{
		super(parent, language.ProjectProperties(),true);
		
		this.lang = language;
		this.appli = parent;
		
		Box b1 = Box.createVerticalBox();
		
		b1.add(new JLabel(language.name()));
		jtfname = new JTextField (appli.projectName);
		b1.add(jtfname);
		
		
		b1.add(new JLabel(language.shortDescription()));
		tadescript = new JTextArea(appli.decription);
		JScrollPane scrollPane = new JScrollPane(tadescript);
		scrollPane.setPreferredSize(new Dimension(200, 100));
		b1.add(scrollPane);
		
		
		b1.add(new JLabel(language.organization()));
		jtorga = new JTextField (appli.organization);
		b1.add(jtorga);
		

		
		getContentPane().add(b1,"Center");
		
		JPanel p = new JPanel();
		JButton ok = new JButton(language.getOk());
		getRootPane().setDefaultButton(ok);
		p.add(ok);
		
		
		//Listener sur le bouton ok
		ok.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{
					setVisible(false);	
					
					appli.projectName=jtfname.getText();
					appli.decription=tadescript.getText();
					appli.organization=jtorga.getText();
				}
			});

		//bouton cancel
		JButton cancel = new JButton(language.getCancel());
		p.add(cancel);
		cancel.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{ 
					setVisible (false); 
				}
			});

		getContentPane().add(p,BorderLayout.SOUTH);

		pack();
		setResizable(true);
		
		Point point = parent.getLocationOnScreen();
		int x = (int)point.getX() + parent.getWidth()/2;
		int y = (int)point.getY() + parent.getHeight()/2;
		setLocation(x-getWidth()/2, y-getHeight()/2);
		
	}
}



