
/**
  * @author THOMAS Alexandre
  * @author AUDRU Cedric
  */

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.tree.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.*;


/**
  * Class that generate the JTree
  */
public class GanttTree extends JPanel
{
	/** The root node of the Tree */
	private DefaultMutableTreeNode rootNode;

	/** The model for the JTree */
    private DefaultTreeModel treeModel;

	/** The JTree. */
    private JTree tree;

	/** Pointer on graphic area */
	private GanttGraphicArea area=null;

	/** Pointer on application */
	private GanttProject appli;

	/** An array for expansion */
	private ArrayList expand = new ArrayList();

	/** The vertical scrollbar on the JTree */
	JScrollBar vbar;
	
	/** The language use*/
	GanttLanguage language;

	/** Constructor. */
	public GanttTree (GanttProject app, GanttLanguage language)
	{
		super();

		this.appli = app;
		this.language=language;

		//Create the root node
		rootNode = new DefaultMutableTreeNode(new GanttTask ("None",	new GanttCalendar(2003,0,1), 1));
        treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener(new GanttTreeModelListener());

		//Create the JTree
		tree = new JTree(treeModel);
        tree.setEditable(false);
        tree.getSelectionModel().setSelectionMode (TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
		tree.setRowHeight(20);
		tree.setRootVisible(false);
		tree.addTreeExpansionListener(new GanttTreeExpansionListener());

		//Change the aspect, icon, color...
		/*DefaultTreeCellRenderer rendu ;
		rendu = (DefaultTreeCellRenderer) tree.getCellRenderer();
		rendu.setOpenIcon(new ImageIcon("icons/tarrow.gif"));
		rendu.setLeafIcon(new ImageIcon("icons/file.gif"));
		rendu.setClosedIcon(new ImageIcon("icons/barrow.gif"));
		rendu.setBackgroundSelectionColor(new Color((float)0.290,(float)0.349,(float)0.643));//new Color((float)0.31,(float)0.569,(float)0.686));
		rendu.setTextSelectionColor(new Color((float)1,(float)1,(float)1));
		rendu.setBorderSelectionColor(new Color(0,0,0));*/
		tree.setCellRenderer(new GanttTreeCellRenderer());
		
		
		//Add The tree on a Scrollpane
		JScrollPane scrollpane = new JScrollPane(tree);
		setLayout( new BorderLayout() );
		add( scrollpane , BorderLayout.CENTER);
		vbar = scrollpane.getVerticalScrollBar();
		vbar.addAdjustmentListener(new GanttAdjustmentListener());
		
	
		//A listener on mouse click (menu)
		MouseListener ml = new MouseAdapter() {
		     public void mousePressed(MouseEvent e) {
		         int selRow = tree.getRowForLocation(e.getX(), e.getY());
		         TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		         if(selRow != -1) {
					 if(e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3 ) {
						tree.setSelectionPath(selPath);
						createPopupMenu(e.getX(), e.getY()) ;

					//double click on left button
		             } /*else if(e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
					 	tree.setSelectionPath(selPath);
					 	appli.propertiesTask();
					 }*/
					 
		         }
		     }
		 };
		 tree.addMouseListener(ml);
	}
	
	

	/** Create a popup menu when mous click*/
	public void createPopupMenu(int x, int y)
	{
		JPopupMenu menu = new JPopupMenu();
		menu.add(appli.createNewItem(language.createTask(), "icons/insert.gif"));
		menu.add(appli.createNewItem(language.deleteTask(), "icons/delete.gif"));
		menu.add(appli.createNewItem(language.propertiesTask(), "icons/properties.gif"));
		menu.add(appli.createNewItem(language.notesTask(), "icons/notes.gif"));
		menu.add(appli.createNewItem(language.upTask(), "icons/up.gif"));
		menu.add(appli.createNewItem(language.downTask(), "icons/down.gif"));
		menu.show(this,x,y-area.getScrollBar());
	}

	/** Prefered size. */
	//public Dimension getPreferredSize() { return new Dimension(300, 540); }

	/** Change grpahic part */
	public void setGraphicArea (GanttGraphicArea area) { this.area = area; }
	
	/** Change language */
	public void setLanguage (GanttLanguage language) { this.language = language; }

	/** Return the array of expansion  */
	public ArrayList getExpand() { return expand; }
	
	/** Set the expand array and modify in consequence */
	public void setExpand(ArrayList exp) 
	{
		expand = exp;
	}

	/** Add a sub task. */
	public DefaultMutableTreeNode addObject(Object child, DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode childNode =
                new DefaultMutableTreeNode(child);

        if (parent == null) parent = rootNode;

		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

		//if((expand.contains(parent.toString()) && !parent.isRoot()) || parent.isRoot())
		
		tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        return childNode;
    }

	/** Return the selected task */
	public GanttTask getSelectedTask()
	{
		TreePath currentSelection = tree.getSelectionPath();
		if (currentSelection == null) return null;
		String selected = currentSelection.getLastPathComponent().toString();
		if(getNode(selected).isRoot()) return null;
		return getTask (selected);
	}

	/** Returne the task with the name name*/
	public GanttTask getTask ( String name )
	{
		DefaultMutableTreeNode base;
		base = (DefaultMutableTreeNode)tree.getModel().getRoot();
		Enumeration e = base.preorderEnumeration();
		while (e.hasMoreElements())
		{
			base = (DefaultMutableTreeNode)e.nextElement();
			if(base.toString() == name)
				return (GanttTask)(base.getUserObject());
		}
		return null;
	}

	/** Return the DefaultMutableTreeNodequi with the name name. */
	public DefaultMutableTreeNode getNode (String name)
	{
		DefaultMutableTreeNode res, base;
		base = (DefaultMutableTreeNode)tree.getModel().getRoot();
		Enumeration e = base.preorderEnumeration();
		while (e.hasMoreElements())
		{
			res =((DefaultMutableTreeNode)e.nextElement());
			if(res.toString() == name)
				return res;
		}
		return null;
	}


	/** Returnan ArrayList with all tasks. */
	public ArrayList getAllTasks ()
	{
		ArrayList res = new ArrayList();
		DefaultMutableTreeNode base;
		Enumeration e = (rootNode).preorderEnumeration();
		while (e.hasMoreElements())
			res.add(e.nextElement());
    	return res;
	}

	/** Return all sub task for the task "task" */
	public ArrayList getAllChildTask (String task)
	{
		DefaultMutableTreeNode base;
		base = (DefaultMutableTreeNode)getNode(task);
		Enumeration e = base.children();
		ArrayList res = new ArrayList();
		while (e.hasMoreElements())
			res.add(e.nextElement());
		return res;
	}


	/** Return all tasks on an array */
	public Object [] getAllTaskArray ()
	{
		ArrayList al = getAllTasks();
		return al.toArray();
	}

	/** Return all task exept the task in parameter */
	public String [] getAllTaskString(String except)
	{
		ArrayList l = getAllTasks();
		String [] res;
		if(except==null) res = new String [ l.size() ];
		else res = new String [ l.size() - 1];

		int i=0,j=0;
		for( ;i<l.size();i++)
		{
			if(except==null || (l.get(i).toString() != except) )
			{
				Array.set(res,j,l.get(i).toString());
				j++;
			}
		}
		return res;
	}

	/** Return an ArrayList with String for all tasks */
	public ArrayList getArryListTaskString(String except)
	{
		ArrayList l = getAllTasks();
		ArrayList res = new ArrayList();
		for(int i=0;i<l.size();i++)
		{
			if((except!=null && l.get(i).toString()!=except) || except==null)
				res.add(l.get(i).toString());
		}
		return res;
	}


	/** Remove the current node. */
	public void removeCurrentNode() {
        TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            if (parent != null) {
                treeModel.removeNodeFromParent(currentNode);
                return;
            }
        }
    }

	/** Clear the JTree. */
	public void clearTree()
	{
		expand.clear();
		rootNode.removeAllChildren();
		treeModel.reload();
	}

	/** Returne the mother task.*/
	public DefaultMutableTreeNode getFatherNode(GanttTask node)
	{
		if(node==null){return null;}
		if(getNode(node.toString()).isRoot()){return null;}
		DefaultMutableTreeNode tmp = (DefaultMutableTreeNode)getNode(node.toString());
		if(tmp==null) {return null;}

		return (DefaultMutableTreeNode)tmp.getParent();
		//return (DefaultMutableTreeNode)getNode(node.toString()).getParent();
	}

	/** Return the JTree. */
	public JTree getJTree() { return tree; }

	/** Return the root node */
	public DefaultMutableTreeNode getRoot() { return rootNode; }


	/** Change the root node (after load from file) */
	public void setRoot (DefaultMutableTreeNode dmtn)
	{
		rootNode = dmtn;
		treeModel = new DefaultTreeModel(rootNode);
        tree.setModel(treeModel);
		
		//Refresh expansion table
		expand.clear();
		ArrayList l = getAllTasks();
		for(int i=0;i<l.size();i++)
		{
			//Collapse all
			String nameOfTask=l.get(i).toString();
			DefaultMutableTreeNode node=this.getNode(nameOfTask);
			tree.scrollPathToVisible(new TreePath(node.getPath()));

			//put on the expansion table
			DefaultMutableTreeNode theFather = getFatherNode(getTask(nameOfTask));			
			if(theFather!=null && !expand.contains(theFather.toString()) )
				expand.add(theFather.toString());
		}
		
		/*for(int i=0;i<expand.size();i++)
		{
			ArrayList child = getAllChildTask((String)expand.get(i));
			DefaultMutableTreeNode node=this.getNode((String)expand.get(i));		
			tree.scrollPathToVisible(new TreePath(node.getPath()));
			
			for(int j=0;j<child.size();j++)
			{
				DefaultMutableTreeNode node2=this.getNode((String)child.get(j));
				tree.scrollPathToVisible(new TreePath(node2.getPath()));
			}
		}*/
	}


	/** Function to put up the selected task */
	public void upCurrentNode()
	{
		GanttTask current = getSelectedTask();
		if( current==null) return;
		
		if(getAllChildTask(current.toString()).size()!=0) return;
		DefaultMutableTreeNode cdmtn = getNode(current.toString());
		DefaultMutableTreeNode cdmtn2 = (DefaultMutableTreeNode)cdmtn.clone();
		DefaultMutableTreeNode father = this.getFatherNode(current);
		int index = father.getIndex((TreeNode)cdmtn);

		treeModel.removeNodeFromParent(cdmtn);

		//New position
		index = (index==0)?0:index-1;

		treeModel.insertNodeInto((MutableTreeNode) cdmtn2, (MutableTreeNode)father, index);
		
		//Select tagain this node
		TreeNode[] treepath=cdmtn2.getPath();
		TreePath path = new TreePath( treepath );
		tree.setSelectionPath(path);
		area.repaint();
	}

	/** Function to put down the selected task */
	public void downCurrentNode()
	{
		GanttTask current = getSelectedTask();
		if( current==null) return;
		
		if(getAllChildTask(current.toString()).size()!=0) return;
		DefaultMutableTreeNode cdmtn = getNode(current.toString());
		DefaultMutableTreeNode cdmtn2 = (DefaultMutableTreeNode)cdmtn.clone();
		DefaultMutableTreeNode father = this.getFatherNode(current);
		int index = father.getIndex((TreeNode)cdmtn);

		treeModel.removeNodeFromParent(cdmtn);

		//New position
		index = (index==father.getChildCount())?father.getChildCount():index+1;

		treeModel.insertNodeInto((MutableTreeNode) cdmtn2, (MutableTreeNode)father, index);
		
		//Select tagain this node
		TreeNode[] treepath=cdmtn2.getPath();
		TreePath path = new TreePath( treepath );
		tree.setSelectionPath(path);
		area.repaint();
	}
//////////////////////////////////////////////////////////////////////////////////////////
	/**
	  * Listener when scrollbar move
	  */
	public class GanttAdjustmentListener implements AdjustmentListener {	
		public void adjustmentValueChanged(AdjustmentEvent e) 
		{
			if(area!=null)
			{
				area.setScrollBar(e.getValue());
				area.repaint();
			}
		}
	}


//////////////////////////////////////////////////////////////////////////////////////////
	/**
	  * Class for expansion and collapse of node
	  */
	public class GanttTreeExpansionListener  implements TreeExpansionListener {
		/** Expansion */
		public void treeExpanded(TreeExpansionEvent e) {
			if(area!=null) area.repaint();
			if(!expand.contains(e.getPath().getLastPathComponent().toString()))
				expand.add(e.getPath().getLastPathComponent().toString());
		}

		/** Collapse */
		public void treeCollapsed(TreeExpansionEvent e) {
			if(area!=null) area.repaint();
			int index=expand.indexOf(e.getPath().getLastPathComponent().toString());
			if(index>=0)
				expand.remove(index);			
		}
	}

//////////////////////////////////////////////////////////////////////////////////////////

	/**
	  * Listener to generate modification on the model
	  */
	public class GanttTreeModelListener implements TreeModelListener {
		/** modify a node */
        public void treeNodesChanged(TreeModelEvent e) {
			if(area!=null) area.repaint();
		}
		/** Insert a new node. */
        public void treeNodesInserted(TreeModelEvent e) {
			
			if(!expand.contains(e.getTreePath().getLastPathComponent().toString()))
				expand.add(e.getTreePath().getLastPathComponent().toString());
			if(area!=null) area.repaint();
        }
		/** Delete a node. */
        public void treeNodesRemoved(TreeModelEvent e) {
			//expand.remove(expand.indexOf(e.getTreePath().getLastPathComponent().toString()));
			if(area!=null) area.repaint();
        }
		/** Structur change. */
        public void treeStructureChanged(TreeModelEvent e) {
			if(area!=null) area.repaint();
        }
    }
	
	
	/** 
	  * Render the cell of the tree
	  */
	public class GanttTreeCellRenderer extends JLabel implements TreeCellRenderer
	{
		
		public GanttTreeCellRenderer() { setOpaque(true); }
		
		public Component getTreeCellRendererComponent(JTree tree,
                                              Object value,
                                              boolean selected,
                                              boolean expanded,
                                              boolean leaf,
                                              int row,
                                              boolean hasFocus)
		{
			
			GanttTask task = getTask(value.toString());

			if(task.getBilan()) {
				setIcon(new ImageIcon("icons/meeting.gif"));
			} else if(leaf) {
				setIcon(new ImageIcon("icons/task.gif"));
			} else if(expanded) {
				setIcon(new ImageIcon("icons/tarrow.gif"));
			} else {
				setIcon(new ImageIcon("icons/barrow.gif"));
			}
			
			setText(task.toString());
			
			setBackground(selected?new Color((float)0.290,(float)0.349,(float)0.643):
						  row%2==0?Color.white:new Color((float)0.933,(float)0.933,(float)0.933));
			setForeground(selected ? Color.white : Color.black);
			return this;
		}
	
	}


}
