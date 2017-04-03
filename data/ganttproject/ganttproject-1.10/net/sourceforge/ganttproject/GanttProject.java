/***************************************************************************
 GanttProject.java  -  description
 -------------------
 begin                : dec 2002
 copyright            : (C) 2002 by Thomas Alexandre
 email                : alexthomas(at)ganttproject.org
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

package net.sourceforge.ganttproject;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileFilter;
//import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import net.sourceforge.ganttproject.action.DeleteHumanAction;
import net.sourceforge.ganttproject.action.ImportResources;
import net.sourceforge.ganttproject.action.NewArtefactAction;
import net.sourceforge.ganttproject.action.NewHumanAction;
import net.sourceforge.ganttproject.action.NewTaskAction;
import net.sourceforge.ganttproject.action.ResourceActionSet;
import net.sourceforge.ganttproject.document.AbstractURLDocument;
import net.sourceforge.ganttproject.document.Document;
import net.sourceforge.ganttproject.document.DocumentCreator;
import net.sourceforge.ganttproject.document.DocumentsMRU;
import net.sourceforge.ganttproject.document.FileDocument;
import net.sourceforge.ganttproject.document.HttpDocument;
import net.sourceforge.ganttproject.document.OpenDocumentAction;
import net.sourceforge.ganttproject.filter.GanttCSVFilter;
import net.sourceforge.ganttproject.filter.GanttHTMLFileFilter;
import net.sourceforge.ganttproject.filter.GanttJPGFileFilter;
import net.sourceforge.ganttproject.filter.GanttPDFFileFilter;
import net.sourceforge.ganttproject.filter.GanttPNGFileFilter;
import net.sourceforge.ganttproject.filter.GanttTXTFileFilter;
import net.sourceforge.ganttproject.filter.GanttXFIGFileFilter;
import net.sourceforge.ganttproject.filter.GanttXMLFileFilter;
import net.sourceforge.ganttproject.gui.GanttDialogCalendar;
import net.sourceforge.ganttproject.gui.GanttDialogInfo;
import net.sourceforge.ganttproject.gui.GanttDialogProperties;
import net.sourceforge.ganttproject.gui.GanttLookAndFeelInfo;
import net.sourceforge.ganttproject.gui.GanttLookAndFeels;
//import net.sourceforge.ganttproject.gui.GanttMetalTheme;
import net.sourceforge.ganttproject.gui.GanttPreviewPrint;
import net.sourceforge.ganttproject.gui.GanttStatusBar;
import net.sourceforge.ganttproject.gui.GanttURLChooser;
import net.sourceforge.ganttproject.gui.TestGanttRolloverButton;
import net.sourceforge.ganttproject.gui.TipsDialog;
import net.sourceforge.ganttproject.gui.UIConfiguration;
import net.sourceforge.ganttproject.gui.about.AboutDialog;
import net.sourceforge.ganttproject.gui.options.SettingsDialog;
import net.sourceforge.ganttproject.gui.projectwizard.NewProjectWizard;
import net.sourceforge.ganttproject.io.GanttCSVExport;
import net.sourceforge.ganttproject.io.GanttHTMLExport;
import net.sourceforge.ganttproject.io.GanttPDFExport;
import net.sourceforge.ganttproject.io.GanttTXTOpen;
import net.sourceforge.ganttproject.io.GanttXFIGSaver;
import net.sourceforge.ganttproject.io.GanttXMLOpen;
import net.sourceforge.ganttproject.io.GanttXMLSaver;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.parser.AllocationTagHandler;
import net.sourceforge.ganttproject.parser.DependencyTagHandler;
import net.sourceforge.ganttproject.parser.ResourceTagHandler;
import net.sourceforge.ganttproject.parser.RoleTagHandler;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.resource.ProjectResource;
import net.sourceforge.ganttproject.resource.ResourceContext;
import net.sourceforge.ganttproject.resource.ResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskContainmentHierarchyFacade;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.TaskManagerConfig;
import net.sourceforge.ganttproject.task.algorithm.AdjustTaskBoundsAlgorithm;
import net.sourceforge.ganttproject.task.algorithm.RecalculateTaskCompletionPercentageAlgorithm;
import net.sourceforge.ganttproject.task.dependency.TaskDependencyException;
import net.sourceforge.ganttproject.util.BrowserControl;
/**
 * Main frame of the project
 */
public class GanttProject
        extends JFrame
        implements ActionListener, IGanttProject {

	/** The current version of ganttproject */
	public static final String version = "1.10";
	/* List of calendar for the project */
	private ArrayList listOfCalendar = new ArrayList();
	/** Command line creation or gui creation */
	public static boolean byCommandLine = false;
	/** The language use */
	private GanttLanguage language = GanttLanguage.getInstance();
	/** The JTree part. */
	private GanttTree tree;
	/** GanttGraphicArea for the calendar with Gantt */
	private GanttGraphicArea area;
	/** GanttPeoplePanel to edit person that work on the project */
	private GanttResourcePanel resp;
	/** The differents menus */
    public JMenu mProject, mMRU, mEdit, mTask, mHuman, mHelp, mServer, mCalendar;

	/** The differetns menuitem */
    public JMenuItem miNew, miOpen, miOpenURL, miSave, miSaveAs, miSaveAsURL, miExport,miImport,/*miOpenDB,miSaveAsDB,*/
    miPrint, miPreview, miQuit,
    miCut, miCopy, miPaste,miOptions,
    miDeleteTask, miPropertiesTask,
    miUp, miDown,
    miDelHuman, miPropHuman, miSendMailHuman,
    miEditCalendar, miPrjCal,
    miWebPage, miTips, miAbout, miManual;

	private static final int maxSizeMRU = 8;
	private DocumentsMRU documentsMRU = new DocumentsMRU(maxSizeMRU);
	/** The differents button of toolbar */
    public TestGanttRolloverButton bNew, bOpen, bSave, bSaveAs, bExport, bImport, bPrint,
    bNewTask, bDelete, bProperties, bUnlink, bLink,
    bInd, bUnind, bUp, bDown, bPrev, bNext,
    bZoomIn, bZoomOut, bZoomFit;

	/** The project filename */
	public Document projectDocument = null;
	/** The tabbed pane with the differents parts of the project */
	public JTabbedPane tabpane;
	
	/** Informations for the current project. */
	public PrjInfos prjInfos = new PrjInfos();
	
	/** Boolean to know if the file has been modify */
	public boolean askForSave = false;
	
	/** The info for the look'n'feel */
	public GanttLookAndFeelInfo lookAndFeel;
	
	/** The list of all managers installed in this project */
	private Hashtable managerHash = new Hashtable();
	private ResourceActionSet myResourceActions;
	private boolean isApplet;
	
	/** Frame for the help Viewer */
	private JFrame helpFrame = null;
	private final TaskManager myTaskManager;
	private FacadeInvalidator myFacadeInvalidator;
	private UIConfiguration myUIConfiguration;

    private static class TaskContainmentHierarchyFacadeImpl implements TaskContainmentHierarchyFacade {
		private Map myTask2treeNode = new HashMap();
		private Task myRootTask;
		public TaskContainmentHierarchyFacadeImpl(GanttTree tree) {
			ArrayList allTasks = tree.getAllTasks();
			//comboBox.addItem("no set");
			//for (int i = 0; i < allTasks.size(); i++) {
            //DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) allTasks.get(i);
			for (Iterator it = allTasks.iterator(); it.hasNext();) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) it.next();
				Task task = (Task) treeNode.getUserObject();
				if (treeNode.isRoot()) {
					myRootTask = task;
				}
				myTask2treeNode.put(task, treeNode);
			}
		}
		public Task[] getNestedTasks(Task container) {
			Task[] result = null;
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) myTask2treeNode.get(container);
			if (treeNode != null) {
				ArrayList list = new ArrayList();
                for (Enumeration children = treeNode.children(); children.hasMoreElements();) {
                    DefaultMutableTreeNode next = (DefaultMutableTreeNode) children.nextElement();
					list.add(next.getUserObject());
				}
				result = (Task[]) list.toArray(new Task[0]);
			}
			return result == null ? new Task[0] : result;
		}
		public Task getRoot() {
			return myRootTask;
		}
		public Task getContainer(Task nestedTask) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) myTask2treeNode.get(nestedTask);
			if (treeNode == null) {
				return null;
			}
            DefaultMutableTreeNode containerNode = (DefaultMutableTreeNode) treeNode.getParent();
            return containerNode == null ? null : (Task) containerNode.getUserObject();
		}
	}
	private static class FacadeInvalidator implements TreeModelListener {
		private boolean isValid;
		public FacadeInvalidator(TreeModel treeModel) {
			isValid = false;
			treeModel.addTreeModelListener(this);
		}
		boolean isValid() {
			return isValid;
		}
		void reset() {
			isValid = true;
		}
		public void treeNodesChanged(TreeModelEvent e) {
			isValid = false;
		}
		public void treeNodesInserted(TreeModelEvent e) {
			isValid = false;
		}
		public void treeNodesRemoved(TreeModelEvent e) {
			isValid = false;
		}
		public void treeStructureChanged(TreeModelEvent e) {
			isValid = false;
		}
	}

	private final GanttOptions options;
	
	//! Toolbar of ui
	private JToolBar toolBar;
	
	//! a status bar on the main frame
	private GanttStatusBar statusBar;
	private NewTaskAction myNewTaskAction;
	private NewHumanAction myNewHumanAction;
	private NewArtefactAction myNewArtefactAction;
	
	/** Constructor */
	public GanttProject(String filename, boolean isApplet) {
		super("Gantt Chart");
		setTitle(language.getText("appliTitle"));
		lookAndFeel = GanttLookAndFeels.getGanttLookAndFeels().getDefaultInfo();
        options = new GanttOptions(getRoleManager());
		//Color color = GanttGraphicArea.taskDefaultColor;
		options.setUIConfiguration(myUIConfiguration);
		options.setDocumentsMRU(documentsMRU);
		options.setLookAndFeel(lookAndFeel);
		if (options.load()) {
			language = options.getLanguage();
			GanttGraphicArea.taskDefaultColor = options.getDefaultColor();
			
			lookAndFeel = options.getLnfInfos();
			HttpDocument.setLockDAVMinutes(options.getLockDAVMinutes());			
		}
		
		myUIConfiguration = options.getUIConfiguration();
		TaskManagerConfig taskConfig = new TaskManagerConfig() {
			public Color getDefaultColor() {
				return myUIConfiguration.getTaskColor();
			}
		};
        myTaskManager = TaskManager.Access.newInstance(new TaskContainmentHierarchyFacade.Factory() {
					public TaskContainmentHierarchyFacade createFacede() {
                if (!GanttProject.this.myFacadeInvalidator.isValid() || myCachedFacade==null) {
                    myCachedFacade = new TaskContainmentHierarchyFacadeImpl(tree);
							GanttProject.this.myFacadeInvalidator.reset();
						}
						return myCachedFacade;
					}
					private TaskContainmentHierarchyFacadeImpl myCachedFacade;
				}, taskConfig);
		this.isApplet = isApplet;
		ImageIcon icon = new ImageIcon(getClass().getResource(
				"/icons/ganttproject.png"));
		setIconImage(icon.getImage());
		//Create each objects
		tree = new GanttTree(this, myTaskManager);
		myFacadeInvalidator = new FacadeInvalidator(tree.getJTree().getModel());
        area = new GanttGraphicArea(this, tree, getTaskManager(), myUIConfiguration);
		GanttImagePanel but = new GanttImagePanel("big.png", "tasks.png",300, 42);
		tree.setGraphicArea(area);
		//Create the menus
		JMenuBar bar = new JMenuBar();
		setJMenuBar(bar);
		//Allocation of the menus
		mProject = new JMenu();
		mMRU = new JMenu();
        mMRU.setIcon(new ImageIcon(getClass().getResource("/icons/recent_16.gif")));
		mEdit = new JMenu();
		mTask = new JMenu();
		mHuman = new JMenu();
		mHelp = new JMenu();
		mCalendar = new JMenu();
		mServer = new JMenu();
        mServer.setIcon(new ImageIcon(getClass().getResource("/icons/server_16.gif")));
        
		miNew = createNewItem("/icons/new_16.gif");
		mProject.add(miNew);
		miOpen = createNewItem("/icons/open_16.gif");
		mProject.add(miOpen);
		mProject.add(mMRU);
		updateMenuMRU();
		mProject.addSeparator();
		miSave = createNewItem("/icons/save_16.gif");
		miSave.setEnabled(false);
		mProject.add(miSave);
		miSaveAs = createNewItem("/icons/saveas_16.gif");
		mProject.add(miSaveAs);
		mProject.addSeparator();
		miImport = createNewItem("/icons/import_16.gif");		
		mProject.add(miImport); 
		miExport = createNewItem("/icons/export_16.gif");		
		mProject.add(miExport);
		mProject.addSeparator();


        miOpenURL = createNewItem("");
		miSaveAsURL = createNewItem("");
		mServer.add(miOpenURL);
		mServer.add(miSaveAsURL);
		mProject.add(mServer);
		mProject.addSeparator();
		miPrint = createNewItem("/icons/print_16.gif");
		mProject.add(miPrint);
		miPreview = createNewItem("/icons/preview_16.gif");
		mProject.add(miPreview);
		mProject.addSeparator();
		miQuit = createNewItem(""/*"/icons/exit_16.gif"*/);
		mProject.add(miQuit);
		miCut = createNewItem("/icons/cut_16.gif");
		mEdit.add(miCut);
		miCopy = createNewItem("/icons/copy_16.gif");
		mEdit.add(miCopy);
		miPaste = createNewItem("/icons/paste_16.gif");
		mEdit.add(miPaste);
		mEdit.addSeparator();
		miOptions = createNewItem("/icons/settings_16.gif");
		mEdit.add(miOptions);
		myNewTaskAction = new NewTaskAction((IGanttProject)this);
		mTask.add(myNewTaskAction);
		miDeleteTask = createNewItem("/icons/delete_16.gif");
		mTask.add(miDeleteTask);
		miPropertiesTask = createNewItem("/icons/properties_16.gif");
		mTask.add(miPropertiesTask);
		//
		// 
		myNewHumanAction = new NewHumanAction(getHumanResourceManager(), getRoleManager(), this); 
		mHuman.add(myNewHumanAction);
        miDelHuman = new JMenuItem(new DeleteHumanAction(getHumanResourceManager(),
				(ResourceContext) getResourcePanel(), this));
		mHuman.add(miDelHuman);
		miPropHuman = createNewItem("/icons/properties_16.gif");
		mHuman.add(miPropHuman);
		miSendMailHuman = createNewItem("/icons/send_mail_16.gif");
		mHuman.add(miSendMailHuman);

		mHuman.add(new ImportResources(getHumanResourceManager(), getTaskManager(), getRoleManager(), this));


		miEditCalendar = createNewItem("/icons/clock_16.gif");
		mCalendar.add(miEditCalendar);
		miPrjCal = createNewItem("/icons/default_calendar_16.gif");
		mCalendar.add(miPrjCal);
		miWebPage = createNewItem("/icons/home_16.gif");
		mHelp.add(miWebPage);
		miManual = createNewItem("/icons/help_16.gif");
		try { //See if helpgui library is on the classpath
            Class.forName("net.sourceforge.helpgui.gui.MainFrame").newInstance();
			mHelp.add(miManual);
			miManual.setAccelerator(KeyStroke.getKeyStroke("F1"));
		} catch (Exception ex) {
			//Not add the help button on the ui
		}
		miTips = createNewItem("/icons/about_16.gif");
		mHelp.add(miTips);
		miAbout = createNewItem("/icons/manual_16.gif");
		mHelp.add(miAbout);
		if (!isApplet) {
            bar.add(mProject); //for a applet veiwer, Project menu is not neccessary By CL
		}
		bar.add(mEdit);
		bar.add(mTask);
		bar.add(mHuman);
        //bar.add(mCalendar);
		bar.add(mHelp);
		setMemonic();
		//to create a default project
		//createDefaultTree(tree);
		JPanel left = new JPanel(new BorderLayout());
		left.add(but, BorderLayout.NORTH);
		left.add(tree, BorderLayout.CENTER);
		left.setPreferredSize(new Dimension(250, 600));
		left.setBackground(Color.white);
		//A splitpane is use
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		if (language.getComponentOrientation() == ComponentOrientation.LEFT_TO_RIGHT) {
			splitPane.setLeftComponent(left);
			splitPane.setRightComponent(area);
			splitPane.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		} else {
			splitPane.setRightComponent(left);
			splitPane.setLeftComponent(area);
			splitPane.setDividerLocation((int)(Toolkit.getDefaultToolkit()
					.getScreenSize().getWidth()
					- left.getPreferredSize().getWidth()));
			splitPane.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		}
		splitPane.setOneTouchExpandable(true);
		splitPane.setPreferredSize(new Dimension(800, 500));
		//Add Gantt Panel, Human resource pannel and RESOURCE panel
		tabpane = new JTabbedPane();
        tabpane.addTab(language.getText("gantt"), new ImageIcon(getClass().getResource("/icons/tasks_16.gif")), splitPane);
        tabpane.addTab(language.getText("human"), new ImageIcon(getClass().getResource("/icons/res_16.gif")), getResourcePanel());
		tabpane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabpane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				bUnlink.setEnabled(tabpane.getSelectedIndex() == 0);
				bLink.setEnabled(tabpane.getSelectedIndex() == 0);
				bInd.setEnabled(tabpane.getSelectedIndex() == 0);
				bUnind.setEnabled(tabpane.getSelectedIndex() == 0);
				if (tabpane.getSelectedIndex() == 0) { //Gantt Chart
                    bNewTask.setToolTipText(getToolTip(correctLabel(language.getText("createTask"))));
                    bDelete.setToolTipText(getToolTip(correctLabel(language.getText("deleteTask"))));
                    bProperties.setToolTipText(getToolTip(correctLabel(language.getText("propertiesTask"))));
                    
                    if(options.getButtonShow()!=GanttOptions.ICONS) {
                    	bNewTask.setText(correctLabel(language.getText("createTask")));
                    	bDelete.setText(correctLabel(language.getText("deleteTask")));
                    	bProperties.setText(correctLabel(language.getText("propertiesTask")));
                    }

				} else if (tabpane.getSelectedIndex() == 1) { //Resources Chart
                    bNewTask.setToolTipText(getToolTip(correctLabel(language.getText("newHuman"))));
                    bDelete.setToolTipText(getToolTip(correctLabel(language.getText("deleteHuman"))));
                    bProperties.setToolTipText(getToolTip(correctLabel(language.getText("propertiesHuman"))));
                    
                    if(options.getButtonShow()!=GanttOptions.ICONS) {
                    	bNewTask.setText(correctLabel(language.getText("newHuman")));
                    	bDelete.setText(correctLabel(language.getText("deleteHuman")));
                    	bProperties.setText(correctLabel(language.getText("propertiesHuman")));
                    }
				}
			}
		});
		//Add tabpan on the content pane
		getContentPane().add(tabpane, BorderLayout.CENTER);
		//Add toolbar
		toolBar = new JToolBar("GanttProject", options.getToolBarPosition());
		toolBar.setBorderPainted(true);
		toolBar.setRollover(true);
		toolBar.setFloatable(true);
		this.addButtons(toolBar);
		getContentPane().add(toolBar, (toolBar.getOrientation()==JToolBar.HORIZONTAL)?BorderLayout.NORTH:BorderLayout.WEST);

		//add the status bar
		statusBar = new GanttStatusBar();
		getContentPane().add(statusBar,BorderLayout.SOUTH);
		statusBar.setVisible(options.getShowStatusBar());
		
		
		//Open the the project passed in argument
		if (filename != null) {
			Document document = DocumentCreator.createDocument(filename);
			try {
				openDocument(document);
			} catch (IOException ex) {
				System.err.println(language.getText("msg2") + "\n" + filename);
				System.err.println(ex.getMessage());
			}
		}
		// update 18-03-2003
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});
		// update 18-03-2003
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = getPreferredSize();
		// Put the frame at the middle of the screen
		setLocation(screenSize.width / 2 - (windowSize.width / 2),
				screenSize.height / 2 - (windowSize.height / 2));
		this.pack();
		changeLanguage();
		changeLookAndFeel(lookAndFeel);
		changeLookAndFeel(lookAndFeel); //Twice call for update font on menu
		if (options.isLoaded()) {
			setBounds(options.getX(), options.getY(), options.getWidth(), options.getHeight());
		}
		if (options.getOpenTips() && !byCommandLine) {
			TipsDialog tips = new TipsDialog(this, options.getOpenTips());
			tips.show();
            tips.toFront(); // somehow assure, that the TipsDialog is the top window an MacOS 
		}
		applyComponentOrientation(GanttLanguage.getInstance()
				.getComponentOrientation());
        
	}
	public GanttProject(String filename) {
		this(filename, false);
	}
	/**
	 * Updates the last open file menu items.
	 */
	private void updateMenuMRU() {
		mMRU.removeAll();
		int index = 0;
		Iterator iterator = documentsMRU.iterator();
		while (iterator.hasNext()) {
			index++;
			Document document = (Document) iterator.next();
            JMenuItem mi =
                    new JMenuItem(
                            new OpenDocumentAction(index, document, this));
			mMRU.add(mi);
		}
	}
	public String getXslDir() {
		return options.getXslDir();
	}
	/** @return the options of ganttproject. */
	public GanttOptions getOptions(){
		return options;
	}
	public void restoreOptions(){
		options.initByDefault(); //options by default
		myUIConfiguration = options.getUIConfiguration();
		GanttGraphicArea.taskDefaultColor = new Color( 140, 182, 206);
		area.repaint();
	}
		/** @return the status Bar of the main frame. */
	public GanttStatusBar getStatusBar() {
		return statusBar;
	}

	public String getXslFo() {
		if (new File(options.getXslFo()).exists())
			return options.getXslFo();
		return GanttProject.class.getResource("/xslfo/ganttproject.xsl")
				.toString();
	}
	/** Create memonic for keyboard */
	public void setMemonic() {
        final int MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		//--NEW----------------------------------
		//miNew.setMnemonic(KeyEvent.VK_N);
		miNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, MENU_MASK));
		//--OPEN----------------------------------
		miOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MENU_MASK));
		//Open from the web
        //miOpenURL.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, MENU_MASK));
		//--SAVE----------------------------------
		miSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, MENU_MASK));
		//--EXPORT----------------------------------
		miExport.setAccelerator(KeyStroke
				.getKeyStroke(KeyEvent.VK_E, MENU_MASK));
//		--IMPORT----------------------------------
		miImport.setAccelerator(KeyStroke
				.getKeyStroke(KeyEvent.VK_I, MENU_MASK));
		//--PRINT----------------------------------
		miPrint
				.setAccelerator(KeyStroke
						.getKeyStroke(KeyEvent.VK_P, MENU_MASK));
		//--QUIT----------------------------------
		miQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, MENU_MASK));
		//--CUT----------------------------------
		//miCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
		// MENU_MASK));
		//--COPY----------------------------------
		//miCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
		// MENU_MASK));
		//--PASTE----------------------------------
		//miPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
		// MENU_MASK));
		//--OPTIONS----------------------------------
		miOptions.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
				MENU_MASK));
		//--NEW TASK----------------------------------
//		miNewTask.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
//				MENU_MASK));
		//--PROPERTIES TASK----------------------------------
		miPropertiesTask.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
				MENU_MASK));
		//--DELETE TASK----------------------------------
		miDeleteTask.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				MENU_MASK));
		//--NEW HUMAN----------------------------------
        //miNewHuman.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, MENU_MASK));
        //miDelHuman.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, MENU_MASK));
        miPropHuman.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, MENU_MASK));

		
	}
	/** Create an item with a label */
	public JMenuItem createNewItemText(String label) {
		JMenuItem item = new JMenuItem(label);
		item.addActionListener(this);
		return item;
	}
	/** Create an item with an icon */
	public JMenuItem createNewItem(String icon) {
		JMenuItem item = new JMenuItem(new ImageIcon(getClass().getResource(
				icon)));
		item.addActionListener(this);
		return item;
	}
	/** Create an item with a label and an icon */
	public JMenuItem createNewItem(String label, String icon) {
        JMenuItem item = new JMenuItem(label,
                new ImageIcon(getClass().getResource(icon)));
		item.addActionListener(this);
		return item;
	}
	/** Function to change language of the project */
	public void changeLanguage() {
		
		applyComponentOrientation(language.getComponentOrientation());
		changeLanguageOfMenu();
		area.repaint();
		getResourcePanel().area.repaint();
		getResourcePanel().refresh(language);
		applyComponentOrientation(language.getComponentOrientation());		
	}
	/** Change the style of the application */
	public void changeLookAndFeel(GanttLookAndFeelInfo lookAndFeel) {
		try {
			UIManager.setLookAndFeel(lookAndFeel.getClassName());
			SwingUtilities.updateComponentTreeUI(this);
			this.lookAndFeel = lookAndFeel;
		} catch (Exception e) {
			GanttLookAndFeelInfo info = GanttLookAndFeels
					.getGanttLookAndFeels().getDefaultInfo();
			System.out.println("Can't find the LookAndFeel\n"
					+ lookAndFeel.getClassName() + "\n" + lookAndFeel.getName()
					+ "\nSetting the default Look'n'Feel" + info.getName());
			try {
				UIManager.setLookAndFeel(info.getClassName());
				SwingUtilities.updateComponentTreeUI(this);
				this.lookAndFeel = info;
			} catch (Exception ex) {
			}
		}
		//MetalLookAndFeel.setCurrentTheme(new GanttMetalTheme());
		//must force to do that instead of the task on tree are not in
		// continuity of the calendar
		tree.getJTree().setRowHeight(20);
	}
	//Correct the label of menu without '$' character
	public static String correctLabel(String label) {
		int index = label.indexOf('$');
		if (index != -1 && label.length() - index > 1)
			label = label.substring(0, index).concat(label.substring(++index));
		return label;
	}
	/** Change the label for menu, in fact check in the label contains a mnemonic */
	public JMenu changeMenuLabel(JMenu menu, String label) {
		int index = label.indexOf('$');
		if (index != -1 && label.length() - index > 1) {
            menu.setText(label.substring(0, index).concat(label.substring(++index)));
			menu.setMnemonic(Character.toLowerCase(label.charAt(index)));
		} else {
			menu.setText(label);
			//menu.setMnemonic('');
		}
		return menu;
	}

    /** Change the label for menuItem, in fact check in the label contains a mnemonic */
	public JMenuItem changeMenuLabel(JMenuItem menu, String label) {
		int index = label.indexOf('$');
		if (index != -1 && label.length() - index > 1) {
            menu.setText(label.substring(0, index).concat(label.substring(++index)));
			menu.setMnemonic(Character.toLowerCase(label.charAt(index)));
		} else {
			menu.setText(label);
			//menu.setMnemonic('');
		}
		return menu;
	}
	/** Set the menus language after the user select a different language */
	private void changeLanguageOfMenu() {
		mProject = changeMenuLabel(mProject, language.getText("project"));
		mEdit = changeMenuLabel(mEdit, language.getText("edit"));
		mTask = changeMenuLabel(mTask, language.getText("task"));
		mHuman = changeMenuLabel(mHuman, language.getText("human"));
		mHelp = changeMenuLabel(mHelp, language.getText("help"));
		mCalendar = changeMenuLabel(mCalendar, language.getText("calendars"));
		miNew = changeMenuLabel(miNew, language.getText("newProject"));
		miOpen = changeMenuLabel(miOpen, language.getText("openProject"));
		mMRU = changeMenuLabel(mMRU, language.getText("lastOpen"));
		miSave = changeMenuLabel(miSave, language.getText("saveProject"));
		miSaveAs = changeMenuLabel(miSaveAs, language.getText("saveAsProject"));

        mServer = changeMenuLabel(mServer, language.getText("webServer"));
        miOpenURL = changeMenuLabel(miOpenURL, language.getText("openFromServer"));
        miSaveAsURL = changeMenuLabel(miSaveAsURL, language.getText("saveToServer"));

		miExport = changeMenuLabel(miExport, language.getText("export"));
		miImport = changeMenuLabel(miImport, language.getText("import"));
		miPrint = changeMenuLabel(miPrint, language.getText("printProject"));
		miPreview = changeMenuLabel(miPreview, language.getText("preview"));
		miQuit = changeMenuLabel(miQuit, language.getText("quit"));
		miCut = changeMenuLabel(miCut, language.getText("cut"));
		miCopy = changeMenuLabel(miCopy, language.getText("copy"));
		miPaste = changeMenuLabel(miPaste, language.getText("paste"));
		miOptions = changeMenuLabel(miOptions, language.getText("settings"));
		//miNewTask = changeMenuLabel(miNewTask, language.getText("createTask"));
        miDeleteTask = changeMenuLabel(miDeleteTask, language.getText("deleteTask"));
        miPropertiesTask = changeMenuLabel(miPropertiesTask, language.getText("propertiesTask"));

        mHuman.insert(changeMenuLabel(mHuman.getItem(0), language.getText("newHuman")), 0);
        miDelHuman = changeMenuLabel(miDelHuman, language.getText("deleteHuman"));
        miPropHuman = changeMenuLabel(miPropHuman, language.getText("propertiesHuman"));
        mHuman.insert(changeMenuLabel(mHuman.getItem(3), language.getText("importResources")), 3);
        miSendMailHuman = changeMenuLabel(miSendMailHuman, language.getText("sendMail"));

        miEditCalendar = changeMenuLabel(miEditCalendar, language.getText("editCalendars"));
        miPrjCal = changeMenuLabel(miPrjCal, language.getText("projectCalendar"));

		miWebPage = changeMenuLabel(miWebPage, language.getText("webPage"));
		miAbout = changeMenuLabel(miAbout, language.getText("about"));
		miTips = changeMenuLabel(miTips, language.getText("tipsOfTheDay"));
		miManual = changeMenuLabel(miManual, language.getText("manual"));
		////////////////////////////////////////////

        bNew.setToolTipText(getToolTip(correctLabel(language.getText("newProject"))));
        bOpen.setToolTipText(getToolTip(correctLabel(language.getText("openProject"))));
        bSave.setToolTipText(getToolTip(correctLabel(language.getText("saveProject"))));
        bSaveAs.setToolTipText(getToolTip(correctLabel(language.getText("saveAsProject"))));
        bPrint.setToolTipText(getToolTip(correctLabel(language.getText("printProject"))));
        bExport.setToolTipText(getToolTip(correctLabel(language.getText("export"))));
        bImport.setToolTipText(getToolTip(correctLabel(language.getText("import"))));
        bNewTask.setToolTipText(getToolTip(correctLabel(language.getText("createTask"))));
        bDelete.setToolTipText(getToolTip(correctLabel(language.getText("deleteTask"))));
        bProperties.setToolTipText(getToolTip(correctLabel(language.getText("propertiesTask"))));
        bUnlink.setToolTipText(getToolTip(correctLabel(language.getText("unlink"))));
		bLink.setToolTipText(getToolTip(correctLabel(language.getText("link"))));
        bInd.setToolTipText(getToolTip(correctLabel(language.getText("indentTask"))));
        bUnind.setToolTipText(getToolTip(correctLabel(language.getText("dedentTask"))));
        bUp.setToolTipText(getToolTip(correctLabel(language.getText("upTask"))));
        bDown.setToolTipText(getToolTip(correctLabel(language.getText("downTask"))));
        bPrev.setToolTipText(getToolTip(correctLabel(language.getText("backDate"))));
        bNext.setToolTipText(getToolTip(correctLabel(language.getText("forwardDate"))));
        bZoomIn.setToolTipText(getToolTip(correctLabel(language.getText("zoomIn"))));
        bZoomOut.setToolTipText(getToolTip(correctLabel(language.getText("zoomOut"))));
		//bZoomFit.setToolTipText(getToolTip(language.zoomFit()));
		tabpane.setTitleAt(1, correctLabel(language.getText("human")));
		
		setButtonText();
	}
	/** Return the tooltip in html (with yello bgcolor */
	public static String getToolTip(String msg) {
		return "<html><body bgcolor=#EAEAEA>" + msg + "</body></html>";
	}
	
	/** Set the text on the buttons.*/ 
	public void setButtonText()
	{
		if(options.getButtonShow()==GanttOptions.ICONS) {
			bNew.setText(""); bOpen.setText(""); bSave.setText("");
			bSaveAs.setText(""); bNewTask.setText("");
			bImport.setText(""); bExport.setText(""); bPrint.setText("");
			bDelete.setText(""); bProperties.setText(""); bUnlink.setText("");
			bLink.setText("");
			bInd.setText(""); bUnind.setText(""); bUp.setText("");
			bDown.setText(""); bPrev.setText(""); bNext.setText("");
			bZoomOut.setText(""); bZoomIn.setText("");
		} else {
			bNew.setText(correctLabel(language.getText("newProject"))); 
			bOpen.setText(correctLabel(language.getText("openProject"))); 
			bSave.setText(correctLabel(language.getText("saveProject")));
			bSaveAs.setText(correctLabel(language.getText("saveAsProject"))); 
			bImport.setText(correctLabel(language.getText("import"))); 
			bExport.setText(correctLabel(language.getText("export"))); 
			bPrint.setText(correctLabel(language.getText("printProject")));
			
			bNewTask.setText(correctLabel(language.getText(
					tabpane.getSelectedIndex() == 0?"createTask":"newHuman")));
			bDelete.setText(correctLabel(language.getText(
					tabpane.getSelectedIndex() == 0?"deleteTask":"deleteHuman"))); 
			bProperties.setText(correctLabel(language.getText(
					tabpane.getSelectedIndex() == 0?"propertiesTask":"propertiesHuman")));
			
			bUnlink.setText(correctLabel(language.getText("unlink")));
			bLink.setText(correctLabel(language.getText("link")));
			bInd.setText(correctLabel(language.getText("indentTask"))); 
			bUnind.setText(correctLabel(language.getText("dedentTask"))); 
			bUp.setText(correctLabel(language.getText("upTask")));
			bDown.setText(correctLabel(language.getText("downTask"))); 
			bPrev.setText(correctLabel(language.getText("backDate"))); 
			bNext.setText(correctLabel(language.getText("forwardDate")));
			bZoomOut.setText(correctLabel(language.getText("zoomOut"))); 
			bZoomIn.setText(correctLabel(language.getText("zoomIn")));
		}
	}
	
	/** Apply Buttons options. */
	public void applyButtonOptions()
	{
		setButtonText();
		if(options.getButtonShow()==GanttOptions.TEXT){
			//remove the icons
			bNew.setDefaultIcon(null); bOpen.setDefaultIcon(null);bSave.setDefaultIcon(null);
			bSaveAs.setDefaultIcon(null);bImport.setDefaultIcon(null);bExport.setDefaultIcon(null);
			bPrint.setDefaultIcon(null);bNewTask.setDefaultIcon(null);bDelete.setDefaultIcon(null);
			bProperties.setDefaultIcon(null);bUnlink.setDefaultIcon(null);
			bLink.setDefaultIcon(null);bInd.setDefaultIcon(null);
			bUnind.setDefaultIcon(null);bUp.setDefaultIcon(null);bDown.setDefaultIcon(null);
			bPrint.setDefaultIcon(null);bPrev.setDefaultIcon(null);bNext.setDefaultIcon(null);
			bZoomOut.setDefaultIcon(null);bZoomIn.setDefaultIcon(null);
		} else {
			//set the approrpiate icons
			bNew.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/new_"+options.getIconSize()+".gif")));
			bOpen.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/open_"+options.getIconSize()+".gif")));
			bSave.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/save_"+options.getIconSize()+".gif")));
			bSaveAs.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/saveas_"+options.getIconSize()+".gif")));
			bImport.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/import_"+options.getIconSize()+".gif")));
			bExport.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/export_"+options.getIconSize()+".gif")));
			bPrint.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/print_"+options.getIconSize()+".gif")));
			bNewTask.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/insert_"+options.getIconSize()+".gif")));
			bDelete.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/delete_"+options.getIconSize()+".gif")));
			bProperties.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/properties_"+options.getIconSize()+".gif")));
			bUnlink.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/unlink_"+options.getIconSize()+".gif")));		
			bLink.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/link_"+options.getIconSize()+".gif")));
			bInd.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/indent_"+options.getIconSize()+".gif")));
			bUnind.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/unindent_"+options.getIconSize()+".gif")));
			bUp.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/up_"+options.getIconSize()+".gif")));
			bDown.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/down_"+options.getIconSize()+".gif")));
			bPrev.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/prev_"+options.getIconSize()+".gif")));
			bNext.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/next_"+options.getIconSize()+".gif")));
			bZoomOut.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/zoomm_"+options.getIconSize()+".gif")));
			bZoomIn.setDefaultIcon(new ImageIcon(getClass().getResource("/icons/zoomp_"+options.getIconSize()+".gif")));
		}
				
	}
	
	/** Create the button on toolbar */
	public void addButtons(JToolBar toolBar) {
		//toolBar.addSeparator(new Dimension(20,0));
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
        bNew = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/new_"+options.getIconSize()+".gif")));
		bNew.addActionListener(this);
		toolBar.add(bNew);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
        bOpen = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/open_"+options.getIconSize()+".gif")));
		bOpen.addActionListener(this);
		toolBar.add(bOpen);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
        bSave = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/save_"+options.getIconSize()+".gif")));
		bSave.setEnabled(false);
		bSave.addActionListener(this);
		toolBar.add(bSave);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
        bSaveAs = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/saveas_"+options.getIconSize()+".gif")));
		bSaveAs.addActionListener(this);
		toolBar.add(bSaveAs);
///////////////////////////////////////////////////////////////////////////////////////////////////////////
        bImport = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/import_"+options.getIconSize()+".gif")));
		bImport.addActionListener(this);
		toolBar.add(bImport);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
        bExport = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/export_"+options.getIconSize()+".gif")));
		bExport.addActionListener(this);
		toolBar.add(bExport);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		bPrint = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/print_"+options.getIconSize()+".gif")));
		bPrint.addActionListener(this);
		toolBar.add(bPrint);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		toolBar.addSeparator(new Dimension(20,0));
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		//bNewTask = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/insert_"+options.getIconSize()+".gif")));
        myNewArtefactAction = new NewArtefactAction(new NewArtefactAction.ActiveActionProvider() {
			public AbstractAction getActiveAction() {
				return tabpane.getSelectedIndex()==0 ? 
                        (AbstractAction)myNewTaskAction : 
                        (AbstractAction)myNewHumanAction;
			}        	
        }, options.getIconSize());
		bNewTask = new TestGanttRolloverButton(myNewArtefactAction);
//		bNewTask.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				if (tabpane.getSelectedIndex() == 0) {//Gantt Chart
//					newTask();
//				} else if (tabpane.getSelectedIndex() == 1) { //Resource chart
//					HumanResource people = new HumanResource();
//					GanttDialogPerson dp = new GanttDialogPerson(
//							GanttProject.this, getLanguage(), people);
//					dp.show();
//					if (dp.result()) {
//						getHumanResourceManager().add(people);
//						setAskForSave(true);
//					}
//				}
//			}
//		});
		toolBar.add(bNewTask);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		bDelete = new TestGanttRolloverButton(new ImageIcon(getClass()
				.getResource("/icons/delete_"+options.getIconSize()+".gif")));
		bDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tabpane.getSelectedIndex() == 0) {//Gantt Chart
					//deleteTask();
					deleteTasks();
				} else if (tabpane.getSelectedIndex() == 1) { //Resource chart
					ProjectResource[] context = getResourcePanel().getContext()
							.getResources();
					if (context.length > 0) {
						GanttDialogInfo gdi = new GanttDialogInfo(
								GanttProject.this, GanttDialogInfo.QUESTION,
								GanttDialogInfo.YES_NO_OPTION, getLanguage()
										.getText("msg6")
										+ getDisplayName(context) + "??",
								getLanguage().getText("question"));
						gdi.show();
						if (gdi.res == GanttDialogInfo.YES) {
							for (int i = 0; i < context.length; i++) {
								getHumanResourceManager().remove(context[i]);
								refreshProjectInfos();
							}
						}
					}
				}
			}
		});
		toolBar.add(bDelete);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		bProperties = new TestGanttRolloverButton(new ImageIcon(getClass()
				.getResource("/icons/properties_"+options.getIconSize()+".gif")));
		bProperties.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tabpane.getSelectedIndex() == 0) {//Gantt Chart
					propertiesTask();
				} else if (tabpane.getSelectedIndex() == 1) { //Resource chart
					getResourcePanel().propertiesHuman(GanttProject.this);
				}
			}
		});
		toolBar.add(bProperties);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		bUnlink = new TestGanttRolloverButton(new ImageIcon(getClass()
				.getResource("/icons/unlink_"+options.getIconSize()+".gif")));
		bUnlink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tabpane.getSelectedIndex() == 0) {//Gantt Chart
					unlinkRelationships();
				}
			}
		});
		toolBar.add(bUnlink);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		bLink = new TestGanttRolloverButton(new ImageIcon(getClass()
				.getResource("/icons/link_"+options.getIconSize()+".gif")));
		bLink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tabpane.getSelectedIndex() == 0) {//Gantt Chart
					linkRelationships();
				}
			}
		});
		toolBar.add(bLink);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////		
		bInd = new TestGanttRolloverButton(new ImageIcon(getClass()
				.getResource("/icons/indent_"+options.getIconSize()+".gif")));
		bInd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tabpane.getSelectedIndex() == 0) {//Gantt Chart
					//tree.indentCurrentNode();
					tree.indentCurrentNodes();
					setAskForSave(true);
				}
			}
		});
		toolBar.add(bInd);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		bUnind = new TestGanttRolloverButton(new ImageIcon(getClass()
				.getResource("/icons/unindent_"+options.getIconSize()+".gif")));
		bUnind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tabpane.getSelectedIndex() == 0) {//Gantt Chart
					//tree.dedentCurrentNode();
					tree.dedentCurrentNodes();
					setAskForSave(true);
				}
			}
		});
		toolBar.add(bUnind);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		bUp = new TestGanttRolloverButton(new ImageIcon(getClass().getResource(
				"/icons/up_"+options.getIconSize()+".gif")));
		bUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tabpane.getSelectedIndex() == 0) {//Gantt Chart
					//tree.upCurrentNode();
					tree.upCurrentNodes();
					setAskForSave(true);
				} else if (tabpane.getSelectedIndex() == 1) { //Resource chart
					getResourcePanel().upResource();
					setAskForSave(true);
					getResourcePanel().setPeople(getResourcePanel().getPeople());
					getResourcePanel().area.repaint();
				}
			}
		});
		toolBar.add(bUp);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		bDown = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/down_"+options.getIconSize()+".gif")));
		bDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (tabpane.getSelectedIndex() == 0) { //Gantt Chart
					//tree.downCurrentNode();
					tree.downCurrentNodes();
					setAskForSave(true);
				} else if (tabpane.getSelectedIndex() == 1) { //Resource chart
					getResourcePanel().downResource();
					setAskForSave(true);
					getResourcePanel()
							.setPeople(getResourcePanel().getPeople());
					getResourcePanel().area.repaint();
				}
			}
		});
		toolBar.add(bDown);
		toolBar.addSeparator(new Dimension(20,0));
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		bPrev = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/prev_"+options.getIconSize()+".gif")));
		bPrev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				area.changeDate(false);
				area.repaint();
				getResourcePanel().area.changeDate(false);
				getResourcePanel().area.repaint();
			}
		});
		toolBar.add(bPrev);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		bNext = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/next_"+options.getIconSize()+".gif")));
		bNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				area.changeDate(true);
				area.repaint();
				getResourcePanel().area.changeDate(true);
				getResourcePanel().area.repaint();
			}
		});
		toolBar.add(bNext);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		bZoomOut = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/zoomm_"+options.getIconSize()+".gif")));
		bZoomOut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (area.getZoom() < 9) {
					area.zoomMore();
					getResourcePanel().area.zoomMore();
				}
				area.zoomToBegin();
				getResourcePanel().area.zoomToBegin();
				area.repaint();
				getResourcePanel().area.repaint();
				bZoomIn.setEnabled(true);
				bZoomOut.setEnabled(true);
				if (area.getZoom() == 9) {
					bZoomOut.setEnabled(false);
				}
			}
		});
		toolBar.add(bZoomOut);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		bZoomIn = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/zoomp_"+options.getIconSize()+".gif")));
		bZoomIn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (area.getZoom() > 0) {
					area.zoomLess();
					getResourcePanel().area.zoomLess();
				}
				area.zoomToBegin();
				getResourcePanel().area.zoomToBegin();
				area.repaint();
				getResourcePanel().area.repaint();
				bZoomIn.setEnabled(true);
				bZoomOut.setEnabled(true);
				if (area.getZoom() == 0) {
					bZoomIn.setEnabled(false);
				}
			}
		});
		toolBar.add(bZoomIn);
		///////////////////////////////////////////////////////////////////////////////////////////////////////////
		/*
		 * bZoomFit = new JButton (new
		 * ImageIcon(getClass().getResource("/icons/zoomf.gif")));
		 * bZoomFit.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent e) { area.fitWholeProject(false);
		 * area.repaint(); } }); toolBar.add(bZoomFit);
		 */
		
		applyButtonOptions();
	}
	private String getDisplayName(ProjectResource[] resources) {
		if (resources.length == 1) {
			return resources[0].toString();
		}
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < resources.length; i++) {
			result.append(resources[i].toString());
			if (i < resources.length - 1) {
				result.append(", ");
			}
		}
		return result.toString();
	}
	/** Exit the Application */
	private void exitForm(java.awt.event.WindowEvent evt) {
		quitApplication();
	}
	/**
	 * Check if the project has been modified, before creating a new one or open
	 * another
	 */
	private boolean checkCurrentProject() {
		GanttDialogInfo gdi = new GanttDialogInfo(this,
				GanttDialogInfo.WARNING, GanttDialogInfo.YES_NO_CANCEL_OPTION,
				language.getText("msg1"), language.getText("warning"));
		GanttDialogInfo gdiSaveError = new GanttDialogInfo(this,
				GanttDialogInfo.ERROR, GanttDialogInfo.YES_NO_CANCEL_OPTION,
				language.getText("msg12"), language.getText("error"));
		if (askForSave == true) {
			gdi.show();
			if (gdi.res == GanttDialogInfo.CANCEL)
				return false;
			if (gdi.res == GanttDialogInfo.YES) {
				boolean trySave = true;
				do {
					try {
						trySave = false;
						saveProject();
					} catch (Exception e) {
						System.err.println(e);
						gdiSaveError.show();
						if (gdiSaveError.res == GanttDialogInfo.CANCEL)
							return false;
						trySave = (gdiSaveError.res == GanttDialogInfo.YES);
					}
				} while (trySave);
			}
		}
		return true;
	}
	/** A menu has been activate */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() instanceof JMenuItem) {
			String arg = evt.getActionCommand();
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			if (arg.equals(correctLabel(language.getText("newProject")))) {
				newProject();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("openProject")))) {
				try {
					if (checkCurrentProject()) {
						openFile();
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (isVisible()) {
						GanttDialogInfo gdi = new GanttDialogInfo(this,
								GanttDialogInfo.ERROR,
								GanttDialogInfo.YES_OPTION, language
										.getText("msg8"), language
										.getText("error"));
						gdi.show();
					} else
						System.out.println("\n====" + language.getText("error")
								+ "====\n" + language.getText("msg8") + "\n");
				}
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("openFromServer")))) {
				if (checkCurrentProject()) {
					openURL();
				}
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("saveProject")))) {
				try {
					saveProject();
				} catch (Exception e) {
					System.err.println(e);
				}
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg
					.equals(correctLabel(language.getText("saveAsProject")))) {
				try {
					saveAsProject();
				} catch (Exception e) {
					System.err.println(e);
				}
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("saveToServer")))) {
				try {
					saveAsURLProject();
				} catch (Exception e) {
					System.err.println(e);
				}
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("export")))) {
				export();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("import")))) {
				importcbk();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("printProject")))) {
				printProject();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("preview")))) {
				previewPrint();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("quit")))) {
				quitApplication();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("deleteTask")))) {
				deleteTasks();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg
					.equals(correctLabel(language.getText("propertiesTask")))) {
				propertiesTask();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("upTask")))) {
				tree.upCurrentNodes();
				setAskForSave(true);
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("downTask")))) {
				tree.downCurrentNodes();
				setAskForSave(true);
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("indentTask")))) {
				tree.indentCurrentNodes();
				setAskForSave(true);
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("dedentTask")))) {
				tree.dedentCurrentNodes();
				setAskForSave(true);
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("unlink")))) {
				unlinkRelationships();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("link")))) {
				linkRelationships();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////			
			else if (arg.equals(correctLabel(language
					.getText("propertiesHuman")))) {
				tabpane.setSelectedIndex(1);
				getResourcePanel().propertiesHuman(this);
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg
					.equals(correctLabel(language.getText("editCalendars")))) {
				GanttDialogCalendar dialogCalendar = new GanttDialogCalendar(
						this);
				dialogCalendar.show();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language
					.getText("projectCalendar")))) {
				System.out.println("Project calendar");
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("webPage")))) {
				try {
					openWebPage();
				} catch (Exception e) {
					System.err.println(e);
				}
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("tipsOfTheDay")))) {
				TipsDialog tips = new TipsDialog(this, options.getOpenTips());
				tips.show();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("manual")))) {
				if (helpFrame == null) {
					try {
						helpFrame = new net.sourceforge.helpgui.gui.MainFrame(
								"/docs/help/", "eclipse");
						helpFrame.setTitle("GanttProject Manual");
						ImageIcon icon = new ImageIcon(getClass().getResource(
								"/icons/ganttproject.png"));
						helpFrame.setIconImage(icon.getImage());
					} catch (Exception e) {
					}
				}
				helpFrame.setVisible(true);
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("about")))) {
				//old dialog box
				/*AboutGanttProject agp = new AboutGanttProject(this);
				agp.show();*/
				
				//new dialog box
				AboutDialog agp = new AboutDialog(this);
				agp.show();
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("sendMail")))) {
				tabpane.setSelectedIndex(1);
				getResourcePanel().sendMail(this);
			}
			//Newly added code /CL
			////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("cut")))) {
				Task t = tree.getSelectedTask();
				if (t != null) {
					tree.cutSelectedNode();
					setAskForSave(true);
				}
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("copy")))) {
				tree.copySelectedNode();
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("paste")))) {
				tree.pasteNode();
				setAskForSave(true);
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (arg.equals(correctLabel(language.getText("settings")))) {
				launchOptionsDialog();
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//Test if it's a file name
		} else if (evt.getSource() instanceof Document) {
				if (checkCurrentProject())
					openStartupDocument((Document) evt.getSource());
			
		} ////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Test if it's buttons actions
		else if( evt.getSource() instanceof JButton) {
			if(evt.getSource() == bNew)					//new
				newProject();
			else if(evt.getSource() == bOpen) {			//open
				try {
					if (checkCurrentProject()) 
						openFile();
				} catch (Exception ex) {
					System.err.println(ex);
				}
			} else if(evt.getSource() == bSave) {		//save
				try {
					saveProject();
				} catch (Exception ex) {
					System.err.println(ex);
				}
			} else if(evt.getSource() == bSaveAs) {		//saveas
				try {
					saveAsProject();
				} catch (Exception ex) {
					System.err.println(ex);
				}
			} else if(evt.getSource() == bImport) {		//import
				importcbk();
			} else if(evt.getSource() == bExport) {		//export
				export();
			} else if(evt.getSource() == bPrint) { 		//print
				printProject();
			}
		}
	}
	/** Launch the options dialog */
	public void launchOptionsDialog() {
		
		// old options dialog box
		/*GanttDialogOptions dialogOptions = new GanttDialogOptions(this,
				myUIConfiguration);
		dialogOptions.show();
		if (dialogOptions.change) {
			setAskForSave(true);
		}*/
		
		// new options dialog box
		statusBar.setFirstText(language.getText("settingsPreferences"),2000);
		SettingsDialog dialogOptions = new SettingsDialog(this);
		dialogOptions.show();
		area.repaint();
	}
	/** Create a new task */
	public Task newTask() {
		tabpane.setSelectedIndex(0);
		GanttTask current = tree.getSelectedTask();
		GanttCalendar cal = new GanttCalendar();
		if (tree.hasTasks())
			cal = new GanttCalendar(area.beg);
		DefaultMutableTreeNode node = null;
		GanttLanguage lang = GanttLanguage.getInstance();
		String nameOfTask = options.getTaskNamePrefix(); //language.getText("newTask");
		if (current != null) {
			current.setMilestone(false);
			node = tree.getSelectedNode();
			cal = current.getStart();
			nameOfTask = current.toString();
		}
		GanttTask task = getTaskManager().createTask();
		task.setStart(cal);
		task.setLength(1);
		getTaskManager().registerTask(task);//create a new task in the tab
											// paneneed to register it
		task.setName(nameOfTask + "_" + task.getTaskID());
		task.setColor(area.getTaskColor());
		if (current != null) {
			if (current.colorDefined()) {
				task.setColor(current.getColor());
			}
			if (current.shapeDefined())
				task.setShape(current.getShape());
		}
		DefaultMutableTreeNode taskNode = tree.addObject(task, node);
		AdjustTaskBoundsAlgorithm alg = getTaskManager()
				.getAlgorithmCollection().getAdjustTaskBoundsAlgorithm();
		alg.run(task);
		RecalculateTaskCompletionPercentageAlgorithm alg2 = getTaskManager()
				.getAlgorithmCollection()
				.getRecalculateTaskCompletionPercentageAlgorithm();
		alg2.run(task);
		//refresh the differents tasks
		if (current != null) {
			tree.refreshAllChild(nameOfTask);
			//      DefaultMutableTreeNode father = tree.getSelectedNode();
			//      GanttTask taskFather = null;
			//For refresh all the parent task
			//      while (tree.getNode(task.getTaskID()).isRoot() == false) {
			//        father = tree.getFatherNode(task);
			//        tree.refreshAllChild(father.toString());
			//        taskFather = (GanttTask) father.getUserObject();
			//        taskFather.refreshDateAndAdvancement(tree);
			//        father.setUserObject(taskFather);
			//        task = taskFather;
			//      }
		}
		area.repaint();
		setAskForSave(true);
		statusBar.setFirstText(language.getText("createNewTask"),1000);
		if (options.getAutomatic()) {
			propertiesTask(taskNode);
		}
		return task;
	}
	
	/** Delete the currant task */
	public void deleteTasks() {
		tabpane.setSelectedIndex(0);

		
		DefaultMutableTreeNode [] cdmtn = tree.getSelectedNodes();
		if(cdmtn==null || cdmtn.length==0) {
			statusBar.setFirstText(language.getText("msg21"),2000);
			return;
		}
		
		GanttDialogInfo gdi = new GanttDialogInfo(this,
				GanttDialogInfo.QUESTION, GanttDialogInfo.YES_NO_OPTION,
				language.getText("msg19"),
				language.getText("question"));
		gdi.show();
		if (gdi.res == GanttDialogInfo.YES) {
			for(int i=0;i<cdmtn.length; i++)
			{
				if(cdmtn[i] != null)
				{
					Task ttask = (Task)(cdmtn[i].getUserObject());
					DefaultMutableTreeNode father = tree.getFatherNode(ttask);
					tree.removeCurrentNode();
					if(father!=null)
					{
						GanttTask taskFather = (GanttTask) father.getUserObject();						
						AdjustTaskBoundsAlgorithm alg = getTaskManager()
							.getAlgorithmCollection().getAdjustTaskBoundsAlgorithm();
						alg.run(taskFather);
						//	taskFather.refreshDateAndAdvancement(tree);
						father.setUserObject(taskFather);
					}
				}
			}
			refreshProjectInfos();
			area.repaint();
			getResourcePanel().area.repaint();
			setAskForSave(true);
		}
	}
	/** Edit task parameters */
	public void propertiesTask() {
		tabpane.setSelectedIndex(0);
		propertiesTask(tree.getSelectedNode());
	}
	/** Edit task parameters */
	public void propertiesTask(DefaultMutableTreeNode node) {
		if (node == null || node.isRoot()) {
			statusBar.setFirstText(language.getText("msg21"),2000);
			return;
		} else {
			statusBar.setFirstText(language.getText("editingParameters"),2000);
			GanttTask t = (GanttTask) (node.getUserObject());
			GanttDialogProperties pd = new GanttDialogProperties(this, tree,
					managerHash, t, area);
			pd.show();
			if (pd.change) {
				setAskForSave(true);
			}
		}
	}

	/** Unlink the relationships of the selected task */
	public void unlinkRelationships() {
		tabpane.setSelectedIndex(0);
		
		DefaultMutableTreeNode [] cdmtn = tree.getSelectedNodes();
		if(cdmtn==null) {
			statusBar.setFirstText(language.getText("msg21"),2000);
			return;
		}
		for(int i=0;i<cdmtn.length;i++)
		{
			if (cdmtn[i] != null && !cdmtn[i].isRoot()) {
				GanttTask t = (GanttTask)(cdmtn[i].getUserObject());
				t.unlink();
			}		
		}
		area.repaint();
		setAskForSave(true);
	}

	/** Link the selected Tasks */
	public void linkRelationships() {
		tabpane.setSelectedIndex(0);

		DefaultMutableTreeNode [] cdmtn = tree.getSelectedNodes();
		if(cdmtn==null) {
			statusBar.setFirstText(language.getText("msg21"),2000);
			return;
		}
		
		if(cdmtn.length<2) {
			statusBar.setFirstText(language.getText("msg22"),2000);
			return;
		}

		for(int i=0;i<cdmtn.length-1;i++)
		{
			if (cdmtn[i]!=null && cdmtn[i+1]!=null)
			{
				GanttTask firstTask = (GanttTask)(cdmtn[i].getUserObject());;
				GanttTask secondTask = (GanttTask)(cdmtn[i+1].getUserObject());
				try {
                    getTaskManager().getDependencyCollection().createDependency(secondTask, firstTask);
                } catch (TaskDependencyException e1) {
                    //e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
			}
		}
		area.repaint();
		setAskForSave(true);
	}
	
	

	/** Export the calendar on a png file */
	public void export() {
		ExportFileInfo info = selectExportFile(null);
		if (!info.equals(ExportFileInfo.EMPTY)) {
			doExport(info);
		}
	}
	/** Execute the export functions. */
	private void doExport(ExportFileInfo info) {
		switch (info.myFormat) {
			case ExportFileInfo.FORMAT_HTML :
				{
					statusBar.setFirstText(language.getText("htmlexport"),2000);
					GanttHTMLExport.save(info.myFile, 
							prjInfos,
							this, tree, area,
							getResourcePanel().area, info.myStorageOptions);
					break;
				}
			case ExportFileInfo.FORMAT_PNG :
				{
					statusBar.setFirstText(language.getText("pnglexport"),2000);
					String filename = info.myFile.toString();
					if (!filename.toUpperCase().endsWith(".PNG"))
						filename += ".png";
					if (tabpane.getSelectedIndex() == 0) {//Gantt Chart
						//area.fitWholeProject(true);
						GanttExportSettings bool = info.myStorageOptions;
						area.export(new File(filename), bool, "png");
					} else if (tabpane.getSelectedIndex() == 1) {//Resources
						GanttExportSettings bool = info.myStorageOptions;										 // Chart
						getResourcePanel().area.export(new File(filename),
								"png", bool);
					}
					break;
				}
			case ExportFileInfo.FORMAT_JPG :
				{
					statusBar.setFirstText(language.getText("jpgexport"),2000);
					String filename = info.myFile.toString();
					if (!filename.toUpperCase().endsWith(".JPG") && 
							!filename.toUpperCase().endsWith(".JPEG"))
						filename += ".jpg";
					if (tabpane.getSelectedIndex() == 0) {//Gantt Chart
						//area.fitWholeProject(true);
						GanttExportSettings bool = info.myStorageOptions;
						area.export(new File(filename), bool, "jpg");
					} else if (tabpane.getSelectedIndex() == 1) {//Resources
						GanttExportSettings bool = info.myStorageOptions;										 // Chart
						getResourcePanel().area.export(new File(filename),
								"jpg", bool);
					}
					break;
				}
			case ExportFileInfo.FORMAT_PDF :
				{
					try {
						Class.forName("org.apache.fop.apps.Driver")
								.newInstance();
						statusBar.setFirstText(language.getText("pdfexport"),2000);
						String filename = info.myFile.toString();
						if (!filename.toUpperCase().endsWith(".PDF"))
							filename += ".pdf";
						GanttPDFExport.save(new File(filename), 
								prjInfos,
								this, tree, area,
								getResourcePanel().area, info.myStorageOptions,
								getXslFo());
					} catch (Exception e) {
						//If not run in console mode
						if (isVisible()) {
							GanttDialogInfo gdi = new GanttDialogInfo(this,
									GanttDialogInfo.ERROR,
									GanttDialogInfo.YES_OPTION, language
											.getText("msg15"), language
											.getText("error"));
							gdi.show();
						}
						//Just show the message into console
						else
							System.out.println("\n===="
									+ language.getText("error") + "====\n"
									+ language.getText("msg15") + "\n");
					}
					break;
				}
			case ExportFileInfo.FORMAT_XFIG :
			{
//				show a message  on the status bar
				statusBar.setFirstText(language.getText("xfigexport"),2000);
				String filename = info.myFile.toString();
				if (!filename.toUpperCase().endsWith(".FIG"))
					filename += ".fig";

				GanttXFIGSaver saver = new GanttXFIGSaver(prjInfos, 
						tree, getResourcePanel(), area);
				try {					
					saver.save(new FileOutputStream(new File(filename)), version);
					//saver.save(System.out,version); //temporary write the file on standard output
				} catch (Exception e){}
				break;
			}
			
			case ExportFileInfo.FORMAT_CSV :
			{
				//show a message  on the status bar
				statusBar.setFirstText(language.getText("csvexport"),2000);
				String filename = info.myFile.toString();
				if (!filename.toUpperCase().endsWith(".CSV"))
					filename += ".csv";

				GanttCSVExport saver = new GanttCSVExport(prjInfos, 
						tree, getResourcePanel(), options.getCSVOptions());
				try {
					saver.save(new FileOutputStream(new File(filename)));					
					//saver.save(System.out);
				} catch (Exception e){}
				break;
			}
		}
	}

	/** Refresh the informations of the project on the status bar. */
	public void refreshProjectInfos()
	{
		if(tree.nbTasks==0 && resp.nbPeople()==0)
			statusBar.setSecondText("");
		else statusBar.setSecondText(correctLabel(language.getText("task"))+" : "+tree.nbTasks+"  "+
				correctLabel(language.getText("resources"))+" "+resp.nbPeople());
	}
	
	private ExportFileInfo selectExportFile(FileFilter currentFilter) {
		ExportFileInfo result = ExportFileInfo.EMPTY;
		FileFilter figFilter = new GanttXFIGFileFilter();
		FileFilter pngFilter = new GanttPNGFileFilter();
		FileFilter jpgFilter = new GanttJPGFileFilter();
		FileFilter csvFilter = new GanttCSVFilter();
		FileFilter pdfFilter = new GanttPDFFileFilter();
		FileFilter htmlFilter = new GanttHTMLFileFilter();
		JFileChooser fc = new JFileChooser(options.getWorkingDir());
		fc.addChoosableFileFilter(figFilter);
		fc.addChoosableFileFilter(pngFilter);
		fc.addChoosableFileFilter(jpgFilter);
		fc.addChoosableFileFilter(csvFilter);
		fc.addChoosableFileFilter(pdfFilter);
		fc.addChoosableFileFilter(htmlFilter);
		if(currentFilter!=null)
			fc.setFileFilter(currentFilter);

		//Remove the possibility to use a file filter for all files
		FileFilter[] filefilters = fc.getChoosableFileFilters();
		for(int i=0;i<filefilters.length;i++){
		 	if(filefilters[i]!=figFilter && filefilters[i]!=pngFilter &&
		 			filefilters[i]!=jpgFilter && filefilters[i]!=csvFilter && 
					filefilters[i]!=pdfFilter && filefilters[i]!=htmlFilter)
		 		System.out.println(fc.removeChoosableFileFilter(filefilters[i]));
		}
		
		int returnVal = fc.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) 
		{
			String filename = fc.getSelectedFile().toString();
			FileFilter selectedFilter = fc.getFileFilter();
			
			//if( the file exists, ask for overwriting
			if(new File(filename).exists())
			{
				GanttDialogInfo gdi = new GanttDialogInfo(this,
						GanttDialogInfo.WARNING, GanttDialogInfo.YES_NO_OPTION, filename+"\n"+language
								.getText("msg18"), language.getText("warning"));
			    gdi.show();
				if (gdi.res == GanttDialogInfo.NO) 
				   	return selectExportFile(selectedFilter);
			}
			
			int type = -1;
			if (selectedFilter.equals(htmlFilter)) {
				type = ExportFileInfo.FORMAT_HTML;
				if (!fc.getFileFilter().accept(new File(filename))) {
					filename += ".html";
				}
			} else if (selectedFilter.equals(pngFilter)) {
				type = ExportFileInfo.FORMAT_PNG;
			} else if (selectedFilter.equals(jpgFilter)) {
				type = ExportFileInfo.FORMAT_JPG;
			} else if (selectedFilter.equals(pdfFilter)) {
				type = ExportFileInfo.FORMAT_PDF;
			} else if (selectedFilter.equals(figFilter)) {
				type = ExportFileInfo.FORMAT_XFIG;
			} else if (selectedFilter.equals(csvFilter)) {
				type = ExportFileInfo.FORMAT_CSV;
			} else {
				statusBar.setFirstText("Unknown file filter has been selected : "+selectedFilter,2000);
				throw new RuntimeException(
						"Unknown file filter has been selected: "
							+ selectedFilter);
			}
			changeWorkingDirectory(new File(filename).getParent());
			File file = new File(filename);
			
			result = new ExportFileInfo(file, type, options.getExportSettings());				
			
		}
		return result;
	}
	
	/** Import function. */
	public void importcbk()
	{
	
//		Create a filechooser
		JFileChooser fc = new JFileChooser(options.getWorkingDir());
		FileFilter txtFilter = new GanttTXTFileFilter();
		FileFilter ganFilter = new GanttXMLFileFilter();
		fc.addChoosableFileFilter(ganFilter);
		fc.addChoosableFileFilter(txtFilter);
		
//		Remove the possibility to use a file filter for all files
		FileFilter[] filefilters = fc.getChoosableFileFilters();
		for(int i=0;i<filefilters.length;i++){
		 	if(filefilters[i]!=txtFilter && filefilters[i]!=ganFilter)
		 		fc.removeChoosableFileFilter(filefilters[i]);
		}
		
				
		int returnVal = fc.showOpenDialog(GanttProject.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			//openDocument(new FileDocument(fc.getSelectedFile()));

			FileFilter selectedFilter = fc.getFileFilter();
			
			//Choose a list of tasks from a txtFile.
			if (selectedFilter.equals(txtFilter)) {
			
				GanttTXTOpen opener = new GanttTXTOpen(tree, this,
					area, getTaskManager());
			
				boolean bMerge = true;

				if(projectDocument!=null || askForSave==true)
				{
					GanttDialogInfo gdi = new GanttDialogInfo(
							GanttProject.this, GanttDialogInfo.QUESTION,
							GanttDialogInfo.YES_NO_OPTION, getLanguage().getText("msg17"),
							getLanguage().getText("question"));
					gdi.show();
					bMerge = (gdi.res == GanttDialogInfo.YES);
				}	
					
				if (bMerge || (!bMerge && checkCurrentProject()))
				{	
					if(!bMerge)
						closeProject();				
				//		Open the tasks
					opener.load(fc.getSelectedFile());
							
					changeWorkingDirectory(fc.getSelectedFile().getParent());
					
					setAskForSave(true);
					
				} 
			} else if (selectedFilter.equals(ganFilter)) { //import an entire project inside the current
				
				Document document = new FileDocument(fc.getSelectedFile());
				
				try 
				{
					GanttXMLOpen opener = new GanttXMLOpen(tree, this,
						getResourcePanel(), area, getTaskManager(),true);
					ResourceManager hrManager = getHumanResourceManager();
					RoleManager roleManager = getRoleManager();
					ResourceTagHandler resourceHandler = new ResourceTagHandler(
						hrManager, roleManager);
					DependencyTagHandler dependencyHandler = new DependencyTagHandler(
						opener.getContext(), getTaskManager());
					AllocationTagHandler allocationHandler = new AllocationTagHandler(
						hrManager, getTaskManager());
					opener.addTagHandler(opener.getDefaultTagHandler());
					//opener.addTagHandler(resourceHandler);
					//opener.addTagHandler(dependencyHandler);
					//opener.addTagHandler(allocationHandler);
					//opener.addParsingListener(dependencyHandler);
					opener.load(document.getInputStream());
				} catch (IOException e)
				{
					GanttDialogInfo gdi = new GanttDialogInfo(this,
							GanttDialogInfo.ERROR, GanttDialogInfo.YES_OPTION,
							language.getText("msg2") + "\n"
									+ document.getDescription(), language
									.getText("error"));
					gdi.show();
				}
			}
		}
	}
	
	/** Print the project */
	public void printProject() {
		if (tabpane.getSelectedIndex() == 0) {//Gantt Chart
			GanttExportSettings bool = new GanttExportSettings();
			//GanttDialogExport gde = new GanttDialogExport(this, bool, language);
			//gde.show();
			//if (bool.ok) 
			//	area.printProject(bool.name, bool.percent, bool.depend);
			area.printProject(options.getExportSettings());
			
		} else if (tabpane.getSelectedIndex() == 1) {//Resources Chart
			getResourcePanel().area.printProject(options.getExportSettings());
		}
	}
	/** Preview the project before print */
	public void previewPrint() {
		GanttPreviewPrint preview = null;
		if (tabpane.getSelectedIndex() == 0) {//Gantt Chart
			preview = new GanttPreviewPrint(this, area.getChart(
					new GanttExportSettings()));
		} else if (tabpane.getSelectedIndex() == 1) {//Resources Chart
			preview = new GanttPreviewPrint(this, getResourcePanel().area
					.getChart(new GanttExportSettings()));
		}
		if (preview != null)
			preview.setVisible(true);
	}
	/** Create a new project */
	public void newProject() {
		if (checkCurrentProject()) {
			closeProject();
			statusBar.setFirstText(language.getText("newProject2"),1500);
			refreshProjectInfos();
		}
        prjInfos = new PrjInfos();
        showNewProjectWizard();
	}
    
    private void showNewProjectWizard() {
        NewProjectWizard wizard = new NewProjectWizard(this);        
        wizard.createNewProject(this);
        
    }
	/**
	 * Closes a project. Make sure you have already called checkCurrentProject()
	 * before.
	 * 
	 * @see #checkCurrentProject()
	 */
	private void closeProject() {
		//Clear the jtree
		//refresh graphic area
		area.repaint();
		area.setProjectLevelTaskColor(null);
		getResourcePanel().area.repaint();
		//reset people
		getResourcePanel().reset();
		getHumanResourceManager().clear();
		prjInfos = new PrjInfos();
		//GanttTask.resetMaxID();
		RoleManager.Access.getInstance().clear();
		if (null != projectDocument)
			projectDocument.releaseLock();
		projectDocument = null;
		//change title of the frame
		this.setTitle(language.getText("appliTitle"));
		setAskForSave(false);
		getTaskManager().clear();
		tree.clearTree();
	}
	/** Copy GanttGraphicArea parameters to Resource panel area */
	public void setResourcePanelToGraphicArea() {
		getResourcePanel().area.setZoom(area.getZoom());
		getResourcePanel().area.setDate(area.getDate());
		getResourcePanel().area.repaint();
	}
	/** Open a local project file with dialog box (JFileChooser) */
	public void openFile() throws IOException {
		//Create a filechooser
		JFileChooser fc = new JFileChooser(options.getWorkingDir());
		FileFilter ganttFilter = new GanttXMLFileFilter();
		fc.addChoosableFileFilter(ganttFilter);
		
		//Remove the possibility to use a file filter for all files
		FileFilter[] filefilters = fc.getChoosableFileFilters();
		for(int i=0;i<filefilters.length;i++){
		 	if(filefilters[i]!=ganttFilter)
		 		System.out.println(fc.removeChoosableFileFilter(filefilters[i]));
		}

		int returnVal = fc.showOpenDialog(GanttProject.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			openDocument(new FileDocument(fc.getSelectedFile()));
			changeWorkingDirectory(fc.getSelectedFile().getParent());
		}
	}
	/** Open a remote project file with dialog box (GanttURLChooser) */
	public void openURL() {
		openURL(projectDocument);
	}
	/** Open a remote project file with dialog box (GanttURLChooser) */
	public void openURL(Document lastDocument) {
		GanttURLChooser uc = new GanttURLChooser(this, true,
				(null != lastDocument) ? lastDocument.getURLPath() : null,
				(null != lastDocument) ? lastDocument.getUsername() : null,
				(null != lastDocument) ? lastDocument.getPassword() : null);
		uc.show();
		if (uc.change) {
			Document openDoc;
			if ((lastDocument instanceof AbstractURLDocument)
					&& uc.fileurl.equals(lastDocument.getURLPath())) {
				lastDocument.setUserInfo(uc.userName, uc.password);
				openDoc = lastDocument;
			} else {
				openDoc = DocumentCreator.createDocument(uc.fileurl,
						uc.userName, uc.password);
			}
			try {
				openDocument(openDoc);
			} catch (IOException e) {
				if (isVisible()) {
					GanttDialogInfo gdi = new GanttDialogInfo(this,
							GanttDialogInfo.ERROR, GanttDialogInfo.YES_OPTION,
							language.getText("msg11"), language
									.getText("error"));
					gdi.show();
				} else
					System.out.println("\n====" + language.getText("error")
							+ "====\n" + language.getText("msg11") + "\n");
				openURL(openDoc);
			}
		}
	}
	private void openDocument(Document document) throws IOException {
		if (document.getDescription().endsWith(".xml")
				|| document.getDescription().endsWith(".gan")) {
			InputStream inputStream = document.getInputStream();
			closeProject();
			boolean locked = document.acquireLock();
			if (!locked) {
				GanttDialogInfo gdi = new GanttDialogInfo(this,
						GanttDialogInfo.WARNING, GanttDialogInfo.YES_NO_OPTION,
						language.getText("msg13"), language.getText("warning"));
				gdi.show();
				if (gdi.res == GanttDialogInfo.NO)
					return;
			}
			GanttXMLOpen opener = new GanttXMLOpen(tree, this,
					getResourcePanel(), area, getTaskManager(),false);
			ResourceManager hrManager = getHumanResourceManager();
			RoleManager roleManager = getRoleManager();
			TaskManager taskManager = getTaskManager();
			ResourceTagHandler resourceHandler = new ResourceTagHandler(
					hrManager, roleManager);
			DependencyTagHandler dependencyHandler = new DependencyTagHandler(
					opener.getContext(), taskManager);
			AllocationTagHandler allocationHandler = new AllocationTagHandler(
					hrManager, getTaskManager());
			RoleTagHandler rolesHandler = new RoleTagHandler(roleManager);
			opener.addTagHandler(opener.getDefaultTagHandler());
			opener.addTagHandler(resourceHandler);
			opener.addTagHandler(dependencyHandler);
			opener.addTagHandler(allocationHandler);
			opener.addTagHandler(rolesHandler);
			opener.addParsingListener(dependencyHandler);
            opener.addParsingListener(resourceHandler);
			if (opener.load(document.getInputStream())) {
				
				//Add this project to the last opened project
				if (documentsMRU.add(document))
					updateMenuMRU();
				if (locked) {
					projectDocument = document;
					this.setTitle(language.getText("appliTitle") + " ["
							+ document.getDescription() + "]");
				}
				bZoomIn.setEnabled(area.getZoom() == 0 ? false : true);
				bZoomOut.setEnabled(area.getZoom() == 9 ? false : true);
				setResourcePanelToGraphicArea();
				
				setAskForSave(false);	
				statusBar.setFirstText(language.getText("opening") + " "+
						document.getPath(),2000);
			}
		} else {
			if (isVisible()) {
				GanttDialogInfo gdi = new GanttDialogInfo(this,
						GanttDialogInfo.ERROR, GanttDialogInfo.YES_OPTION,
						language.getText("msg2") + "\n"
								+ document.getDescription(), language
								.getText("error"));
				gdi.show();
			} else{
				System.out.println("\n====" + language.getText("error")
						+ "====\n" + language.getText("msg2") + "\n");
			}
		}
	}
	public void openStartupDocument(String path) {
		if (path != null) {
			Document document = DocumentCreator.createDocument(path);
			openStartupDocument(document);
		}
	}
	public void openStartupDocument(Document document) {
		try {
			openDocument(document);
		} catch (IOException ex) {
			if (document instanceof AbstractURLDocument) {
				// if there are problems opening an AbstractURLDocument
				// (possibly because of bad authentication),
				// give the user a chance, to enter correct credentials
				openURL(document);
			} else {
				GanttDialogInfo gdi = new GanttDialogInfo(this,
						GanttDialogInfo.ERROR, GanttDialogInfo.YES_OPTION,
						language.getText("msg2") + "\n"
								+ document.getDescription(), language
								.getText("error"));
				gdi.show();
			}
		}
	}
	/**
	 * Open a XML stream that represent the file //By CL !@#
	 * 
	 * @param ins
	 */
	public void openXMLStream(InputStream ins, String path) {
		try {
			tree.clearTree();
			getResourcePanel().reset();
			getHumanResourceManager().clear();
			//GanttTask.resetMaxID();
			getTaskManager().clear();
			RoleManager.Access.getInstance().clear();
			GanttXMLOpen opener = new GanttXMLOpen(tree, this,
					getResourcePanel(), area, getTaskManager(),false);
			ResourceManager hrManager = getHumanResourceManager();
			RoleManager roleManager = getRoleManager();
			ResourceTagHandler resourceHandler = new ResourceTagHandler(
					hrManager, roleManager);
			DependencyTagHandler dependencyHandler = new DependencyTagHandler(
					opener.getContext(), getTaskManager());
			AllocationTagHandler allocationHandler = new AllocationTagHandler(
					hrManager, getTaskManager());
			opener.addTagHandler(opener.getDefaultTagHandler());
			opener.addTagHandler(resourceHandler);
			opener.addTagHandler(dependencyHandler);
			opener.addTagHandler(allocationHandler);
			opener.addParsingListener(dependencyHandler);
			opener.load(ins);
			//addProjectFileToLastOpen(projectfile);
		} catch (Exception ex) {
			GanttDialogInfo gdi = new GanttDialogInfo(this,
					GanttDialogInfo.ERROR, GanttDialogInfo.YES_OPTION, language
							.getText("msg2")
							+ "\n" + path, language.getText("error"));
			gdi.show();
		}
		setAskForSave(false);
	}
	/** Save the project as (with a dialog file chooser) */
	public boolean saveAsProject() throws IOException {
		JFileChooser fc = new JFileChooser(options.getWorkingDir());
		FileFilter  ganttFilter = new GanttXMLFileFilter();
		fc.addChoosableFileFilter(ganttFilter);
		
		//Remove the possibility to use a file filter for all files
		FileFilter[] filefilters = fc.getChoosableFileFilters();
		for(int i=0;i<filefilters.length;i++){
		 	if(filefilters[i]!=ganttFilter)
		 		fc.removeChoosableFileFilter(filefilters[i]);
		}
		
		int returnVal = fc.showSaveDialog(GanttProject.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String projectfile = fc.getSelectedFile().toString();
			if (!fc.getFileFilter().accept(new File(projectfile))) {
				if (fc.getFileFilter().accept(new File(projectfile + ".gan"))) {
					projectfile += ".gan";
				}
			}
			
			//if( the file exists, ask for overwriting
			if(new File(projectfile).exists())
			{
				GanttDialogInfo gdi = new GanttDialogInfo(this,
					GanttDialogInfo.WARNING, GanttDialogInfo.YES_NO_OPTION, projectfile+"\n"+language
							.getText("msg18"), language.getText("warning"));
			    gdi.show();
			    if (gdi.res == GanttDialogInfo.NO) 
			    	saveAsProject();
			}

			saveProject(new FileDocument(new File(projectfile)));
			return true;
		}
		return false;
	}
	/** Save the project on a server (with a GanttURLChooser) */
	public boolean saveAsURLProject() throws IOException {
		return saveAsURLProject(projectDocument);
	}
	/** Save the project on a server (with a GanttURLChooser) */
	public boolean saveAsURLProject(Document document) throws IOException 
	{
		GanttURLChooser uc = new GanttURLChooser(this, false,
				(null != document) ? document.getURLPath() : null,
				(null != document) ? document.getUsername() : null,
				(null != document) ? document.getPassword() : null);
		uc.show();
		if (uc.change) {
			Document saveDocument = null;
			if (null != document)
				if (uc.fileurl.equals(document.getURLPath())
						&& uc.userName.equals(document.getUsername())
						&& uc.password.equals(document.getPassword()))
					saveDocument = document;
			if (null == saveDocument)
				saveDocument = DocumentCreator.createDocument(uc.fileurl,
						uc.userName, uc.password);
			saveProject(saveDocument);
			return true;
		}
		return false;
		
		/*ServerDialog sd = new ServerDialog(this);
		sd.show();		
		return false;*/
	}
	/** Save the project on a file */
	public void saveProject() throws IOException {
		saveProject(projectDocument);
	}
	/** Save the project to the given document, if possible */
	public void saveProject(Document document) throws IOException {
		if (null == document) {
			saveAsProject();
			return;
		}
		if (!document.canWrite()) {
			GanttDialogInfo gdi = new GanttDialogInfo(this,
					GanttDialogInfo.ERROR, GanttDialogInfo.YES_OPTION, language
							.getText("msg10"), language.getText("error"));
			gdi.show();
			if (document instanceof AbstractURLDocument) {
				saveAsURLProject(document);
			} else {
				saveAsProject();
			}
			return;
		}
		if (!document.acquireLock()) {
			GanttDialogInfo gdi = new GanttDialogInfo(this,
					GanttDialogInfo.ERROR, GanttDialogInfo.YES_OPTION, language
							.getText("msg14"), language.getText("error"));
			gdi.show();
			if (document instanceof AbstractURLDocument) {
				saveAsURLProject(document);
			} else {
				saveAsProject();
			}
			return;
		}
		if (document.getDescription().endsWith(".xml")
				|| document.getDescription().endsWith(".gan")) {
			GanttXMLSaver saver = new GanttXMLSaver((IGanttProject)this,
					tree, getResourcePanel(), area);
			saver.save(document.getOutputStream(), version);
			statusBar.setFirstText(GanttLanguage.getInstance().getText("saving")+" "+
					document.getPath(), 2000);
			
			//Add this project to the last opened projects
			if (documentsMRU.add(document))
				updateMenuMRU();
			if (projectDocument != document) {
				if(projectDocument!= null)
					projectDocument.releaseLock();
				projectDocument = document;
			}
		}
				
		//change title of the window
		this.setTitle(language.getText("appliTitle") + " ["
				+ document.getDescription() + "]");
		String filepath = document.getFilePath();
		if (null != filepath) {
			changeWorkingDirectory(new File(filepath).getParent());			
		}
		setAskForSave(false);
	}
	public void changeWorkingDirectory(String newWorkDir) {
		if (null != newWorkDir)
			options.setWorkingDirectory(newWorkDir);
	}
	
	
	/** @return the uiconfiguration. */
	public UIConfiguration getUIConfiguration() {
		return myUIConfiguration;
	}
	
	/** Function that launch the dialog to edit project properties */
	/*
	 * public void editSettings() { GanttDialogSettings ds = new
	 * GanttDialogSettings(this, language); ds.show(); if (ds.change) {
	 * setAskForSave(ds.change); } }
	 */
	/** Quit the application */
	public void quitApplication() {
		options.setWindowPosition(getX(), getY());
		options.setWindowSize(getWidth(), getHeight());
		options.setUIConfiguration(myUIConfiguration);
		options.setDocumentsMRU(documentsMRU);
		options.setLookAndFeel(lookAndFeel);
		options.setToolBarPosition(toolBar.getOrientation());
		options.save();
		if (checkCurrentProject()) {
			closeProject();
			setVisible(false);
			dispose();
			System.exit(0);
		} else {
			setVisible(true);
		}
	}
	/** Open the web page */
	public void openWebPage() throws IOException {
		if (!BrowserControl.displayURL("http://ganttproject.org/")) {
			GanttDialogInfo gdi = new GanttDialogInfo(this,
					GanttDialogInfo.ERROR, GanttDialogInfo.YES_OPTION, language
							.getText("msg4"), language.getText("error"));
			gdi.show();
			return;
		}
		statusBar.setFirstText(GanttLanguage.getInstance().getText("opening")+
				" www.ganttproject.org", 2000);
	}
	

	//change by G. Herrmann
	public void setAskForSave(boolean afs) {
		String title = getTitle();
		//String last = title.substring(title.length() - 11, title.length());
		bSave.setEnabled(afs);
		miSave.setEnabled(afs);
		askForSave = afs;
		try {
			if (System.getProperty("mrj.version") != null) {
				rootPane.putClientProperty("windowModified", Boolean
						.valueOf(afs));
				// see http://developer.apple.com/qa/qa2001/qa1146.html
			} else {
				if (askForSave) {
					/*if (!last.equals(" (modified)")) {
						setTitle(getTitle() + " (modified)");
					}*/
					
					if (!title.endsWith(" *")) {
						setTitle(getTitle() + " *");
					}
				}
			
			}
		} catch (AccessControlException e) {
			// This can happen when running in a sandbox (Java WebStart)
			System.err.println(e + ": " + e.getMessage());
		}
	}
	/** Print the help for ganttproject on the system.out */
	private static void usage() {
		System.out.println();
		System.out
				.println("GanttProject usage : java -jar ganttproject-(VERSION).jar <OPTIONS>");
		System.out.println();
		System.out.println("  Here are the possible options:");
		System.out.println("    -h, --help : Print this message");
		System.out
				.println("    [project_file_name] a XML file based on ganttproject format to directly open (project.xml or project.gan)");
		System.out
				.println("    -html [project_file_name] [export_directory_name], export directly a ganttproject file to web pages");
		System.out
				.println("         -xsl-dir [xsl_directory]                        localisation of the xsl directory for html export");
		System.out
				.println("    -pdf  [project_file_name] [pdf_file_name],         export directly a ganttproject file to web pages");
		System.out
				.println("         -xsl-fo [xsl_fo_file]                           localisation of the xsl-fo file for pdf export");
		System.out
				.println("    -csv  [project_file_name] [csv_image_filename],    export directly a ganttproject file to csv document compatible with spreadsheets");
		System.out		
				.println("    -png  [project_file_name] [png_image_filename],    export directly a ganttproject file to png image");
		System.out
				.println("    -jpg  [project_file_name] [jpg_image_filename],    export directly a ganttproject file to jpg image");
		System.out
				.println("    -fig/-xfig  [project_file_name] [fig_image_filename],    export directly a ganttproject file to xfig image");
		System.out.println();
		System.out
				.println("    In all these cases the project_file_name can either be a file on local disk or an URL.");
		System.out
				.println("    If the URL is password-protected, you can give credentials this way:");
		System.out
				.println("      http://username:password@example.com/filename");
		System.out.println(" ");
	}
	public GanttResourcePanel getResourcePanel() {
		if (this.resp == null) {
			this.resp = new GanttResourcePanel(this, getTree());
			this.resp.setResourceActions(getResourceActions()); //TODO pass
																// resource
																// actions
																// created with
																// resource
																// panel as
																// context
		}
		return this.resp;
	}
	private GanttLanguage getLanguage() {
		return this.language;
	}
	public GanttGraphicArea getArea() {
		return this.area;
	}
	private GanttTree getTree() {
		return this.tree;
	}
	private ResourceActionSet getResourceActions() {
		if (myResourceActions == null) {
			myResourceActions = new ResourceActionSet(
					(IGanttProject)this,
					(ResourceContext) getResourcePanel(), this);
		}
		return myResourceActions;
	}
	/** The main */
	public static void main(String[] arg) {
		if (arg.length > 0 && ("-h".equals(arg[0]) || "--help".equals(arg[0]))) {
			//Help for the command line
			usage();
			System.exit(0);
		}
		/*
		 * If -xsl-dir with directory has been provided, use the xsls in this
		 * dir instead of the ones from .ganttproject Can be used in both
		 * interacive and -html/-export modes From Pawel Lipinski.
		 * And set the new path as default.
		 */
        GanttOptions options = new GanttOptions(null);
		for (int i = 0; i < arg.length; i++) {
			if (arg[i].equals("-xsl-dir") && (arg.length > i + 1)) {
				options.setXslDir(arg[i + 1]);
				break;
			}
		}
		for (int i = 0; i < arg.length; i++) {
			if (arg[i].equals("-xsl-fo") && (arg.length > i + 1)) {
				options.setXslFo(arg[i + 1]);
				break;
			}
		}
		if (arg.length > 0
				&& ("-html".equals(arg[0]) || "-htm".equals(arg[0]) 
					|| "-export".equals(arg[0]))) {
			if (checkProjectFile(arg))
				exportProject(arg);
			System.exit(0);
		} else if (arg.length > 0 && "-pdf".equals(arg[0])) {
			if (checkProjectFile(arg))
				exportPDF(arg);
			System.exit(0);
		} else if (arg.length > 0 && "-png".equals(arg[0])) {
			if (checkProjectFile(arg))
				exportPNG(arg);
			System.exit(0);
		} else if (arg.length > 0 && "-jpg".equals(arg[0])) {
			if (checkProjectFile(arg))
				exportJPG(arg);
			System.exit(0);
		} else if (arg.length > 0 && ("-fig".equals(arg[0]) || "-xfig".equals(arg[0]))) {
			if (checkProjectFile(arg))
				exportXFIG(arg);
			System.exit(0);
		} else if (arg.length > 0 && ("-csv".equals(arg[0]))) {
			if (checkProjectFile(arg))
				exportCSV(arg);
			System.exit(0);
		}
		/* Splash image */
		GanttSplash splash = new GanttSplash();
		splash.setVisible(true);
		/** Create main frame */
		GanttProject ganttFrame = new GanttProject(null);
		ganttFrame.setVisible(true);
		if (arg.length > 0) {
			ganttFrame.openStartupDocument(arg[0]);
		}
        else {
        	//ganttFrame.showNewProjectWizard();
        }
		splash.close();
	}
	static boolean checkProjectFile(String[] arg) {
		if (!(arg.length > 1)) {
			System.out.println("ERROR...\nMust pass a project file");
			return false;
		}
		return true;
	}
	/**
	 * Possibility to export the project into HTML directly by command Line From
	 * Dmitry Barashev
	 */
	private static void exportProject(String[] args) {
		try {
			byCommandLine = true;
			GanttProject project = new GanttProject(args[1]);
			String exportPath;
			if (args.length > 2)
				exportPath = args[2];
			else
				exportPath = "htmlExport";
			File targetDir = new File(exportPath);
			if (!targetDir.exists()) {
				targetDir.mkdir();
			} else {
				if (!targetDir.isDirectory()) {
					throw new RuntimeException("File " + args[2]
							+ " must be directory");
				}
			}
			System.out.println(targetDir.getAbsolutePath());
			String index = project.getProjectName();
			if (index == null || index.length() == 0) {
				index = new String("ganttproject");
			} else {
				index = project.getProjectName().toLowerCase();
			}
			File targetFile = new File(targetDir, index + ".html");
			System.err.println(targetFile.getAbsolutePath());
			if (!targetFile.exists()) {
				targetFile.createNewFile();
			}
			ExportFileInfo info = new ExportFileInfo(targetFile,
					ExportFileInfo.FORMAT_HTML, new GanttExportSettings());
			project.doExport(info);
		} catch (IOException e) {
			throw new RuntimeException("IO error", e);
		}
	}
	/** Possibility to export the project into PDF directly by command Line */
	private static void exportPDF(String[] args) {
		byCommandLine = true;
		GanttProject project = new GanttProject(args[1]);
		String exportFile;
		if (args.length > 2)
			exportFile = args[2];
		else
			exportFile = "ganttproject.pdf";
		File targetFile = new File(exportFile);
		ExportFileInfo info = new ExportFileInfo(targetFile,
				ExportFileInfo.FORMAT_PDF, new GanttExportSettings());
		project.doExport(info);
	}
	/** Export directly in PNG image */
	private static void exportPNG(String[] args) {
		byCommandLine = true;
		GanttProject project = new GanttProject(args[1]);
		File targetFile = null;
		if (args.length > 2)
			targetFile = new File(args[2]);
		ExportFileInfo info = new ExportFileInfo(targetFile,
				ExportFileInfo.FORMAT_PNG, new GanttExportSettings());
		project.doExport(info);
	}
	/** Export directly in XFIG image */
	private static void exportXFIG(String[] args) {
		byCommandLine = true;
		GanttProject project = new GanttProject(args[1]);
		File targetFile = null;
		if (args.length > 2)
			targetFile = new File(args[2]);
		ExportFileInfo info = new ExportFileInfo(targetFile,
				ExportFileInfo.FORMAT_XFIG, new GanttExportSettings());
		project.doExport(info);
	}
	
	/** Export directly in CSV image */
	private static void exportCSV(String[] args) {
		byCommandLine = true;
		GanttProject project = new GanttProject(args[1]);
		File targetFile = null;
		if (args.length > 2)
			targetFile = new File(args[2]);
		ExportFileInfo info = new ExportFileInfo(targetFile,
				ExportFileInfo.FORMAT_CSV, new GanttExportSettings());
		project.doExport(info);
	}
	
	/** Export directly in JPG image */
	private static void exportJPG(String[] args) {
		byCommandLine = true;
		GanttProject project = new GanttProject(args[1]);
		File targetFile = null;
		if (args.length > 2)
			targetFile = new File(args[2]);
		ExportFileInfo info = new ExportFileInfo(targetFile,
				ExportFileInfo.FORMAT_JPG, new GanttExportSettings());
		project.doExport(info);
	}
	/**
	 * The class able to export directly by command line From Dmitry Barashev
	 */
	private static class ExportFileInfo {
		public final File myFile;
		public final int myFormat;
		public final GanttExportSettings myStorageOptions;
		public static final int FORMAT_HTML = 1;
		public static final int FORMAT_PNG = 2;
		public static final int FORMAT_JPG = 3;
		public static final int FORMAT_PDF = 4;
		public static final int FORMAT_XFIG = 5;
		public static final int FORMAT_CSV = 6;
		public static final ExportFileInfo EMPTY = new ExportFileInfo(null, -1,
				null);
		public ExportFileInfo(File file, int format, GanttExportSettings options) {
			myFile = file;
			myFormat = format;
			myStorageOptions = options;
		}
	}
	private static final String HUMAN_RESOURCE_MANAGER_ID = "HUMAN_RESOURCE";

    /////////////////////////////////////////////////////////
    // IGanttProject implementation
    public String getProjectName() {
        return prjInfos._sProjectName;
    }
    public void setProjectName(String projectName) {
        prjInfos._sProjectName = projectName;
        setAskForSave(true);
    }
    
    public String getDescription() {
        return prjInfos.getDescription();
    }
    public void setDescription(String description) {
        prjInfos._sDescription = description;
        setAskForSave(true);
    }
    public String getOrganization() {
        return prjInfos.getOrganization();
    }
    public void setOrganization(String organization) {
        prjInfos._sOrganization = organization;
        setAskForSave(true);
    }
    public String getWebLink() {
        return prjInfos.getWebLink();
    }
    public void setWebLink(String webLink) {
        prjInfos._sWebLink = webLink;
        setAskForSave(true);
    }   
    public ResourceManager getHumanResourceManager() {
        ResourceManager result = (ResourceManager) managerHash
                .get(HUMAN_RESOURCE_MANAGER_ID);
        if (result == null) {
            result = new HumanResourceManager();
            //result.addView(getPeople());
            result.addView(getResourcePanel());
            managerHash.put(HUMAN_RESOURCE_MANAGER_ID, result);
        }
        return result;
    }
    public TaskManager getTaskManager() {
        return myTaskManager;
    }
    public RoleManager getRoleManager() {
        return RoleManager.Access.getInstance();
    }
	/* (non-Javadoc)
	 * @see net.sourceforge.ganttproject.IGanttProject#getI18n()
	 */
	public GanttLanguage getI18n() {
		return getLanguage();
	}
}