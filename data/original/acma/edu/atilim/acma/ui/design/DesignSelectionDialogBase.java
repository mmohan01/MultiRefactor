package edu.atilim.acma.ui.design;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public abstract class DesignSelectionDialogBase extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	protected JButton btnLoadDesign;
	protected JButton btnExit;
	protected JPanel panelPredefined;
	protected JComboBox comboBoxDesigns;
	protected JPanel panel;
	protected JTextField textFieldPath;
	protected JButton btnBrowse;
	protected Component horizontalStrut;
	protected JLabel lblbrowseForAny;
	protected JLabel lblyouCanSelect;

	public DesignSelectionDialogBase() {
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("A-CMA Design Loader");
		setIconImage(Toolkit.getDefaultToolkit().getImage(DesignSelectionDialogBase.class.getResource("/resources/acmaicon.png")));
		setBounds(100, 100, 455, 302);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		panelPredefined = new JPanel();
		panelPredefined.setBorder(new TitledBorder(null, "Predefined Design", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelPredefined.setBounds(10, 11, 429, 117);
		contentPanel.add(panelPredefined);
		panelPredefined.setLayout(null);
		
		comboBoxDesigns = new JComboBox();
		comboBoxDesigns.setBounds(10, 85, 409, 22);
		panelPredefined.add(comboBoxDesigns);
		
		lblyouCanSelect = new JLabel("<html>You can select a preloaded design from the list below. Also, you can place any <b>zip</b> file containing the <b>bin</b> folder of any java project to the <b>input/benchmarks</b> folder if you want them to be listed here.</html>");
		lblyouCanSelect.setForeground(SystemColor.textInactiveText);
		lblyouCanSelect.setBounds(10, 25, 409, 49);
		panelPredefined.add(lblyouCanSelect);
		
		panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Custom Design", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setBounds(10, 139, 429, 104);
		contentPanel.add(panel);
		panel.setLayout(null);
		
		textFieldPath = new JTextField();
		textFieldPath.setEditable(false);
		textFieldPath.setBounds(10, 71, 359, 23);
		panel.add(textFieldPath);
		textFieldPath.setColumns(10);
		
		btnBrowse = new JButton("...");
		btnBrowse.setBounds(379, 71, 40, 23);
		panel.add(btnBrowse);
		
		lblbrowseForAny = new JLabel("<html>Browse for any folder containing compiled Java code. That would be a <b>bin</b> folder, generally...</html>");
		lblbrowseForAny.setForeground(SystemColor.textInactiveText);
		lblbrowseForAny.setBounds(10, 25, 409, 35);
		panel.add(lblbrowseForAny);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(null);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			
			btnLoadDesign = new JButton("Load Design");
			buttonPane.add(btnLoadDesign);
			
			btnExit = new JButton("Exit");
			btnExit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					System.exit(0);
				}
			});
			buttonPane.add(btnExit);
			
			horizontalStrut = Box.createHorizontalStrut(1);
			buttonPane.add(horizontalStrut);
		}
	}
}
