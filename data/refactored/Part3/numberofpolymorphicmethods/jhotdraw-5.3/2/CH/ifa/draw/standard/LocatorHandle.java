/*
 * @(#)LocatorHandle.java
 *
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	© by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
 */

package CH.ifa.draw.standard;

import java.awt.Point;
import CH.ifa.draw.framework.*;

import CH.ifa.draw.framework.*;
import java.awt.*;
import CH.ifa.draw.util.Undoable;

/**
 * A LocatorHandle implements a Handle by delegating the location requests to
 * a Locator object.
 *
 * @see Locator
 *
 * @version <$CURRENT_VERSION$>
 */
public abstract class LocatorHandle extends AbstractHandle {

    private Locator       fLocator;

    /**
	 * Initializes the LocatorHandle with the given Locator.
	 */
    public LocatorHandle(Figure owner, Locator l) {
        super(owner);
        fLocator = l;
    }

    public void invokeStep(int x, int y, int anchorX, int anchorY, DrawingView view) {
        Rectangle r = owner().displayBox();
        owner().displayBox(
            new Point(r.x, Math.min(r.y + r.height, y)),
            new Point(Math.max(r.x, x), r.y + r.height));
    }
    /**
	 * This should be cloned or it gives the receiver the opportunity to alter
	 * our internal behavior.
	 */
    public Locator getLocator() {
        return fLocator;
    }

    /**
	 * Factory method for undo activity. To be overriden by subclasses.
	 */
    protected Undoable createUndoActivity(DrawingView view) {
        return new ResizeHandle.UndoActivity(view);
    }

    /**
	 * Locates the handle on the figure by forwarding the request
	 * to its figure.
	 */
    public Point locate() {
        return fLocator.locate(owner());
    }

    public void invokeEnd(int x, int y, int anchorX, int anchorY, DrawingView view) {
        Rectangle oldDisplayBox = ((ResizeHandle.UndoActivity)getUndoActivity()).getOldDisplayBox();
        if (owner().displayBox().equals(oldDisplayBox)) {
            // display box hasn't change so there is nothing to undo
            setUndoActivity(null);
        }
    }
}
