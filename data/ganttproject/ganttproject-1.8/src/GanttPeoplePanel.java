
/**
  * @author THOMAS Alexandre
  * @author AUDRU Cedric
  */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;
import java.util.ArrayList;
import javax.swing.table.*;



/**
  * Class to edit the differents person that work on the project
  */
public class GanttPeoplePanel extends JPanel
{
	/** Number of personne on the table */
	int nbobj=0;

	/** The JTable where will be stored the data */
	JTable table;
	
	/** The model of data*/
	GanttTableModel model;
	
	/** The language uses */
	GanttLanguage lang;

	/** The main application*/
	GanttProject appli;
	
	int cx=0, cy=0;
	
	
	public GanttPeoplePanel(GanttProject prj, GanttLanguage language)
	{
		this.appli = prj;
		this.lang = language;
	
		model = new GanttTableModel(language);
		table = new JTable(model);
		
		setLayout(new BorderLayout());
		add(new JScrollPane(table), BorderLayout.CENTER);
		
		table.setSelectionBackground(new Color((float)0.29,(float)0.349,(float)0.643));
		table.setSelectionForeground(new Color((float)1,(float)1,(float)1));
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//Add listener for mouse click
		MouseListener ml = new MouseAdapter() {
		     public void mousePressed(MouseEvent e) {
			 		Point p = new Point (e.getX(), e.getY());
					int selRow = table.rowAtPoint(p);
					int selCol = table.columnAtPoint(p);
					cx = selCol;
					cy = selRow;
					
					if(e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3 ) {
						createPopupMenu(e.getX(), e.getY()) ;
					}
		     }
		 };
		 table.addMouseListener(ml);
		
		this.setBackground(new Color(0.0f, 0.0f, 0.0f));
	}

	/* Create the popup menu*/
	public void createPopupMenu(int x, int y)
	{
		JPopupMenu menu = new JPopupMenu();
		menu.add(appli.createNewItem(lang.newHuman(), "icons/newhuman.gif"));
		menu.add(appli.createNewItem(lang.deleteHuman(), "icons/delhuman.gif"));
		menu.add(appli.createNewItem(lang.propertiesHuman(), "icons/prophuman.gif"));
		menu.show(this,x,y+20);
	}


	/** Function called when the language is changed */
	public void refresh(GanttLanguage language)
	{
		model.changeLanguage(language);
		
		lang = language;
	}
	
	
	
	/** Create a new Human */
	public void newHuman(GanttProject parent)
	{
		GanttPeople people = new GanttPeople();		
		GanttDialogPeople dp = new GanttDialogPeople (parent, lang, people);
		dp.show();
		if(dp.result())model.addRow(people);
	}
	
	/**Delete the selected human*/
	public void deleteHuman(GanttProject parent)
	{
		GanttPeople people=null;
		if(cy>=0 && cy<model.data.size())
			people = (GanttPeople)model.data.get(cy);
		if(people!=null)
		{
			int res = JOptionPane.showConfirmDialog(this, lang.msg6()+people.toString()+"??", lang.warning(),
								JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(res==JOptionPane.YES_OPTION)
			{
				model.deleteRow(cy);
			}
		}
	}
	
	
	/** Edit the setting for this human */
	public void propertiesHuman(GanttProject parent)
	{
		GanttPeople people=null;
		if(cy>=0 && cy<model.data.size())
			people = (GanttPeople)model.data.get(cy);
		if(people!=null)
		{
			GanttDialogPeople dp = new GanttDialogPeople (parent, lang, people);
			dp.show();
			if(dp.result()) model.updateRow(cy,people);
		}
	}
		
	/** Return the arrylist of the person */
	public ArrayList getPeople() { return model.data; } 
	
	/** Set the change to the model */
	public void setPeople(ArrayList list)
	{
		model.changePeople(list);
	}
	
	/** Reset all human...*/
	public void reset()
	{
		model.reset();
	}
	
	/**
	  * Class model of table to store person data
	  */
	public class GanttTableModel extends AbstractTableModel {
		/** The data */
		public ArrayList data;
		/** The colums title */
		public ArrayList colums;
		
		/** The differents parametres for person function */
		String [] personFunction;
			
		GanttTableModel(GanttLanguage language) {
			data = new ArrayList();
			colums = new ArrayList();
			
			//Add the colums name
			changeLanguage(language);
			
			//data.add(new GanttPeople("Alexandre THOMAS","alex.thomas@netcourrier.com", 1));
			//data.add(new GanttPeople("Cédric AUDRU","cedricaudru@yahoo.fr", 2));
			
			personFunction = language.getPersonFunction();
						
		}

		/** Number of row on the table */
		public int getRowCount() {
			return data.size();
		}

		/** Number of colums on the table */
  		public int getColumnCount() {
			return colums.size();
		}

		/** Return the value at the specificed case */
		public Object getValueAt(int row, int col) {
			
			if(row<0 || col <0 || row >= data.size() || col>= colums.size())
				return null;
			GanttPeople people = (GanttPeople)data.get(row);
			
			
			if(col==0)	//Name
				return people.getName();	

			if(col==2) //Contacts
				return people.getMail(); //Contacts
				
			
			return personFunction[people.getFunction()];
		}
		
		/** Return  the name of the specified colums */
  		public String getColumnName(int col) {
			return (String)colums.get(col);
		}
		
		
		/** Change the name of colums in function of the language */
		public void changeLanguage(GanttLanguage language) {
			colums.clear();
			String [] cols = language.getColsName();
			for(int i=0;i<cols.length;i++)
				colums.add(new String(cols[i]));
			fireTableRowsUpdated(0,data.size()-1);			
		}
		
		/** Add a new human */
		public void addRow(GanttPeople people)
		{
			data.add(people);
			fireTableRowsInserted(data.size()-1, data.size()-1);
		}
		
		/** Update the specified row */
		public void updateRow(int row, GanttPeople people)
		{
			data.set(row,people);
			fireTableRowsUpdated(row,row);
		}
		
		/** Delete the row specidied */
		public void deleteRow(int row)
		{
			data.remove(row);
			fireTableRowsDeleted(row,row);
		}
		
		public void changePeople(ArrayList list)
		{
			//data.clear();
			data = list;
			//fireTableRowsUpdated(0,list.size()-1);
			fireTableDataChanged();
		}
		
		/** Are the cell editable */
		public boolean isCellEditable(int rowIndex, int columnIndex) 
		{
			//if(columnIndex==0 || columnIndex==2) return true;
			return false;
		}
		
		/** Reset all human...*/
		public void reset()
		{
			int size = data.size();
			data.clear();
			if(size>0)
				fireTableRowsDeleted(0,size-1);
		}
		
	}

}


