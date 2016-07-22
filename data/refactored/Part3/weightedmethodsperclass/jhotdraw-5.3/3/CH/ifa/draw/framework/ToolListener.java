/*
 * @(#)ToolListener.java
 *
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	© by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
 */

package CH.ifa.draw.framework;

import java.util.EventListener;
import java.util.EventObject;

/**
 * @author Wolfram Kaiser
 * @version <$CURRENT_VERSION$>
 */
public interface ToolListener extends EventListener {
    public void toolEnabled(final EventObject toolEvent);
    public void toolDisabled(final EventObject toolEvent);
    public void toolUsable(final EventObject toolEvent);
    public void toolUnusable(final EventObject toolEvent);
    public void toolActivated(final EventObject toolEvent);
    public void toolDeactivated(EventObject toolEvent);
}
