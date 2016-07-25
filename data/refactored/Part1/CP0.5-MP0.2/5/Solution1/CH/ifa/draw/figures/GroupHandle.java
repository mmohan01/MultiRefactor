/*
 * @(#)GroupHandle.java
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
import CH.ifa.draw.standard.NullHandle;
import CH.ifa.draw.framework.*;
import CH.ifa.draw.standard.*;

/**
 * A Handle for a GroupFigure.
 *
 * @version <$CURRENT_VERSION$>
 */ class GroupHandle extends NullHandle {

    /**
	 * The handle's locator.
	 */
    protected Locator fLocator;

    public GroupHandle(Figure owner, Locator locator) {
        super(owner, locator);
    }
}
