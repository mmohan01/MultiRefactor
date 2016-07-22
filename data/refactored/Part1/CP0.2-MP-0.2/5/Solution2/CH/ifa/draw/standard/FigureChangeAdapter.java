/*
 * @(#)FigureChangeAdapter.java
 *
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	© by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
 */

package CH.ifa.draw.standard;

import CH.ifa.draw.framework.*;
import CH.ifa.draw.util.*;
import CH.ifa.draw.util.*;

/**
 * Empty implementation of FigureChangeListener.
 *
 * @version <$CURRENT_VERSION$>
 */
public abstract class FigureChangeAdapter implements FigureChangeListener {
    public double FRICTION_FACTOR = 0.75;
    public double LENGTH_FACTOR = 1.0;

    /**
	 *  Sent when an area is invalid
	 */
    public void figureInvalidated(FigureChangeEvent e) {}

    /**
	 * Sent when a figure changed
	 */
    public void figureChanged(FigureChangeEvent e) {}

    /**
	 * Sent when a figure was removed
	 */
    public void figureRemoved(FigureChangeEvent e) {}

    /**
	 * Sent when requesting to remove a figure.
	 */
    public void figureRequestRemove(FigureChangeEvent e) {}

    /**
	 * Sent when an update should happen.
	 *
	 */
    public void figureRequestUpdate(FigureChangeEvent e) {}
}
