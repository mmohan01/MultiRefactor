
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
  * Dialog to edit the properties of a task
  */
public class GanttDialogProperties extends JDialog implements ActionListener
{
	/** JTextField for the name of task */
	private JTextField jtfname;
	/** JConboBox for the mother task */
	private JComboBox jcbfather;
	/** JTextField for the begining of the task */
	private JTextField jtfbegin;
	/** JTextField for duration of task */
	private JTextField jtflength;
	/** JList for depends of task */
	private JList jldepend;
	/** JSlider for advent state of task */
	private JSlider spercent;
	/** JCheckBox . Is is a meetin point??? */
	private JCheckBox jcbbilan;
	/** Boolean to say if the task has child */
	boolean haschild=false;

	/** The GanttTree of the application. */
	private GanttTree ttree;
	/** GanttTask to edit */
	private GanttTask ttask;
	/** GanttGraphicArea of the application. */
	private GanttGraphicArea aarea;
	/** Button to choose the date */
	private JButton button;
	/** Store old percent state value */
	int percentValue;
	/** The language */
	GanttLanguage lang;

	/** Constructor*/
	public GanttDialogProperties(JFrame parent, GanttTree tree, GanttTask task,GanttGraphicArea area,
		GanttLanguage language)
	{
		super(parent, language.propertiesFor()+task.toString(),true);
		
		this.ttree = tree;
		this.ttask = task;
		this.aarea = area;
		this.percentValue = task.getPercent();
		this.lang = language;

		if(tree.getAllChildTask(task.toString()).size()!=0)
			haschild=true;

		final String [] lot = tree.getAllTaskString(task.toString());
		Box b1 = Box.createVerticalBox();

		//THe name of the task
		b1.add(new JLabel(language.name()));
		jtfname = new JTextField (task.toString());
		b1.add(jtfname);

		//Le pere de la tache
		/*b1.add(new JLabel("The father"));
		jcbfather = new JComboBox(lot);
		jcbfather.setSelectedItem(tree.getFatherNode(task).toString());
		b1.add(jcbfather);*/

		//begin date
		b1.add(new JLabel(language.dateOfBegining()));
		jtfbegin=new JTextField(task.getStart().toString());
		jtfbegin.setEnabled(false);
		if(haschild)jtfbegin.setEnabled(false);
		
		Box hb1 = Box.createHorizontalBox();
		hb1.add(jtfbegin);
		button = new JButton(new ImageIcon("icons/calendar.gif"));
		button.addActionListener(this);
		hb1.add(button);
		b1.add(hb1);
		if(haschild)button.setEnabled(false);

		//Duration of task
		b1.add(new JLabel(language.length()));
		jtflength=new JTextField(new Integer(task.getLength()).toString());
		if(haschild)jtflength.setEnabled(false);
		b1.add(jtflength);

		//pour savoir si c'est une tache bilan
		jcbbilan = new JCheckBox(language.meetingPoint());
		jcbbilan.setSelected(task.getBilan());
		b1.add(jcbbilan);
		if(haschild)jcbbilan.setEnabled(false);
		/*jcbbilan.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{ 
					//System.out.println("CheckBox pressé");
				}
			});*/

		Box b2 = Box.createVerticalBox();

		//Depend list
		b2.add(new JLabel(language.depends()));
		final String lot2 [] = new String[lot.length-1];
		for(int i=1;i<lot.length;i++) lot2[i-1]=lot[i];
		jldepend = new JList(lot2);
		
		jldepend.setSelectionBackground(new Color((float)0.29,(float)0.349,(float)0.643));
		jldepend.setSelectionForeground(new Color((float)1,(float)1,(float)1));
		
		ArrayList depend = task.getDepend();
		int [] index = new int [depend.size()];

		for(int i=0;i<depend.size();i++)
			index[i] = findTheIndex(depend.get(i), lot2);
		jldepend.setSelectedIndices(index);

		JScrollPane scrollPane = new JScrollPane(jldepend);
		b2.add(scrollPane);

		//Advanc state, could be modify in real time
		b2.add(new JLabel(language.advancement()));
		spercent = new JSlider(JSlider.HORIZONTAL, 0, 100, task.getPercent()) ;
		spercent.setPaintLabels(true);
		spercent.setPaintTicks(true);
		spercent.setPaintTrack(true);
		spercent.addChangeListener(new ChangeListener ()
			{
				public void stateChanged(ChangeEvent e) 
				{
					ttask.setPercent(spercent.getValue());
					DefaultMutableTreeNode node = ttree.getNode(ttask.toString());
					node.setUserObject(ttask);
					
					DefaultMutableTreeNode father = ttree.getFatherNode(ttask);
					GanttTask taskFather = (GanttTask)father.getUserObject();
					taskFather.refreshDateAndAdvancement(ttree);
					father.setUserObject(taskFather);
					
					aarea.repaint();
				}
			});
		
		if(haschild)spercent.setEnabled(false);
		b2.add(spercent);


		getContentPane().add(b1,"West");
		getContentPane().add(b2,"East");

		JPanel p = new JPanel();
		JButton ok = new JButton(language.getOk());
		getRootPane().setDefaultButton(ok);
		p.add(ok);
		
		final JDialog theDialog = this;
		
		//Listener on the ok bouton 
		ok.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{
					ArrayList alt = ttree.getArryListTaskString(ttask.toString());
					String theNewName = jtfname.getText();
					
					//Check the name of task
					if(!alt.contains((Object)(theNewName)))
					{					
						//hide the dialog
						setVisible (false);
					
					
						ttask.setStart(new GanttCalendar(jtfbegin.getText()));
						ttask.setBilan(jcbbilan.isSelected());
						if(ttask.getBilan())
							ttask.setLength(1);
						else ttask.setLength(new Integer(jtflength.getText()).hashCode());
						ttask.setPercent(spercent.getValue());
					
						
						//Check the depend array
						ArrayList lgt = ttree.getAllTasks();
						
						for(int i=0;i<lgt.size();i++)
						{
							GanttTask tmpTask = (GanttTask) ((DefaultMutableTreeNode)lgt.get(i)).getUserObject();
							tmpTask.checkDepend(ttask.toString(), theNewName);
						}
															
						//Change the name
						ttask.setName(theNewName);
	
	
	
						int [] indices = jldepend.getSelectedIndices();					
						ttask.clearDepend();
						for(int i=0;i<indices.length;i++)
						{
							//if(!(ttree.getTask(lot2[indices[i]]).getDepend().contains(ttask.toString())))
							if(ttree.checkDepend(ttask, lot2[indices[i]]))
							{
								ttask.setDepend(lot2[indices[i]]);
								ttree.refreshAllChild(lot2[indices[i]]);
								ttree.refreshAllFather(lot2[indices[i]]);
							}
						}
	
						DefaultMutableTreeNode node = ttree.getNode(ttask.toString());
						node.setUserObject(ttask);

						/*DefaultMutableTreeNode father = ttree.getFatherNode(ttask);
						GanttTask taskFather = (GanttTask)father.getUserObject();
						taskFather.refreshDateAndAdvancement(ttree);
						father.setUserObject(taskFather);*/
						
						ttree.refreshAllChild(theNewName);
						
						GanttTask taskFather = new GanttTask("__Bidon__",null,0);
						DefaultMutableTreeNode father = ttree.getFatherNode(ttask);
		
						while(ttree.getNode(ttask.toString()).isRoot()==false)
						{
							father = ttree.getFatherNode(ttask);
							ttree.refreshAllChild(father.toString());
							taskFather = (GanttTask)father.getUserObject();
							taskFather.refreshDateAndAdvancement(ttree);
							father.setUserObject(taskFather);
							ttask = taskFather;
						}
						
						aarea.repaint();
					} 
					else
					{
						JOptionPane.showConfirmDialog(theDialog,
							lang.propertiesMsg(theNewName), lang.error(),
							JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE );					
					}
				}
			});

		//cancel button
		JButton cancel = new JButton(language.getCancel());
		p.add(cancel);
		cancel.addActionListener(new ActionListener()
		 	{	public void actionPerformed(ActionEvent evt)
				{ 
					setVisible (false); 
					ttask.setPercent(percentValue);
					DefaultMutableTreeNode node = ttree.getNode(ttask.toString());
					node.setUserObject(ttask);
					aarea.repaint();
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

	/** Search a string on an array */
	private int findTheIndex(Object s, String []lot)
	{
		for (int i=0;i<lot.length;i++)
			if(s.toString()==lot[i]) return i;
		return 0;
	}
	
	
	/** When click on date button, it open the dialog to select date. */
	public void actionPerformed (ActionEvent evt)
	{
		if(evt.getSource() == button)
		{
			GanttCalendar date = new GanttCalendar(jtfbegin.getText());
			GanttDialogDate dd = new GanttDialogDate(this,date,lang);
			dd.show();
			jtfbegin.setText(dd.getDate().toString());
		}
	}
			
	
	
}






