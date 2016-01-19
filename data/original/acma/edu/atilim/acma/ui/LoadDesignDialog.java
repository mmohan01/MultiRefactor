package edu.atilim.acma.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.io.DesignLoader;
import edu.atilim.acma.design.io.DesignReader;
import edu.atilim.acma.design.io.ZIPDesignReader;
import edu.atilim.acma.util.Log;

public class LoadDesignDialog extends JDialog {
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

	public LoadDesignDialog() {
		super(MainWindow.getInstance(), true);
		
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		setTitle("A-CMA Design Loader");
		setIconImage(Toolkit.getDefaultToolkit().getImage(LoadDesignDialog.class.getResource("/resources/acmaicon.png")));
		setBounds(100, 100, 455, 302);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		panelPredefined = new JPanel();
		panelPredefined.setBorder(new TitledBorder(null, "Predefined Design", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelPredefined.setBounds(10, 11, 429, 127);
		contentPanel.add(panelPredefined);
		panelPredefined.setLayout(null);
		
		comboBoxDesigns = new JComboBox();
		comboBoxDesigns.setBounds(10, 94, 409, 22);
		panelPredefined.add(comboBoxDesigns);
		
		lblyouCanSelect = new JLabel("<html>You can select a preloaded design from the list below. Also, you can place any <b>zip</b> file containing the <b>bin</b> folder of any java project to the <b>input/benchmarks</b> folder if you want them to be listed here.</html>");
		lblyouCanSelect.setForeground(SystemColor.textInactiveText);
		lblyouCanSelect.setBounds(10, 25, 409, 58);
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
			
			btnExit = new JButton("Cancel");
			btnExit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
				}
			});
			buttonPane.add(btnExit);
			
			horizontalStrut = Box.createHorizontalStrut(1);
			buttonPane.add(horizontalStrut);
		}
		
		setLocationRelativeTo(MainWindow.getInstance());
		
		init();
	}
	
	private void init() {
		File bmdir = new File("./data/benchmarks");
		
		comboBoxDesigns.addItem(new PredefinedDesign(null));
		
		for (File f : bmdir.listFiles()) {
			if (!f.isDirectory() && f.getName().endsWith(".zip")) {
				comboBoxDesigns.addItem(new PredefinedDesign(f));
				textFieldPath.setText("");
			}
		}
		
		Log.info("Found %d predefined designs in data/benchmarks.", comboBoxDesigns.getItemCount());
		
		btnBrowse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File("."));
				fc.setDialogTitle("Please select the bin directory of Java project");
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setAcceptAllFileFilterUsed(false);
				
				if (fc.showOpenDialog(LoadDesignDialog.this) == JFileChooser.APPROVE_OPTION) {
					textFieldPath.setText(fc.getSelectedFile().getAbsolutePath());
					comboBoxDesigns.setSelectedIndex(0);
				}
			}
		});
		
		btnLoadDesign.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				DesignLoader loader = null;

				if (comboBoxDesigns.getSelectedIndex() != 0) {
					PredefinedDesign design = (PredefinedDesign)comboBoxDesigns.getSelectedItem();
					loader = new ZIPDesignReader(design.getFile().getAbsolutePath());
				}
				else if (textFieldPath.getText() != null && !textFieldPath.getText().equals("")) {	
					File file = new File(textFieldPath.getText());
					if (file.exists() && file.isDirectory()) {		
						loader = new DesignReader(file.getAbsolutePath());
					}
				}
				
				if (loader == null) {
					JOptionPane.showMessageDialog(LoadDesignDialog.this, "Please select a design");
					return;
				}
				
				final DesignLoader floader = loader;
				final HourGlassDialog hg = new HourGlassDialog();
				
				new SwingWorker<Design, Void>() {
					@Override
					protected Design doInBackground() throws Exception {
						Log.info("Loading design.");
						try {
							return floader.read();
						} catch (Exception e) {
							e.printStackTrace();
							throw e;
						}
					}
					
					protected void done() {
						Log.info("Completed loading design.");
						try {
							MainWindow.getInstance().getLoadedDesigns().addDesign(get());
						} catch (Exception e) {
							e.printStackTrace();
						}
						hg.setVisible(false);
					}
				}.execute();
				
				setVisible(false);
				hg.setVisible(true);
			}
		});
	}
	
	private class PredefinedDesign {
		private File file;
		
		public File getFile() {
			return file;
		}

		public PredefinedDesign(File file) {
			this.file = file;
		}

		@Override
		public String toString() {
			if (file == null)
				return "Select Design";
			
			return file.getName();
		}
	}
}
