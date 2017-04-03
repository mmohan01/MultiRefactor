/*
 * Created on 26.02.2005
 */
package net.sourceforge.ganttproject.gui.scrolling;

/**
 * @author bard
 */
public interface ScrollingManager {
	void scrollLeft();
	void scrollRight();
	void addScrollingListener(ScrollingListener listener);
	void removeScrollingListener(ScrollingListener listener);
}
