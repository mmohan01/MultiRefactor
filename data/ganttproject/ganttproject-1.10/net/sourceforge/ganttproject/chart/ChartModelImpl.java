/*
 * Created on 17.06.2004
 *
 */
package net.sourceforge.ganttproject.chart;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Date;

import net.sourceforge.ganttproject.time.TimeFrame;
import net.sourceforge.ganttproject.time.TimeUnit;
import net.sourceforge.ganttproject.time.TimeUnitFunctionOfDate;
import net.sourceforge.ganttproject.time.gregorian.GregorianTimeUnitStack;

/**
 * @author bard
 *
 */
public class ChartModelImpl implements ChartModel {
    
    private final ChartHeader myChartHeader;
    private final ChartUIConfiguration myChartUIConfiguration;
    private Dimension myBounds;
	private Date myStartDate;
	private int myAtomUnitPixels;
	private TimeFrame[] myTimeFrames;
	private final GregorianTimeUnitStack myTimeUnitStack = new GregorianTimeUnitStack();
	private TimeUnit myTopUnit;
	private TimeUnit myBottomUnit;
    
    public ChartModelImpl() {
        myChartHeader = new ChartHeaderImpl(this);
        myChartUIConfiguration = new ChartUIConfiguration();
    }
    public ChartHeader getChartHeader() {
        return myChartHeader;
    }
    
    ChartUIConfiguration getChartUIConfiguration() {
        return myChartUIConfiguration;
    }
    
    public void setBounds(Dimension bounds) {
        myBounds = bounds;        
    }
    
	public void setStartDate(Date startDate) {
		System.err.println("start date="+startDate);
		if (!startDate.equals(myStartDate)) {
			myStartDate = startDate;		
			myTimeFrames = null;
		}
	}
	
	public void setBottomUnitWidth(int pixelsWidth) {
		myAtomUnitPixels = pixelsWidth;
	}
	
    public void setTopTimeUnit(TimeUnit topTimeUnit) {
    	if (topTimeUnit instanceof TimeUnitFunctionOfDate) {
    		if (myStartDate==null) {
    			throw new RuntimeException("No date is set");
    		}
    		else {
    			myTopUnit = ((TimeUnitFunctionOfDate)topTimeUnit).createTimeUnit(myStartDate);
    		}
    	}
    	else {
    		myTopUnit = topTimeUnit;
    	}
    }
    
    public void setBottomTimeUnit(TimeUnit bottomTimeUnit) {
    	myBottomUnit = bottomTimeUnit;
    }
	
    Dimension getBounds() {
        return myBounds;
    }
    
    TimeFrame[] getTimeFrames() {
    	if (myTimeFrames==null) {
    		myTimeFrames = calculateTimeFrames();
    	}
    	return myTimeFrames;
    }

    int getBottomUnitWidth() {
    	return myAtomUnitPixels;
    }
    
	private TimeFrame[] calculateTimeFrames() {
		ArrayList result = new ArrayList();
		int totalFramesWidth = 0;
		Date currentDate = myStartDate;
		do {
			TimeFrame currentFrame = myTimeUnitStack.createTimeFrame(currentDate, myTopUnit, myBottomUnit);
			result.add(currentFrame);
			totalFramesWidth += currentFrame.getUnitCount(myBottomUnit)*myAtomUnitPixels;
			currentDate = currentFrame.getFinishDate();
			
		} while (totalFramesWidth<=getBounds().getWidth());
		//
		return (TimeFrame[]) result.toArray(new TimeFrame[0]);
	}

}
