package edu.atilim.acma.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import edu.atilim.acma.Core;
import edu.atilim.acma.ui.design.MainWindowBase;
import edu.atilim.acma.util.WeakHashSet;

public class MainWindow extends MainWindowBase {
	private static final long serialVersionUID = 1L;
	
	private static MainWindow instance;
	private static Listener listener;
	
	public static MainWindow getInstance() {
		if (instance == null) instance = new MainWindow();
		return instance;
	}
	
	private WeakHashSet<WindowEventListener> eventListeners;
	
	void addEventListener(WindowEventListener listener) {
		eventListeners.add(listener);
	}
	
	void fireEvent(Object e) {
		if (e == null) return;
		for (WindowEventListener listener : eventListeners)
			listener.onWindowEvent(e);
	}
	
	public MainWindow() {
		eventListeners = new WeakHashSet<MainWindow.WindowEventListener>();
	}
	
	LoadedDesigns getLoadedDesigns() {
		return loadedDesigns;
	}
	
	JTabbedPane getTabs() {
		return mainTabs;
	}
	
	public static Listener getListener() {
		if (listener == null) listener = new Listener();
		return listener;
	}
	
	private static class Listener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String ac = e.getActionCommand();
			
			if (ac.equals(Actions.DESIGN_LOAD)) {
				new LoadDesignDialog().setVisible(true);
			// need code to handle action listener for unload design menu item
//			} else if (ac.equals(Actions.DESIGN_UNLOAD)) {
//				int resp = JOptionPane.showConfirmDialog(MainWindow.this, "Are you sure?", "Confirm", JOptionPane.YES_NO_OPTION);
//				if (resp == JOptionPane.YES_OPTION) {
//					removeDesign(activeDesign);
			} else if (ac.equals(Actions.EXIT)) {
				System.exit(0);
			} else if (ac.equals(Actions.CLEAR_CONSOLE)) {
				MainWindow.getInstance().console.clear();
			} else if (ac.equals(Actions.CONFIG_RUN)) {
				new RunConfigDialog().setVisible(true);
			} else if (ac.equals("PARETO")) {
				Core.paretoMode = MainWindow.getInstance().mnItmParetoMode.isSelected();
			}
		}
	}
	
	public static interface WindowEventListener {
		public void onWindowEvent(Object e);
	}
}
