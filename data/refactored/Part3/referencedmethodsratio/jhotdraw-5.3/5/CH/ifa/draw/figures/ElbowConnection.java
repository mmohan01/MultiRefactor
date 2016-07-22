/*
 * @(#)ElbowConnection.java
 *
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	© by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
 */

package CH.ifa.draw.figures;

import java.awt.*;
import java.util.*;
import java.io.IOException;
import CH.ifa.draw.framework.*;
import CH.ifa.draw.standard.*;
import CH.ifa.draw.util.*;

/**
 * A LineConnection that constrains a connection to
 * orthogonal lines.
 *
 * @version <$CURRENT_VERSION$>
 */
public  class ElbowConnection extends LineConnection {

    /*
	 * Serialization support.
	 */
    private static final long serialVersionUID = 2193968743082078559L;
    private int elbowConnectionSerializedDataVersion = 1;

    public ElbowConnection() {
        super();
    }

    public void updateConnection() {
        super.updateConnection();
        updatePoints();
    }

    public void layoutConnection() {
    }

    /**
	 * Gets the handles of the figure.
	 */
    public Vector handles() {
        Vector handles = new Vector(fPoints.size() * 2);
        handles.addElement(new ChangeConnectionStartHandle(this));
        for (int i = 1; i < fPoints.size() - 1; i++) {
            handles.addElement(new NullHandle(this, locator(i)));
        }
        handles.addElement(new ChangeConnectionEndHandle(this));
        for (int i = 0; i < fPoints.size() - 1; i++) {
            handles.addElement(new ElbowHandle(this, i));
        }
        return handles;
    }

    public Locator connectedTextLocator(Figure f) {
        return new ElbowTextLocator();
    }
}

class ElbowTextLocator extends AbstractLocator {
    public Point locate(Figure owner) {
        Point p = owner.center();
        Rectangle r = owner.displayBox();
        return new Point(p.x, p.y - 10); // hack
    }
}
