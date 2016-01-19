package edu.atilim.acma.ui.design;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class ConsolePanelBase extends JPanel {
	private static final long serialVersionUID = 1L;
	protected JScrollPane scrollPane;
	protected JTextArea out;
	protected JPanel panel;

	public ConsolePanelBase() {
		setOpaque(false);
		setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		panel.setOpaque(false);
		panel.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), new EtchedBorder(EtchedBorder.LOWERED, null, null)));
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		panel.add(scrollPane);
		scrollPane.setOpaque(false);
		scrollPane.setBorder(null);
		
		out = new JTextArea();
		out.setEditable(false);
		out.setFont(new Font("Monospaced", Font.PLAIN, 11));
		scrollPane.setViewportView(out);
	}
}
