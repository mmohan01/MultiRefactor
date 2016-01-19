package edu.atilim.acma.ui.design;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.atilim.acma.ui.Actions;
import edu.atilim.acma.ui.RunConfigDialog;

import javax.swing.JCheckBox;

public class DesignPanelBase extends JPanel {

	private static final long serialVersionUID = 1L;
	protected JTabbedPane tabbedPane;
	protected JPanel metricsPanel;
	protected JScrollPane scrollPane;
	protected JTable metricTable;
	protected JPanel actionsPanel;
	protected JPanel chartPanel;
	protected JPanel infoPanel;
	protected JLabel lblNumberOfMetrics;
	protected JLabel lblValNumMetrics;
	protected JLabel lblItems;
	protected JLabel lblValNumItems;
	protected JLabel lblScore;
	protected JLabel lblValWeightedSum;
	protected JPanel posActionsPanel;
	protected JPanel posActionsListPanel;
	protected JScrollPane postActionsListScroller;
	protected JList posActionsList;
	protected JButton btnPosActionsRefresh;
	protected JButton btnPosActionsChart;
	protected JPanel algorithmsPanel;
	protected JTabbedPane algorithmsTabPane;
	
	protected JPanel randomSearchPanel;
	protected JLabel lblIterationCount_rnd;
	protected JSpinner rsIterationCount;
	protected JButton rsBtnStart;
	protected JButton rsBtnAddTask;
	protected Component hs6;
	protected Component horizontalGlue;
	
	protected JPanel hillClimbingPanel;
	protected JCheckBox hcFirstDescent;
	protected JLabel lblRestartCount;
	protected JSpinner hcRestartCount;
	protected JLabel lblRestartDepth;
	protected JSpinner hcRestartDepth;
	protected JCheckBox hcLimitIterations;
	protected JButton hcBtnStart;
	protected JButton hcBtnAddTask;
	protected Component hg1;
	protected Component hs50;
	protected Component hs1;
	protected Component hs2;
	protected Component hs3;
	protected Component hs30;
	
	protected JPanel simAnnPanel;
	protected JLabel lblStartTemp;
	protected JSpinner saStartTemp;
	protected JLabel lblIterationCount;
	protected JSpinner saIterationCnt;
	protected JButton saBtnStart;
	protected JButton saBtnAddTask;
	protected Component hs40;
	protected Component hg20;
	protected Component hs4;
	protected Component hg2;
	
	protected JPanel beeColonyPanel;
	protected JCheckBox abcParallel;
	protected JLabel lblPopulationSize;
	protected JSpinner abcPopSize;
	protected JLabel lblMaximumTrials;
	protected JSpinner abcMaxTrials;
	protected JLabel lblIterations_1;
	protected JSpinner abcIterations;
	protected JButton abcBtnStart;
	protected JButton abcBtnAddTask;
	protected Component horizontalStrut_2;
	protected Component hs10;
	protected Component hs11;
	protected Component hs12;
	protected Component hs13;
	protected Component hs14;
	protected Component horizontalGlue_1;
	
	protected JPanel beamSearchPanel;
	protected JCheckBox bsIsStochastic;
	protected JLabel lblBeamLength;
	protected JSpinner bsBeamLength;
	protected JLabel lblRandomizationDepth;
	protected JSpinner bsRandomDepth;
	protected JLabel lblIterations;
	protected JSpinner bsIterations;
	protected JButton bsBtnStart;
	protected JButton bsBtnAddTask;
	protected Component hs100;
	protected Component hs5;
	protected Component hs7;
	protected Component hs9;
	protected Component hs8;
	protected Component horizontalStrut_1;
	protected Component hg3;
	
	protected JButton btnSave;
	protected JButton btnPreset;
	protected JPanel configPanel;
	protected JComboBox runConfigBox;
	protected JButton btnConfigure;
	protected Component horizontalStrut;
	protected JPanel appliedActionsPanel;
	protected JScrollPane appliedActionsScrollPane;
	protected JList appliedActionsList;
	protected JPanel appliedActionsButtonPanel;
	protected JButton appliedActionsRefreshButton;
	protected JPanel appliedActionsScrollPanePanel;

	public DesignPanelBase() {
		setOpaque(false);
		setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(tabbedPane);
		
		metricsPanel = new JPanel();
		metricsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		metricsPanel.setOpaque(false);
		tabbedPane.addTab("Metrics", new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/statistics2_16.png")), metricsPanel, null);
		
		scrollPane = new JScrollPane();
		scrollPane.setOpaque(false);
		
		metricTable = new JTable();
		metricTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		metricTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrollPane.setViewportView(metricTable);
		
		chartPanel = new JPanel();
		chartPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		chartPanel.setOpaque(false);
		
		infoPanel = new JPanel();
		infoPanel.setBorder(new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Information", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		infoPanel.setOpaque(false);
		GroupLayout gl_metricsPanel = new GroupLayout(metricsPanel);
		gl_metricsPanel.setHorizontalGroup(
			gl_metricsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_metricsPanel.createSequentialGroup()
					.addComponent(chartPanel, GroupLayout.DEFAULT_SIZE, 643, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(infoPanel, GroupLayout.PREFERRED_SIZE, 198, GroupLayout.PREFERRED_SIZE))
				.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 847, Short.MAX_VALUE)
		);
		gl_metricsPanel.setVerticalGroup(
			gl_metricsPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_metricsPanel.createSequentialGroup()
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 364, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_metricsPanel.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(infoPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(chartPanel, GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)))
		);
		GridBagLayout gbl_infoPanel = new GridBagLayout();
		gbl_infoPanel.columnWidths = new int[]{88, 88, 0};
		gbl_infoPanel.rowHeights = new int[]{25, 25, 25, 25, 25, 25, 25, 0};
		gbl_infoPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_infoPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		infoPanel.setLayout(gbl_infoPanel);
		
		lblNumberOfMetrics = new JLabel("# Metrics:");
		GridBagConstraints gbc_lblNumberOfMetrics = new GridBagConstraints();
		gbc_lblNumberOfMetrics.fill = GridBagConstraints.BOTH;
		gbc_lblNumberOfMetrics.insets = new Insets(0, 0, 5, 5);
		gbc_lblNumberOfMetrics.gridx = 0;
		gbc_lblNumberOfMetrics.gridy = 0;
		infoPanel.add(lblNumberOfMetrics, gbc_lblNumberOfMetrics);
		
		lblValNumMetrics = new JLabel("");
		lblValNumMetrics.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblValNumMetrics = new GridBagConstraints();
		gbc_lblValNumMetrics.fill = GridBagConstraints.BOTH;
		gbc_lblValNumMetrics.insets = new Insets(0, 0, 5, 0);
		gbc_lblValNumMetrics.gridx = 1;
		gbc_lblValNumMetrics.gridy = 0;
		infoPanel.add(lblValNumMetrics, gbc_lblValNumMetrics);
		
		lblItems = new JLabel("# Items:");
		GridBagConstraints gbc_lblItems = new GridBagConstraints();
		gbc_lblItems.fill = GridBagConstraints.BOTH;
		gbc_lblItems.insets = new Insets(0, 0, 5, 5);
		gbc_lblItems.gridx = 0;
		gbc_lblItems.gridy = 1;
		infoPanel.add(lblItems, gbc_lblItems);
		
		lblValNumItems = new JLabel("");
		lblValNumItems.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblValNumItems = new GridBagConstraints();
		gbc_lblValNumItems.fill = GridBagConstraints.BOTH;
		gbc_lblValNumItems.insets = new Insets(0, 0, 5, 0);
		gbc_lblValNumItems.gridx = 1;
		gbc_lblValNumItems.gridy = 1;
		infoPanel.add(lblValNumItems, gbc_lblValNumItems);
		
		lblScore = new JLabel("<html><b>Score:</b></html>");
		GridBagConstraints gbc_lblScore = new GridBagConstraints();
		gbc_lblScore.fill = GridBagConstraints.BOTH;
		gbc_lblScore.insets = new Insets(0, 0, 5, 5);
		gbc_lblScore.gridx = 0;
		gbc_lblScore.gridy = 3;
		infoPanel.add(lblScore, gbc_lblScore);
		
		lblValWeightedSum = new JLabel("");
		lblValWeightedSum.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblValWeightedSum = new GridBagConstraints();
		gbc_lblValWeightedSum.fill = GridBagConstraints.BOTH;
		gbc_lblValWeightedSum.insets = new Insets(0, 0, 5, 0);
		gbc_lblValWeightedSum.gridx = 1;
		gbc_lblValWeightedSum.gridy = 3;
		infoPanel.add(lblValWeightedSum, gbc_lblValWeightedSum);
		
		btnSave = new JButton("Save CSV");
		btnSave.setActionCommand(Actions.SAVE_METRICS);
		btnSave.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/save_16.png")));
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.gridwidth = 2;
		gbc_btnSave.fill = GridBagConstraints.BOTH;
		gbc_btnSave.gridx = 0;
		gbc_btnSave.gridy = 5;
		infoPanel.add(btnSave, gbc_btnSave);
		
		btnPreset = new JButton("Add to Normalization List");
		btnPreset.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/add.png")));
		GridBagConstraints gbc_btnPreset = new GridBagConstraints();
		gbc_btnPreset.gridwidth = 2;
		gbc_btnPreset.fill = GridBagConstraints.BOTH;
		gbc_btnPreset.gridx = 0;
		gbc_btnPreset.gridy = 6;
		infoPanel.add(btnPreset, gbc_btnPreset);
		chartPanel.setLayout(new BorderLayout(0, 0));
		metricsPanel.setLayout(gl_metricsPanel);
		
		actionsPanel = new JPanel();
		actionsPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		actionsPanel.setOpaque(false);
		tabbedPane.addTab("Refactor", new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/misc3_16.png")), actionsPanel, null);
		
		posActionsPanel = new JPanel();
		posActionsPanel.setBorder(new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Possible Actions", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		posActionsPanel.setOpaque(false);
		
		algorithmsPanel = new JPanel();
		algorithmsPanel.setOpaque(false);
		algorithmsPanel.setBorder(new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Initiate Search", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		
		algorithmsTabPane = new JTabbedPane(JTabbedPane.TOP);
		GroupLayout gl_algorithmsPanel = new GroupLayout(algorithmsPanel);
		gl_algorithmsPanel.setHorizontalGroup(
			gl_algorithmsPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(algorithmsTabPane, GroupLayout.DEFAULT_SIZE, 825, Short.MAX_VALUE)
		);
		gl_algorithmsPanel.setVerticalGroup(
			gl_algorithmsPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(algorithmsTabPane, GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
		);
		
		randomSearchPanel = new JPanel();
		randomSearchPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		randomSearchPanel.setOpaque(false);
		algorithmsTabPane.addTab("Random Search", null, randomSearchPanel, null);
		randomSearchPanel.setLayout(new BoxLayout(randomSearchPanel, BoxLayout.X_AXIS));
		
		lblIterationCount_rnd = new JLabel("Iteration Count:");
		randomSearchPanel.add(lblIterationCount_rnd);
		
		hs6 = Box.createHorizontalStrut(5);
		randomSearchPanel.add(hs6);
		
		rsIterationCount = new JSpinner();
//		rsIterationCount.setModel(new SpinnerNumberModel(1000, 0, 1000000000, 1));
		rsIterationCount.setModel(new SpinnerNumberModel(500, 0, 1000000000, 1));
		randomSearchPanel.add(rsIterationCount);
		
		horizontalGlue = Box.createHorizontalGlue();
		randomSearchPanel.add(horizontalGlue);
		
		rsBtnStart = new JButton("Start");
		rsBtnStart.setActionCommand("RS");
		rsBtnStart.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/next_16.png")));
		randomSearchPanel.add(rsBtnStart);
		
		rsBtnAddTask = new JButton("Add Task");
		rsBtnAddTask.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/save_16.png")));
		rsBtnAddTask.setActionCommand("RS");
		randomSearchPanel.add(rsBtnAddTask);
		
		hillClimbingPanel = new JPanel();
		hillClimbingPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		hillClimbingPanel.setOpaque(false);
		algorithmsTabPane.addTab("Hill Climbing", null, hillClimbingPanel, null);
		hillClimbingPanel.setLayout(new BoxLayout(hillClimbingPanel, BoxLayout.X_AXIS));
		
		hcFirstDescent = new JCheckBox("First Descent");
		hcFirstDescent.setOpaque(false);
		hillClimbingPanel.add(hcFirstDescent);
		
		hs50 = Box.createHorizontalStrut(5);
		hillClimbingPanel.add(hs50);
		
		lblRestartCount = new JLabel("Restart Count:");
		hillClimbingPanel.add(lblRestartCount);
		
		hs1 = Box.createHorizontalStrut(5);
		hillClimbingPanel.add(hs1);
		
		hcRestartCount = new JSpinner();
//		hcRestartCount.setModel(new SpinnerNumberModel(5, -1, 100, 1));
		hcRestartCount.setModel(new SpinnerNumberModel(5, 0, 100, 1));
		hillClimbingPanel.add(hcRestartCount);
		
		hs2 = Box.createHorizontalStrut(5);
		hillClimbingPanel.add(hs2);
		
		lblRestartDepth = new JLabel("Restart Depth:");
		hillClimbingPanel.add(lblRestartDepth);
		
		hs3 = Box.createHorizontalStrut(5);
		hillClimbingPanel.add(hs3);
		
		hcRestartDepth = new JSpinner();
		hcRestartDepth.setModel(new SpinnerNumberModel(20, 0, 1000, 1));
		hillClimbingPanel.add(hcRestartDepth);
		
		hs30 = Box.createHorizontalStrut(5);
		hillClimbingPanel.add(hs30);
		
		hcLimitIterations = new JCheckBox("Limit Iterations");
		hcLimitIterations.setOpaque(false);
		hillClimbingPanel.add(hcLimitIterations);
		
		hg1 = Box.createHorizontalGlue();
		hillClimbingPanel.add(hg1);
		
		hcBtnStart = new JButton("Start");
		hcBtnStart.setActionCommand("HC");
		hcBtnStart.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/next_16.png")));
		hillClimbingPanel.add(hcBtnStart);
		
		hcBtnAddTask = new JButton("Add Task");
		hcBtnAddTask.setActionCommand("HC");
		hcBtnAddTask.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/save_16.png")));
		hillClimbingPanel.add(hcBtnAddTask);
		
		simAnnPanel = new JPanel();
		simAnnPanel.setOpaque(false);
		simAnnPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		algorithmsTabPane.addTab("Simulated Annealing", null, simAnnPanel, null);
		simAnnPanel.setLayout(new BoxLayout(simAnnPanel, BoxLayout.X_AXIS));
		
		lblStartTemp = new JLabel("Starting Temperature: ");
		simAnnPanel.add(lblStartTemp);
		
		hs40 = Box.createHorizontalStrut(5);
		simAnnPanel.add(hs40);
		
		saStartTemp = new JSpinner();
		saStartTemp.setModel(new SpinnerNumberModel(4, 0, 100, 0.5));
		simAnnPanel.add(saStartTemp);
		
		hg20 = Box.createHorizontalStrut(5);
		simAnnPanel.add(hg20);
		
		lblIterationCount = new JLabel("Iteration Count:");
		simAnnPanel.add(lblIterationCount);
		
		hs4 = Box.createHorizontalStrut(5);
		simAnnPanel.add(hs4);
		
		saIterationCnt = new JSpinner();
		saIterationCnt.setModel(new SpinnerNumberModel(5000, 100, 1000000, 250));
		simAnnPanel.add(saIterationCnt);
		
		hg2 = Box.createHorizontalGlue();
		simAnnPanel.add(hg2);
		
		saBtnStart = new JButton("Start");
		saBtnStart.setActionCommand("SA");
		saBtnStart.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/next_16.png")));
		simAnnPanel.add(saBtnStart);
		
		saBtnAddTask = new JButton("Add Task");
		saBtnAddTask.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/save_16.png")));
		saBtnAddTask.setActionCommand("SA");
		simAnnPanel.add(saBtnAddTask);
		
		beeColonyPanel = new JPanel();
		beeColonyPanel.setOpaque(false);
		beeColonyPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		algorithmsTabPane.addTab("Artificial Bee Colony", null, beeColonyPanel, null);
		beeColonyPanel.setLayout(new BoxLayout(beeColonyPanel, BoxLayout.X_AXIS));
		
		abcParallel = new JCheckBox("Parallel");
		abcParallel.setOpaque(false);
		beeColonyPanel.add(abcParallel);
		
		horizontalStrut_2 = Box.createHorizontalStrut(5);
		beeColonyPanel.add(horizontalStrut_2);
		
		lblPopulationSize = new JLabel("Population Size:");
		beeColonyPanel.add(lblPopulationSize);
		
		hs10 = Box.createHorizontalStrut(5);
		beeColonyPanel.add(hs10);
		
		abcPopSize = new JSpinner();
		abcPopSize.setModel(new SpinnerNumberModel(70, 1, 5000, 1));
		beeColonyPanel.add(abcPopSize);
		
		hs11 = Box.createHorizontalStrut(5);
		beeColonyPanel.add(hs11);
		
		lblMaximumTrials = new JLabel("Maximum Trials:");
		beeColonyPanel.add(lblMaximumTrials);
		
		hs12 = Box.createHorizontalStrut(5);
		beeColonyPanel.add(hs12);
		
		abcMaxTrials = new JSpinner();
		abcMaxTrials.setModel(new SpinnerNumberModel(50, 10, 500, 1));
		beeColonyPanel.add(abcMaxTrials);
		
		hs13 = Box.createHorizontalStrut(5);
		beeColonyPanel.add(hs13);
		
		lblIterations_1 = new JLabel("Iteration Count:");
		beeColonyPanel.add(lblIterations_1);
		
		hs14 = Box.createHorizontalStrut(5);
		beeColonyPanel.add(hs14);
		
		abcIterations = new JSpinner();
		abcIterations.setModel(new SpinnerNumberModel(5000, 10, 1000000, 1));
		beeColonyPanel.add(abcIterations);
		
		horizontalGlue_1 = Box.createHorizontalGlue();
		beeColonyPanel.add(horizontalGlue_1);
		
		abcBtnStart = new JButton("Start");
		abcBtnStart.setActionCommand("ABC");
		abcBtnStart.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/next_16.png")));
		beeColonyPanel.add(abcBtnStart);
		
		abcBtnAddTask = new JButton("Add Task");
		abcBtnAddTask.setActionCommand("ABC");
		abcBtnAddTask.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/save_16.png")));
		beeColonyPanel.add(abcBtnAddTask);		
		
		beamSearchPanel = new JPanel();
		beamSearchPanel.setOpaque(false);
		beamSearchPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		algorithmsTabPane.addTab("Beam Search", null, beamSearchPanel, null);
		beamSearchPanel.setLayout(new BoxLayout(beamSearchPanel, BoxLayout.X_AXIS));
		
		bsIsStochastic = new JCheckBox("Stochastic");
		bsIsStochastic.setOpaque(false);
		beamSearchPanel.add(bsIsStochastic);
		
		hs100 = Box.createHorizontalStrut(5);
		beamSearchPanel.add(hs100);
		
		lblBeamLength = new JLabel("Beam Length:");
		beamSearchPanel.add(lblBeamLength);
		
		hs5 = Box.createHorizontalStrut(5);
		beamSearchPanel.add(hs5);
		
		bsBeamLength = new JSpinner();
//		bsBeamLength.setModel(new SpinnerNumberModel(50, 1, 1000, 1));
		bsBeamLength.setModel(new SpinnerNumberModel(5, 1, 1000, 1));
		beamSearchPanel.add(bsBeamLength);
		
		hs7 = Box.createHorizontalStrut(5);
		beamSearchPanel.add(hs7);
		
		lblRandomizationDepth = new JLabel("Randomization Depth:");
		beamSearchPanel.add(lblRandomizationDepth);
		
		hs9 = Box.createHorizontalStrut(5);
		beamSearchPanel.add(hs9);
		
		bsRandomDepth = new JSpinner();
//		bsRandomDepth.setModel(new SpinnerNumberModel(100, 10, 2000, 1));
		bsRandomDepth.setModel(new SpinnerNumberModel(10, 10, 2000, 1));
		beamSearchPanel.add(bsRandomDepth);
		
		hs8 = Box.createHorizontalStrut(5);
		beamSearchPanel.add(hs8);
		
		lblIterations = new JLabel("Iteration Count:");
		beamSearchPanel.add(lblIterations);
		
		horizontalStrut_1 = Box.createHorizontalStrut(5);
		beamSearchPanel.add(horizontalStrut_1);
		
		bsIterations = new JSpinner();
//		bsIterations.setModel(new SpinnerNumberModel(500, 5, 1000000, 1));
		bsIterations.setModel(new SpinnerNumberModel(50, 5, 1000000, 1));
		beamSearchPanel.add(bsIterations);
		
		hg3 = Box.createHorizontalGlue();
		beamSearchPanel.add(hg3);
		
		bsBtnStart = new JButton("Start");
		bsBtnStart.setActionCommand("BS");
		bsBtnStart.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/next_16.png")));
		beamSearchPanel.add(bsBtnStart);
		
		bsBtnAddTask = new JButton("Add Task");
		bsBtnAddTask.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/save_16.png")));
		bsBtnAddTask.setActionCommand("BS");
		beamSearchPanel.add(bsBtnAddTask);
		algorithmsPanel.setLayout(gl_algorithmsPanel);
		actionsPanel.setLayout(new BorderLayout(0, 0));
		
		posActionsListPanel = new JPanel();
		posActionsListPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		posActionsListPanel.setOpaque(false);
		
		btnPosActionsRefresh = new JButton("");
		btnPosActionsRefresh.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/refresh.png")));
		
		btnPosActionsChart = new JButton("");
		btnPosActionsChart.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/statistics2_16.png")));
		GroupLayout gl_posActionsPanel = new GroupLayout(posActionsPanel);
		gl_posActionsPanel.setHorizontalGroup(
			gl_posActionsPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_posActionsPanel.createSequentialGroup()
					.addComponent(posActionsListPanel, GroupLayout.DEFAULT_SIZE, 776, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_posActionsPanel.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(btnPosActionsChart, 0, 0, Short.MAX_VALUE)
						.addComponent(btnPosActionsRefresh, GroupLayout.PREFERRED_SIZE, 42, Short.MAX_VALUE)))
		);
		gl_posActionsPanel.setVerticalGroup(
			gl_posActionsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_posActionsPanel.createSequentialGroup()
					.addComponent(btnPosActionsRefresh)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnPosActionsChart)
					.addContainerGap(147, Short.MAX_VALUE))
				.addComponent(posActionsListPanel, GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
		);
		posActionsListPanel.setLayout(new BorderLayout(0, 0));
		
		postActionsListScroller = new JScrollPane();
		postActionsListScroller.setBorder(null);
		posActionsListPanel.add(postActionsListScroller);
		
		posActionsList = new JList();
		posActionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		posActionsList.setCellRenderer(new PossibleActionsRenderer());
		postActionsListScroller.setViewportView(posActionsList);
		posActionsPanel.setLayout(gl_posActionsPanel);
		actionsPanel.add(posActionsPanel, BorderLayout.CENTER);
		actionsPanel.add(algorithmsPanel, BorderLayout.NORTH);
		
		appliedActionsPanel = new JPanel();
		appliedActionsPanel.setOpaque(false);
		tabbedPane.addTab("Applied Actions", new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/misc1_16.png")), appliedActionsPanel, null);
		appliedActionsPanel.setLayout(new BorderLayout(0, 0));
		
		appliedActionsButtonPanel = new JPanel();
		appliedActionsButtonPanel.setOpaque(false);
		appliedActionsPanel.add(appliedActionsButtonPanel, BorderLayout.EAST);
		
		appliedActionsRefreshButton = new JButton("");
		appliedActionsRefreshButton.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/refresh.png")));
		appliedActionsButtonPanel.add(appliedActionsRefreshButton);
		
		appliedActionsScrollPanePanel = new JPanel();
		appliedActionsScrollPanePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		appliedActionsPanel.add(appliedActionsScrollPanePanel, BorderLayout.CENTER);
		appliedActionsScrollPanePanel.setLayout(new BorderLayout(0, 0));
		
		appliedActionsScrollPane = new JScrollPane();
		appliedActionsScrollPanePanel.add(appliedActionsScrollPane);
		appliedActionsScrollPanePanel.setOpaque(false);
		appliedActionsScrollPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		
		appliedActionsList = new JList();
		appliedActionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		appliedActionsList.setCellRenderer(new AppliedActionsRenderer());
		appliedActionsScrollPane.setViewportView(appliedActionsList);
		
		configPanel = new JPanel();
		configPanel.setOpaque(false);
		configPanel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 0, 5), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Metric & Action Configuration", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		add(configPanel, BorderLayout.NORTH);
		configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.X_AXIS));
		
		runConfigBox = new JComboBox();
		runConfigBox.setModel(new DefaultComboBoxModel(new String[] {"Default"}));
		configPanel.add(runConfigBox);
		
		horizontalStrut = Box.createHorizontalStrut(5);
		configPanel.add(horizontalStrut);
		
		btnConfigure = new JButton("Configure");
		btnConfigure.setIcon(new ImageIcon(DesignPanelBase.class.getResource("/resources/icons/engine_16.png")));
		configPanel.add(btnConfigure);
		
		btnConfigure.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new RunConfigDialog().setVisible(true);
			}
		});
	}
	
	private static class PossibleActionsRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		
		private static final Icon actionIcon = new ImageIcon(PossibleActionsRenderer.class.getResource("/resources/icons/misc3_16.png"));
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			
			setIcon(actionIcon);
			
			return this;
		}
	}
	
	private static class AppliedActionsRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		
		private static final Icon actionIcon = new ImageIcon(AppliedActionsRenderer.class.getResource("/resources/icons/misc1_16.png"));
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			
			setIcon(actionIcon);
			
			return this;
		}
	}
}
