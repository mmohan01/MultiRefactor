package net.sourceforge.ganttproject.time.gregorian;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import net.sourceforge.ganttproject.time.DateFrameable;
import net.sourceforge.ganttproject.time.TimeFrame;
import net.sourceforge.ganttproject.time.TimeUnit;
import net.sourceforge.ganttproject.time.TimeUnitFunctionOfDate;
import net.sourceforge.ganttproject.time.TimeUnitGraph;

/**
 * Created by IntelliJ IDEA.
 * @author bard
 * Date: 01.02.2004
 */
public class GregorianTimeUnitStack {
    private static TimeUnitGraph ourGraph = new TimeUnitGraph();
    private static final DateFrameable DAY_FRAMER = new FramerImpl(Calendar.DATE);
    private static final DateFrameable MONTH_FRAMER = new FramerImpl(Calendar.MONTH);
    private static final DateFrameable HOUR_FRAMER = new FramerImpl(Calendar.HOUR);
    private static final DateFrameable MINUTE_FRAMER = new FramerImpl(Calendar.MINUTE);
    private static final DateFrameable SECOND_FRAMER = new FramerImpl(Calendar.SECOND);
    public static final TimeUnit SECOND;// = ourGraph.createAtomTimeUnit("second");
    public static final TimeUnit MINUTE;// = ourGraph.createTimeUnit("minute", SECOND, 60);
    public static final TimeUnit HOUR;// = ourGraph.createTimeUnit("hour", MINUTE, 60);
    public static final TimeUnit DAY;
    public static final TimeUnitFunctionOfDate MONTH;
    private static final TimeUnit ATOM_UNIT;
    private static final HashMap ourUnit2field = new HashMap();
    static {
        SECOND = ourGraph.createAtomTimeUnit("second");
        MINUTE = ourGraph.createDateFrameableTimeUnit("minute", SECOND, 60, MINUTE_FRAMER);
        HOUR = ourGraph.createDateFrameableTimeUnit("hour", MINUTE, 60, HOUR_FRAMER);;
        DAY = ourGraph.createDateFrameableTimeUnit("day", HOUR, 24, DAY_FRAMER);
        DAY.setTextFormatter(new DayTextFormatter());
        MONTH = ourGraph.createTimeUnitFunctionOfDate("month", DAY, MONTH_FRAMER);
        MONTH.setTextFormatter(new MonthTextFormatter());
        ATOM_UNIT = SECOND;
        ourUnit2field.put(DAY, new Integer(Calendar.DAY_OF_MONTH));
        ourUnit2field.put(HOUR, new Integer(Calendar.HOUR_OF_DAY));
        ourUnit2field.put(MINUTE, new Integer(Calendar.MINUTE));
        ourUnit2field.put(SECOND, new Integer(Calendar.SECOND));
    }
    public GregorianTimeUnitStack () {

    }

    public TimeFrame createTimeFrame(Date baseDate, TimeUnit topUnit, TimeUnit bottomUnit) {
        return new TimeFrameImpl(baseDate, topUnit, bottomUnit);
    }


    private static class TimeFrameImpl implements TimeFrame {
        private final Date myBaseDate;
        private final TimeUnit myTopUnit;
        private final TimeUnit myBottomUnit;
        private Date myNextFrameStartDate;
        private GregorianCalendar myCalendar;
        private DateFrameable myLowestFrameable;
        private Date myStartDate;
        private DateFrameable myHighestFrameable;
        private Date myEndDate;
        private final LineHeader myLineHeader;

        TimeFrameImpl(Date baseDate, TimeUnit topUnit, TimeUnit bottomUnit) {
            if (!topUnit.isConstructedFrom(bottomUnit)) {
                throw new RuntimeException("Top unit="+topUnit+" is not constructed from bottom unit="+bottomUnit);
            }
            myBaseDate = baseDate;
            myHighestFrameable = calculateHighestFrameableUnit(topUnit, bottomUnit);
            myLowestFrameable = calculateLowestFrameableUnit(topUnit, bottomUnit);
            if (myHighestFrameable!=topUnit || myLowestFrameable!=bottomUnit) {
                throw new RuntimeException("Current implementation requires all units to be frameable. myHighestFrameable="+myHighestFrameable+" myLowestFrameable="+myLowestFrameable);
            }
            myTopUnit = topUnit;
            myBottomUnit = bottomUnit;
            myCalendar = (GregorianCalendar) GregorianCalendar.getInstance().clone();
            myCalendar.setTime(myBaseDate);
            if (myLowestFrameable==null) {
                throw new RuntimeException("Failed to find any time frameable unit :(");
            }
            myStartDate = myLowestFrameable.adjustLeft(myBaseDate);
            System.err.println("TimeFrame.init: myStartDate="+myStartDate+" myBaseDate="+myBaseDate);
            //myEndDate = calculateEndDate();
            myLineHeader = calculateLines(null);
            //myNextFrameStartDate = shiftDate(myBaseDate, myTopUnit, 1);
        }

        private DateFrameable calculateLowestFrameableUnit(TimeUnit topUnit, TimeUnit bottomUnit) {
            DateFrameable lowestFrameable = null;
            for (TimeUnit timeUnit = topUnit; timeUnit!=null; timeUnit = timeUnit.getDirectAtomUnit()) {
                if (timeUnit instanceof DateFrameable) {
                    lowestFrameable = (DateFrameable)timeUnit;
                }
                if (timeUnit==bottomUnit) {
                    break;
                }
            }
            return lowestFrameable;
        }

        private DateFrameable calculateHighestFrameableUnit(TimeUnit topUnit, TimeUnit bottomUnit) {
            DateFrameable highestFrameable = null;
            for (TimeUnit timeUnit = topUnit; timeUnit!=null; timeUnit = timeUnit.getDirectAtomUnit()) {
            	System.err.println("next time unit="+timeUnit);
                if (timeUnit instanceof DateFrameable) {
                    highestFrameable = (DateFrameable)timeUnit;
                    break;
                }
                if (timeUnit==bottomUnit) {
                    break;
                }
            }
            return highestFrameable;

        }

        private Date calculateEndDate() {
            int countFrameable = myTopUnit.getAtomCount((TimeUnit)myHighestFrameable);
            Date date = myBaseDate;
            for (int i=0; i<countFrameable; i++) {
                date = myHighestFrameable.adjustRight(date);
            }
            return date;
        }

        private LineHeader calculateLines(LineHeader lastHeader) {
            TimeUnit curUnit = lastHeader==null ? myTopUnit : lastHeader.myUnit.getDirectAtomUnit();
            LineHeader curHeader = createHeader(curUnit);
            fillLine(lastHeader, curHeader);
            if (lastHeader!=null) {
                lastHeader.append(curHeader);
            }
            if (curUnit!=myBottomUnit) {
                calculateLines(curHeader);
            }
            return curHeader;
        }

        private void fillLine(LineHeader higherHeader, LineHeader header) {
            if (higherHeader==null) {
                Date startDate = myStartDate;
                Date endDate = ((DateFrameable)myTopUnit).adjustRight(myBaseDate);
                System.err.println("filling line="+header+" endDate="+endDate);
                LineItem item = createLineItem(startDate, endDate);
                header.myFirstItem = item;
            }
            else {
            	System.err.println("filling line="+header);
                for (LineItem higherItem = higherHeader.myFirstItem; higherItem!=null; higherItem = higherItem.myNextItem) {
                    int unitCount = getUnitCount(higherHeader, header, higherItem);
                    Date curStartDate = higherItem.myStartDate;
                    LineItem curItem = null;
                    System.err.println("unit count="+unitCount+" startDate="+curStartDate);
                    for (int i=0; i<unitCount && curStartDate.compareTo(higherItem.myEndDate)<0; i++) {
                        Date nextEndDate = ((DateFrameable)header.myUnit).adjustRight(curStartDate);
                        LineItem newItem = createLineItem(curStartDate, nextEndDate);
                        if (curItem==null) {
                            header.myFirstItem = newItem;
                        }
                        else {
                            curItem.myNextItem = newItem;
                        }
                        curItem = newItem;
                        curStartDate = nextEndDate;
                    }
                    System.err.println("result: "+header.fullDump());
                }
            }

        }

        private int getUnitCount(LineHeader higherHeader, LineHeader header, LineItem higherItem) {
            TimeUnit higherUnit = higherHeader.myUnit instanceof TimeUnitFunctionOfDate ?
                ((TimeUnitFunctionOfDate)higherHeader.myUnit).createTimeUnit(higherItem.myStartDate) :
                higherHeader.myUnit;
            TimeUnit lowerUnit = header.myUnit;
            int result = higherUnit.getAtomCount(lowerUnit);
            return result;
        }

        private LineItem createLineItem(Date startDate, Date endDate) {
            return new LineItem(startDate, endDate);
        }

        private LineHeader createHeader(TimeUnit unit) {
            return new LineHeader(unit);
        }

        public Date getFinishDate() {
        	if (myEndDate==null) {
        		myEndDate = calculateEndDate();
        		System.err.println("getFinishDate(): startDate="+myStartDate+" finish="+myEndDate);
        	}
        	return myEndDate;
        }
        public int getUnitCount(TimeUnit unit) {
            LineHeader lineHeader = getLineHeader(unit);
            int result = lineHeader==null ? -1 : lineHeader.getItemCount();
            if (result==-1) {
                throw new RuntimeException("There is not time unit="+unit+" in this time frame");
            }
            return result;
        }
        
        private LineHeader getLineHeader(TimeUnit timeUnit) {
        	LineHeader result = myLineHeader;
            for (; result!=null; result = result.next()) {
                if (result.myUnit==timeUnit) {
                    break;
                }
            }
        	return result;
        }

//        public int _getUnitCount(TimeUnit unit) {
//            if (unit.isConstructedFrom((TimeUnit)myLowestFrameable)) {
//                UnitInfo info = calculateInfo(myHighestFrameable, myEndDate);
//                if (unit==myHighestFrameable) {
//                    return info.myRoundedCount;
//                }
//                else if (unit.isConstructedFrom((TimeUnit)myHighestFrameable)) {
//                    int atomCount = unit.getAtomCount((TimeUnit)myHighestFrameable);
//                    return (info.myRoundedCount/atomCount) + (info.myRoundedCount%atomCount==0 ? 0 : 1);
//                }
//                else {
//                    int atomCount1 = ((TimeUnit)myHighestFrameable).getAtomCount(unit);
//                    int count = info.myTruncatedCount*atomCount1;
//                    if (info.myRoundedCount>info.myTruncatedCount) {
//                        UnitInfo lowestInfo = calculateInfo(myLowestFrameable, info.lastDate);
//                        int atomCount2 = unit.getAtomCount((TimeUnit)myLowestFrameable);
//                        count += lowestInfo.myRoundedCount/atomCount2 + (lowestInfo.myRoundedCount%atomCount2==0 ? 0 : 1);
//                    }
//                    return count;
//                }
//            }
//            else {
//                UnitInfo lowestInfo = calculateInfo(myLowestFrameable, myEndDate);
//                int atomCount = ((TimeUnit)myLowestFrameable).getAtomCount(unit);
//                return atomCount*lowestInfo.myRoundedCount;
//            }
//        }
//
//        private UnitInfo calculateInfo(DateFrameable frameable, Date date) {
//            Date lastDate = date;
//            int count = 0;
//            for (;date.compareTo(myStartDate)>0; count++) {
//                lastDate = date;
//                date = frameable.jumpLeft(date);
//            }
//            int truncatedCount = date.compareTo(myStartDate)<0 ? count-1 : count;
//            return new UnitInfo(truncatedCount, count, lastDate);
//        }
//        private Date shiftDate(Date currentDate, TimeUnit timeUnit, int unitCount) {
//            Calendar c = (Calendar) myCalendar.clone();
//            c.setTime(currentDate);
//            int calendarField = getCalendarField(timeUnit);
//            int currentValue = c.get(calendarField);
//            clearFields(c, timeUnit);
//            c.add(calendarField, unitCount);
//            return c.getTime();
//        }
//
//        private void clearFields(Calendar c, TimeUnit topUnit) {
//            for (TimeUnit currentUnit = topUnit; currentUnit!=null; currentUnit = currentUnit.getDirectAtomUnit()) {
//                int calendarField = getCalendarField(currentUnit);
//                c.clear(calendarField);
//                c.getTime();
//            }
//        }
//
//        private int getCalendarField(TimeUnit timeUnit) {
//            Integer field = (Integer) ourUnit2field.get(timeUnit);
//            return field.intValue();
//        }

        public Date getStartDate() {
            return myStartDate;
        }

        public TimeUnit getTopUnit() {
            return myTopUnit;
        }

        public TimeUnit getBottomUnit() {
            return myBottomUnit;
        }


//        public int getUnitCount(TimeUnit unitLine) {
//            int counter = 0;
//            for (Date nextUnitStart = shiftDate(myStartDate, unitLine, counter);
//                 nextUnitStart.before(myNextFrameStartDate);
//                 nextUnitStart = shiftDate(myStartDate, unitLine, ++counter)) {
//                //System.err.println("myStart="+myStartDate+" nextFrame="+myNextFrameStartDate+" nextUnitStart="+nextUnitStart);
//            }
//            return counter;
//        }

        public String getUnitText(TimeUnit unitLine, int position) {
        	LineHeader lineHeader = getLineHeader(unitLine);
        	LineItem lineItem = lineHeader==null ? null : lineHeader.getLineItem(position);
        	Date startDate = lineItem==null ? null : lineItem.myStartDate;
        	String result = startDate==null ? null : getUnitText(unitLine, startDate);
        	if ("31".equals(result)) {
        		System.err.println("unit line="+unitLine+" position="+position);
        	}
        	return result;
        }
        

		private String getUnitText(TimeUnit unitLine, Date startDate) {
			String result = null;
            return unitLine.format(startDate);
//			if (unitLine.equals(GregorianTimeUnitStack.DAY)) {
//				result = ""+startDate.getDate();
//			}
//			return result;
		}

		public Date getUnitStart(TimeUnit unitLine, int position) {
            return null;  //To change body of implemented methods use Options | File Templates.
        }

        private static class UnitInfo {
            final int myTruncatedCount;
            final int myRoundedCount;
            private final Date lastDate;

            public UnitInfo(int myTruncatedCount, int myRoundedCount, Date lastDate) {
                this.myTruncatedCount = myTruncatedCount;
                this.myRoundedCount = myRoundedCount;
                this.lastDate = lastDate;
            }
        }


        private static class LineHeader {
            final TimeUnit myUnit;
            LineItem myFirstItem;
            private LineHeader myNextHeader;
            private int myItemCount = -1;

            public LineHeader(TimeUnit myUnit) {
                this.myUnit = myUnit;
            }

            /**
			 * @return
			 */
			public String fullDump() {
				StringBuffer result = new StringBuffer(toString());
				for (int i=0; i<getItemCount(); i++) {
					LineItem next = getLineItem(i);
					result.append("\n"+next);
				}
				return result.toString();
			}

			public String toString() {
				return myUnit.toString();
			}
			void append(LineHeader next) {
                myNextHeader = next;
            }

            LineHeader next() {
                return myNextHeader;
            }

            public int getItemCount() {
                if (myItemCount==-1) {
                    myItemCount = 0;
                    for (LineItem item = myFirstItem; item!=null; item = item.myNextItem) {
                        myItemCount++;
                    }
                }
                return myItemCount;
            }

            LineItem getLineItem(int position) {
            	LineItem result = myFirstItem;
                for (;result!=null && position-->0; result = result.myNextItem) {
                	 //position--;
                	 
                }
                return result;            	
            }
        }

        private static class LineItem {
            LineItem myNextItem;
            final Date myStartDate;
            final Date myEndDate;

            public LineItem(Date myStartDate, Date myEndDate) {
                this.myStartDate = myStartDate;
                this.myEndDate = myEndDate;
            }
            
            public String toString() {
            	return myStartDate.toString()+" - "+myEndDate.toString();
            }
        }
    }
}
