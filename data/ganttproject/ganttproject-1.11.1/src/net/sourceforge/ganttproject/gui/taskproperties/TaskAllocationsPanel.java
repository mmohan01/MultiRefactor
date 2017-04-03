package net.sourceforge.ganttproject.gui.taskproperties;

import net.sourceforge.ganttproject.gui.ResourcesTableModel;
import net.sourceforge.ganttproject.gui.TestGanttRolloverButton;
import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.ResourceAssignmentCollection;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Dmitry.Barashev
 */
public class TaskAllocationsPanel extends CommonPanel {
    private JPanel resourcesPanel;
    private ResourcesTableModel myResourcesTableModel;
    private JTable resourcesTable;
    private final HumanResourceManager myHRManager;
    private JScrollPane resourcesScrollPane;

    public TaskAllocationsPanel(Task task, HumanResourceManager hrManager) {
        super(task);
        myHRManager = hrManager;
    }

    public JPanel getComponent() {
        if (resourcesPanel == null) {
            constructResourcesPanel(getTask().getAssignmentCollection());
        }
        return resourcesPanel;
    }

    private void constructResourcesPanel(ResourceAssignmentCollection assignments) {

        resourcesPanel = new JPanel(new GridBagLayout());
        myResourcesTableModel = new ResourcesTableModel(assignments);
        resourcesTable = new JTable(myResourcesTableModel);
        setUpResourcesComboColumn(resourcesTable); //set column editor
        resourcesTable.setRowHeight(23);
        resourcesTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        resourcesTable.getColumnModel().getColumn(1).setPreferredWidth(240);
        resourcesTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        resourcesScrollPane = new JScrollPane(resourcesTable);
        resourcesScrollPane.setPreferredSize(new Dimension(500, 130));
        JPanel secondResourcesScrollPane = new JPanel();
        secondResourcesScrollPane.setBorder(new TitledBorder(new EtchedBorder(),
                GanttProject.correctLabel(getLanguage().getText("human"))));
        secondResourcesScrollPane.add(resourcesScrollPane);


        JButton bremove = new TestGanttRolloverButton(new ImageIcon(getClass().getResource(
                "/icons/delete_16.gif")));
        bremove.setToolTipText(GanttProject.getToolTip(getLanguage().getText("removeResources")));
        bremove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                int[] selectedRow = resourcesTable.getSelectedRows();
                for (int i = 0; i < selectedRow.length; ++i) {
                    resourcesTable.getModel().setValueAt(null, selectedRow[i], 1);
                }
            }
        });
        secondResourcesScrollPane.add(bremove);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets.right = 15;
        gbc.insets.left = 10;
        gbc.insets.top = 10;
        gbc.weighty = 0;
        JPanel commonFields = setupCommonFields();
        addUsingGBL(resourcesPanel, commonFields, gbc, 0, 0, 1, 1);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 8;
        gbc.gridheight = 1;
        gbc.weighty = 1;
        resourcesPanel.add(secondResourcesScrollPane, gbc);
    }

    public ResourcesTableModel getTableModel() {
        return myResourcesTableModel;
    }

    private void setUpResourcesComboColumn(final JTable resourceTable) {
        ArrayList resources = myHRManager.getResources();
        final JComboBox comboBox = new JComboBox();
        for (int i = 0; i < resources.size(); i++) {
            comboBox.addItem(resources.get(i));
        }

        TableColumn resourcesColumn = resourceTable.getColumnModel().getColumn(1);
        comboBox.setEditable(false);
        resourcesColumn.setCellEditor(new DefaultCellEditor(comboBox));
    }
}
