package edu.atilim.acma.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;

import edu.atilim.acma.design.Design;
import edu.atilim.acma.search.AbstractAlgorithm;
import edu.atilim.acma.search.AlgorithmObserver;
import edu.atilim.acma.search.SolutionDesign;
import edu.atilim.acma.ui.design.RunPanelBase;

public class RunPanel extends RunPanelBase implements AlgorithmObserver {
	private static final long serialVersionUID = 1L;
	
	private long startTime;
	private long endTime;

	private TimeSeries currentSeries;
	private TimeSeries bestSeries;
	
	private AbstractAlgorithm algorithm;
	
	private Timer chartTimer;
	private double currentScore = Double.NaN, bestScore = Double.NaN;
	
	public RunPanel(AbstractAlgorithm algorithm) {
		this.algorithm = algorithm;
		this.algorithm.setObserver(this);
		
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int answer = JOptionPane.showConfirmDialog(RunPanel.this, "Do you want to close this task?", "Close task", JOptionPane.YES_NO_OPTION);
				
				if (answer == JOptionPane.YES_OPTION) {
					RunPanel.this.chartTimer.stop();
					
					RunPanel.this.algorithm.pause();
					RunPanel.this.algorithm = null;
					RunPanel.this.getParent().remove(RunPanel.this);
				}
			}
		});
		
		pauseContinueButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int curstate = RunPanel.this.algorithm.getState();
				if (curstate == AbstractAlgorithm.STATE_NEW || curstate == AbstractAlgorithm.STATE_PAUSED) {
					RunPanel.this.chartTimer.start();
					RunPanel.this.algorithm.start();
					pauseContinueButton.setIcon(pauseIcon);
				} else if (curstate == AbstractAlgorithm.STATE_RUNNING) {
					RunPanel.this.chartTimer.stop();
					RunPanel.this.algorithm.pause();
					pauseContinueButton.setIcon(playIcon);
				}
			}
		});
		
		initChart();
		
		this.algorithm.start();
		this.chartTimer.start();
		pauseContinueButton.setIcon(pauseIcon);
	}
	
	private void initChart() {
		currentSeries = new TimeSeries("Current");
		currentSeries.setMaximumItemCount(150);
		bestSeries = new TimeSeries("Best");
		bestSeries.setMaximumItemCount(150);
		
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(currentSeries);
		dataset.addSeries(bestSeries);
		
		JFreeChart chart = ChartFactory.createTimeSeriesChart(null, "Time", "Metric Score", dataset, true, false, false);
		
		chart.getLegend().setPosition(RectangleEdge.RIGHT);
		
		XYPlot plot = chart.getXYPlot();
		plot.getDomainAxis().setVisible(false);
		
		ChartPanel panel = new ChartPanel(chart);
		panel.setOpaque(false);
		
		chartPanel.add(panel, BorderLayout.CENTER);
		
		chartTimer = new Timer(500, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!Double.isNaN(currentScore)) currentSeries.addOrUpdate(new Millisecond(), currentScore);
				if (!Double.isNaN(bestScore)) bestSeries.addOrUpdate(new Millisecond(), bestScore);
			}
		});
	}

	@Override
	public void onStart(AbstractAlgorithm algorithm, SolutionDesign initial) {
		final Design idesign = initial.getDesign().copy();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				startTime = System.currentTimeMillis();
				
				DesignPanel dp = new DesignPanel(idesign);
				dp.setCompactView(true);

				tabbedPane.addTab("Initial Design", 
						new ImageIcon(RunPanel.class.getResource("/resources/icons/design_16.png")), 
						dp, 
						null);
			}
		});
	}

	@Override
	public void onFinish(final AbstractAlgorithm algorithm, SolutionDesign last) {
		final Design ldesign = last.getDesign().copy();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				endTime = System.currentTimeMillis();
				
				pauseContinueButton.setEnabled(false);
				chartTimer.stop();
				
				DesignPanel dp = new DesignPanel(ldesign);
				
				dp.setCompactView(true);

				tabbedPane.addTab("Final Design", 
						new ImageIcon(RunPanel.class.getResource("/resources/icons/design_16.png")), 
						dp, 
						null);
				
				onLog(algorithm, String.format("Entire process took %.4f seconds", (endTime - startTime) / 1000.0));
			}
		});
	}

	@Override
	public void onExpansion(AbstractAlgorithm algorithm, int currentExpanded) {

	}
	
	@Override
	public void onUpdateItems(AbstractAlgorithm algorithm,
			final SolutionDesign current, final SolutionDesign best, final int updateType) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (updateType == AlgorithmObserver.UPDATE_CURRENT) {
					currentScore = current.getScore();
				}
				else {
					bestScore = best.getScore();
				}
			}
		});
	}

	@Override
	public void onLog(final AbstractAlgorithm algorithm, final String log) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				logArea.append(log + "\n");
			}
		});
	}
	
	@Override
	public void onAdvance(AbstractAlgorithm algorithm, final int current, final int total) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progressBar.setMaximum(total);
				progressBar.setValue(current);
			}
		});
	}
	
	@Override
	public void onStep(AbstractAlgorithm algorithm, int step) {
	}
}
