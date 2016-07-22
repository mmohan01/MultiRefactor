/*
 * @(#)ChopPolygonConnector.java
 *
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	© by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
 */

package CH.ifa.draw.contrib;

import java.awt.*;
import CH.ifa.draw.framework.*;
import CH.ifa.draw.standard.*;

import java.awt.*;
import CH.ifa.draw.framework.*;
import CH.ifa.draw.standard.*;
import CH.ifa.draw.standard.*;

/**
 * A ChopPolygonConnector locates a connection point by
 * chopping the connection at the polygon boundary.
 *
 * @author Erich Gamma
 * @version <$CURRENT_VERSION$>
 */
public class ChopPolygonConnector extends ChopBoxConnector {

    /*
	 * Serialization support.
	 */
    private static final long serialVersionUID = -156024908227796826L;

    public ChopPolygonConnector() {
    }

    public Point findStart(ConnectionFigure connection) {
        Figure startFigure = connection.getStartConnector().owner();
        Rectangle r2 = connection.getEndConnector().displayBox();
        Point r2c = null;

        if (connection.pointCount() == 2) {
            r2c = new Point(r2.x + r2.width / 2, r2.y + r2.height / 2);
        }
         else {
            r2c = connection.pointAt(1);
        }

        return chop(startFigure, r2c);
    }

    public Point findEnd(ConnectionFigure connection) {
        Figure endFigure = connection.getEndConnector().owner();
        Rectangle r1 = connection.getStartConnector().displayBox();
        Point r1c = null;

        if (connection.pointCount() == 2) {
            r1c = new Point(r1.x + r1.width / 2, r1.y + r1.height / 2);
        }
         else {
            r1c = connection.pointAt(connection.pointCount() - 2);
        }

        return chop(endFigure, r1c);
    }

    public ChopPolygonConnector(Figure owner) {
        super(owner);
    }

    protected Point chop(Figure target, Point from) {
        return ((PolygonFigure)target).chop(from);
    }
}
