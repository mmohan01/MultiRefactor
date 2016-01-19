package edu.atilim.acma.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.ui.ConfigManager.NormalMetric;
import edu.atilim.acma.ui.design.RunConfigPanelBase;
import edu.atilim.acma.util.ACMAUtil;
import edu.atilim.acma.util.Log;

public class RunConfigPanel extends RunConfigPanelBase implements ActionListener, ListSelectionListener {
    private static final long serialVersionUID = 1L;

    public RunConfigPanel() {
        removePresetButton.addActionListener(this);
        duplicateButton.addActionListener(this);
        presetList.addListSelectionListener(this);

        updatePresetsList();
    }

    private RunConfig getSelectedConfig() {
        Object sel = presetList.getSelectedValue();
        if (sel == null) {
            presetList.setSelectedIndex(0);
            sel = ConfigManager.runConfigs().get(0);
        }

        return (RunConfig)sel;
    }

    private void updatePresetsList() {
        DefaultListModel lm = new DefaultListModel();
        for (RunConfig c: ConfigManager.runConfigs())
            lm.addElement(c);
        presetList.setModel(lm);

        presetList.setSelectedIndex(0);
    }

    private void updatePresetConfig() {
        RunConfig curconf = getSelectedConfig();

        actionTable.setModel(new ActionTableModel(ConfigManager.getActions(curconf), !curconf.getName().equals("Default")));
        actionTable.getColumnModel().getColumn(0).setPreferredWidth(500);
        actionTable.getColumnModel().getColumn(1).setPreferredWidth(60);

        normalMetricsTable.setModel(new NormalMetricsTableModel(curconf));
        normalMetricsTable.getColumnModel().getColumn(0).setPreferredWidth(500);
        normalMetricsTable.getColumnModel().getColumn(1).setPreferredWidth(60);

        availableMetricsTable.setModel(new AvailableMetricsTableModel(ConfigManager.getMetrics(curconf), !curconf.getName().equals("Default")));
        availableMetricsTable.getColumnModel().getColumn(0).setPreferredWidth(280);
        availableMetricsTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        availableMetricsTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        availableMetricsTable.getColumnModel().getColumn(3).setPreferredWidth(75);
        availableMetricsTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        availableMetricsTable.getColumnModel().getColumn(5).setPreferredWidth(60);
    }

    @Override
     public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(Actions.CONF_DUPLICATE)) {
            RunConfig current = getSelectedConfig();

            String name = null;
            do {
                name = JOptionPane.showInputDialog(this, "Please input a name for duplicated config", String.format("%s Copy", current.getName()));

                if (name == null) return;

                if (ConfigManager.getRunConfig(name) != null) {
                    JOptionPane.showMessageDialog(this, "Configuration with this name already exists.");
                    name = null;
                }
            } while(name == null);

            RunConfig copy = ACMAUtil.deepCopy(current);
            copy.setName(name);
            ConfigManager.add(copy);

            updatePresetsList();
        } else if (e.getActionCommand().equals(Actions.CONF_REMOVE)) {
            RunConfig current = getSelectedConfig();
            if (current.getName().equals("Default")) return;

            ConfigManager.remove(current);
            updatePresetsList();
        }
    }

    @Override
     public void valueChanged(ListSelectionEvent e) {
        if (e.getSource().equals(presetList)) {
            updatePresetConfig();
        }
    }

    private static class ActionTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        private List<ConfigManager.Action> actions;
        private boolean editable;

        private ActionTableModel(List<ConfigManager.Action> actions, boolean editable) {
            this.actions = actions;
            this.editable = editable;
        }

        @Override
         public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (!(aValue instanceof Boolean) || columnIndex != 1) return;
            actions.get(rowIndex).setEnabled((Boolean)aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
         public int getColumnCount() {
            return 2;
        }

        @Override
         public int getRowCount() {
            return actions.size();
        }

        @Override
         public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1 && editable;
        }

        @Override
         public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 1) return Boolean.class;
            return super.getColumnClass(columnIndex);
        }

        @Override
         public String getColumnName(int column) {
            switch (column) {
                case 1:
                    return "Enabled";
                case 0:
                    return "Name";
            }
            return "";
        }

        @Override
         public Object getValueAt(int row, int col) {
            ConfigManager.Action action = actions.get(row);
            switch (col) {
                case 1:
                    return action.isEnabled();
                case 0:
                    return action.getName();
            }
            return null;
        }
    }

    private static class AvailableMetricsTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        private List<ConfigManager.Metric> metrics;
        private boolean editable;

        private AvailableMetricsTableModel(List<ConfigManager.Metric> metrics, boolean editable) {
            this.metrics = metrics;
            this.editable = editable;
        }

        @Override
         public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                if (aValue instanceof Boolean && columnIndex == 5) {
                       metrics.get(rowIndex).setEnabled((Boolean)aValue);
            } else if (aValue instanceof String && columnIndex == 1) {
                        try {
                               double val = Double.parseDouble((String)aValue);
                               metrics.get(rowIndex).setWeight(val);
                } catch (NumberFormatException e) {}
            } else {
                        return;
            }
                fireTableCellUpdated(rowIndex, columnIndex);
        }

        @Override
         public int getColumnCount() {
            return 6;
        }

        @Override
         public int getRowCount() {
            return metrics.size();
        }

        @Override
         public boolean isCellEditable(int rowIndex, int columnIndex) {
            return (columnIndex == 1 || columnIndex == 5) && editable;
        }

        @Override
         public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 5 || columnIndex == 4 || columnIndex == 3 || columnIndex == 2) return Boolean.class;
            return super.getColumnClass(columnIndex);
        }

        @Override
         public String getColumnName(int column) {
            switch (column) {
                case 5:
                    return "Enabled";
                case 4:
                    return "Package";
                case 3:
                    return "Maximized";
                case 2:
                    return "Minimized";
                case 1:
                    return "Weight";
                case 0:
                    return "Name";
            }
            return "";
        }

        @Override
         public Object getValueAt(int row, int col) {
            ConfigManager.Metric m = metrics.get(row);
            switch (col) {
                case 5:
                    return m.isEnabled();
                case 4:
                    return m.isPackageMetric();
                case 3:
                    return m.isMaximized();
                case 2:
                    return m.isMinimized();
                case 1:
                    return m.getWeight();
                case 0:
                    return m.getName();
            }
            return null;
        }
    }

    private static class NormalMetricsTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        private RunConfig config;
        private List<ConfigManager.NormalMetric> normalMetrics;

        private List<ConfigManager.NormalMetric> getMetrics() {
            if (normalMetrics == null) {
                normalMetrics = ConfigManager.getNormalMetrics(config);
            }
            return normalMetrics;
        }

        private NormalMetricsTableModel(RunConfig config) {
            this.config = config;
        }

        @Override
         public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (!(aValue instanceof Boolean) || columnIndex != 1) return;
            if (!(Boolean)aValue) {
                int answer = JOptionPane.showConfirmDialog(MainWindow.getInstance(),
                        "Are you sure that you want to delete this normalization metric?",
                        "Delete",
                        JOptionPane.YES_NO_OPTION);

                if (answer == JOptionPane.YES_OPTION) {
                    getMetrics().get(rowIndex).remove();
                    normalMetrics = null;
                    fireTableDataChanged();
                }
            }
        }

        @Override
         public int getColumnCount() {
            return 2;
        }

        @Override
         public int getRowCount() {
            return getMetrics().size();
        }

        @Override
         public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
         public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 1) return Boolean.class;
            return super.getColumnClass(columnIndex);
        }

        @Override
         public String getColumnName(int column) {
            switch (column) {
                case 1:
                    return "Enabled";
                case 0:
                    return "Name";
            }
            return "";
        }

        @Override
         public Object getValueAt(int row, int col) {
            NormalMetric nm = getMetrics().get(row);
            switch (col) {
                case 1:
                    return true;
                case 0:
                    return nm.getName();
            }
            return null;
        }
    }
}
