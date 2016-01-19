package edu.atilim.acma.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Toolkit;

public class PanelDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	public static void display(JPanel panel, int w, int h) {
		PanelDialog dialog = new PanelDialog();
		dialog.setBounds(100, 100, w, h);
		dialog.contentPanel.add(panel);
		dialog.setLocationRelativeTo(MainWindow.getInstance());
		dialog.setVisible(true);
	}
	
	private final JPanel contentPanel = new JPanel();

	public PanelDialog() {
		super(MainWindow.getInstance(), true);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(false);
		
		setTitle("A-CMA");
		setIconImage(Toolkit.getDefaultToolkit().getImage(PanelDialog.class.getResource("/resources/acmaicon.png")));
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
	}
}
