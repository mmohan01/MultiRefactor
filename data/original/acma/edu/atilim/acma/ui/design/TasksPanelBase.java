package edu.atilim.acma.ui.design;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class TasksPanelBase extends JPanel {
	private static final long serialVersionUID = 1L;
	protected JPanel panel;
	protected JScrollPane scrollPane;
	protected JList taskList;
	protected JPanel buttonPanel;
	protected JButton deleteButton;
	protected JButton deleteAllButton;
	protected JButton btnPareto;

	public TasksPanelBase() {
		setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Tasks", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(panel, BorderLayout.CENTER);
		panel.setOpaque(false);
		panel.setLayout(new BorderLayout(0, 0));
		
		scrollPane = new JScrollPane();
		scrollPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.add(scrollPane, BorderLayout.CENTER);
		
		taskList = new JList();
		taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		taskList.setCellRenderer(new TaskListRenderer());
		scrollPane.setViewportView(taskList);
		
		buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		
		btnPareto = new JButton("");
		btnPareto.setActionCommand("PARETO");
		btnPareto.setIcon(new ImageIcon(TasksPanelBase.class.getResource("/resources/icons/misc1_16.png")));
		buttonPanel.add(btnPareto);
		
		deleteButton = new JButton("");
		deleteButton.setActionCommand("delete");
		deleteButton.setIcon(new ImageIcon(TasksPanelBase.class.getResource("/resources/icons/delete.png")));
		buttonPanel.add(deleteButton);
		
		deleteAllButton = new JButton("");
		deleteAllButton.setActionCommand("deleteall");
		deleteAllButton.setIcon(new ImageIcon(TasksPanelBase.class.getResource("/resources/icons/stop_16.png")));
		buttonPanel.add(deleteAllButton);
	}
	
	private static class TaskListRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		
		private static final Icon actionIcon = new ImageIcon(TaskListRenderer.class.getResource("/resources/icons/box_16.png"));
		
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
