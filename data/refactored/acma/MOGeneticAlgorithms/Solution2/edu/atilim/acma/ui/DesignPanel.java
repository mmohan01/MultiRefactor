package edu.atilim.acma.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.TaskQueue;
import edu.atilim.acma.concurrent.ConcurrentTask;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.metrics.MetricCalculator;
import edu.atilim.acma.metrics.MetricSummary;
import edu.atilim.acma.metrics.MetricTable;
import edu.atilim.acma.search.AbstractAlgorithm;
import edu.atilim.acma.search.AlgorithmObserver;
import edu.atilim.acma.search.BeeColonyAlgorithm;
import edu.atilim.acma.search.BeamSearchAlgorithm;
import edu.atilim.acma.search.ConcurrentAlgorithm;
import edu.atilim.acma.search.ConcurrentBeamSearch;
import edu.atilim.acma.search.ConcurrentBeeColony;
import edu.atilim.acma.search.ConcurrentHillClimbing;
import edu.atilim.acma.search.ConcurrentParallelBeeColony;
import edu.atilim.acma.search.ConcurrentRandomSearch;
import edu.atilim.acma.search.ConcurrentSimAnn;
import edu.atilim.acma.search.ConcurrentStochasticBeamSearch;
import edu.atilim.acma.search.HillClimbingAlgorithm;
import edu.atilim.acma.search.RandomSearchAlgorithm;
import edu.atilim.acma.search.SimAnnAlgorithm;
import edu.atilim.acma.search.SolutionDesign;
import edu.atilim.acma.transition.TransitionManager;
import edu.atilim.acma.transition.actions.Action;
import edu.atilim.acma.ui.MainWindow.WindowEventListener;
import edu.atilim.acma.ui.design.DesignPanelBase;
import edu.atilim.acma.util.ACMAUtil;
import edu.atilim.acma.util.Log;

public class DesignPanel extends DesignPanelBase implements WindowEventListener {
    private static final long serialVersionUID = 1L;

    private Design design;
    private DesignData designData;

    DesignPanel(Design design) {
        this.design = design;
        this.designData = new DesignData();

        MainWindow.getInstance().addEventListener(this);

        initPossibleActions();
        initAppliedActions();
        initMetrics();
        initConfigSelector();
        initRunButtons();

        validateDesignData();
    }

    public void setCompactView(boolean cv) {
        configPanel.setVisible(!cv);
        algorithmsPanel.setVisible(!cv);
    }

    private void initRunButtons() {
        ActionListener algoListener = new ActionListener(){
            @Override
             public void actionPerformed(ActionEvent e) {
                int runs = -1;
                while (runs <= 0)
                 {
                    try { runs = Integer.parseInt(JOptionPane.showInputDialog(DesignPanel.this, "# of runs:")); } catch (Exception ex) {}
                    if (runs == JOptionPane.CANCEL_OPTION || runs == JOptionPane.CLOSED_OPTION)
                        break;
                }

                if (runs == JOptionPane.CANCEL_OPTION || runs == JOptionPane.CLOSED_OPTION)
                    runs = 0;

                for (int i = 0; i < runs; i++)
                 {
                    AbstractAlgorithm algo = null;

                    if (e.getActionCommand().equals("RS")) {
                        int mi = (Integer)rsIterationCount.getValue();
                        algo = new RandomSearchAlgorithm(new SolutionDesign(design, getRunConfig()), null, mi);
                    } else if (e.getActionCommand().equals("HC")) {
                        int rc = (Integer)hcRestartCount.getValue();
                        int rd = (Integer)hcRestartDepth.getValue();
                        algo = new HillClimbingAlgorithm(new SolutionDesign(design, getRunConfig()), null);
                        ((HillClimbingAlgorithm)algo).setRestartCount(rc);
                        ((HillClimbingAlgorithm)algo).setRestartDepth(rd);
                        if (hcFirstDescent.isSelected())
                            ((HillClimbingAlgorithm)algo).setFirstDescent(true);

                        if (hcLimitIterations.isSelected())
                         {
                            int maxIterations = 0;
                            while (maxIterations <= 0)
                                try { maxIterations = Integer.parseInt(JOptionPane.showInputDialog(DesignPanel.this, "Maximum Iterations:")); } catch (Exception ex) {}
                            ((HillClimbingAlgorithm)algo).setMaxIterations(maxIterations);
                        }
                    } else if (e.getActionCommand().equals("SA")) {
                        double st = (Double)saStartTemp.getValue();
                        int mi = (Integer)saIterationCnt.getValue();
                        algo = new SimAnnAlgorithm(new SolutionDesign(design, getRunConfig()), null, st, mi);
                    } else if (e.getActionCommand().equals("ABC")) {
                        int mi = (Integer)abcIterations.getValue();
                        int mt = (Integer)abcMaxTrials.getValue();
                        int pc = (Integer)abcPopSize.getValue();
                        algo = new BeeColonyAlgorithm(new SolutionDesign(design, getRunConfig()), null, mt, pc, mi);
                    } else if (e.getActionCommand().equals("BS")) {
                        int rd = (Integer)bsRandomDepth.getValue();
                        int bl = (Integer)bsBeamLength.getValue();
                        algo = new BeamSearchAlgorithm(new SolutionDesign(design, getRunConfig()), null, bl, rd);
                    }

                    RunPanel rp = new RunPanel(algo);

                    MainWindow.getInstance().getTabs().addTab(algo.getName(),
                            new ImageIcon(DesignPanel.class.getResource("/resources/icons/play_16.png")),
                            rp,
                            null);

                    MainWindow.getInstance().getTabs().setSelectedComponent(rp);
                }
            }
        };

        rsBtnStart.addActionListener(algoListener);
        hcBtnStart.addActionListener(algoListener);
        saBtnStart.addActionListener(algoListener);
        abcBtnStart.addActionListener(algoListener);
        bsBtnStart.addActionListener(algoListener);

        ActionListener taskListener = new ActionListener(){

            @Override
             public void actionPerformed(ActionEvent e) {
                ConcurrentTask task = null;

                String name = null;
                while (name == null || name.trim().equals(""))
                 {
                    name = JOptionPane.showInputDialog(DesignPanel.this, "Name of the task:");
                    if (name == null)
                        break;
                }

                int runs = -1;

                if (name != null)
                 {
                    while (runs <= 0)
                        try { runs = Integer.parseInt(JOptionPane.showInputDialog(DesignPanel.this, "# of runs:")); } catch (Exception ex) {}

                    if (e.getActionCommand().equals("RS")) {
                        int mi = (Integer)rsIterationCount.getValue();
                        task = new ConcurrentRandomSearch(name, getRunConfig(), design, mi, runs);
                    } else if (e.getActionCommand().equals("HC")) {
                        int rc = (Integer)hcRestartCount.getValue();
                        int rd = (Integer)hcRestartDepth.getValue();
                        ConcurrentAlgorithm algo = null;

                        if (hcFirstDescent.isSelected())
                            algo = new ConcurrentHillClimbing(name, getRunConfig(), design, rc, rd, true, runs);
                         else
                            algo = new ConcurrentHillClimbing(name, getRunConfig(), design, rc, rd, false, runs);

                        if (hcLimitIterations.isSelected())
                         {
                            int maxIterations = 0;
                            while (maxIterations <= 0)
                                try { maxIterations = Integer.parseInt(JOptionPane.showInputDialog(DesignPanel.this, "Maximum Iterations:")); } catch (Exception ex) {}
                            ((ConcurrentHillClimbing)algo).setMaxIterations(maxIterations);
                        }

                        task = algo;
                    } else if (e.getActionCommand().equals("SA")) {
                        double st = (Double)saStartTemp.getValue();
                        int mi = (Integer)saIterationCnt.getValue();
                        task = new ConcurrentSimAnn(name, getRunConfig(), design, st, mi, runs);
                    } else if (e.getActionCommand().equals("ABC")) {
                        int mi = (Integer)abcIterations.getValue();
                        int mt = (Integer)abcMaxTrials.getValue();
                        int pc = (Integer)abcPopSize.getValue();

                        if (abcParallel.isSelected())
                            task = new ConcurrentParallelBeeColony(name, getRunConfig(), design, mt, pc, mi, runs);
                         else
                            task = new ConcurrentBeeColony(name, getRunConfig(), design, mt, pc, mi, runs);
                    } else if (e.getActionCommand().equals("BS")) {
                        int bl = (Integer)bsBeamLength.getValue();
                        int bi = (Integer)bsIterations.getValue();
                        int br = (Integer)bsRandomDepth.getValue();

                        if (bsIsStochastic.isSelected())
                            task = new ConcurrentStochasticBeamSearch(name, getRunConfig(), design, bl, br, bi, runs);
                         else
                            task = new ConcurrentBeamSearch(name, getRunConfig(), design, bl, br, bi, runs);
                    }
                }

                if (task != null)
                    TaskQueue.push(task);
            }
        };

        rsBtnAddTask.addActionListener(taskListener);
        hcBtnAddTask.addActionListener(taskListener);
        saBtnAddTask.addActionListener(taskListener);
        abcBtnAddTask.addActionListener(taskListener);
        bsBtnAddTask.addActionListener(taskListener);
    }

    private void initAppliedActions() {
        List<String> mods = design.getModifications();

        if (mods.size() == 0) {
            tabbedPane.remove(appliedActionsPanel);
        }

        final DefaultListModel model = new DefaultListModel();
        for (String act: design.getModifications()) {
            model.addElement(act);
        }
        appliedActionsList.setModel(model);
    }

    private void initPossibleActions() {
        btnPosActionsRefresh.addActionListener(new ActionListener(){
            @Override
             public void actionPerformed(ActionEvent arg0) {
                posActionsList.validate();
            }
        });

        btnPosActionsChart.addActionListener(new ActionListener(){
            @Override
             public void actionPerformed(ActionEvent arg0) {
                HashMap<String, Integer> actionMap = new HashMap<String, Integer>();
                for (Action act: designData.getActions()) {
                    String type = act.getClass().getEnclosingClass().getSimpleName();

                    if (actionMap.containsKey(type))
                        actionMap.put(type, actionMap.get(type) + 1);
                     else
                        actionMap.put(type, 1);
                }

                DefaultPieDataset dataset = new DefaultPieDataset();
                for (Entry<String, Integer> e: actionMap.entrySet()) {
                    dataset.setValue(ACMAUtil.splitCamelCase(e.getKey()), e.getValue());
                }

                JFreeChart chart = ChartFactory.createPieChart3D("Action Distribution", dataset, true, false, false);
                chart.setBackgroundPaint(new Color(255, 255, 255, 0));

                PiePlot plot = (PiePlot)chart.getPlot();
                plot.setBackgroundPaint(new Color(255, 255, 255, 0));
                plot.setOutlineVisible(false);
                plot.setForegroundAlpha(0.75f);
                ChartPanel panel = new ChartPanel(chart);
                panel.setOpaque(false);
                PanelDialog.display(panel, 700, 470);
            }
        });
    }

    private void initMetrics() {
        metricTable.setDefaultRenderer(Object.class, new MetricTableRenderer());

        metricTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            @Override
             public void valueChanged(ListSelectionEvent e) {
                EventQueue.invokeLater(new Runnable(){
                    @Override
                     public void run() {
                        drawMetricsChart();
                    }
                });
            }
        });

        btnSave.addActionListener(new ActionListener(){
            @Override
             public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileFilter(){
                    @Override
                     public String getDescription() {
                        return "CSV File";
                    }

                    @Override
                     public boolean accept(File f) {
                        if (f.isDirectory()) return true;
                        return f.getName().endsWith(".csv");
                    }
                });
                fc.showSaveDialog(DesignPanel.this);

                File out = fc.getSelectedFile();

                if (out == null) return;

                if (out.exists()) {
                    int res = JOptionPane.showConfirmDialog(DesignPanel.this, "This file already exists.\nRewrite?", "File exists", JOptionPane.OK_CANCEL_OPTION);
                    if (res == JOptionPane.CANCEL_OPTION) return;
                }

                Log.info("Saving metrics to %s", out.getAbsolutePath());

                try {
                    writeCSV(out.getAbsolutePath());
                } catch (IOException e1) {
                    Log.severe("Error saving metrics");
                }
            }
        });

        btnPreset.addActionListener(new ActionListener(){
            @Override
             public void actionPerformed(ActionEvent arg0) {
                String name = JOptionPane.showInputDialog(DesignPanel.this, "Please input a name for preset", design.toString());

                if (name == null) return;

                MetricSummary ms = new MetricSummary(name, design.getMetrics());
                ConfigManager.add(ms);
                ConfigManager.saveChanges();
            }
        });
    }

    public void writeCSV(String fileName) throws IOException {
        Double val;
        BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
        output.write("Class/Folder Name," + designData.getCols().toString() + "\n");

        for (int i = 0; i < designData.getRows().size(); i++)
         {
            output.write(designData.getRows().get(i) + ",");

            for (int j = 0; j < designData.getCols().size(); j++)
             {
                val = designData.getTable().get(designData.getRows().get(i), designData.getCols().get(j));

                if (Double.isNaN(val))
                    output.write(",");
                 else
                    output.write(val.toString() + ",");
            }
            output.write("\n");
            output.flush();
        }

        output.write("\nAverage Scores,");

        for (int k = 0; k < designData.getCols().size(); k++)
         {
            val = designData.getTable().getAverage(designData.getCols().get(k));

            if (Double.isNaN(val))
                output.write(",");
             else
                output.write(val.toString() + ",");
        }

        output.write("\n\nMetrics:," + designData.getCols().size() + "\n");
        output.write("Items:," + designData.getRows().size() + "\n");
        output.write("Combined Metric Average Score:," + designData.getScore());
        output.close();
    }

    private void drawMetricsChart() {
        List<String> cols = designData.getCols();
        List<String> rows = designData.getRows();

        DefaultCategoryDataset ds = new DefaultCategoryDataset();

        for (int i = 0; i < cols.size(); i++) {
            ds.addValue(designData.getTable().getAverage(cols.get(i)), "Averages", cols.get(i));
            //ds.addValue(Math.log(designData.getTable().getAverage(cols.get(i)) + 1), "Averages", cols.get(i));
        }

        if (metricTable.getSelectedRow() >= 0) {
            for (int i = 0; i < cols.size(); i++)
                ds.addValue(Math.log(designData.getTable().get(rows.get(metricTable.getSelectedRow()), cols.get(i)) + 1), "Selected", cols.get(i));
        }

        JFreeChart chart = ChartFactory.createBarChart("", "", "Value", ds, PlotOrientation.VERTICAL, true, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.getDomainAxis().setVisible(false);
        ChartPanel panel = new ChartPanel(chart);

        chartPanel.removeAll();
        chartPanel.add(panel);
        chartPanel.validate();
    }

    private void validateDesignData() {
        designData = new DesignData();

        metricTable.setModel(new MetricTableModel());
        metricTable.getColumnModel().getColumn(0).setPreferredWidth(300);

        lblValNumMetrics.setText(String.valueOf(designData.getCols().size()));
        lblValNumItems.setText(String.valueOf(designData.getRows().size()));
        lblValWeightedSum.setText(String.format("%.2f", designData.getScore()));

        final DefaultListModel model = new DefaultListModel();
        for (Action act: designData.getActions()) {
            model.addElement(act.toString());
        }
        posActionsList.setModel(model);

        drawMetricsChart();
    }

    private RunConfig getRunConfig() {
        Object rc = runConfigBox.getSelectedItem();
        if (rc == null || !(rc instanceof RunConfig)) return RunConfig.getDefault();
        return (RunConfig)rc;
    }

    private void initConfigSelector() {
        updateConfigSelector();

        runConfigBox.addItemListener(new ItemListener(){
            @Override
             public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    validateDesignData();
                }
            }
        });
    }

    private void updateConfigSelector() {
        boolean prevfound = false;
        UUID previd = UUID.randomUUID();
        Object prc = runConfigBox.getSelectedItem();
        if (prc != null && prc instanceof RunConfig)
            previd = ((RunConfig)prc).getId();

        runConfigBox.removeAllItems();

        for (RunConfig rc: ConfigManager.runConfigs()) {
            runConfigBox.addItem(rc);

            if (rc.getId().equals(previd)) {
                prevfound = true;
                prc = rc;
            }
        }

        if (prevfound) {
            runConfigBox.setSelectedItem(prc);
        } else {
            runConfigBox.setSelectedIndex(0);
        }
    }

    @Override
     public void onWindowEvent(Object e) {
        if (ConfigManager.CONFIG_CHANGED.equals(e)) {
            updateConfigSelector();
        }
    }

    private class DesignData {
        private MetricTable table;
        private List<String> cols;
        private List<String> rows;
        private List<Action> actions;

        public List<String> getRows() {
            return rows;
        }

        public List<String> getCols() {
            return cols;
        }

        public MetricTable getTable() {
            return table;
        }

        public List<Action> getActions() {
            return actions;
        }

        public double getScore() {
            return MetricCalculator.normalize(table, getRunConfig());
        }

        private DesignData() {
            table = MetricCalculator.calculate(design, getRunConfig());
            rows = table.getRows();
            Collections.sort(rows);

            cols = new ArrayList<String>();
            for (ConfigManager.Metric m: ConfigManager.getMetrics(getRunConfig())) {
                if (m.isEnabled())
                    cols.add(m.getName());
            }
            Collections.sort(cols);

            actions = new ArrayList<Action>(TransitionManager.getPossibleActions(design, getRunConfig()));
        }
    }

    private class MetricTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        @Override
         public int getColumnCount() {
            return designData.getCols().size() + 1;
        }

        @Override
         public String getColumnName(int arg0) {
            if (arg0 == 0)
                return "Name";

            return designData.getCols().get(arg0 - 1);
        }

        @Override
         public int getRowCount() {
            return designData.getRows().size();
        }

        @Override
         public Object getValueAt(int row, int col) {
            if (col == 0)
                return getRow(designData.getRows().get(row));

            return designData.getTable().get(designData.getRows().get(row), designData.getCols().get(col - 1));
        }

        private Object getRow(String name) {
                Type t = design.getType(name);
                if (t != null)
                    return t;
                return name;
        }
    }

    private static class MetricTableRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;

        private static final Icon packageIcon = new ImageIcon(MetricTableRenderer.class.getResource("/resources/icons/java/package.gif"));
        private static final Icon classIcon = new ImageIcon(MetricTableRenderer.class.getResource("/resources/icons/java/class.gif"));
        private static final Icon interfaceIcon = new ImageIcon(MetricTableRenderer.class.getResource("/resources/icons/java/interface.gif"));
        private static final Icon annotIcon = new ImageIcon(MetricTableRenderer.class.getResource("/resources/icons/java/annotation.gif"));

        @Override
         public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                    row, column);

            setIcon(null);

            if (value instanceof Double) {
                double dval = (Double)value;
                if (Double.isNaN(dval)) {
                    setText("");
                }
            } else if (value instanceof Type) {
                Type tval = (Type)value;
                if (tval.isAnnotation())
                    setIcon(annotIcon);
                 else if (tval.isInterface())
                    setIcon(interfaceIcon);
                 else
                    setIcon(classIcon);
            } else if (value instanceof String) {
                setIcon(packageIcon);
            }

            return this;
        }
    }
}
