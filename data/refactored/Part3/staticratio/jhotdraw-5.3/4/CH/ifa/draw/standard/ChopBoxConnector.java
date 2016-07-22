/*
 * @(#)ChopBoxConnector.java
 *
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	© by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
 */

package CH.ifa.draw.standard;

import java.awt.*;
import CH.ifa.draw.framework.*;
import CH.ifa.draw.util.Geom;

/**
 * A ChopBoxConnector locates connection points by
 * choping the connection between the centers of the
 * two figures at the display box.
 *
 * @see Connector
 *
 * @version <$CURRENT_VERSION$>
 */
public class ChopBoxConnector extends AbstractConnector {

    public ChopBoxConnector() { // only used for Storable implementation
    }

    public ChopBoxConnector(Figure owner) {
        super(owner);
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

    public Point chop(Figure target, Point from) {
        Rectangle r = target.displayBox();
        return Geom.angleToPoint(r, (Geom.pointToAngle(r, from)));
    }
}
