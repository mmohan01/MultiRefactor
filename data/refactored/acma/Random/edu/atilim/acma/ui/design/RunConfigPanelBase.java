package edu.atilim.acma.ui.design;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.ui.Actions;
import javax.swing.JTable;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Box;
import javax.swing.ScrollPaneConstants;

public class RunConfigPanelBase extends JPanel {
    private static long serialVersionUID = 1L;
    protected JPanel presetsPanel;
    protected JScrollPane presetsScrollPane;
    protected JList presetList;
    protected JPanel presetsButtonsPanel;
    protected JButton removePresetButton;
    protected JTabbedPane tabbedPane;
    protected JPanel metricsPanel;
    protected JPanel actionsPanel;
    protected JButton duplicateButton;
    protected JScrollPane actionListScrollPane;
    protected JTable actionTable;
    protected JPanel availableMetricsPanel;
    protected JPanel normalMetricsPanel;
    protected JScrollPane availableMetricsScrollPane;
    protected JScrollPane normalMetricsScrollPane;
    protected JTable availableMetricsTable;
    protected JTable normalMetricsTable;
    protected Component horizontalStrut;

    public RunConfigPanelBase() {
        setOpaque(false);
        setLayout(new BorderLayout(5, 5));

        presetsPanel = new JPanel();
        presetsPanel.setBorder(new TitledBorder(null, "Presets", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        add(presetsPanel, BorderLayout.WEST);
        presetsPanel.setLayout(new BorderLayout(0, 0));

        presetsScrollPane = new JScrollPane();
        presetsScrollPane.setOpaque(false);
        presetsScrollPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        presetsPanel.add(presetsScrollPane, BorderLayout.CENTER);

        presetList = new JList();
        presetList.setCellRenderer(new PresetListRenderer());
        presetsScrollPane.setViewportView(presetList);

        presetsButtonsPanel = new JPanel();
        FlowLayout fl_presetsButtonsPanel = (FlowLayout) presetsButtonsPanel.getLayout();
        fl_presetsButtonsPanel.setVgap(0);
        fl_presetsButtonsPanel.setHgap(0);
        fl_presetsButtonsPanel.setAlignment(FlowLayout.RIGHT);
        presetsPanel.add(presetsButtonsPanel, BorderLayout.SOUTH);

        duplicateButton = new JButton("");
        duplicateButton.setActionCommand(Actions.CONF_DUPLICATE);
        duplicateButton.setIcon(new ImageIcon(RunConfigPanelBase.class.getResource("/resources/icons/copy_16.png")));
        presetsButtonsPanel.add(duplicateButton);

        removePresetButton = new JButton("");
        removePresetButton.setActionCommand(Actions.CONF_REMOVE);
        removePresetButton.setIcon(new ImageIcon(RunConfigPanelBase.class.getResource("/resources/icons/delete.png")));
        presetsButtonsPanel.add(removePresetButton);

        horizontalStrut = Box.createHorizontalStrut(175);
        presetsPanel.add(horizontalStrut, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        add(tabbedPane, BorderLayout.CENTER);

        metricsPanel = new JPanel();
        metricsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        metricsPanel.setOpaque(false);
        tabbedPane.addTab("Metrics", new ImageIcon(RunConfigPanelBase.class.getResource("/resources/icons/statistics2_16.png")), metricsPanel, null);

        availableMetricsPanel = new JPanel();
        availableMetricsPanel.setOpaque(false);
        availableMetricsPanel.setBorder(new TitledBorder(null, "Available Metrics", TitledBorder.LEADING, TitledBorder.TOP, null, null));

        normalMetricsPanel = new JPanel();
        normalMetricsPanel.setOpaque(false);
        normalMetricsPanel.setBorder(new TitledBorder(null, "Normalization Metrics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        GroupLayout gl_metricsPanel = new GroupLayout(metricsPanel);
        gl_metricsPanel.setHorizontalGroup(
            gl_metricsPanel.createParallelGroup(Alignment.TRAILING).addComponent(normalMetricsPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE).addComponent(availableMetricsPanel, GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE));
        gl_metricsPanel.setVerticalGroup(
            gl_metricsPanel.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING, gl_metricsPanel.createSequentialGroup().addComponent(availableMetricsPanel, GroupLayout.DEFAULT_SIZE, 262, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.RELATED).addComponent(normalMetricsPanel, GroupLayout.PREFERRED_SIZE, 142, GroupLayout.PREFERRED_SIZE)));
        normalMetricsPanel.setLayout(new BorderLayout(0, 0));

        normalMetricsScrollPane = new JScrollPane();
        normalMetricsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        normalMetricsPanel.add(normalMetricsScrollPane, BorderLayout.CENTER);

        normalMetricsTable = new JTable();
        normalMetricsTable.setDefaultRenderer(Object.class, new NormalMetricsTableRenderer());
        normalMetricsTable.setRowSelectionAllowed(false);
        normalMetricsTable.setRowHeight(24);
        normalMetricsScrollPane.setViewportView(normalMetricsTable);
        availableMetricsPanel.setLayout(new BorderLayout(0, 0));

        availableMetricsScrollPane = new JScrollPane();
        availableMetricsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        availableMetricsPanel.add(availableMetricsScrollPane, BorderLayout.CENTER);

        availableMetricsTable = new JTable();
        availableMetricsTable.setDefaultRenderer(Object.class, new AvailableMetricsTableRenderer());
        availableMetricsTable.setRowSelectionAllowed(false);
        availableMetricsTable.setRowHeight(24);
        availableMetricsScrollPane.setViewportView(availableMetricsTable);
        metricsPanel.setLayout(gl_metricsPanel);

        actionsPanel = new JPanel();
        actionsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        actionsPanel.setOpaque(false);
        tabbedPane.addTab("Actions", new ImageIcon(RunConfigPanelBase.class.getResource("/resources/icons/misc3_16.png")), actionsPanel, null);
        actionsPanel.setLayout(new BorderLayout(0, 0));

        actionListScrollPane = new JScrollPane();
        actionListScrollPane.setOpaque(false);
        actionListScrollPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        actionsPanel.add(actionListScrollPane);

        actionTable = new JTable();
        actionTable.setRowSelectionAllowed(false);
        actionTable.setRowHeight(24);
        actionTable.setDefaultRenderer(Object.class, new ActionTableRenderer());
        actionListScrollPane.setViewportView(actionTable);
    }

    private static class PresetListRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        private static final Icon configIcon = new ImageIcon(PresetListRenderer.class.getResource("/resources/icons/misc1_16.png"));

        @Override
         public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected,
                    cellHasFocus);

            if (!(value instanceof RunConfig)) return this;

            RunConfig rc = (RunConfig)value;

            setIcon(configIcon);
            setText(rc.getName());

            return this;
        }
    }

    private static class ActionTableRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        private static final Icon actionIcon = new ImageIcon(ActionTableRenderer.class.getResource("/resources/icons/misc3_16.png"));

        @Override
         public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);

            setIcon(actionIcon);

            return this;
        }
    }

    private static class AvailableMetricsTableRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        private static final Icon metricIcon = new ImageIcon(AvailableMetricsTableRenderer.class.getResource("/resources/icons/statistics2_16.png"));

        @Override
         public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);

            if (column == 0)
                setIcon(metricIcon);
             else
                setIcon(null);

            return this;
        }
    }

    private static class NormalMetricsTableRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        private static final Icon metricIcon = new ImageIcon(NormalMetricsTableRenderer.class.getResource("/resources/icons/statistics_16.png"));

        @Override
         public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);

            setIcon(metricIcon);

            return this;
        }
    }
}
