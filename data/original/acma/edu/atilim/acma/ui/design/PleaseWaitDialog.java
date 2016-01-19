package edu.atilim.acma.ui.design;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class PleaseWaitDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	protected JPanel panel;
	protected JLabel lblPleaseWait;

	/**
	 * Create the dialog.
	 */
	public PleaseWaitDialog() {
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
		lblPleaseWait.setIcon(new ImageIcon(PleaseWaitDialog.class.getResource("/resources/icons/button-yellow.png")));
		lblPleaseWait.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblPleaseWait);

	}

}
