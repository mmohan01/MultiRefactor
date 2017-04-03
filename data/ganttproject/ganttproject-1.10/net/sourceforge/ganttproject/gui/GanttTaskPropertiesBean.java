package net.sourceforge.ganttproject.gui;

/**
 *
 * <p>Title: </p>
 *
 * <p>Description: Provide the properties of selected task</p>
 *
 * <p>Copyright: Copyright (c) 2003</p>
 *
 * <p>Company: </p>
 *
 * @author ganttproject
 *
 * @version 1.0
 *
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;

import net.sourceforge.ganttproject.GanttCalendar;
import net.sourceforge.ganttproject.GanttGraphicArea;
import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.GanttTask;
import net.sourceforge.ganttproject.GanttTree;
import net.sourceforge.ganttproject.gui.taskproperties.TaskDependenciesPanel;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.shape.JPaintCombo;
import net.sourceforge.ganttproject.shape.ShapeConstants;
import net.sourceforge.ganttproject.shape.ShapePaint;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.util.BrowserControl;


/**
  *Real panel for editing task properties
  */
public class GanttTaskPropertiesBean

    extends JPanel {

  //Input attributes

  protected GanttTask selectedTask; //Task whose properties will be shown
  
  protected Vector savePredecessors;

  private GanttTree tree; //GanttTree that contain all the tasks information

  private GanttLanguage language = GanttLanguage.getInstance(); // language the panel will display

  private JDialog parent;

  //Output attributes: you can find the definition is GanttTask

//  private String name;

  private int length;

  private int percentComplete;

  private int priority;

  private GanttCalendar start;

  private GanttCalendar end;

  private boolean bilan;

  private String notes;

//  private Hashtable managerHash;
//
//  private Hashtable assignedResources = new Hashtable();
  //private attributes for internal use

  GridBagConstraints gbc = new GridBagConstraints();

  FlowLayout flowL = new FlowLayout(FlowLayout.LEFT, 10, 10);

  JTabbedPane tabbedPane; //TabbedPane that include the following four items

  JPanel generalPanel;

  JPanel predecessorsPanel;

  JPanel resourcesPanel;

  JPanel notesPanel;

  //Components on general pannel

  JPanel firstRowPanel1; //components in first row

  JTextField nameField1;

  JTextField durationField1;

  JLabel nameLabel1;

  JLabel durationLabel1;
  
  JLabel lblWebLink;
  JTextField tfWebLink;
  JButton bWebLink;

  JPanel secondRowPanel1; //components in second row

  JSpinner percentCompleteSlider;

  JLabel percentCompleteLabel1;

  JLabel priorityLabel1;

  JComboBox priorityComboBox;

  JPanel thirdRowPanel1; //componets in third row

  JTextField startDateField1;

  JTextField finishDateField1;

  JLabel startDateLabel1;

  JLabel finishDateLabel1;

  JButton startDateButton1;

  JButton finishDateButton1;

  JPanel lastRowPanel1; //components in last row
  JPanel webLinkPanel; //components in web link panel

  JLabel mileStoneLabel1;

  JCheckBox mileStoneCheckBox1;

  JButton colorButton;

  JButton colorSpace;

  JPanel colorPanel;
  
  /** Shape chooser combo Box */
  JPaintCombo shapeComboBox;

  //Components on predecessors pannel

  JLabel nameLabel2; //first row, here the textfield is un-editable

  JLabel durationLabel2;

  JTextField nameField2;

  //JTextField durationField2;

  JPanel firstRowPanel2;

  JScrollPane predecessorsScrollPane; //second row, a table

  JTable predecessorsTable;

  //Components on resourses pannel

  JLabel nameLabel3;

  JLabel durationLabel3;

  JTextField nameField3;

  JTextField durationField3;

  JPanel firstRowPanel3;

  JScrollPane resourcesScrollPane;

  JTable resourcesTable;

  //Components on notes pannel

  JLabel nameLabelNotes;

  JLabel durationLabelNotes;

  JTextField nameFieldNotes;

  JTextField durationFieldNotes;

  JScrollPane scrollPaneNotes;

  JTextArea noteAreaNotes;

  JPanel firstRowPanelNotes;

  JPanel secondRowPanelNotes;

  //Component on the SOUTH ok CANCEL buttons

  public JButton okButton;

  JButton cancelButton;

  JPanel southPanel;
    private ResourcesTableModel myResourcesTableModel;
    private TaskDependenciesPanel myDependenciesPanel;
    private boolean isStartFixed;

    /**add a component to container by using GridBagConstraints.*/

  private void addUsingGBL(Container container, Component component,

                           GridBagConstraints gbc, int x,

                           int y, int w, int h) {
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = w;
    gbc.gridheight = h;
    gbc.weighty = 0;
    container.add(component, gbc);
  }

  /**set the first row in all the tabbed pane. thus give them a common look*/

  private void setFirstRow(Container container, GridBagConstraints gbc,
                           JLabel nameLabel, JTextField nameField,
                           JLabel durationLabel, JTextField durationField) {
    container.setLayout(new GridBagLayout());
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets.right = 15;
    gbc.insets.left = 10;
    gbc.insets.top = 10;
    addUsingGBL(container, nameLabel, gbc, 0, 0, 1, 1);
    addUsingGBL(container, nameField, gbc, 1, 0, 1, 1);
    addUsingGBL(container, durationLabel, gbc, 2, 0, 1, 1);
    gbc.weightx = 1;
    addUsingGBL(container, durationField, gbc, 3, 0, 1, 1);
  }

  /**set resources editor for resources table*/
  private void setUpResourcesComboColumn(ArrayList users,
                                         final JTable resourceTable) {
    final JComboBox comboBox = new JComboBox();
    for (int i = 0; i < users.size(); i++) {
      comboBox.addItem(users.get(i));
    }

    TableColumn resourcesColumn = resourceTable.getColumnModel().getColumn(1);
    comboBox.setEditable(false);
    resourcesColumn.setCellEditor(new DefaultCellEditor(comboBox));
    comboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        HumanResource hr = (HumanResource) comboBox.getSelectedItem();
        if (hr != null && resourceTable.getEditingRow() != -1) {
          // if the resource is already selected we
          // cannot assign it again

          resourceTable.setValueAt(String.valueOf(hr.getId()), resourceTable.getEditingRow(), 0);
	  if(!(resourceTable.getValueAt(resourceTable.getEditingRow(),2) instanceof String))
		resourceTable.setValueAt("100", resourceTable.getEditingRow(), 2);
        }
      }
    });
  }

  /**Construct the general panel*/
  private void constructGeneralPanel() {
    generalPanel = new JPanel(new GridBagLayout());
    //first row
    nameLabel1 = new JLabel(language.getText("name") + ":");
    nameField1 = new JTextField(20);
    nameField1.setName("name_of_task");
    durationLabel1 = new JLabel(language.getText("length") + ":");
    durationField1 = new JTextField(8);
    durationField1.setName("length");
    firstRowPanel1 = new JPanel(flowL);
    setFirstRow(firstRowPanel1, gbc, nameLabel1, nameField1, durationLabel1,
                durationField1);
    //second row
    percentCompleteLabel1 = new JLabel(language.getText("advancement")); //Progress
    SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 100, 1);
    percentCompleteSlider = new JSpinner(spinnerModel);


    secondRowPanel1 = new JPanel(flowL);
    secondRowPanel1.add(percentCompleteLabel1);
    //secondRowPanel1.add(percentCompleteField1);
    secondRowPanel1.add(percentCompleteSlider);
    priorityLabel1 = new JLabel(language.getText("priority"));
    secondRowPanel1.add(priorityLabel1);
    priorityComboBox = new JComboBox();
    priorityComboBox.addItem(language.getText("low"));
    priorityComboBox.addItem(language.getText("normal"));
    priorityComboBox.addItem(language.getText("hight"));
    priorityComboBox.setEditable(false);

    secondRowPanel1.add(priorityComboBox);

    //third row

    startDateLabel1 = new JLabel(language.getText("dateOfBegining") + ":");
    startDateField1 = new JTextField(12);
    startDateField1.setEditable(false);
    finishDateLabel1 = new JLabel(language.getText("dateOfEnd") + ":");

    finishDateField1 = new JTextField(12);
    finishDateField1.setEditable(false);

    ImageIcon icon = new ImageIcon(getClass().getResource("/icons/calendar_16.gif"));

    startDateButton1 = new TestGanttRolloverButton(icon);
    startDateButton1.setName("start");
	startDateButton1.setToolTipText(GanttProject.getToolTip(language.getText("chooseDate")));
    finishDateButton1 = new TestGanttRolloverButton(icon);
    finishDateButton1.setName("finish");
	finishDateButton1.setToolTipText(GanttProject.getToolTip(language.getText("chooseDate")));
    thirdRowPanel1 = new JPanel(flowL);
    thirdRowPanel1.setBorder(new TitledBorder(new EtchedBorder(), language.getText("date")));
    thirdRowPanel1.add(startDateLabel1);
    thirdRowPanel1.add(startDateField1);
    thirdRowPanel1.add(startDateButton1);
    thirdRowPanel1.add(finishDateLabel1);
    thirdRowPanel1.add(finishDateField1);
    thirdRowPanel1.add(finishDateButton1);

    //fourth row

    mileStoneCheckBox1 = new JCheckBox(language.getText("meetingPoint")); //Milestone
    lastRowPanel1 = new JPanel(flowL);
    lastRowPanel1.add(mileStoneCheckBox1);
    
    shapeComboBox = new JPaintCombo(ShapeConstants.PATTERN_LIST);


    JPanel shapePanel = new JPanel();
    shapePanel.setLayout(new BorderLayout());
    JLabel lshape = new JLabel("  "+language.getText("shape")+" ");
    shapeComboBox = new JPaintCombo(ShapeConstants.PATTERN_LIST);

    shapePanel.add(lshape, BorderLayout.WEST);
    shapePanel.add(shapeComboBox, BorderLayout.CENTER);
    
    
    colorButton = new JButton(language.getText("colorButton"));
    colorButton.setBackground(selectedTask.getColor());
    final String colorChooserTitle = language.getText("selectColor");
    colorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JDialog dialog;
        dialog = JColorChooser.createDialog(parent, colorChooserTitle, true,
                                            GanttDialogProperties.colorChooser,
                                            new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            colorButton.setBackground(GanttDialogProperties.colorChooser.
                                      getColor());
          }
        }

        , new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            // nothing to do for "Cancel"
          }
        });

		/*AbstractColorChooserPanel[] panels = GanttDialogProperties.colorChooser.getChooserPanels();
        GanttDialogProperties.colorChooser.removeChooserPanel(panels[0]);
        GanttDialogProperties.colorChooser.addChooserPanel(panels[0]);*/
        
        GanttDialogProperties.colorChooser.setColor(colorButton.getBackground());        
        dialog.show();
      }
    });

    colorSpace = new JButton(language.getText("defaultColor"));
    colorSpace.setBackground(GanttGraphicArea.taskDefaultColor);
    colorSpace.setToolTipText(GanttProject.getToolTip(language.getText("resetColor")));
    colorSpace.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        colorButton.setBackground(GanttGraphicArea.taskDefaultColor);
      }
    });

    colorPanel = new JPanel();
    colorPanel.setLayout(new BorderLayout());
    colorPanel.add(colorButton, "West");
    colorPanel.add(colorSpace, "Center");
    colorPanel.add( shapePanel, BorderLayout.EAST);
    lastRowPanel1.add(colorPanel);

    //---Set GridBagConstraints constant
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets.right = 15;
    gbc.insets.left = 10;
    gbc.insets.top = 10;
    addUsingGBL(generalPanel, firstRowPanel1, gbc, 0, 0, 1, 1);
    addUsingGBL(generalPanel, secondRowPanel1, gbc, 0, 1, 1, 1);
    addUsingGBL(generalPanel, thirdRowPanel1, gbc, 0, 2, 1, 1);
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 1;
    gbc.gridheight = GridBagConstraints.RELATIVE;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.weighty = 1;
    generalPanel.add(lastRowPanel1, gbc);

    //The panel for the web link
    webLinkPanel = new JPanel(flowL);
    lblWebLink = new JLabel(language.getText("webLink"));
    webLinkPanel.add(lblWebLink);
    tfWebLink = new JTextField(30);
    webLinkPanel.add(tfWebLink);
    bWebLink = new TestGanttRolloverButton(
    			new ImageIcon(getClass().getResource("/icons/web_16.gif")));
    bWebLink.setToolTipText(GanttProject.getToolTip(language.getText("openWebLink")));
    webLinkPanel.add(bWebLink);
    
    bWebLink.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			//link to open the web link
			try{
				if (!BrowserControl.displayURL(tfWebLink.getText())) {
					GanttDialogInfo gdi = new GanttDialogInfo(null,
						GanttDialogInfo.ERROR, GanttDialogInfo.YES_OPTION, 
						language.getText("msg4"), 
						language.getText("error"));							
					gdi.show();
				}
			} catch (Exception ex) {
			}
		}
	});
    
    gbc.gridy = 4;
    generalPanel.add(webLinkPanel, gbc);
    
  }

  /** Add the differents action listener on the differents widgets */
  public void addActionListener(ActionListener l) {

    nameField1.addActionListener(l);
    
    startDateButton1.addActionListener(l);

    finishDateButton1.addActionListener(l);

    okButton.addActionListener(l);

    cancelButton.addActionListener(l);

    durationField1.addActionListener(l);

  }
  
  /** Change the name of the task on all textfiled of task name */
  public void changeNameOfTask() {
  	if(nameField1!=null && nameField2!=null && nameField3!=null && nameFieldNotes!=null) {
		String nameOfTask = nameField1.getText().trim();
	  	nameField1.setText(nameOfTask);
		nameField2.setText(nameOfTask);
		nameField3.setText(nameOfTask);
		nameFieldNotes.setText(nameOfTask);
	}
  }

  /**Construct the predecessors tabbed pane*/

  private void constructPredecessorsPanel() {
      myDependenciesPanel = new TaskDependenciesPanel(selectedTask);
    predecessorsPanel = myDependenciesPanel.getComponent();
      predecessorsTable = myDependenciesPanel.getTable();
  }

  /**Construct the resources panel*/

  private void constructResourcesPanel(ArrayList users) {

    resourcesPanel = new JPanel(new GridBagLayout());

    //first row

    nameLabel3 = new JLabel(language.getText("name") + ":");

    nameField3 = new JTextField(20);

    durationLabel3 = new JLabel(language.getText("length") + ":");

    durationField3 = new JTextField(8);

    nameField3.setEditable(false);

    durationField3.setEditable(false);

    firstRowPanel3 = new JPanel(flowL);

    setFirstRow(firstRowPanel3, gbc, nameLabel3, nameField3, durationLabel3,

                durationField3);

    //Second row

      myResourcesTableModel = new ResourcesTableModel(
              selectedTask.getAssignmentCollection());

    resourcesTable = new JTable(myResourcesTableModel);

    resourcesTable.setPreferredScrollableViewportSize(new Dimension(500, 130));

    setUpResourcesComboColumn(users, resourcesTable); //set column editor

    //set column size

    resourcesTable.setRowHeight(23);

    resourcesTable.getColumnModel().getColumn(0).setPreferredWidth(10);

    resourcesTable.getColumnModel().getColumn(1).setPreferredWidth(240);

    resourcesTable.getColumnModel().getColumn(2).setPreferredWidth(60);

    resourcesScrollPane = new JScrollPane(resourcesTable);
	
	JPanel secondResourcesScrollPane=new JPanel();
	secondResourcesScrollPane.setBorder(new TitledBorder(new EtchedBorder(),
        GanttProject.correctLabel(language.getText("human"))));
	secondResourcesScrollPane.add(resourcesScrollPane);
	
	
	JButton bremove=new TestGanttRolloverButton(new ImageIcon(getClass().getResource(
        "/icons/delete_16.gif")));
	bremove.setToolTipText(GanttProject.getToolTip(language.getText("removeResources")));
	bremove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        int [] selectedRow=resourcesTable.getSelectedRows();
		for(int i=0;i<selectedRow.length;++i) {
			//resourcesTable.getModel().setValueAt("",selectedRow[i],0);
			resourcesTable.getModel().setValueAt(null,selectedRow[i],1);
			//resourcesTable.getModel().setValueAt("",selectedRow[i],2);
		}
      }
    });
	secondResourcesScrollPane.add(bremove);
	
	gbc.anchor = GridBagConstraints.WEST;

    gbc.insets.right = 15;

    gbc.insets.left = 10;

    gbc.insets.top = 10;

    gbc.weighty = 0;

    addUsingGBL(resourcesPanel, firstRowPanel3, gbc, 0, 0, 1, 1);

    gbc.gridx = 0;

    gbc.gridy = 1;

    gbc.gridwidth = 8;

    gbc.gridheight = 1;

    gbc.weighty = 1;

    resourcesPanel.add(secondResourcesScrollPane, gbc);
  }

  /**construct the notes pannel*/

  private void constructNotesPanel() {

    notesPanel = new JPanel(new GridBagLayout());

    //first row

    nameLabelNotes = new JLabel(language.getText("name") + ":");

    nameFieldNotes = new JTextField(20);

    durationLabelNotes = new JLabel(language.getText("length") + ":");

    durationFieldNotes = new JTextField(8);

    nameFieldNotes.setEditable(false);

    durationFieldNotes.setEditable(false);

    firstRowPanelNotes = new JPanel();

    setFirstRow(firstRowPanelNotes, gbc, nameLabelNotes, nameFieldNotes,

                durationLabelNotes, durationFieldNotes);

    secondRowPanelNotes = new JPanel();

    secondRowPanelNotes.setBorder(new TitledBorder(new EtchedBorder(),
        language.getText("notesTask") + ":"));

    noteAreaNotes = new JTextArea(8, 40);
    noteAreaNotes.setLineWrap(true);
    noteAreaNotes.setWrapStyleWord(true);
    noteAreaNotes.setBackground(new Color(1.0f, 1.0f, 1.0f));

    scrollPaneNotes = new JScrollPane(noteAreaNotes);

    secondRowPanelNotes.add(scrollPaneNotes);

    JButton bdate = new TestGanttRolloverButton(new ImageIcon(getClass().getResource(
        "/icons/clock_16.gif")));
    bdate.setToolTipText(GanttProject.getToolTip(language.getText("putDate")));
    bdate.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        noteAreaNotes.append("\n"+ GanttCalendar.getDateAndTime() +"\n");
      }
    });
    secondRowPanelNotes.add(bdate);

    gbc.anchor = GridBagConstraints.WEST;

    gbc.insets.right = 15;

    gbc.insets.left = 10;

    gbc.insets.top = 10;

    gbc.weighty = 0;

    addUsingGBL(notesPanel, firstRowPanelNotes, gbc, 0, 0, 1, 1);

    gbc.weighty = 1;

    gbc.gridx = 0;

    gbc.gridy = 1;

    gbc.gridwidth = 1;

    gbc.gridheight = 1;

    notesPanel.add(secondRowPanelNotes, gbc);

  }

  /**Construct the south panel*/

  private void constructSouthPanel() {

    okButton = new JButton(language.getText("ok"));

    okButton.setName("ok");

    if(getRootPane() != null)
    	getRootPane().setDefaultButton(okButton); //set ok the defuault button when press "enter"  --> check because getRootPane()==null !!!
    
    
    
    cancelButton = new JButton(language.getText("cancel"));

    cancelButton.setName("cancel");

    southPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 50, 10));

    southPanel.add(okButton);

    southPanel.add(cancelButton);

  }

  /** Constructor */
  public GanttTaskPropertiesBean(JDialog parent, GanttTask selectedTask,
                                 GanttTree tree,
                                 Hashtable managerHash) {

    this.parent = parent;

    this.selectedTask = selectedTask;
	savePredecessors=selectedTask.getPredecessorsOld();
    HumanResourceManager hr = (HumanResourceManager) managerHash.get(
        "HUMAN_RESOURCE");
    init(hr.getResources());
    //this.managerHash = managerHash;

    setSelectedTask(selectedTask);

    setTree(tree);
    
    

    //set predecessor comboBox

    //setUpPredecessorComboColumn(predecessorsTable.getColumnModel().getColumn(1), predecessorsTable); //set column editor

    //setUpTypeComboColumn(predecessorsTable.getColumnModel().getColumn(2)); //set column editor

    //set resources comboBox

    /*      setUpResourcesComboColumn(((ResourceManager)managerHash.get("HUMAN_RESOURCE")).getResources(),
            resourcesTable.getColumnModel().getColumn(0)); //set column editor
     */

    /*
         tabbedPane = new JTabbedPane();
         constructGeneralPanel();
         tabbedPane.add("General", generalPanel);
         constructPredecessorsPanel();
         tabbedPane.add("Predecessors", predecessorsPanel);
         constructResourcesPanel();
         tabbedPane.add("Resources", resourcesPanel);
         constructNotesPanel();
         tabbedPane.add("Notes", notesPanel);
         add(tabbedPane);*/

  }

  /** Init the widgets */
  public void init(ArrayList users) {

    tabbedPane = new JTabbedPane();
    tabbedPane.addChangeListener(new ChangeListener() {
    	public void stateChanged(ChangeEvent e) {
		changeNameOfTask();
	}
    });
    constructGeneralPanel();

    tabbedPane.addTab(language.getText("general"), 
    		new ImageIcon(getClass().getResource("/icons/properties_16.gif")),generalPanel);

    constructPredecessorsPanel();

    tabbedPane.addTab(language.getText("predecessors"), 
    		new ImageIcon(getClass().getResource("/icons/relashion.gif")),predecessorsPanel);

    constructResourcesPanel(users);

    tabbedPane.addTab(GanttProject.correctLabel(language.getText("human")), 
    		new ImageIcon(getClass().getResource("/icons/res_16.gif")),resourcesPanel);

    constructNotesPanel();

    tabbedPane.addTab(language.getText("notesTask"), 
    		new ImageIcon(getClass().getResource("/icons/note_16.gif")),notesPanel);
    
    setLayout(new BorderLayout());

    add(tabbedPane, BorderLayout.CENTER);

    constructSouthPanel();

    add(southPanel, BorderLayout.SOUTH);

  }

  
  //Input methods

  /**as the name indicated, it will not replace the original GanttTask in the Tree. */

  public Task getReturnTask() {
      myResourcesTableModel.commit();
    GanttTask returnTask = selectedTask;
    //returnTask.setTaskID(selectedTask.getTaskID());
    returnTask.setName(getTaskName()); //getName()
    returnTask.setWebLink(getWebLink()); //getName()
    returnTask.setMilestone(isBilan());
    returnTask.setChecked(false);
    returnTask.setStart(getStart());
    returnTask.setEnd(getEnd());
    returnTask.setLength(getLength());
    returnTask.setNotes(getNotes());
    returnTask.setCompletionPercentage(getPercentComplete());
    returnTask.setPriority(getPriority());
      returnTask.setStartFixed(isStartFixed);
    returnTask.setColor(colorButton.getBackground());
    if(shapeComboBox.getSelectedIndex()!=0) //only if it's not the default shape
    	returnTask.setShape(new ShapePaint((ShapePaint)shapeComboBox.getSelectedPaint(), Color.white, colorButton.getBackground()));
      myDependenciesPanel.getTableModel().commit();

    return returnTask;

  }

  /**as the name indicated*/

  public void setSelectedTask(GanttTask selectedTask) {

      this.selectedTask = selectedTask;

      nameField1.setText(selectedTask.getName());

      //nameField2.setText(selectedTask.toString());

      nameField3.setText(selectedTask.getName());

      nameFieldNotes.setText(selectedTask.toString());

      setName(selectedTask.toString());

      durationField1.setText(selectedTask.getLength() + "");

      //durationField2.setText(selectedTask.getLength() + "");

      durationField3.setText(selectedTask.getLength() + "");

      durationFieldNotes.setText(selectedTask.getLength() + "");

      percentCompleteSlider.setValue(new Integer(selectedTask.getCompletionPercentage()));
      percentCompleteLabel1.setText(language.getText("advancement"));

      priorityComboBox.setSelectedIndex(selectedTask.getPriority());

      startDateField1.setText(selectedTask.getStart().toString());

      finishDateField1.setText(selectedTask.getEnd().toString());

      setStart(selectedTask.getStart().Clone(), true);

      setEnd(selectedTask.getEnd().Clone(), true);

      bilan = selectedTask.isMilestone();

      mileStoneCheckBox1.setSelected(bilan);

      tfWebLink.setText(selectedTask.getWebLink());
      
      if (selectedTask.shapeDefined()) {
          for (int i = 0; i < ShapeConstants.PATTERN_LIST.length; i++) {
              if (selectedTask.getShape().equals(ShapeConstants.PATTERN_LIST[i])) {
                  shapeComboBox.setSelectedIndex(i);
                  break;
              }
          }
      }

      noteAreaNotes.setText(selectedTask.getNotes());
      setStartFixed(selectedTask.isStartFixed());
  }

  /**as the name indicated*/

  public void setTree(GanttTree tree) {

    this.tree = tree;

  }

  //Output methods

  /**as the name indicated*/

  public boolean isBilan() {

    bilan = mileStoneCheckBox1.isSelected();

    return bilan;

  }

  /**as the name indicated*/

  public GanttCalendar getEnd() {

    return end;

  }

  /**as the name indicated*/

  public int getLength() {

    length = Integer.parseInt(durationField1.getText().trim());

    return length;

  }

  /** Set the duration of the task */
  public void changeLength(int _length) {

		if(_length<=0) _length=1;
    
		durationField1.setText(_length + "");
    //durationField2.setText(_length + "");
    durationField3.setText(_length + "");
    durationFieldNotes.setText(_length + "");

    length = _length;

    //change the end date
    GanttCalendar _end = start.newAdd(length);
    this.end = _end;
    finishDateField1.setText(_end.toString());

  }

  /**as the name indicated*/

  public String getNotes() {

    notes = noteAreaNotes.getText();

    return notes;

  }

  /** Return the name of the task*/

  public String getTaskName() {
    String text = nameField1.getText();
    return text == null ? "" : text.trim();
  }
  /** @return the web link of the task. */
  public String getWebLink() {
    String text = tfWebLink.getText();
    return text == null ? "" : text.trim();
  }
  

  /**as the name indicated*/

  public int getPercentComplete() {

    percentComplete = ((Integer)percentCompleteSlider.getValue()).hashCode();

    return percentComplete;

  }

  /** Return the priority level of the task */

  public int getPriority() {
    priority = priorityComboBox.getSelectedIndex();
    return priority;
  }


    public void setStartFixed(boolean startFixed) {
        isStartFixed = startFixed;
        startDateField1.setForeground(isStartFixed ? Color.BLACK : Color.GRAY);
    }

    /** Return the start date of the task */
  public GanttCalendar getStart() {
      start.setFixed(isStartFixed);
    return start;

  }

  /** Change the start date of the task */
  public void setStart(GanttCalendar dstart, boolean test) {

      if (test == true) {
          startDateField1.setText(dstart.toString());
          this.start = dstart;
          return;
      }

      startDateField1.setText(dstart.toString());
      this.start = dstart;
      this.setStartFixed(dstart.isFixed());

      if (this.start.compareTo(this.end) < 0)
          length = start.diff(end);
      else {
          GanttCalendar _end = start.newAdd(length);
          this.end = _end;
          finishDateField1.setText(_end.toString());
      }

      durationField1.setText("" + length);
      //durationField2.setText(""+length);
      durationField3.setText("" + length);
      durationFieldNotes.setText("" + length);
  }

  /** Change the end date of the task */
  public void setEnd(GanttCalendar dend, boolean test) {

    if(test==true)
		{
			finishDateField1.setText(dend.toString());
    this.end = dend;
			return;
		}
		
		finishDateField1.setText(dend.toString());
    this.end = dend;
		
		if(this.start.compareTo(this.end)<0)
			length=this.start.diff(this.end);
		else {
			GanttCalendar _start = this.end.newAdd( -length);
      this.start = _start;
      startDateField1.setText(_start.toString());
		}
		
		durationField1.setText(""+length);
		//durationField2.setText(""+length);
		durationField3.setText(""+length);
		durationFieldNotes.setText(""+length);
	}
}

