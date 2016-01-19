package edu.atilim.acma.ui;

import javax.swing.JDialog;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Toolkit;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JButton;
import javax.swing.ImageIcon;

public class RunConfigDialog extends JDialog implements WindowListener {

	private static final long serialVersionUID = 1L;
	
	protected JPanel panel;
	protected RunConfigPanel runConfigPanel;
	protected JPanel buttonPanel;
	protected JButton saveButton;
	protected JButton cancelButton;

	public RunConfigDialog() {
		super(MainWindow.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		setIconImage(Toolkit.getDefaultToolkit().getImage(RunConfigDialog.class.getResource("/resources/acmaicon.png")));
		setTitle("Run Configurations");
		setBounds(100, 100, 740, 550);
		setResizable(false);
		
		panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		runConfigPanel = new RunConfigPanel();
		panel.add(runConfigPanel);
		
		buttonPanel = new JPanel();
		FlowLayout fl_buttonPanel = (FlowLayout) buttonPanel.getLayout();
		fl_buttonPanel.setVgap(0);
		fl_buttonPanel.setHgap(0);
		fl_buttonPanel.setAlignment(FlowLayout.RIGHT);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		
		saveButton = new JButton("Save");
		saveButton.setIcon(new ImageIcon(RunConfigDialog.class.getResource("/resources/icons/save_16.png")));
		buttonPanel.add(saveButton);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setIcon(new ImageIcon(RunConfigDialog.class.getResource("/resources/icons/stop_16.png")));
		buttonPanel.add(cancelButton);
		
		setLocationRelativeTo(MainWindow.getInstance());
		
		init();
	}
	
	private void init() {
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConfigManager.saveChanges();
				RunConfigDialog.this.dispose();
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				RunConfigDialog.this.dispose();
			}
		});
		
		addWindowListener(this);
	}

	@Override
	public void windowActivated(WindowEvent arg0) {

	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		ConfigManager.discardChanges();
	}

	@Override
	public void windowClosing(WindowEvent arg0) {

	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {

	}

	@Override
	public void windowIconified(WindowEvent arg0) {

	}

	@Override
	public void windowOpened(WindowEvent arg0) {

	}
}
