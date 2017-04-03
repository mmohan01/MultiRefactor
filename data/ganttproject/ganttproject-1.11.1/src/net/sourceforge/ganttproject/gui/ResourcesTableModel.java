package net.sourceforge.ganttproject.gui;

import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.ProjectResource;
import net.sourceforge.ganttproject.task.ResourceAssignment;
import net.sourceforge.ganttproject.task.ResourceAssignmentCollection;
import net.sourceforge.ganttproject.task.ResourceAssignmentMutator;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class ResourcesTableModel extends AbstractTableModel {

    final String[] columnNames = {
        GanttLanguage.getInstance().getText("id"),
        GanttLanguage.getInstance().getText("resourcename"),
        GanttLanguage.getInstance().getText("unit")};
    private final ResourceAssignmentCollection myAssignmentCollection;
    private final List myAssignments;
    private static final int MAX_ROW_COUNT = 100;
    private final ResourceAssignmentMutator myMutator;

    public ResourcesTableModel(ResourceAssignmentCollection assignmentCollection) {
        myAssignmentCollection = assignmentCollection;
        myAssignments = new ArrayList(Arrays.asList(assignmentCollection.getAssignments()));
        myMutator = assignmentCollection.createMutator();
    }

    /**
     * Return the number of colums
     */
    public int getColumnCount() {

        return columnNames.length;

    }

    /**
     * Return the number of rows
     */
    public int getRowCount() {
        return myAssignments.size()+1;
    }

    /**
     * Return the name of the column at col index
     */
    public String getColumnName(int col) {

        return columnNames[col];

    }

    /**
     * Return the object a specify cell
     */
    public Object getValueAt(int row, int col) {
        Object result;
        if (row>=0) {
            if (row<myAssignments.size()) {
                ResourceAssignment assignment = (ResourceAssignment) myAssignments.get(row);
                switch (col) {
                    case 0:
                        result = String.valueOf(assignment.getResource().getId());
                        break;
                    case 1:
                        result = assignment.getResource();
                        break;
                    case 2:
                        result = String.valueOf(assignment.getLoad());
                        break;
                    default:
                        result = "";
                }
            }
            else {
                result = "";
            }
        }
        else {
            throw new IllegalArgumentException("I can't return data in row="+row);
        }
        return result;
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */

    public Class getColumnClass(int c) {
        if (c == 0 || c == 2) {
            return String.class;
        } else {
            return HumanResource.class;
        }
    }

    public boolean isCellEditable(int row, int col) {
        boolean result = col>0;
        if (result) {
            result = col==2 ? row<myAssignments.size() : row<=myAssignments.size();
        }
        return result;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */

    public void setValueAt(Object value, int row, int col) {
        if (row>=0) {
            if (row>=myAssignments.size()) {
                createAssignment(value);
            }
            else {
                updateAssignment(value, row, col);
            }
        }
        else {
            throw new IllegalArgumentException("I can't set data in row="+row);
        }

        //fireTableCellUpdated(row, col);

    }

    private void updateAssignment(Object value, int row, int col) {
        ResourceAssignment updateTarget = (ResourceAssignment) myAssignments.get(row);
        switch (col) {
            case 2:
                {
                    float loadAsFloat = Float.parseFloat(String.valueOf(value));
                    updateTarget.setLoad(loadAsFloat);
                    break;
                }
            case 1:
                {
                    if (value==null) {
                        updateTarget.delete();
                        myAssignments.remove(row);
                        fireTableRowsDeleted(row, row);
                    }
                    else if (value instanceof ProjectResource) {
                        float load = updateTarget.getLoad();
                        updateTarget.delete();
                        ResourceAssignment newAssignment = myMutator.addAssignment((ProjectResource)value);
                        newAssignment.setLoad(load);
                        myAssignments.set(row, newAssignment);
                    }
                    break;

                }
            default:
                break;
        }
    }

    private void createAssignment(Object value) {
        if (value instanceof ProjectResource) {
            ResourceAssignment newAssignment = myMutator.addAssignment((ProjectResource)value);
            newAssignment.setLoad(100);
            myAssignments.add(newAssignment);
            fireTableRowsInserted(myAssignments.size(), myAssignments.size());
        }
    }

    void commit() {
        myMutator.commit();
    }

}
