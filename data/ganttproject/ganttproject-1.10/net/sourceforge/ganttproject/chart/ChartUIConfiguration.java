/*
 * Created on 17.06.2004
 *
 */
package net.sourceforge.ganttproject.chart;

import java.awt.Color;
import java.awt.Font;

/**
 * @author bard
 *
 */
class ChartUIConfiguration {
    
    private final Font mySpanningRowTextFont;
    private final Color mySpanningHeaderBackgroundColor;
	private final Color myHeaderBorderColor;
	private final Color myHorizontalGutterColor1 = new Color(0.807f, 0.807f, 0.807f);
	private final Color myHorizontalGutterColor2 = Color.white;
	private final Color myBottomUnitGridColor;
	private final Font myBottomUnitFont;
	
    ChartUIConfiguration() {
        mySpanningRowTextFont = new Font("SansSerif", Font.PLAIN, 12);
        myBottomUnitFont = new Font("SansSerif", Font.PLAIN, 8);
        mySpanningHeaderBackgroundColor = new Color(0.930f, 0.930f, 0.930f);
        myHeaderBorderColor = new Color(0.482f, 0.482f, 0.482f);
        //myHeaderBorderColor = new Color(0f, 1f, 0f);
        myBottomUnitGridColor = new Color(0.482f, 0.482f, 0.482f);
    }
    
    Font getSpanningHeaderFont() {
        return mySpanningRowTextFont;
    }
    
    Font getBottomUnitFont() {
    	return myBottomUnitFont;
    }

    public int getSpanningHeaderHeight() {
        return 22;
    }
    
    public Color getSpanningHeaderBackgroundColor() {
        return mySpanningHeaderBackgroundColor;
    }

	public Color getHeaderBorderColor() {
		return myHeaderBorderColor;
	}

	public Color getHorizontalGutterColor1() {
		return myHorizontalGutterColor1;
	}

	public Color getHorizontalGutterColor2() {
		return myHorizontalGutterColor2;
	}

	public Color getBottomUnitGridColor() {
		return myBottomUnitGridColor;
	}
}
