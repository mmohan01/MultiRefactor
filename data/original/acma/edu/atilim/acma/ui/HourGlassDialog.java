package edu.atilim.acma.ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class HourGlassDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	protected JPanel panel;
	protected JLabel lblPleaseWait;

	public HourGlassDialog() {
		super(MainWindow.getInstance(), true);
		
		setUndecorated(true);
		setTitle("Working");
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setBounds(100, 100, 188, 75);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		getContentPane().add(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		lblPleaseWait = new JLabel("Please wait...");
		lblPleaseWait.setIcon(new ImageIcon(HourGlassDialog.class.getResource("/resources/icons/button-yellow.png")));
		lblPleaseWait.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblPleaseWait);

		setLocationRelativeTo(MainWindow.getInstance());
	}

}
