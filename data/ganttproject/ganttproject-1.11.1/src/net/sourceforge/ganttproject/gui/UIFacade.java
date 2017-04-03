/*
 * Created on 26.02.2005
 */
package net.sourceforge.ganttproject.gui;

import java.awt.Component;

import javax.swing.Action;
import javax.swing.JDialog;

import net.sourceforge.ganttproject.chart.ChartViewState;
import net.sourceforge.ganttproject.gui.scrolling.ScrollingManager;
import net.sourceforge.ganttproject.gui.zoom.ZoomManager;

/**
 * @author bard
 */
public interface UIFacade {
	ScrollingManager getScrollingManager();

	ChartViewState getGanttChartViewState();

	ZoomManager getZoomManager();
	
	JDialog createDialog(Component content, Action[] actions);
}
