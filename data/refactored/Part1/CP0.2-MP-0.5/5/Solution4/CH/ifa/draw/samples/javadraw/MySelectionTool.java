/*
 * @(#)MySelectionTool.java
 *
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	© by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
 */

package CH.ifa.draw.samples.javadraw;

import java.awt.*;
import java.awt.event.MouseEvent;
import CH.ifa.draw.framework.*;
import CH.ifa.draw.standard.*;
import CH.ifa.draw.contrib.DragNDropTool;
import java.awt.event.MouseEvent;
import CH.ifa.draw.standard.*;

/**
 * A SelectionTool that interprets double clicks to inspect the clicked figure
 *
 * @version <$CURRENT_VERSION$>
 */
public  class MySelectionTool extends SelectionTool {

    public MySelectionTool(DrawingEditor newDrawingEditor) {
        super(newDrawingEditor);
    }

    /**
	 * Handles mouse down events and starts the corresponding tracker.
	 */
    public void mouseDown(MouseEvent e, int x, int y) {
        if (e.getClickCount() == 2) {
            Figure figure = drawing().findFigure(e.getX(), e.getY());
            if (figure != null) {
                inspectFigure(figure);
                return;
            }
        }
        super.mouseDown(e, x, y);
    }

    /**
	 * Handles mouse moves (if the mouse button is up).
	 * Switches the cursors depending on whats under them.
	 */
    public void mouseMove(MouseEvent evt, int x, int y) {
        DragNDropTool.setCursor(evt.getX(), evt.getY(), view());
    }

    protected void inspectFigure(Figure f) {
        System.out.println("inspect figure" + f);
    }
}
