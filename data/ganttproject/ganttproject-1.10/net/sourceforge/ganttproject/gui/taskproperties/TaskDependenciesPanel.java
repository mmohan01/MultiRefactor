package net.sourceforge.ganttproject.gui.taskproperties;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;

import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.GanttTask;
import net.sourceforge.ganttproject.GanttTaskRelationship;
import net.sourceforge.ganttproject.gui.TestGanttRolloverButton;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.dependency.TaskDependencyConstraint;
import net.sourceforge.ganttproject.task.dependency.constraint.FinishFinishConstraintImpl;
import net.sourceforge.ganttproject.task.dependency.constraint.FinishStartConstraintImpl;
import net.sourceforge.ganttproject.task.dependency.constraint.StartFinishConstraintImpl;
import net.sourceforge.ganttproject.task.dependency.constraint.StartStartConstraintImpl;

/**
 * Created by IntelliJ IDEA.
 * User: bard
 */
public class TaskDependenciesPanel extends CommonPanel {
    protected GanttLanguage language = GanttLanguage.getInstance(); // language the panel will display
    private GridBagConstraints gbc = new GridBagConstraints();
    private FlowLayout flowL = new FlowLayout(FlowLayout.LEFT, 10, 10);
    private JPanel predecessorsPanel;
    private JLabel nameLabel2; //first row, here the textfield is un-editable
    private JLabel durationLabel2;
    private JTextField nameField2;
    private JTextField durationField2;
    private JPanel firstRowPanel2;
    private JScrollPane predecessorsScrollPane; //second row, a table
    private JTable predecessorsTable;
    private final TaskManager myTaskManager;
    private final Task myTask;
    private DependencyTableModel myTableModel;

    public TaskDependenciesPanel(Task task) {
        myTaskManager = task.getManager();
        myTask = task;
    }

    public JPanel getComponent() {
        if (predecessorsPanel==null) {
            constructPredecessorsPanel();
        }
        return predecessorsPanel;
    }

    public DependencyTableModel getTableModel() {
        return myTableModel;
    }

    protected void constructPredecessorsPanel() {

        predecessorsPanel = new JPanel(new GridBagLayout());

        //first row

        nameLabel2 = new JLabel(language.getText("name") + ":");

        nameField2 = new JTextField(20);

        durationLabel2 = new JLabel(language.getText("length") + ":");

        durationField2 = new JTextField(8);
        durationField2.setText(myTask.getDuration().getLength()+"");

        firstRowPanel2 = new JPanel(flowL);

        nameField2.setEditable(false);
        nameField2.setText(myTask.getName());

        durationField2.setEditable(false);

        setFirstRow(firstRowPanel2, gbc, nameLabel2, nameField2, durationLabel2,

                durationField2);

        //Second row

        myTableModel = new DependencyTableModel(myTask);

        predecessorsTable = new JTable(myTableModel);

        predecessorsTable.setPreferredScrollableViewportSize(new Dimension(500, 130));

        setUpPredecessorComboColumn(predecessorsTable.getColumnModel().getColumn(1), predecessorsTable); //set column editor

        setUpTypeComboColumn(predecessorsTable.getColumnModel().getColumn(2)); //set column editor

        predecessorsTable.setRowHeight(23); //set row height

        predecessorsTable.getColumnModel().getColumn(0).setPreferredWidth(10); //set column size

        predecessorsTable.getColumnModel().getColumn(1).setPreferredWidth(240);

        predecessorsTable.getColumnModel().getColumn(2).setPreferredWidth(60);

        predecessorsScrollPane = new JScrollPane(predecessorsTable);

        JPanel secondPredecessorsPanel = new JPanel();
        secondPredecessorsPanel.setBorder(new TitledBorder(new EtchedBorder(),
                language.getText("predecessors")));
        secondPredecessorsPanel.add(predecessorsScrollPane);

        JButton bremove = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/delete_16.gif")));
        bremove.setToolTipText(GanttProject.getToolTip(language.getText("removeRelationShip")));
        bremove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int[] selectedRow = predecessorsTable.getSelectedRows();
                for (int i = 0; i < selectedRow.length; ++i) {
                    predecessorsTable.getModel().setValueAt(null, selectedRow[i], 1);
                }
            }
        });

        secondPredecessorsPanel.add(bremove);


        gbc.anchor = GridBagConstraints.WEST;

        gbc.insets.right = 15;

        gbc.insets.left = 10;

        gbc.insets.top = 10;

        gbc.weighty = 0;

        addUsingGBL(predecessorsPanel, firstRowPanel2, gbc, 0, 0, 1, 1);

        gbc.gridx = 0;

        gbc.gridy = 1;

        gbc.gridwidth = 8;

        gbc.gridheight = 1;

        gbc.weighty = 1;

        predecessorsPanel.add(secondPredecessorsPanel, gbc);
    }

    private void fillTable() {
        Vector predecessors = ((GanttTask)myTask).getPredecessorsOld();

        for (int i = 0; i < predecessors.size(); i++) {

            GanttTaskRelationship predecessor = (GanttTaskRelationship) predecessors.

                    get(i);

            myTableModel.setValueAt("" + predecessor.getPredecessorTask().getTaskID(), i, 0);
            myTableModel.setValueAt("" + predecessor.getPredecessorTask().getTaskID() + " - " + predecessor.getPredecessorTask(), i, 1);

            switch (predecessor.getRelationshipType()) {

                case GanttTaskRelationship.FF:

                    myTableModel.setValueAt(language.getText("finishfinish") /*"Finish-Finish"*/,
                            i, 2);

                    break;

                case GanttTaskRelationship.SF:

                    myTableModel.setValueAt(language.getText("startfinish") /*"Start-Finish"*/, i,
                            2);

                    break;

                case GanttTaskRelationship.SS:

                    myTableModel.setValueAt(language.getText("startstart") /*"Start-Start"*/, i,
                            2);

                    break;

                case GanttTaskRelationship.FS:

                    myTableModel.setValueAt(language.getText("finishstart") /*"Finish-Start"*/, i,
                            2);

                    break;

            }
        }
    }

    protected void setUpPredecessorComboColumn(TableColumn predecessorColumn, final JTable predecessorTable) {
        //Set up the editor for the sport cells.
        final JComboBox comboBox = new JComboBox();
        Task[] possiblePredecessors = myTaskManager.getAlgorithmCollection().getFindPossibleDependeesAlgorithm().run(myTask);
        for (int i=0; i<possiblePredecessors.length; i++) {
            Task next = possiblePredecessors[i];
            comboBox.addItem(new DependencyTableModel.TaskComboItem(next));

        }

        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (predecessorTable.getEditingRow() != -1) {
                    DependencyTableModel.TaskComboItem selectedItem = (DependencyTableModel.TaskComboItem) comboBox.getSelectedItem();
                    if (selectedItem!=null) {
                        predecessorTable.setValueAt(selectedItem, predecessorTable.getEditingRow(), 0);
                        predecessorTable.setValueAt(CONSTRAINTS[0], predecessorTable.getEditingRow(), 2);
                    }
                }
            }
        });
        comboBox.setEditable(false);
        predecessorColumn.setCellEditor(new DefaultCellEditor(comboBox));
    }


    protected void setUpTypeComboColumn(TableColumn typeColumn) {
        //Set up the editor for the sport cells.
        DefaultComboBoxModel model = new DefaultComboBoxModel(CONSTRAINTS);
        JComboBox comboBox = new JComboBox(model);
        comboBox.setSelectedIndex(0);
        comboBox.setEditable(false);
        typeColumn.setCellEditor(new DefaultCellEditor(comboBox));
    }

    public JTable getTable() {
        return predecessorsTable;
    }

    private static TaskDependencyConstraint[] CONSTRAINTS = new TaskDependencyConstraint[] {
        new FinishStartConstraintImpl(),
        new FinishFinishConstraintImpl(),
        new StartFinishConstraintImpl(),
        new StartStartConstraintImpl()
    };

}
