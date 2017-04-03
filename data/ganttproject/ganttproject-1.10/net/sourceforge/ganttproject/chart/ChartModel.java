package net.sourceforge.ganttproject.chart;

import java.awt.Dimension;
import java.util.Date;

import net.sourceforge.ganttproject.time.TimeUnit;


/**
 * @author dbarashev
 *
 */
public interface ChartModel {
    ChartHeader getChartHeader();
    void setBounds(Dimension bounds);
    void setStartDate(Date startDate);
    void setBottomUnitWidth(int pixelsWidth);
    void setTopTimeUnit(TimeUnit topTimeUnit);
    void setBottomTimeUnit(TimeUnit bottomTimeUnit);
}
