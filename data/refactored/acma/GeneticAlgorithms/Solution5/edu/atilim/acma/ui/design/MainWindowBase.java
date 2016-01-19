package edu.atilim.acma.ui.design;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import edu.atilim.acma.Core;
import edu.atilim.acma.ui.Actions;
import edu.atilim.acma.ui.Console;
import edu.atilim.acma.ui.LoadedDesigns;
import edu.atilim.acma.ui.MainWindow;
import edu.atilim.acma.ui.TasksPanel;
import javax.swing.JCheckBoxMenuItem;

public class MainWindowBase extends JFrame {

    private static final long serialVersionUID = 1L;

    private JPanel contentPane;
    protected JPanel leftPanel;
    protected LoadedDesigns loadedDesigns;
    protected TasksPanel tasksPanel;
    protected JPanel topPanel;
    protected JMenuBar menuBar;
    protected JMenu mnFile;
    protected JPanel mainPanel;
    protected JPanel rightPanel;
    protected JTabbedPane mainTabs;
    protected Console console;
    protected JMenuItem mnitmLoadDesign;
    protected JMenuItem mnitmUnloadDesign;
    protected JSeparator sp1;
    protected JMenuItem mnitmExit;
    protected JMenu mnSettings;
    protected JMenuItem mnitmRunConfig;
    protected JSeparator sp2;
    protected JMenuItem mnitmClearConsole;
    protected JMenu mnHelp;
    protected JMenuItem mnitmAbout;
    protected JCheckBoxMenuItem mnItmParetoMode;

    public MainWindowBase() {
        setTitle("A-CMA");
        setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindowBase.class.getResource("/resources/acmaicon.png")));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 10, 1200, 700);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        mainPanel = new JPanel();
        contentPane.add(mainPanel, BorderLayout.CENTER);

        leftPanel = new JPanel();
        leftPanel.setBorder(new CompoundBorder(new MatteBorder(0, 0, 0, 1, (Color) new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));

        loadedDesigns = new LoadedDesigns();

        tasksPanel = new TasksPanel();
        GroupLayout gl_leftPanel = new GroupLayout(leftPanel);
        gl_leftPanel.setHorizontalGroup(
            gl_leftPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_leftPanel.createSequentialGroup().addGroup(gl_leftPanel.createParallelGroup(Alignment.TRAILING, false).addComponent(loadedDesigns, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(tasksPanel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)).addGap(18)));
        gl_leftPanel.setVerticalGroup(
            gl_leftPanel.createParallelGroup(Alignment.TRAILING).addGroup(gl_leftPanel.createSequentialGroup().addComponent(loadedDesigns, GroupLayout.DEFAULT_SIZE, 479, Short.MAX_VALUE).addPreferredGap(ComponentPlacement.RELATED).addComponent(tasksPanel, GroupLayout.PREFERRED_SIZE, 186, GroupLayout.PREFERRED_SIZE)));
        leftPanel.setLayout(gl_leftPanel);

        rightPanel = new JPanel();
        rightPanel.setBorder(new EmptyBorder(5, 0, 5, 5));
        GroupLayout gl_mainPanel = new GroupLayout(mainPanel);
        gl_mainPanel.setHorizontalGroup(
            gl_mainPanel.createParallelGroup(Alignment.LEADING).addGroup(gl_mainPanel.createSequentialGroup().addComponent(leftPanel, GroupLayout.PREFERRED_SIZE, 260, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED).addComponent(rightPanel, GroupLayout.DEFAULT_SIZE, 918, Short.MAX_VALUE)));
        gl_mainPanel.setVerticalGroup(
            gl_mainPanel.createParallelGroup(Alignment.LEADING).addComponent(leftPanel, GroupLayout.DEFAULT_SIZE, 681, Short.MAX_VALUE).addComponent(rightPanel, GroupLayout.DEFAULT_SIZE, 681, Short.MAX_VALUE));
        rightPanel.setLayout(new BorderLayout(0, 0));

        mainTabs = new JTabbedPane(JTabbedPane.TOP);
        rightPanel.add(mainTabs, BorderLayout.CENTER);

        console = new Console();
        mainTabs.addTab("Console", new ImageIcon(MainWindowBase.class.getResource("/resources/icons/note.png")), console, null);
        mainPanel.setLayout(gl_mainPanel);

        topPanel = new JPanel();
        contentPane.add(topPanel, BorderLayout.NORTH);
        topPanel.setLayout(new BorderLayout(0, 0));

        menuBar = new JMenuBar();
        topPanel.add(menuBar, BorderLayout.NORTH);

        mnFile = new JMenu("File");
        menuBar.add(mnFile);

        mnitmLoadDesign = new JMenuItem("Load Design");
        mnitmLoadDesign.setIcon(new ImageIcon(MainWindowBase.class.getResource("/resources/icons/java/design.gif")));
        mnitmLoadDesign.setActionCommand(Actions.DESIGN_LOAD);
        mnitmLoadDesign.addActionListener(MainWindow.getListener());
        mnFile.add(mnitmLoadDesign);

        // No code to handle item (and a separate button already exists to remove selected designs)
//		mnitmUnloadDesign = new JMenuItem("Unload Design");
//		mnitmUnloadDesign.setActionCommand(Actions.DESIGN_UNLOAD);
//		mnitmUnloadDesign.addActionListener(MainWindow.getListener());
//		mnFile.add(mnitmUnloadDesign);

        sp1 = new JSeparator();
        mnFile.add(sp1);

        mnitmExit = new JMenuItem("Exit");
        mnitmExit.setActionCommand(Actions.EXIT);
        mnitmExit.addActionListener(MainWindow.getListener());
        mnFile.add(mnitmExit);

        mnSettings = new JMenu("Settings");
        menuBar.add(mnSettings);

        mnitmRunConfig = new JMenuItem("Run Configuration");
        mnitmRunConfig.setIcon(new ImageIcon(MainWindowBase.class.getResource("/resources/icons/engine_16.png")));
        mnitmRunConfig.setActionCommand(Actions.CONFIG_RUN);
        mnitmRunConfig.addActionListener(MainWindow.getListener());
        mnSettings.add(mnitmRunConfig);

        mnItmParetoMode = new JCheckBoxMenuItem("Pareto Mode");
        mnItmParetoMode.setActionCommand("PARETO");
        mnItmParetoMode.addActionListener(MainWindow.getListener());
        mnItmParetoMode.setSelected(Core.paretoMode);
        mnSettings.add(mnItmParetoMode);

        sp2 = new JSeparator();
        mnSettings.add(sp2);

        mnitmClearConsole = new JMenuItem("Clear Console");
        mnitmClearConsole.setIcon(new ImageIcon(MainWindowBase.class.getResource("/resources/icons/file_16.png")));
        mnitmClearConsole.setActionCommand(Actions.CLEAR_CONSOLE);
        mnitmClearConsole.addActionListener(MainWindow.getListener());
        mnSettings.add(mnitmClearConsole);

//		Adds "Help" menu bar and "About" item but nothing happens when "About" is selected
//		mnHelp = new JMenu("Help");
//		menuBar.add(mnHelp);
//
//		mnitmAbout = new JMenuItem("About");
//		mnitmAbout.setIcon(new ImageIcon(MainWindowBase.class.getResource("/resources/icons/info_16.png")));
//		mnitmAbout.setActionCommand(Actions.ABOUT);
//		mnitmAbout.addActionListener(MainWindow.getListener());
//		mnHelp.add(mnitmAbout);
    }
}
