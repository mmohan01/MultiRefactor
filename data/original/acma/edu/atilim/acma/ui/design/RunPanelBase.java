package edu.atilim.acma.ui.design;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class RunPanelBase extends JPanel {
	private static final long serialVersionUID = 1L;
	protected JTabbedPane tabbedPane;
	protected JPanel runPanel;
	protected JPanel statusPanel;
	protected JProgressBar progressBar;
	protected JButton pauseContinueButton;
	protected JPanel logPanel;
	protected JScrollPane scrollPane;
	protected JTextArea logArea;
	protected JPanel chartPanel;
	
	protected static final Icon playIcon = new ImageIcon(RunPanelBase.class.getResource("/resources/icons/play_16.png"));
	protected static final Icon pauseIcon = new ImageIcon(RunPanelBase.class.getResource("/resources/icons/pause_16.png"));
	protected JButton closeButton;

	public RunPanelBase() {
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		runPanel = new JPanel();
		runPanel.setOpaque(false);
		tabbedPane.addTab("Refactoring Run", new ImageIcon(RunPanelBase.class.getResource("/resources/icons/info_16.png")), runPanel, null);
		runPanel.setLayout(new BorderLayout(0, 0));
		
		statusPanel = new JPanel();
		statusPanel.setOpaque(false);
		statusPanel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Status", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		runPanel.add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new BorderLayout(5, 5));
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		statusPanel.add(progressBar);
		
		pauseContinueButton = new JButton("");
		pauseContinueButton.setIcon(new ImageIcon(RunPanelBase.class.getResource("/resources/icons/play_16.png")));
		statusPanel.add(pauseContinueButton, BorderLayout.EAST);
		
		closeButton = new JButton("");
		closeButton.setIcon(new ImageIcon(RunPanelBase.class.getResource("/resources/icons/stop_16.png")));
		statusPanel.add(closeButton, BorderLayout.WEST);
		
		logPanel = new JPanel();
		logPanel.setOpaque(false);
		logPanel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Run Log", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		runPanel.add(logPanel, BorderLayout.CENTER);
		logPanel.setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		logPanel.add(scrollPane, BorderLayout.CENTER);
		
		logArea = new JTextArea();
		logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
		scrollPane.setViewportView(logArea);
		
		chartPanel = new JPanel();
		chartPanel.setPreferredSize(new Dimension(0, 250));
		chartPanel.setOpaque(false);
		chartPanel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Design Score", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		runPanel.add(chartPanel, BorderLayout.NORTH);
		chartPanel.setLayout(new BorderLayout(0, 0));

	}
}
