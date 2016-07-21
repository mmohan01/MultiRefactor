/*
 * @(#)ChopEllipseConnector.java
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
import CH.ifa.draw.framework.*;
import CH.ifa.draw.standard.*;
import CH.ifa.draw.util.Geom;

import java.awt.*;
import CH.ifa.draw.framework.*;
import CH.ifa.draw.standard.*;

/**
 * A ChopEllipseConnector locates a connection point by
 * chopping the connection at the ellipse defined by the
 * figure's display box.
 *
 * @version <$CURRENT_VERSION$>
 */
public class ChopEllipseConnector extends ChopBoxConnector {

    /*
	 * Serialization support.
	 */
    private static final long serialVersionUID = -3165091511154766610L;

    public ChopEllipseConnector() {
    }

    public ChopEllipseConnector(Figure owner) {
        super(owner);
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

    protected Point chop(Figure target, Point from) {
        Rectangle r = target.displayBox();
        double angle = Geom.pointToAngle(r, from);
        return Geom.ovalAngleToPoint(r, angle);
    }
}
