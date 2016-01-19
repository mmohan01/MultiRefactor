package edu.atilim.acma.ui.design;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Package;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.ui.Actions;
import edu.atilim.acma.ui.MainWindow;

public class LoadedDesignsPanelBase extends JPanel {
	private static final long serialVersionUID = 1L;
	protected JPanel panel;
	protected JScrollPane scrollPane;
	protected JTree designTree;
	protected JPanel buttonPanel;
	protected JButton btnUnload;
	protected JButton btnRefresh;
	protected Component horizontalGlue;
	protected JButton btnLoad;
	protected DefaultMutableTreeNode rootNode;

	public LoadedDesignsPanelBase() {
		setBorder(new TitledBorder(null, "Loaded Designs", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BorderLayout(0, 0));
		
		rootNode = new DefaultMutableTreeNode("Designs");
		
		buttonPanel = new JPanel();
		add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setOpaque(false);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		btnRefresh = new JButton("");
		btnRefresh.setIcon(new ImageIcon(LoadedDesignsPanelBase.class.getResource("/resources/icons/refresh.png")));
		buttonPanel.add(btnRefresh);
		
		horizontalGlue = Box.createHorizontalGlue();
		buttonPanel.add(horizontalGlue);
		
		btnLoad = new JButton("");
		btnLoad.setActionCommand(Actions.DESIGN_LOAD);
		btnLoad.addActionListener(MainWindow.getListener());
		btnLoad.setIcon(new ImageIcon(LoadedDesignsPanelBase.class.getResource("/resources/icons/add.png")));
		buttonPanel.add(btnLoad);
		
		btnUnload = new JButton("");
		btnUnload.setEnabled(false);
		btnUnload.setActionCommand(Actions.DESIGN_UNLOAD);
		btnUnload.setIcon(new ImageIcon(LoadedDesignsPanelBase.class.getResource("/resources/icons/delete.png")));
		buttonPanel.add(btnUnload);
		
		panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		panel.setOpaque(false);
		panel.setLayout(new BorderLayout(0, 5));
		
		scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		panel.add(scrollPane, BorderLayout.CENTER);
		designTree = new JTree(rootNode);
		designTree.setRootVisible(false);
		designTree.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		designTree.setCellRenderer(new CellRenderer());
		scrollPane.setViewportView(designTree);
	}
	
	private static class CellRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;
		
		private static final Icon designIcon = new ImageIcon(LoadedDesignsPanelBase.class.getResource("/resources/icons/java/design.gif"));
		private static final Icon packageIcon = new ImageIcon(LoadedDesignsPanelBase.class.getResource("/resources/icons/java/package.gif"));
		private static final Icon classIcon = new ImageIcon(LoadedDesignsPanelBase.class.getResource("/resources/icons/java/class.gif"));
		private static final Icon interfaceIcon = new ImageIcon(LoadedDesignsPanelBase.class.getResource("/resources/icons/java/interface.gif"));
		private static final Icon annotIcon = new ImageIcon(LoadedDesignsPanelBase.class.getResource("/resources/icons/java/annotation.gif"));
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
					row, hasFocus);
			
			Object o = getObject(value);
			
			if (o instanceof Design) {
				setIcon(designIcon);
				setText("");
			} else if (o instanceof Package) {
				setIcon(packageIcon);
			} else if (o instanceof Type) {
				Type t = (Type)o;
				
				if (t.isAnnotation())
					setIcon(annotIcon);
				else if (t.isInterface())
					setIcon(interfaceIcon);
				else
					setIcon(classIcon);
				
				setText(t.getSimpleName());
			}
			
			return this;
		}
		
		private Object getObject(Object value) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			return node.getUserObject();
		}
	}
}
